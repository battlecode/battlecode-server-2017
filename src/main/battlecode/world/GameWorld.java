package battlecode.world;

import battlecode.common.*;
import battlecode.server.ErrorReporter;
import battlecode.server.GameState;
import battlecode.util.SquareArray;
import battlecode.world.signal.AutoSignalHandler;
import battlecode.world.signal.InternalSignal;
import battlecode.world.signal.SignalHandler;
import battlecode.serial.GameStats;
import battlecode.world.control.RobotControlProvider;
import battlecode.world.signal.*;

import java.util.*;

/**
 * The primary implementation of the GameWorld interface for containing and
 * modifying the game map and the objects on it.
 */
public class GameWorld implements SignalHandler {
    /**
     * The current round we're running.
     */
    protected int currentRound;

    /**
     * Whether we're running.
     */
    protected boolean running = true;

    protected Team winner = null;
    protected final String teamAName;
    protected final String teamBName;
    protected final List<InternalSignal> currentInternalSignals;
    protected final List<InternalSignal> injectedInternalSignals;
    protected final long[][] teamMemory;
    protected final long[][] oldTeamMemory;
    protected final Map<Integer, InternalRobot> gameObjectsByID;
    protected final IDGenerator idGenerator;

    private final GameMap gameMap;

    private final RobotControlProvider controlProvider;

    private final GameStats gameStats = new GameStats(); // end-of-game stats

    private double[] teamResources = new double[4];

    private Map<Team, Set<InternalRobot>> baseArchons = new EnumMap<>(Team.class);
    private final Map<MapLocation, InternalRobot> gameObjectsByLoc = new HashMap<>();

    private SquareArray.Double rubble;
    private SquareArray.Double parts;

    private Map<Team, Map<Integer, Integer>> radio = new EnumMap<>(
            Team.class);

    private Map<Team, Map<RobotType, Integer>> robotTypeCount = new EnumMap<>(
            Team.class);

    @SuppressWarnings("unchecked")
    public GameWorld(GameMap gm, RobotControlProvider cp,
                     String teamA, String teamB,
                     long[][] oldTeamMemory) {

        currentRound = -1;
        teamAName = teamA;
        teamBName = teamB;
        gameObjectsByID = new LinkedHashMap<>();
        currentInternalSignals = new ArrayList<>();
        injectedInternalSignals = new ArrayList<>();
        idGenerator = new IDGenerator(gm.getSeed());
        teamMemory = new long[2][oldTeamMemory[0].length];
        this.oldTeamMemory = oldTeamMemory;

        gameMap = gm;
        controlProvider = cp;

        radio.put(Team.A, new HashMap<>());
        radio.put(Team.B, new HashMap<>());

        robotTypeCount.put(Team.A, new EnumMap<>(
                RobotType.class));
        robotTypeCount.put(Team.B, new EnumMap<>(
                RobotType.class));
        robotTypeCount.put(Team.NEUTRAL, new EnumMap<>(
                RobotType.class));
        robotTypeCount.put(Team.ZOMBIE, new EnumMap<>(
                RobotType.class));

        baseArchons.put(Team.A, new HashSet<>());
        baseArchons.put(Team.B, new HashSet<>());

        adjustResources(Team.A, GameConstants.PARTS_INITIAL_AMOUNT);
        adjustResources(Team.B, GameConstants.PARTS_INITIAL_AMOUNT);

        this.rubble = new SquareArray.Double(gm.getWidth(), gm.getHeight());
        this.parts = new SquareArray.Double(gm.getWidth(), gm.getHeight());

        for (int i = 0; i < gm.getWidth(); i++) {
            for (int j = 0; j < gm.getHeight(); j++) {
                this.rubble.set(i, j,
                        gm.initialRubbleAtLocation(
                                i + gm.getOrigin().x,
                                j + gm.getOrigin().y
                        )
                );
                this.parts.set(i, j,
                        gm.initialPartsAtLocation(
                                i + gm.getOrigin().x,
                                j + gm.getOrigin().y
                        )
                );
            }
        }

        controlProvider.matchStarted(this);

        // Add the robots contained in the GameMap to this world.
        for (GameMap.InitialRobotInfo initialRobot : gameMap.getInitialRobots()) {
            // Side-effectful constructor; will add robot to relevant stuff
            spawnRobot(
                    initialRobot.type,
                    initialRobot.getLocation(gameMap.getOrigin()),
                    initialRobot.team,
                    0,
                    Optional.empty()
            );
        }
    }

