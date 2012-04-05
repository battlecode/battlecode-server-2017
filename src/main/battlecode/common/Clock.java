package battlecode.common;

import battlecode.engine.Engine;
import battlecode.engine.instrumenter.RobotMonitor;

/**
 * A robot's internal clock, used for measuring "time" in bytecodes and rounds.
 *
 * @author Teh Devs
 */
public class Clock {

    // enforce singleton
    private Clock() {
    }

    /**
     * Returns the number of bytecodes the current robot has executed since the beginning
     * of the current round.
     */
    public static int getBytecodeNum() {
        return RobotMonitor.getBytecodeNum();
    }

    /**
     * Returns the current round number, where round 0 is the first round of the match.
     */
    public static int getRoundNum() {
        return Engine.getRoundNum();
    }

    /**
     * Returns the number of bytecodes this robot has left in this round.
     */
    public static int getBytecodesLeft() {
        return RobotMonitor.getBytecodesLeft();
    }
}
