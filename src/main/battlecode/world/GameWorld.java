package battlecode.world;

import battlecode.common.*;
import battlecode.server.ErrorReporter;
import battlecode.server.GameState;
import battlecode.util.SquareArray;
import battlecode.world.signal.AutoSignalHandler;
import battlecode.world.signal.InternalSignal;
import battlecode.world.signal.SignalHandler;
import battlecode.serial.GameStats;
import battlecode.server.Config;
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
    protected final IDGenerator idGeneratorRobots;
    protected final IDGenerator idGeneratorTrees;
    protected final IDGenerator idGeneratorBullets;

    private final GameMap gameMap;
    private final ObjectMap objectMap;

    private final RobotControlProvider controlProvider;

    private final GameStats gameStats = new GameStats(); // end-of-game stats

    private int[] teamVictoryPoints = new int[3];
    private double[] teamBulletSupplies = new double[3];
    private int[][] teamSharedArrays = new int[3][GameConstants.BROADCAST_MAX_CHANNELS];

    private Map<Team, Map<RobotType, Integer>> robotTypeCount = new EnumMap<>(
            Team.class);
    private int[] robotCount = new int[3];
    private int[] treeCount = new int[3];
    private Random rand;

    @SuppressWarnings("unchecked")
    public GameWorld(GameMap gm, RobotControlProvider cp,
                     String teamA, String teamB,
                     long[][] oldTeamMemory) {
        
        currentRound = -1;
        teamAName = teamA;
        teamBName = teamB;
        currentInternalSignals = new ArrayList<>();
        injectedInternalSignals = new ArrayList<>();
        idGeneratorRobots = new IDGenerator(gm.getSeed());
        idGeneratorTrees = new IDGenerator(gm.getSeed());
        idGeneratorBullets = new IDGenerator(gm.getSeed());
        teamMemory = new long[2][oldTeamMemory[0].length];
        this.oldTeamMemory = oldTeamMemory;

        gameMap = gm;
        objectMap = new ObjectMap(gm);
        controlProvider = cp;

        robotTypeCount.put(Team.A, new EnumMap<>(
                RobotType.class));
        robotTypeCount.put(Team.B, new EnumMap<>(
                RobotType.class));

        adjustBulletSupply(Team.A, GameConstants.BULLETS_INITIAL_AMOUNT);
        adjustBulletSupply(Team.B, GameConstants.BULLETS_INITIAL_AMOUNT);

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

        // Add the trees contained in the GameMap to this world.
        
        rand = new Random(gameMap.getSeed());
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
                
                if(robot.getHealthLevel() > 0) { // Only processEndOfTurn if robot is still alive
                    robot.processEndOfTurn();
                }
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
        throw new RuntimeException("Implement me!");
    }
    
    public boolean isEmpty(MapLocation loc) {
        throw new RuntimeException("Implement me!");
    }

    public boolean isEmpty(MapLocation loc, double radius) {
        throw new RuntimeException("Implement me!");
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
    // ****** COUNTING METHODS *********
    // *********************************

    public int getRobotCount(Team team) {
        return robotCount[team.ordinal()];
    }

    public int getTreeCount(Team team) {
        return treeCount[team.ordinal()];
    }

    public int getRobotTypeCount(Team team, RobotType type) {
        if (robotTypeCount.get(team).containsKey(type)) {
            return robotTypeCount.get(team).get(type);
        } else {
            return 0;
        }
    }
    
    public void incrementRobotCount(Team team) {
        robotCount[team.ordinal()]++;
    }

    public void decrementRobotCount(Team team) {
        robotCount[team.ordinal()]--;
    }

    public void incrementTreeCount(Team team) {
        treeCount[team.ordinal()]++;
    }

    public void decrementTreeCount(Team team) {
        treeCount[team.ordinal()]--;
    }

    public void incrementRobotTypeCount(Team team, RobotType type) {
        if (robotTypeCount.get(team).containsKey(type)) {
            robotTypeCount.get(team).put(type,
                    robotTypeCount.get(team).get(type) + 1);
        } else {
            robotTypeCount.get(team).put(type, 1);
        }
    }
    
    public void decrementRobotTypeCount(Team team, RobotType type) {
        Integer currentCount = getRobotTypeCount(team, type);
        robotTypeCount.get(team).put(type,currentCount - 1);
    }

    // *********************************
    // ***** BULLET/VP METHODS *********
    // *********************************

    public void adjustBulletSupply(Team t, double amount) {
        teamBulletSupplies[t.ordinal()] += amount;
    }

    public double getBulletSupply(Team t) {
        return teamBulletSupplies[t.ordinal()];
    }

    public void adjustVictoryPoints(Team t, int amount) {
        teamVictoryPoints[t.ordinal()] += amount;
    }

    public int getVictoryPoints(Team t) {
        return teamVictoryPoints[t.ordinal()];
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
        teamResources[Team.A.ordinal()] += Math.max(0.0, GameConstants
                .ARCHON_PART_INCOME - GameConstants.PART_INCOME_UNIT_PENALTY
                * getRobotCount(Team.A));
        teamResources[Team.B.ordinal()] += Math.max(0.0, GameConstants
                .ARCHON_PART_INCOME - GameConstants.PART_INCOME_UNIT_PENALTY
                * getRobotCount(Team.B));

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
    // *** SIGNAL HANDLER METHODS ***
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

        visitDeathSignal(new DeathSignal(toBeActivated.getID(), DeathSignal
                .RobotDeathCause.ACTIVATION));

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
                if (target.getType() == RobotType.GUARD && damage > GameConstants.GUARD_DEFENSE_THRESHOLD) {
                    target.takeDamage(damage - GameConstants
                            .GUARD_DAMAGE_REDUCTION, attacker.getType());
                } else {
                    target.takeDamage(damage, attacker.getType());
                }

                // Reward parts to destroyer of zombie den
                if (target.getType() == RobotType.ZOMBIEDEN && target
                        .getHealthLevel() <= 0.0) {
                    adjustResources(attacker.getTeam(),
                            GameConstants.DEN_PART_REWARD);
                }
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
        int parentID = s.getParentID();
        MapLocation loc = s.getLoc();
        InternalRobot parent = getObjectByID(parentID);

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
        decrementRobotCount(obj.getTeam());

        if (obj.getType() == RobotType.ARCHON && obj.getTeam().isPlayer()) {
            int totalArchons = getRobotTypeCount(obj.getTeam(),
                    RobotType.ARCHON);
            if (totalArchons == 0 && winner == null) {
                if (gameMap.isArmageddon()) {
                    setWinner(Team.ZOMBIE, DominationFactor.ZOMBIFIED);
                } else {
                    setWinner(obj.getTeam().opponent(), DominationFactor.DESTROYED);
                }
            }
        } else if (gameMap.isArmageddon()
                && obj.getTeam() == Team.ZOMBIE
                && getRobotCount(Team.ZOMBIE) == 0) {
            setWinner(Team.A, DominationFactor.CLEANSED);
        }

        // update rubble
        if (s.getCause() != DeathSignal.RobotDeathCause.ACTIVATION && !obj
                .isInfected()) {
            double rubbleFactor = 1.0;
            if (s.getCause() == DeathSignal.RobotDeathCause.TURRET) {
                rubbleFactor = GameConstants.RUBBLE_FROM_TURRET_FACTOR;
            }
            alterRubble(loc, getRubble(loc) + rubbleFactor * obj.getMaxHealth());
            addSignal(new RubbleChangeSignal(loc, getRubble(loc)));
        }

        controlProvider.robotKilled(obj);
        gameObjectsByID.remove(obj.getID());
        gameObjectsByLoc.remove(loc);

        // if it was an infected robot, create a Zombie in its place.
        if (obj.isInfected() && s.getCause() != DeathSignal.RobotDeathCause
                .ACTIVATION) {
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
        if (r.getType() == RobotType.ARCHON) {
            double newParts = takeParts(r.getLocation());
            adjustResources(r.getTeam(), newParts);
            if (newParts > 0) {
                addSignal(new PartsChangeSignal(s.getNewLoc(), 0));
            }
        }
        addSignal(s);
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
        incrementRobotCount(s.getTeam());

        gameObjectsByID.put(s.getRobotID(), robot);

        if (s.getLoc() != null) {
            gameObjectsByLoc.put(s.getLoc(), robot);

            // If you are an archon, pick up parts on that location.
            if (s.getType() == RobotType.ARCHON && s.getTeam().isPlayer()) {
                double newParts = takeParts(s.getLoc());
                adjustResources(s.getTeam(), newParts);
                if (newParts > 0) {
                    addSignal(new PartsChangeSignal(s.getLoc(), 0));
                }
            }
        }

        // Robot might be killed during creation if player
        // contains errors; enqueue the spawn before we
        // tell the control provider about it
        addSignal(s);

        controlProvider.robotSpawned(robot);
    }

    @SuppressWarnings("unused")
    public void visitTypeChangeSignal(TypeChangeSignal s) {
        addSignal(s);
    }
}
