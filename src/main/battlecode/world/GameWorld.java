package battlecode.world;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.engine.ErrorReporter;
import battlecode.engine.instrumenter.RobotDeathException;
import battlecode.engine.instrumenter.RobotMonitor;
import battlecode.engine.signal.AutoSignalHandler;
import battlecode.engine.signal.Signal;
import battlecode.engine.signal.SignalHandler;
import battlecode.serial.DominationFactor;
import battlecode.serial.GameStats;
import battlecode.serial.RoundStats;
import battlecode.world.signal.*;

import java.util.*;

/**
 * The primary implementation of the GameWorld interface for containing and
 * modifying the game map and the objects on it.
 */
@SuppressWarnings("unused")
public class GameWorld implements SignalHandler {
    protected int currentRound; // do we need this here?? -- yes
    protected boolean running = true; // do we need this here?? -- yes
    protected boolean wasBreakpointHit = false;
    protected Team winner = null;
    protected final String teamAName;
    protected final String teamBName;
    protected final Random randGen;
    protected int nextID;
    protected final ArrayList<Signal> signals;
    protected final long[][] teamMemory;
    protected final long[][] oldTeamMemory;
    protected final Map<Integer, InternalRobot> gameObjectsByID;
    protected final ArrayList<Integer> randomIDs = new ArrayList<>();

    private final GameMap gameMap;
    private RoundStats roundStats = null; // stats for each round; new object is
                                          // created for each round
    private final GameStats gameStats = new GameStats(); // end-of-game stats

    private double[] teamResources = new double[4];

    private Map<Team, Set<InternalRobot>> baseArchons = new EnumMap<>(Team.class);
    private final Map<MapLocation, InternalRobot> gameObjectsByLoc = new HashMap<>();

    private Map<MapLocation, Integer> partsMap = new HashMap<>();
    private Map<MapLocation, Integer> rubbleMap = new HashMap<>();
     
    private Map<Team, GameMap.MapMemory> mapMemory = new EnumMap<>(
            Team.class);

    private Map<Team, Map<Integer, Integer>> radio = new EnumMap<>(
            Team.class);

    // a count for each robot type per team for tech tree checks and for tower
    // counts
    private Map<Team, Map<RobotType, Integer>> activeRobotTypeCount = new EnumMap<>(
            Team.class); // only includes active bots
    private Map<Team, Map<RobotType, Integer>> totalRobotTypeCount = new EnumMap<>(
            Team.class); // includes inactive buildings

    // robots to remove from the game at end of turn
    private List<InternalRobot> deadRobots = new ArrayList<>();
    private boolean removingDead = false;

    @SuppressWarnings("unchecked")
    public GameWorld(GameMap gm, String teamA, String teamB,
            long[][] oldTeamMemory) {
        currentRound = -1;
        teamAName = teamA;
        teamBName = teamB;
        gameObjectsByID = new LinkedHashMap<>();
        signals = new ArrayList<>();
        randGen = new Random(gm.getSeed());
        nextID = 1;
        teamMemory = new long[2][oldTeamMemory[0].length];
        this.oldTeamMemory = oldTeamMemory;

        gameMap = gm;

        mapMemory.put(Team.A, new GameMap.MapMemory(gameMap));
        mapMemory.put(Team.B, new GameMap.MapMemory(gameMap));
        mapMemory.put(Team.NEUTRAL, new GameMap.MapMemory(gameMap));
        mapMemory.put(Team.ZOMBIE, new GameMap.MapMemory(gameMap));

        radio.put(Team.A, new HashMap<>());
        radio.put(Team.B, new HashMap<>());

        activeRobotTypeCount.put(Team.A, new EnumMap<>(
                RobotType.class));
        activeRobotTypeCount.put(Team.B, new EnumMap<>(
                RobotType.class));
        activeRobotTypeCount.put(Team.NEUTRAL, new EnumMap<>(
                RobotType.class));
        activeRobotTypeCount.put(Team.ZOMBIE, new EnumMap<>(
                RobotType.class));
        totalRobotTypeCount.put(Team.A, new EnumMap<>(
                RobotType.class));
        totalRobotTypeCount.put(Team.B, new EnumMap<>(
                RobotType.class));
        totalRobotTypeCount.put(Team.NEUTRAL, new EnumMap<>(
                RobotType.class));
        totalRobotTypeCount.put(Team.ZOMBIE, new EnumMap<>(
                RobotType.class));

        baseArchons.put(Team.A, new HashSet<>());
        baseArchons.put(Team.B, new HashSet<>());

        adjustResources(Team.A, GameConstants.PARTS_INITIAL_AMOUNT);
        adjustResources(Team.B, GameConstants.PARTS_INITIAL_AMOUNT);

        removingDead = false;

        reserveRandomIDs(32000);

        // Add the robots contained in the GameMap to this world.
        for (GameMap.InitialRobotInfo initialRobot : gameMap.getInitialRobots()) {
            // Side-effectful constructor; will add robot to relevant stuff
            new InternalRobot(
                    this,
                    initialRobot.type,
                    initialRobot.getLocation(gameMap.getMapOrigin()),
                    initialRobot.team,
                    0,
                    Optional.empty()
            );
        }
    }

