package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

/**
 * Signifies that a robot's energon has just changed
 *
 * @author adamd
 */
public class BytecodesUsedSignal extends Signal {

    private static final long serialVersionUID = 6617731359077155785L;

    /**
     * TheID of the robot whose energon has changed.
     */
    private final int[] robotIDs;

    /**
     * The number of bytecodes each robot used this round
     */
    private final int[] numBytecodes;

    public BytecodesUsedSignal(InternalRobot[] robots) {
        robotIDs = new int[robots.length];
        numBytecodes = new int[robots.length];
        for (int i = 0; i < robots.length; i++) {
            robotIDs[i] = robots[i].getID();
            numBytecodes[i] = robots[i].getBytecodesUsed();
        }
    }

    public int[] getRobotIDs() {
        return robotIDs;
    }

    public int[] getNumBytecodes() {
        return numBytecodes;
    }
}