    /**
     * Run a single round of the game.
     * Synchronized because you shouldn't call this and inject() at the same time,
     * but their order of being executed isn't guaranteed.
     *
     * @return the state of the game after the round has run.
     */
    public synchronized GameState runRound() {
        if (!this.isRunning()) {
            return GameState.DONE;
        }

        try {
            if (this.getCurrentRound() != -1) {
                this.clearAllSignals();
            }
            this.processBeginningOfRound();
            this.controlProvider.roundStarted();

            // We iterate through the IDs so that we avoid ConcurrentModificationExceptions
            // of an iterator. Kinda gross, but whatever.
            final int[] idsToRun = gameObjectsByID.keySet().stream()
                    .mapToInt(i -> i)
                    .toArray();

            for (final int id : idsToRun) {
                final InternalRobot robot = gameObjectsByID.get(id);
                if (robot == null) {
                    // Robot might have died earlier in the iteration; skip it
                    continue;
                }

                robot.processBeginningOfTurn();
                this.controlProvider.runRobot(robot);
                robot.setBytecodesUsed(this.controlProvider.getBytecodesUsed(robot));
                robot.processEndOfTurn();
                // If the robot terminates but the death signal has not yet
                // been visited:
                if (this.controlProvider.getTerminated(robot) && gameObjectsByID
                        .get(id) != null) {
                    robot.suicide();
                }
            }

            this.controlProvider.roundEnded();
            this.processEndOfRound();

            if (!this.isRunning()) {
                this.controlProvider.matchEnded();
            }

        } catch (Exception e) {
            ErrorReporter.report(e);
            return GameState.DONE;
        }

        return GameState.RUNNING;
    }

    /**
     * Inject a signal into the game world, and return any new signals
     * that result from changes created by the signal.
     *
     * Synchronized because you shouldn't call this and runRound() at the same time,
     * but their order of being executed isn't guaranteed.
     *
     * @param injectedInternalSignal the signal to inject
     * @return signals that result from the injected signal (including the injected signal)
     * @throws RuntimeException if the signal injection fails
     */
    public synchronized InternalSignal[] inject(InternalSignal injectedInternalSignal) throws RuntimeException {
        clearAllSignals();

        visitSignal(injectedInternalSignal);

        return getAllSignals(false);

    }

    // *********************************
    // ****** BASIC MAP METHODS ********
    // *********************************