    // *********************************
    // ****** BASIC MAP METHODS ********
    // *********************************

    public GameMap.MapMemory getMapMemory(Team t) {
        return mapMemory.get(t);
    }

    public int getMapSeed() {
        return gameMap.getSeed();
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public InternalRobot getObject(MapLocation loc) {
        return gameObjectsByLoc.get(loc);
    }

    public <T extends InternalRobot> T getObjectOfType(MapLocation loc,
            Class<T> cl) {
        InternalRobot o = getObject(loc);
        if (cl.isInstance(o))
            return cl.cast(o);
        else
            return null;
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

    public InternalRobot getRobotByID(int id) {
        return getObjectByID(id);
    }

    public boolean exists(InternalRobot o) {
        return gameObjectsByID.containsKey(o.getID());
    }

    public int getMessage(Team t, int channel) {
        Integer val = radio.get(t).get(channel);
        return val == null ? 0 : val;
    }

    public RoundStats getRoundStats() {
        return roundStats;
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

    public void addSignal(Signal s) {
        signals.add(s);
    }

    public void clearAllSignals() {
        signals.clear();
    }

    public void notifyBreakpoint() {
        wasBreakpointHit = true;
    }

    public boolean wasBreakpointHit() {
        return wasBreakpointHit;
    }

    public void reserveRandomIDs(int num) {
        while (num > 0) {
            randomIDs.add(nextID++);
            num--;
        }
        Collections.shuffle(randomIDs, randGen);
    }

    public int nextID() {
        if (randomIDs.isEmpty()) {
            int ret = nextID;
            nextID += randGen.nextInt(3) + 1;
            return ret;
        } else
            return randomIDs.remove(randomIDs.size() - 1);
    }

    public boolean canSense(Team team, MapLocation loc) {
        return mapMemory.get(team).canSense(loc);
    }

    public void updateMapMemoryAdd(Team team, MapLocation loc, int radiusSquared) {
        mapMemory.get(team).rememberLocation(loc, radiusSquared, partsMap, rubbleMap);
    }

    public void updateMapMemoryRemove(Team team, MapLocation loc,
            int radiusSquared) {
        mapMemory.get(team).removeLocation(loc, radiusSquared);
    }

    public boolean canMove(MapLocation loc, RobotType type) {
        return (gameMap.onTheMap(loc) &&
                (!rubbleMap.containsKey(loc) || rubbleMap.get(loc) < GameConstants.RUBBLE_OBSTRUCTION_THRESH || type == RobotType.SCOUT) &&
                gameObjectsByLoc.get(loc) == null);
    }

    protected boolean canAttackSquare(InternalRobot ir, MapLocation loc) {
        MapLocation myLoc = ir.getLocation();
        int d = myLoc.distanceSquaredTo(loc);
        int radius = ir.type.attackRadiusSquared;
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

    // *********************************
    // ****** ENGINE ACTIONS ***********
    // *********************************

    // should only be called by the InternalRobot constructor
    public void notifyAddingNewObject(InternalRobot o) {
        if (gameObjectsByID.containsKey(o.getID()))
            return;
        gameObjectsByID.put(o.getID(), o);
        if (o.getLocation() != null) {
            gameObjectsByLoc.put(o.getLocation(), o);
        }
    }

    // TODO: move stuff to here
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

    public void removeObject(InternalRobot o) {
        if (o.getLocation() != null) {
            MapLocation loc = o.getLocation();
            if (gameObjectsByLoc.get(loc) == o)
                gameObjectsByLoc.remove(loc);
            else
                System.out.println("Couldn't remove " + o + " from the game");
        } else
            System.out.println("Couldn't remove " + o + " from the game");

        if (gameObjectsByID.get(o.getID()) == o) {
            gameObjectsByID.remove(o.getID());
        }

        o.freeMemory();
    }

    public void beginningOfExecution(int robotID) {
        InternalRobot r = getObjectByID(robotID);
        if (r != null)
            r.processBeginningOfTurn();
    }

    public void endOfExecution(int robotID) {
        InternalRobot r = getObjectByID(robotID);
        // if the robot is dead, it won't be in the map any more
        if (r != null) {
            r.setBytecodesUsed(RobotMonitor.getBytecodesUsed());
            r.processEndOfTurn();
        }
    }

    public void notifyDied(InternalRobot r) {
        deadRobots.add(r);
    }

    public void removeDead() {
        removingDead = true;
        boolean current = false;
        while (deadRobots.size() > 0) {
            InternalRobot r = deadRobots.remove(deadRobots.size() - 1);
            if (r.getID() == RobotMonitor.getCurrentRobotID())
                current = true;
            visitSignal(new DeathSignal(r.getID())); // If infected, also turns into zombie
        }
        removingDead = false;
        if (current)
            throw new RobotDeathException();
    }

    // *********************************
    // ****** COUNTING ROBOTS **********
    // *********************************

    // only returns active robots
    public int getActiveRobotTypeCount(Team team, RobotType type) {
        if (activeRobotTypeCount.get(team).containsKey(type)) {
            return activeRobotTypeCount.get(team).get(type);
        } else {
            return 0;
        }
    }

    public void incrementActiveRobotTypeCount(Team team, RobotType type) {
        if (activeRobotTypeCount.get(team).containsKey(type)) {
            activeRobotTypeCount.get(team).put(type,
                    activeRobotTypeCount.get(team).get(type) + 1);
        } else {
            activeRobotTypeCount.get(team).put(type, 1);
        }
    }
    
    // decrement from active robots (used during TTM <-> Turret transform)
    public void decrementActiveRobotTypeCount(Team team, RobotType type) {
        Integer currentCount = activeRobotTypeCount.get(team).get(type);
        activeRobotTypeCount.get(team).put(type,currentCount - 1);
    }

    // returns active and inactive robots
    public int getTotalRobotTypeCount(Team team, RobotType type) {
        if (totalRobotTypeCount.get(team).containsKey(type)) {
            return totalRobotTypeCount.get(team).get(type);
        } else {
            return 0;
        }
    }

    public void incrementTotalRobotTypeCount(Team team, RobotType type) {
        if (totalRobotTypeCount.get(team).containsKey(type)) {
            totalRobotTypeCount.get(team).put(type,
                    totalRobotTypeCount.get(team).get(type) + 1);
        } else {
            totalRobotTypeCount.get(team).put(type, 1);
        }
    }
    
    // *********************************
    // ****** RUBBLE METHODS **********
    // *********************************
    public int getRubble(MapLocation loc) {
        return rubbleMap.get(loc);
    }
    
    public int senseRubble(Team team, MapLocation loc) {
        return mapMemory.get(team).recallRubble(loc);
    }
    
    public void alterRubble(MapLocation loc, int amount) {
        rubbleMap.put(loc, rubbleMap.get(loc)+amount);
    }

    // *********************************
    // ****** PARTS METHODS ************
    // *********************************
    public int getParts(MapLocation loc) {
        return partsMap.get(loc);
    }

    public int senseParts(Team team, MapLocation loc) {
        return mapMemory.get(team).recallParts(loc);
    }

    public int takeParts(MapLocation loc) { // Remove parts from location
        int prevVal = partsMap.containsKey(loc) ? partsMap.get(loc) : 0;
        partsMap.put(loc,0);
        return prevVal;
      /*  double cur = 0;
        if (oreMined.containsKey(loc)) {
            cur = oreMined.get(loc);
        }
        oreMined.put(loc, cur + amount);
        addSignal(new LocationOreChangeSignal(loc, cur + amount));*/
    }

    protected boolean spendResources(Team t, int amount) {
        if (teamResources[t.ordinal()] >= amount) {
            teamResources[t.ordinal()] -= amount;
            return true;
        } else
            return false;
    }

    protected void adjustResources(Team t, int amount) {
        teamResources[t.ordinal()] += amount;
    }

    public double resources(Team t) {
        return teamResources[t.ordinal()];
    }

    // *********************************
    // ****** GAMEPLAY *****************
    // *********************************

    public void processBeginningOfRound() {
        currentRound++;

        nextID += randGen.nextInt(10);

        wasBreakpointHit = false;

        // process all gameobjects
        InternalRobot[] gameObjects = new InternalRobot[gameObjectsByID.size()];
        gameObjects = gameObjectsByID.values().toArray(gameObjects);
        for (InternalRobot gameObject : gameObjects) {
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
        InternalRobot[] gameObjects = new InternalRobot[gameObjectsByID.size()];
        gameObjects = gameObjectsByID.values().toArray(gameObjects);
        for (InternalRobot gameObject : gameObjects) {
            gameObject.processEndOfRound();
        }
        removeDead();

        // update map memory
        /*
         * for (int i = 0; i < gameObjects.length; i++) { InternalRobot ir =
         * (InternalRobot) gameObjects[i];
         * mapMemory.get(ir.getTeam()).rememberLocations(ir.getLoc(),
         * ir.type.sensorRadiusSquared, oreMined); }
         */

        // free parts
        teamResources[Team.A.ordinal()] += GameConstants.ARCHON_PART_INCOME
                * getActiveRobotTypeCount(Team.A, RobotType.ARCHON);
        teamResources[Team.B.ordinal()] += GameConstants.ARCHON_PART_INCOME
                * getActiveRobotTypeCount(Team.B, RobotType.ARCHON);

        // Add signals for team resources
        for (final Team team : Team.values()) {
            addSignal(new TeamOreSignal(team, teamResources[team.ordinal()]));
        }

        if (timeLimitReached() && winner == null) {
            // tiebreak by number of Archons
            if (!(setWinnerIfNonzero(
                    getActiveRobotTypeCount(Team.A, RobotType.ARCHON)
                            - getActiveRobotTypeCount(Team.B, RobotType.ARCHON),
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
                        partsDiff += obj.type.partCost;
                    } else if (obj.getTeam() == Team.B) {
                        partsDiff -= obj.type.partCost;
                    }
                    if (obj.type == RobotType.ARCHON) {
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
                    RobotMonitor.killRobot(o.getID());
                }
            }
        }

        roundStats = new RoundStats(teamResources[0], teamResources[1]);
    }

    public Signal[] getAllSignals(boolean includeBytecodesUsedSignal) {
        ArrayList<InternalRobot> allRobots = new ArrayList<>();
        for (InternalRobot obj : gameObjectsByID.values()) {
            if (obj == null)
                continue;
            allRobots.add(obj);
        }

        InternalRobot[] robots = allRobots.toArray(new InternalRobot[allRobots.size()]);

        if (includeBytecodesUsedSignal) {
            signals.add(new BytecodesUsedSignal(robots));
        }
        signals.add(new RobotDelaySignal(robots));

        HealthChangeSignal healthChange = new HealthChangeSignal(robots);

        // Reset health levels.
        for (final InternalRobot robot : robots) {
            robot.clearHealthChanged();
        }

        if (healthChange.getRobotIDs().length > 0) {
            signals.add(healthChange);
        }

        return signals.toArray(new Signal[signals.size()]);
    }

    // ******************************
    // SIGNAL HANDLER METHODS
    // ******************************

    SignalHandler signalHandler = new AutoSignalHandler(this);

    public void visitSignal(Signal s) {
        signalHandler.visitSignal(s);
    }

    public void visitAttackSignal(AttackSignal s) {
        InternalRobot attacker = getObjectByID(s.getRobotID());

        MapLocation targetLoc = s.getTargetLoc();
        double rate = 1.0;

        switch (attacker.type) { // Only attacking types
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

            InternalRobot[] targets = getAllRobotsWithinRadiusSq(targetLoc,
                    splashRadius);

            for (InternalRobot target : targets) {
                if (target.getTeam() != attacker.getTeam()) {

                    if (attacker.type == RobotType.GUARD
                            && target.type.isZombie)
                        rate = GameConstants.GUARD_ZOMBIE_MULTIPLIER;

                    if (attacker.type.canInfect() && target.type.isInfectable())
                        target.setInfected(attacker);

                    double damage = (attacker.type.attackPower) * rate;
                    target.takeDamage(damage, attacker);
                }
            }
            break;
        default:
            // ERROR, should never happen
        }
        addSignal(s);
        removeDead();
    }

    public void visitBroadcastSignal(BroadcastSignal s) {
        radio.get(s.getRobotTeam()).putAll(s.broadcastMap);
        addSignal(s);
    }

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

        InternalRobot robot = new InternalRobot(
                this,
                s.getType(),
                loc,
                s.getTeam(),
                s.getDelay(),
                Optional.of(parent)
        );

        // addSignal(s); //client doesn't need this one
    }

    public void visitControlBitsSignal(ControlBitsSignal s) {
        InternalRobot r = getObjectByID(s.getRobotID());
        r.setControlBits(s.getControlBits());

        addSignal(s);
    }

    // TODO: this might need some reorganization
    // right now, visitDeathSignal is often called from removeDead
    // and visitDeathSignal might trigger new robot deaths
    // which gets complicated, because it might call removeDead again.
    // there's various hacks in place to deal with this but it's all really
    // messy
    // and for the same robot visitDeathSignal might get called multiple times.
    public void visitDeathSignal(DeathSignal s) {
        if (!running) {
            // All robots emit death signals after the game
            // ends. We still want the client to draw
            // the robots.
            return;
        }
        int ID = s.getObjectID();
        InternalRobot obj = getObjectByID(ID);

        if (obj != null) {
            removeObject(obj);
        }
        if (obj != null) {
            RobotMonitor.killRobot(ID);

            // update robot counting
            if (obj.isActive()) {
                Integer currentCount = activeRobotTypeCount.get(obj.getTeam())
                        .get(obj.type);
                activeRobotTypeCount.get(obj.getTeam()).put(obj.type,
                        currentCount - 1);
            }
            Integer currentCount = totalRobotTypeCount.get(obj.getTeam()).get(
                    obj.type);
            totalRobotTypeCount.get(obj.getTeam()).put(obj.type, currentCount - 1);

            if (obj.hasBeenAttacked()) {
                gameStats.setUnitKilled(obj.getTeam(), currentRound);
            }
            if (obj.type == RobotType.ARCHON) {
                int totalArchons = getActiveRobotTypeCount(obj.getTeam(),
                        RobotType.ARCHON);
                if (totalArchons == 0) {
                    setWinner(obj.getTeam().opponent(),
                            DominationFactor.DESTROYED);
                }
            }
            // if it was an infected robot, create a Zombie in its place.
            if (obj.isInfected()) {
                RobotType zombieType = obj.type.turnsInto; // Type of Zombie this unit turns into

                // Create new Zombie

                InternalRobot robot = new InternalRobot(
                        this,
                        zombieType,
                        obj.getLocation(),
                        Team.ZOMBIE,
                        0,
                        Optional.of(obj)); // TODO: Figure out Runnable and get that working
            }

            updateMapMemoryRemove(obj.getTeam(), obj.getLocation(),
                    obj.type.sensorRadiusSquared);
        }
        if (obj != null) {
            addSignal(s);
        }
    }

    public void visitIndicatorDotSignal(IndicatorDotSignal s) {
        addSignal(s);
    }

    public void visitIndicatorLineSignal(IndicatorLineSignal s) {
        addSignal(s);
    }

    public void visitIndicatorStringSignal(IndicatorStringSignal s) {
        addSignal(s);
    }

    public void visitMatchObservationSignal(MatchObservationSignal s) {
        addSignal(s);
    }

    public void visitMovementSignal(MovementSignal s) {
        InternalRobot r = getObjectByID(s.getRobotID());
        r.setLocation(s.getNewLoc());
        int newParts = takeParts(r.getLocation());
        adjustResources(r.getTeam(), newParts);
        addSignal(s);
    }

    public void visitMovementOverrideSignal(MovementOverrideSignal s) {
        InternalRobot r = getObjectByID(s.getRobotID());
        r.setLocation(s.getNewLoc());

        addSignal(s);
    }

    @SuppressWarnings("unchecked")
    public void visitSpawnSignal(SpawnSignal s) {
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

        InternalRobot robot =
                new InternalRobot(this, s.getType(), loc, s.getTeam(), s.getDelay(), Optional.of(parent));
    }
}
