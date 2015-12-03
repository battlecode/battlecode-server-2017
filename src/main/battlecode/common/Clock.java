package battlecode.common;

import battlecode.engine.instrumenter.RobotMonitor;

/**
 * Clock is a singleton that allows contestants to introspect the state of their running
 * code.
 *
 * @author james
 */
@SuppressWarnings("unused")
public final class Clock {

    /**
     * Ends the processing of this robot during the current round. Never fails.
     */
    public static void yield() {
        RobotMonitor.pause();
    }

    /**
     * Returns the number of bytecodes this robot has left in this round.
     * @return the number of bytecodes this robot has left in this round.
     */
    public static int getBytecodesLeft() {
        return RobotMonitor.getBytecodesLeft();
    }

    /**
     * Returns the number of bytecodes the current robot has executed since the beginning
     *      of the current round.
     * @return the number of bytecodes the current robot has executed since the beginning
     *      of the current round.
     */
    public static int getBytecodeNum() {
        return RobotMonitor.getBytecodeNum();
    }

    /**
     * Prevent construction.
     */
    private Clock() {}

    // TODO(jhgilles): robotmonitor reference check
}
