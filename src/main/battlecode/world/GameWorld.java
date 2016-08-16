package battlecode.world;

import battlecode.common.*;
import battlecode.server.ErrorReporter;
import battlecode.server.GameState;
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
    protected final long[][] teamMemory;
    protected final long[][] oldTeamMemory;
    protected final IDGenerator idGeneratorRobots;
    protected final IDGenerator idGeneratorTrees;
    protected final IDGenerator idGeneratorBullets;

    private final GameMap gameMap;
    private final ObjectInfo objectMap;

    private final RobotControlProvider controlProvider;

    private final GameStats gameStats = new GameStats(); // end-of-game stats

    private int[] teamVictoryPoints = new int[3];
    private double[] teamBulletSupplies = new double[3];
    private int[][] teamSharedArrays = new int[3][GameConstants.BROADCAST_MAX_CHANNELS];

    private Random rand;

    @SuppressWarnings("unchecked")
    public GameWorld(GameMap gm, RobotControlProvider cp,
                     String teamA, String teamB,
                     long[][] oldTeamMemory) {
        
        currentRound = -1;
        teamAName = teamA;
        teamBName = teamB;
        idGeneratorRobots = new IDGenerator(gm.getSeed());
        idGeneratorTrees = new IDGenerator(gm.getSeed());
        idGeneratorBullets = new IDGenerator(gm.getSeed());
        teamMemory = new long[2][oldTeamMemory[0].length];
        this.oldTeamMemory = oldTeamMemory;

        gameMap = gm;
        objectMap = new ObjectInfo(gm);
        controlProvider = cp;

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

}
