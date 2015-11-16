package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;
import com.fasterxml.jackson.annotation.JsonCreator;

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

    /**
     * Convenience constructor.
     *
     * @param robots the robots to poll for bytecode usage.
     */
    public BytecodesUsedSignal(final InternalRobot[] robots) {
        robotIDs = new int[robots.length];
        numBytecodes = new int[robots.length];
        for (int i = 0; i < robots.length; i++) {
            robotIDs[i] = robots[i].getID();
            numBytecodes[i] = robots[i].getBytecodesUsed();
        }
    }

    /**
     * Create a BytecodesUsed signal from a list of robots and a list of bytecodes.
     *
     * @param robotIDs      the ids of the robots
     * @param numBytecodes  the bytecodes used by the robots; must be same length as robotIDs
     */
    public BytecodesUsedSignal(int[] robotIDs, int[] numBytecodes) {
        assert robotIDs.length == numBytecodes.length;

        this.robotIDs = robotIDs;
        this.numBytecodes = numBytecodes;
    }

    public int[] getRobotIDs() {
        return robotIDs;
    }

    public int[] getNumBytecodes() {
        return numBytecodes;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private BytecodesUsedSignal() {
        this.robotIDs = null;
        this.numBytecodes = null;
    }
}
