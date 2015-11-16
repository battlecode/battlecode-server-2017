package battlecode.engine.instrumenter;

import battlecode.engine.GenericRobot;
import battlecode.engine.instrumenter.lang.RoboPrintStream;
import battlecode.engine.instrumenter.lang.SilencedPrintStream;
import battlecode.engine.scheduler.Scheduler;
import battlecode.server.Config;
import battlecode.world.GameWorld;

import java.util.HashSet;
import java.util.Set;

/**
 * RobotMonitor is a singleton class for monitoring robots' bytecode execution and stack size, and killing robots' threads.  Player's classes should
 * be instrumented so that they make calls to RobotMonitor as appropriate.
 *
 * @author adamd
 */
public class RobotMonitor {

    private static int DEBUG_BYTECODES;

    private static final Set<Integer> robotsToKill = new HashSet<>();

    // the RobotData for the currently running robot
    public volatile static RobotData currentRobotData;

    private static boolean[] silenced = new boolean[4];

    private static int debugLevel;
    private static int bytecodeLimit;
    private static int bytecodesLeft;

    private static GameWorld myGameWorld = null;


    /**
     * A "struct" that holds data about a robot's execution, e.g., bytecodes, stack size, etc.
     */
    public static class RobotData {
        public int bytecodesLeft;
        public int debugLevel = 0;
        public final int ID;
        public boolean thrownRobotDeathException = false;

        public RobotData(int ID) {
            this.ID = ID;
        }
    }

    // private constructor enforces the singleton pattern
    private RobotMonitor() {
    }

    private static void init() {
        robotsToKill.clear();
        Config options = Config.getGlobalConfig();
        silenced[0] = options.getBoolean("bc.engine.silence-a");
        silenced[1] = options.getBoolean("bc.engine.silence-b");
        silenced[2] = options.getBoolean("bc.engine.silence-c");
        silenced[3] = options.getBoolean("bc.engine.silence-d");
        DEBUG_BYTECODES = options.getInt("bc.engine.debug-max-bytecodes");
    }

    /**
     * Resets the internal state of the RobotMonitor.  Should be called between games.
     */
    public static void reset() {
        init();
    }

    /**
     * Switches the currently active robot to the one referred to by the given RobotData.
     */
    public static void switchRunner(RobotData newData) {

        // switch RobotData to the new robot
        currentRobotData = newData;

        if (robotsToKill.contains(newData.ID)) {
            //System.out.println("Killing "+newData.ID);
            throw new RobotDeathException();
        }

        if (newData.ID >= 0) {
            myGameWorld.beginningOfExecution(newData.ID);
            GenericRobot robot = myGameWorld.getRobotByID(newData.ID);
            bytecodeLimit = robot.getBytecodeLimit();
            debugLevel = currentRobotData.debugLevel;
            currentRobotData.bytecodesLeft += bytecodeLimit;
            if (debugLevel == 0)
                bytecodesLeft = currentRobotData.bytecodesLeft;
            else
                bytecodesLeft = DEBUG_BYTECODES;

            if (silenced[robot.getTeam().ordinal()]) {
                SilencedPrintStream stream = SilencedPrintStream.theInstance();
                battlecode.engine.instrumenter.lang.System.out = stream;
                battlecode.engine.instrumenter.lang.System.err = stream;
            } else {
                RoboPrintStream stream = RoboPrintStream.theInstance();
                stream.changeRobot();
                battlecode.engine.instrumenter.lang.System.out = stream;
                battlecode.engine.instrumenter.lang.System.err = stream;
            }
        }
    }

    public static int getCurrentRobotID() {
        return currentRobotData.ID;
    }

    public static GenericRobot getCurrentRobot() {
        return myGameWorld.getRobotByID(currentRobotData.ID);
    }

    /**
     * Increments the active robot's debug level.  Should be called at the beginning of any debug method.
     */
    public static void incrementDebugLevel() {
        if (debugLevel == 0) {
            currentRobotData.bytecodesLeft = bytecodesLeft;
            bytecodesLeft = DEBUG_BYTECODES;
        }
        debugLevel++;
    }

    /**
     * Decrements the active robot's debug level.  Should be called at the end of any debug method.
     */
    public static void decrementDebugLevel() {
        debugLevel--;
        if (debugLevel == 0) {
            bytecodesLeft = currentRobotData.bytecodesLeft;
        }
    }

    /**
     * Increments the currently active robot's bytecode count by the given amount.  If the robot exceeds its bytecode limit for the round,
     * this method will block until the robot's next round.  Should be called at the end of every basic block.
     *
     * @param numBytecodes the number of bytecodes the robot just executed
     */
    public static void incrementBytecodes(int numBytecodes) {
        bytecodesLeft -= numBytecodes;

        while (bytecodesLeft <= 0) {
            endRunner();
        }
    }

    /**
     * Ends the run of the currently active robot.
     */
    public static void endRunner() {
        myGameWorld.endOfExecution(currentRobotData.ID);
        currentRobotData.debugLevel = debugLevel;
        if (debugLevel == 0)
            currentRobotData.bytecodesLeft = bytecodesLeft;
        if (currentRobotData.bytecodesLeft > 0)
            currentRobotData.bytecodesLeft = 0;
        Scheduler.passToNextThread();
    }

    /**
     * Kills the robot thread of the robot with the given ID.  More specifically, the next time the thread is activated,
     * it will throw a RobotDeathException.
     *
     * @param robotID the integer ID of the robot to kill
     */
    public static void killRobot(int robotID) {
        robotsToKill.add(robotID);
    }

    /**
     * Returns the bytecode number that the active robot is currently on.  Note that this can be above bytecodeLimit in some cases.
     */
    public static int getBytecodeNum() {
        return bytecodeLimit - getBytecodesLeft();
    }

    public static int getBytecodeLimit() {
        return bytecodeLimit;
    }

    public static int getBytecodesLeft() {
        if (debugLevel == 0)
            return bytecodesLeft;
        else
            return currentRobotData.bytecodesLeft;
    }

    /**
     * Returns the number of bytecodes that this robot has used this round.  Equal to
     * min(getBytecodeNum(),bytecodeLimit).
     */
    public static int getBytecodesUsed() {
        int num = getBytecodeNum();
        return (num <= bytecodeLimit) ? num : bytecodeLimit;
    }

    /**
     * Returns the percentage of this robot's maximum bytecodes that were used this round.
     */
    public static double getBytecodesUsedPercent() {
        return (double) getBytecodesUsed() / bytecodeLimit;
    }

    /**
     * Notifies the RobotMonitor of what the current GameWorld is.  Should be called before the start of each game.
     */
    public static void setGameWorld(GameWorld gw) {
        myGameWorld = gw;
    }

    public static boolean thrownRobotDeathException() {
        return currentRobotData.thrownRobotDeathException;
    }

    public static void checkForRobotDeath() {
        if (currentRobotData.thrownRobotDeathException)
            throw new RobotDeathException();
    }
}