    public int getMapSeed() {
        return gameMap.getSeed();
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public InternalRobot getObject(MapLocation loc) {
        return gameObjectsByLoc.get(loc);
    }

    public InternalRobot getRobot(MapLocation loc) {
        return getObject(loc);
    }

    public Collection<InternalRobot> allObjects() {
        return gameObjectsByID.values();
    }

    public InternalRobot[] getAllGameObjects() {
        return gameObjectsByID.values().toArray(
                new InternalRobot[gameObjectsByID.size()]);
    }

    public boolean exists(InternalRobot o) {
        return gameObjectsByID.containsKey(o.getID());
    }

    public int getMessage(Team t, int channel) {
        Integer val = radio.get(t).get(channel);
        return val == null ? 0 : val;
    }

    public GameStats getGameStats() {
        return gameStats;
    }

    public String getTeamName(Team t) {
        switch (t) {
        case A:
            return teamAName;
        case B:
            return teamBName;
        case NEUTRAL:
            return "neutralplayer";
        default:
            return null;
        }
    }

    public Team getWinner() {
        return winner;
    }

    public boolean isRunning() {
        return running;
    }

    public long[][] getTeamMemory() {
        return teamMemory;
    }

    public long[][] getOldTeamMemory() {
        return oldTeamMemory;
    }

    public void setTeamMemory(Team t, int index, long state) {
        teamMemory[t.ordinal()][index] = state;
    }

    public void setTeamMemory(Team t, int index, long state, long mask) {
        long n = teamMemory[t.ordinal()][index];
        n &= ~mask;
        n |= (state & mask);
        teamMemory[t.ordinal()][index] = n;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public InternalRobot getObjectByID(int id) {
        return gameObjectsByID.get(id);
    }

    // *********************************
    // ****** MISC UTILITIES ***********
    // *********************************

    /**
     * Store a signal, to be passed out of the world.
     * The signal should have already been processed.
     *
     * @param s the signal
     */
    private void addSignal(InternalSignal s) {
        currentInternalSignals.add(s);
    }

    /**
     * Clear all processed signals from the last round / injection.
     */
    private void clearAllSignals() {
        currentInternalSignals.clear();
    }

    public boolean canMove(MapLocation loc, RobotType type) {
        return gameMap.onTheMap(loc) && (getRubble(loc) < GameConstants
                .RUBBLE_OBSTRUCTION_THRESH || type.ignoresRubble) &&
                gameObjectsByLoc.get(loc) == null;
    }

    protected boolean canAttackSquare(InternalRobot ir, MapLocation loc) {
        MapLocation myLoc = ir.getLocation();
        int d = myLoc.distanceSquaredTo(loc);
        int radius = ir.getType().attackRadiusSquared;
        if (ir.getType() == RobotType.TURRET) {
            return (d <= radius && d >= GameConstants.TURRET_MINIMUM_RANGE);
        }
        return d <= radius;
    }

    // TODO: make a faster implementation of this
    public MapLocation[] getAllMapLocationsWithinRadiusSq(MapLocation center,
            int radiusSquared) {
        ArrayList<MapLocation> locations = new ArrayList<>();

        int radius = (int) Math.sqrt(radiusSquared);
        radius = Math.min(radius, Math.max(GameConstants.MAP_MAX_HEIGHT,
                GameConstants.MAP_MAX_WIDTH));

        int minXPos = center.x - radius;
        int maxXPos = center.x + radius;
        int minYPos = center.y - radius;
        int maxYPos = center.y + radius;

        for (int x = minXPos; x <= maxXPos; x++) {
            for (int y = minYPos; y <= maxYPos; y++) {
                MapLocation loc = new MapLocation(x, y);
                if (gameMap.onTheMap(loc)
                        && loc.distanceSquaredTo(center) <= radiusSquared)
                    locations.add(loc);
            }
        }

        return locations.toArray(new MapLocation[locations.size()]);
    }

    // TODO: make a faster implementation of this
    protected InternalRobot[] getAllRobotsWithinRadiusSq(MapLocation center,
            int radiusSquared) {
        if (radiusSquared == 0) {
            if (getRobot(center) == null) {
                return new InternalRobot[0];
            } else {
                return new InternalRobot[]{ getRobot(center) };
            }
        } else if (radiusSquared < 16) {
            MapLocation[] locs = getAllMapLocationsWithinRadiusSq(center,
                    radiusSquared);
            ArrayList<InternalRobot> robots = new ArrayList<>();
            for (MapLocation loc : locs) {
                InternalRobot res = getRobot(loc);
                if (res != null) {
                    robots.add(res);
                }
            }
            return robots.toArray(new InternalRobot[robots.size()]);
        }

        ArrayList<InternalRobot> robots = new ArrayList<>();

        for (InternalRobot o : gameObjectsByID.values()) {
            if (o == null)
                continue;
            if (o.getLocation() != null
                    && o.getLocation().distanceSquaredTo(center) <= radiusSquared)
                robots.add(o);
        }

        return robots.toArray(new InternalRobot[robots.size()]);
    }

    // Used by zombies.

    /**
     * @param loc the location to find nearest robots.
     * @return the info of the nearest player-controlled robot, or null
     *         if there are no player-controlled robots
     */
    public RobotInfo getNearestPlayerControlled(MapLocation loc) {
        int distSq = Integer.MAX_VALUE;
        MapLocation closest = null;
        for (InternalRobot robot : gameObjectsByID.values()) {
            if (!robot.getTeam().isPlayer()) continue;

            MapLocation newLoc = robot.getLocation();
            int newDistSq = newLoc.distanceSquaredTo(loc);
            if (newDistSq < distSq) {
                closest = newLoc;
                distSq = newDistSq;
            }
        }

        if (closest == null) {
            return null;
        }

        return gameObjectsByLoc.get(closest).getRobotInfo();
    }

    // *********************************
    // ****** ENGINE ACTIONS ***********
    // *********************************

    // should only be called by InternalRobot.setLocation
    public void notifyMovingObject(InternalRobot o, MapLocation oldLoc,
            MapLocation newLoc) {
        if (oldLoc != null) {
            if (gameObjectsByLoc.get(oldLoc) != o) {
                ErrorReporter
                        .report("Internal Error: invalid oldLoc in notifyMovingObject");
                return;
            }
            gameObjectsByLoc.remove(oldLoc);
        }
        if (newLoc != null) {
            gameObjectsByLoc.put(newLoc, o);
        }
    }

    // *********************************
    // ****** COUNTING ROBOTS **********
    // *********************************

    // only returns active robots
    public int getRobotTypeCount(Team team, RobotType type) {
        if (robotTypeCount.get(team).containsKey(type)) {
            return robotTypeCount.get(team).get(type);
        } else {
            return 0;
        }
    }

    public void incrementRobotTypeCount(Team team, RobotType type) {
        if (robotTypeCount.get(team).containsKey(type)) {
            robotTypeCount.get(team).put(type,
                    robotTypeCount.get(team).get(type) + 1);
        } else {
            robotTypeCount.get(team).put(type, 1);
        }
    }
    
    // decrement from active robots (used during TTM <-> Turret transform)
    public void decrementRobotTypeCount(Team team, RobotType type) {
        Integer currentCount = getRobotTypeCount(team, type);
        robotTypeCount.get(team).put(type,currentCount - 1);
    }

    // *********************************
    // ****** RUBBLE METHODS **********
    // *********************************
    public double getRubble(MapLocation loc) {
        if (!gameMap.onTheMap(loc)) {
            return 0;
        }
        return rubble.get(
                loc.x - gameMap.getOrigin().x,
                loc.y - gameMap.getOrigin().y
        );
    }
    
    public void alterRubble(MapLocation loc, double amount) {
        rubble.set(loc.x - gameMap.getOrigin().x, loc.y - gameMap.getOrigin().y,
                Math.max(0.0, amount));
    }

    // *********************************
    // ****** PARTS METHODS ************
    // *********************************
    public double getParts(MapLocation loc) {
        if (!gameMap.onTheMap(loc)) {
            return 0;
        }
        return parts.get(
                loc.x - gameMap.getOrigin().x,
                loc.y - gameMap.getOrigin().y
        );
    }

    public double takeParts(MapLocation loc) { // Remove parts from location
        double prevVal = getParts(loc);

        parts.set(loc.x - gameMap.getOrigin().x, loc.y - gameMap.getOrigin().y,
                0.0);
        return prevVal;
    }

    protected void adjustResources(Team t, double amount) {
        teamResources[t.ordinal()] += amount;
    }

    public double resources(Team t) {
        return teamResources[t.ordinal()];
    }

    // *********************************
    // ****** GAMEPLAY *****************
    // *********************************

    /**
     * Spawns a new robot with the given parameters.
     *
     * @param type the type of the robot
     * @param loc the location of the robot
     * @param team the team of the robot
     * @param buildDelay the build delay of the robot
     * @param parent the parent of the robot, or Optional.empty() if there is no parent
     * @return the ID of the spawned robot.
     */
    public int spawnRobot(RobotType type,
                           MapLocation loc,
                           Team team,
                           int buildDelay,
                           Optional<InternalRobot> parent) {

        int ID = idGenerator.nextID();

        visitSpawnSignal(new SpawnSignal(
                ID,
                parent.isPresent() ? parent.get().getID() : SpawnSignal.NO_ID,
                loc,
                type,
                team,
                buildDelay
        ));
        return ID;
    }

    public void processBeginningOfRound() {
        currentRound++;

        // process all gameobjects
        for (InternalRobot gameObject : gameObjectsByID.values()) {
            gameObject.processBeginningOfRound();
        }
    }

    public boolean setWinnerIfNonzero(double n, DominationFactor d) {
        if (n > 0)
            setWinner(Team.A, d);
        else if (n < 0)
            setWinner(Team.B, d);
        return n != 0;
    }

    public void setWinner(Team t, DominationFactor d) {
        winner = t;
        gameStats.setDominationFactor(d);
        // running = false;

    }

    public boolean timeLimitReached() {
        return currentRound >= gameMap.getRounds() - 1;
    }

    public void processEndOfRound() {
        // process all gameobjects
        for (InternalRobot gameObject : gameObjectsByID.values()) {
            gameObject.processEndOfRound();
        }

        // free parts
        teamResources[Team.A.ordinal()] += GameConstants.ARCHON_PART_INCOME
                * getRobotTypeCount(Team.A, RobotType.ARCHON);
        teamResources[Team.B.ordinal()] += GameConstants.ARCHON_PART_INCOME
                * getRobotTypeCount(Team.B, RobotType.ARCHON);

        // Add signals for team resources
        for (final Team team : Team.values()) {
            addSignal(new TeamResourceSignal(team, teamResources[team.ordinal()]));
        }

        if (timeLimitReached() && winner == null) {
            // tiebreak by number of Archons
            if (!(setWinnerIfNonzero(
                    getRobotTypeCount(Team.A, RobotType.ARCHON)
                            - getRobotTypeCount(Team.B, RobotType.ARCHON),
                    DominationFactor.PWNED))) {
                // tiebreak by total Archon health
                double archonDiff = 0.0;
                double partsDiff = resources(Team.A) - resources(Team.B);
                int highestAArchonID = 0;
                int highestBArchonID = 0;
                InternalRobot[] objs = getAllGameObjects();
                for (InternalRobot obj : objs) {
                    if (obj == null) continue;

                    if (obj.getTeam() == Team.A) {
                        partsDiff += obj.getType().partCost;
                    } else if (obj.getTeam() == Team.B) {
                        partsDiff -= obj.getType().partCost;
                    }
                    if (obj.getType() == RobotType.ARCHON) {
                        if (obj.getTeam() == Team.A) {
                            archonDiff += obj.getHealthLevel();
                            highestAArchonID = Math.max(highestAArchonID,
                                    obj.getID());
                        } else if (obj.getTeam() == Team.B) {
                            archonDiff -= obj.getHealthLevel();
                            highestBArchonID = Math.max(highestBArchonID,
                                    obj.getID());
                        }
                    }
                }

                // total part cost of units + part stockpile
                if (!(setWinnerIfNonzero(archonDiff, DominationFactor.OWNED))
                        && !(setWinnerIfNonzero(partsDiff,
                                DominationFactor.BARELY_BEAT))) {
                    // just tiebreak by ID
                    if (highestAArchonID > highestBArchonID)
                        setWinner(Team.A,
                                DominationFactor.WON_BY_DUBIOUS_REASONS);
                    else
                        setWinner(Team.B,
                                DominationFactor.WON_BY_DUBIOUS_REASONS);
                }
            }
        }

        if (winner != null) {
            running = false;
            for (InternalRobot o : gameObjectsByID.values()) {
                if (o != null) {
                    controlProvider.robotKilled(o);
                }
            }
        }
    }

    public InternalSignal[] getAllSignals(boolean includeBytecodesUsedSignal) {
        ArrayList<InternalRobot> allRobots = new ArrayList<>();
        for (InternalRobot obj : gameObjectsByID.values()) {
            if (obj == null)
                continue;
            allRobots.add(obj);
        }

        InternalRobot[] robots = allRobots.toArray(new InternalRobot[allRobots.size()]);

        if (includeBytecodesUsedSignal) {
            currentInternalSignals.add(new BytecodesUsedSignal(robots));
        }
        currentInternalSignals.add(new RobotDelaySignal(robots));
        currentInternalSignals.add(new InfectionSignal(robots));

        HealthChangeSignal healthChange = new HealthChangeSignal(robots);

        // Reset health levels.
        for (final InternalRobot robot : robots) {
            robot.clearHealthChanged();
        }

        if (healthChange.getRobotIDs().length > 0) {
            currentInternalSignals.add(healthChange);
        }

        return currentInternalSignals.toArray(new InternalSignal[currentInternalSignals.size()]);
    }

    // ******************************
    // SIGNAL HANDLER METHODS
    // ******************************

    SignalHandler signalHandler = new AutoSignalHandler(this);

    public void visitSignal(InternalSignal s) {
        signalHandler.visitSignal(s);
    }

    @SuppressWarnings("unused")
    public void visitActivationSignal(ActivationSignal s) {
        InternalRobot activator = getObjectByID(s.getRobotID());
        MapLocation targetLoc = s.getLoc();
        InternalRobot toBeActivated = getRobot(targetLoc);

        visitDeathSignal(new DeathSignal(toBeActivated.getID(), true));

        spawnRobot(
                toBeActivated.getType(),
                targetLoc,
                activator.getTeam(),
                0,
                Optional.of(activator)
        );
    }

    @SuppressWarnings("unused")
    public void visitAttackSignal(AttackSignal s) {
        InternalRobot attacker = getObjectByID(s.getRobotID());

        MapLocation targetLoc = s.getTargetLoc();
        double rate = 1.0;

        switch (attacker.getType()) { // Only attacking types
        case STANDARDZOMBIE:
        case FASTZOMBIE:
        case RANGEDZOMBIE:
        case BIGZOMBIE:
        case SCOUT:
        case SOLDIER:
        case GUARD:
        case VIPER:
        case TURRET:
            int splashRadius = 0;

            // TODO - we're not going to find any targets?
            InternalRobot[] targets = getAllRobotsWithinRadiusSq(targetLoc,
                    splashRadius);

            for (InternalRobot target : targets) {
                
                if (attacker.getType() == RobotType.GUARD
                        && target.getType().isZombie)
                    rate = GameConstants.GUARD_ZOMBIE_MULTIPLIER;

                if (attacker.getType().canInfect() && target.getType().isInfectable()) {
                    target.setInfected(attacker);
                }

                double damage = (attacker.getAttackPower()) * rate;
                target.takeDamage(damage);

                // Reward parts to destroyer of zombie den
                if (target.getType() == RobotType.ZOMBIEDEN && target
                        .getHealthLevel() <= 0.0) {
                    adjustResources(attacker.getTeam(),
                            GameConstants.DEN_PART_REWARD);
                }
            }
            break;
        case ZOMBIEDEN:
            InternalRobot[] denTargets = getAllRobotsWithinRadiusSq(targetLoc,0);
            
            for (InternalRobot target : denTargets) {
                target.takeDamage(GameConstants.DEN_SPLASH_DAMAGE);
            }
            break;
        default:
            // ERROR, should never happen
        }
        addSignal(s);
    }

    @SuppressWarnings("unused")
    public void visitBroadcastSignal(BroadcastSignal s) {
        int robotID = s.getRobotID();
        InternalRobot robot = getObjectByID(robotID);
        MapLocation location = robot.getLocation();
        int radius = s.getRadius();
        Signal mess = s.getSignal();
        InternalRobot[] receiving = getAllRobotsWithinRadiusSq(location,
                radius);
        for (int i = 0; i < receiving.length; i++) {
            if (robot != receiving[i]) {
                receiving[i].receiveSignal(mess);
            }
        }

        // delay costs
        double x = (radius / (double) robot.getType().sensorRadiusSquared) - 2;
        double delayIncrease = GameConstants.BROADCAST_BASE_DELAY_INCREASE +
                GameConstants.BROADCAST_ADDITIONAL_DELAY_INCREASE * (Math.max
                        (0, x));
        robot.addCoreDelay(delayIncrease);
        robot.addWeaponDelay(delayIncrease);

        addSignal(s);
    }

    @SuppressWarnings("unused")
    public void visitBuildSignal(BuildSignal s) {
        InternalRobot parent;
        int parentID = s.getParentID();
        MapLocation loc;
        if (parentID == 0) {
            parent = null;
            loc = s.getLoc();
        } else {
            parent = getObjectByID(parentID);
            loc = s.getLoc();
        }

        int cost = s.getType().partCost;
        adjustResources(s.getTeam(), -cost);

        // note: this also adds the signal

        spawnRobot(s.getType(),
                loc,
                s.getTeam(),
                s.getDelay(),
                Optional.of(parent));
    }

    @SuppressWarnings("unused")
    public void visitClearRubbleSignal(ClearRubbleSignal s) {
        MapLocation loc = s.getLoc();
        double currentRubble = getRubble(loc);
        alterRubble(loc, (currentRubble  * (1 - GameConstants
                .RUBBLE_CLEAR_PERCENTAGE)) - GameConstants
                .RUBBLE_CLEAR_FLAT_AMOUNT);

        addSignal(s);
        addSignal(new RubbleChangeSignal(loc, getRubble(loc)));
    }

    @SuppressWarnings("unused")
    public void visitControlBitsSignal(ControlBitsSignal s) {
        InternalRobot r = getObjectByID(s.getRobotID());
        r.setControlBits(s.getControlBits());

        addSignal(s);
    }

    @SuppressWarnings("unused")
    public void visitDeathSignal(DeathSignal s) {
        if (!running) {
            // All robots emit death signals after the game
            // ends. We still want the client to draw
            // the robots.
            return;
        }

        int ID = s.getObjectID();
        InternalRobot obj = getObjectByID(ID);
        boolean isDeathByActivation = s.isDeathByActivation();

        if (obj == null) {
            throw new RuntimeException("visitDeathSignal of nonexistent robot: "+s.getObjectID());
        }

        if (obj.getLocation() == null) {
            throw new RuntimeException("Object has no location: "+obj);
        }

        MapLocation loc = obj.getLocation();
        if (gameObjectsByLoc.get(loc) != obj) {
            throw new RuntimeException("Object location out of sync: "+obj);
        }

        decrementRobotTypeCount(obj.getTeam(), obj.getType());

        if (obj.getType() == RobotType.ARCHON && obj.getTeam().isPlayer()) {
            int totalArchons = getRobotTypeCount(obj.getTeam(),
                    RobotType.ARCHON);
            if (totalArchons == 0) {
                setWinner(obj.getTeam().opponent(),
                        DominationFactor.DESTROYED);
            }
        }

        // update rubble
        if (!isDeathByActivation && !obj.isInfected()) {
            alterRubble(loc, getRubble(loc) + obj.getMaxHealth());
            addSignal(new RubbleChangeSignal(loc, getRubble(loc)));
        }

        controlProvider.robotKilled(obj);
        gameObjectsByID.remove(obj.getID());
        gameObjectsByLoc.remove(loc);

        // if it was an infected robot, create a Zombie in its place.
        if (obj.isInfected() && !isDeathByActivation) {
            RobotType zombieType = obj.getType().turnsInto; // Type of Zombie this unit turns into

            // Create new Zombie
            spawnRobot(
                    zombieType,
                    obj.getLocation(),
                    Team.ZOMBIE,
                    0,
                    Optional.of(obj)
            );
        }

        addSignal(s);
    }

    @SuppressWarnings("unused")
    public void visitIndicatorDotSignal(IndicatorDotSignal s) {
        addSignal(s);
    }

    @SuppressWarnings("unused")
    public void visitIndicatorLineSignal(IndicatorLineSignal s) {
        addSignal(s);
    }

    @SuppressWarnings("unused")
    public void visitIndicatorStringSignal(IndicatorStringSignal s) {
        addSignal(s);
    }

    @SuppressWarnings("unused")
    public void visitMatchObservationSignal(MatchObservationSignal s) {
        addSignal(s);
    }

    @SuppressWarnings("unused")
    public void visitMovementSignal(MovementSignal s) {
        InternalRobot r = getObjectByID(s.getRobotID());
        r.setLocation(s.getNewLoc());
        double newParts = takeParts(r.getLocation());
        adjustResources(r.getTeam(), newParts);
        addSignal(s);
        if (newParts > 0) {
            addSignal(new PartsChangeSignal(s.getNewLoc(), 0));
        }
    }

    @SuppressWarnings("unused")
    public void visitMovementOverrideSignal(MovementOverrideSignal s) {
        InternalRobot r = getObjectByID(s.getRobotID());
        r.setLocation(s.getNewLoc());

        addSignal(s);
    }

    @SuppressWarnings({"unchecked", "unused"})
    public void visitSpawnSignal(SpawnSignal s) {
        // This robot has no id.
        // We need to assign it an id and spawn that.
        // Note that the current spawn signal is discarded.
        if (s.getRobotID() == SpawnSignal.NO_ID) {
            spawnRobot(
                    s.getType(),
                    s.getLoc(),
                    s.getTeam(),
                    s.getDelay(),
                    Optional.ofNullable(
                            gameObjectsByID.get(s.getParentID())
                    )
            );
            return;
        }

        InternalRobot parent;
        int parentID = s.getParentID();

        if (parentID == SpawnSignal.NO_ID) {
            parent = null;
        } else {
            parent = getObjectByID(parentID);
        }

        InternalRobot robot =
                new InternalRobot(
                        this,
                        s.getRobotID(),
                        s.getType(),
                        s.getLoc(),
                        s.getTeam(),
                        s.getDelay(),
                        Optional.ofNullable(parent)
                );

        incrementRobotTypeCount(s.getTeam(), s.getType());

        gameObjectsByID.put(s.getRobotID(), robot);

        if (s.getLoc() != null) {
            gameObjectsByLoc.put(s.getLoc(), robot);
        }

        controlProvider.robotSpawned(robot);

        addSignal(s);
    }

    @SuppressWarnings("unused")
    public void visitTypeChangeSignal(TypeChangeSignal s) {
        addSignal(s);
    }
}
