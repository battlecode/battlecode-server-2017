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
public class GameWorld{
    /**
     * The current round we're running.
     */
    protected int currentRound;

    /**
     * Whether we're running.
     */
    protected boolean running = true;

    protected Team winner = null;
    protected final IDGenerator idGeneratorRobots;
    protected final IDGenerator idGeneratorTrees;
    protected final IDGenerator idGeneratorBullets;

    private final GameMap gameMap;
    private final TeamInfo teamInfo;
    private final ObjectInfo objectInfo;

    private final RobotControlProvider controlProvider;
    private final GameStats gameStats = new GameStats();
    private Random rand;

    @SuppressWarnings("unchecked")
    public GameWorld(GameMap gm, RobotControlProvider cp,
                     String teamA, String teamB,
                     long[][] oldTeamMemory) {
        
        this.currentRound = -1;
        this.idGeneratorRobots = new IDGenerator(gm.getSeed());
        this.idGeneratorTrees = new IDGenerator(gm.getSeed());
        this.idGeneratorBullets = new IDGenerator(gm.getSeed());

        this.gameMap = gm;
        this.objectInfo = new ObjectInfo(gm);
        this.teamInfo = new TeamInfo(teamA, teamB, oldTeamMemory);

        this.controlProvider = cp;

        controlProvider.matchStarted(this);

        // Add the robots contained in the GameMap to this world.

        // Add the trees contained in the GameMap to this world.

        this.rand = new Random(gameMap.getSeed());
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
            this.processBeginningOfRound();
            this.controlProvider.roundStarted();

            // We iterate through the IDs so that we avoid ConcurrentModificationExceptions
            // of an iterator. Kinda gross, but whatever.
            final int[] idsToRun = objectInfo.getRobotIDs();

            for (final int id : idsToRun) {
                final InternalRobot robot = objectInfo.getRobotByID(id);
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
                if (this.controlProvider.getTerminated(robot) && objectInfo.getRobotByID(id) != null) {
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

    public GameStats getGameStats() {
        return gameStats;
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public TeamInfo getTeamInfo() {
        return teamInfo;
    }

    public ObjectInfo getObjectInfo() {
        return objectInfo;
    }

    public Team getWinner() {
        return winner;
    }

    public boolean isRunning() {
        return running;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    // *********************************
    // ****** GAMEPLAY *****************
    // *********************************

    public void processBeginningOfRound() {

    }

    public void setWinner(Team t, DominationFactor d) {
        winner = t;
        gameStats.setDominationFactor(d);
        // running = false;

    }

    public boolean setWinnerIfNonzero(double n, DominationFactor d) {
        if (n > 0)
            setWinner(Team.A, d);
        else if (n < 0)
            setWinner(Team.B, d);
        return n != 0;
    }

    public boolean timeLimitReached() {
        return currentRound >= gameMap.getRounds() - 1;
    }

    public void processEndOfRound() {

    }

}
