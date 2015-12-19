package battlecode.instrumenter.inject;

import battlecode.instrumenter.SandboxedRobotPlayer;

import java.io.PrintStream;

/**
 * The class used to count bytecodes and debug levels at player runtime; calls to its methods are injected
 * by the instrumenter.
 *
 * Don't let the fact that this class is "static" fool you. It's static for instrumentation convenience;
 * a new version is loaded every time a new robot player is loaded, and is specific to that robot.
 *
 * The specific call the instrumentation uses is "incrementBytecodes".
 *
 * @author adamd
 */
public final class RobotMonitor {
    private static int BYTECODE_LIMIT;

    private static int randomSeed;

    private static int bytecodesLeft;
    private static boolean shouldDie;

    private static SandboxedRobotPlayer.Pauser pauser;
    private static SandboxedRobotPlayer.Killer killer;

    // Methods called from SandboxedRobotPlayer

    /**
     * A "constructor".
     * Initializes the monitor.
     *
     * Called in the robot thread from SandboxedRobotPlayer.
     *
     * @param thePauser pauser to use to pause the thread
     */
    @SuppressWarnings("unused")
    public static void init(SandboxedRobotPlayer.Pauser thePauser,
                            SandboxedRobotPlayer.Killer theKiller,
                            int seed) {
        shouldDie = false;
        bytecodesLeft = 0;
        randomSeed = seed;
        pauser = thePauser;
        killer = theKiller;
    }

    /**
     * Set the bytecode limit of this robot.
     *
     * @param limit the new limit
     */
    @SuppressWarnings("unused")
    public static void setBytecodeLimit(int limit) {
        BYTECODE_LIMIT = limit;
    }

    /**
     * Set System.out for this robot.
     *
     * @param out the printstream to replace System.out with
     */
    @SuppressWarnings("unused")
    public static void setSystemOut(PrintStream out) {
        System.out = out;
        System.err = out;
    }

    /**
     * Kills the robot associated with this monitor.
     *
     * More specifically, the next time the thread is activated, it will throw a RobotDeathException.
     */
    @SuppressWarnings("unused")
    public static void killRobot() {
        shouldDie = true;
    }

    /**
     * @return the bytecode number that the active robot is currently on.
     *         Note that this can be above bytecodeLimit in some cases.
     */
    @SuppressWarnings("unused")
    public static int getBytecodeNum() {
        return BYTECODE_LIMIT - getBytecodesLeft();
    }

    /**
     * @return the bytecodes this robot has left to use.
     */
    @SuppressWarnings("unused")
    public static int getBytecodesLeft() {
        return bytecodesLeft;
    }

    // Methods called from RobotPlayer

    /**
     * Increments the currently active robot's bytecode count by the given amount.
     * If the robot exceeds its bytecode limit for the round, this method will block until the robot's next round.
     * Should be called at the end of every basic block.
     *
     * THIS METHOD IS CALLED BY THE INSTRUMENTER.
     *
     * @param numBytecodes the number of bytecodes the robot just executed
     */
    @SuppressWarnings("unused")
    public static void incrementBytecodes(int numBytecodes) {
        bytecodesLeft -= numBytecodes;

        while (bytecodesLeft <= 0) {
            pause();
        }
    }

    /**
     * Used to construct new Random instances.
     *
     * THIS METHOD IS CALLED BY THE INSTRUMENTER.
     *
     * @return the random seed for this robot
     */
    public static int getRandomSeed() {
        return randomSeed;
    }

    /**
     * Pauses the run of the current robot.
     *
     * Must be called from the robot's main thread.
     */
    public static void pause() {
        bytecodesLeft = 0;

        pauser.pause();

        reactivate();
    }

    /**
     * Restarts a paused robot.
     *
     * Must be called from the robot's main thread.
     */
    public static void reactivate() {

        if (shouldDie) {
            killer.kill();
        }

        bytecodesLeft = BYTECODE_LIMIT;
    }
}
