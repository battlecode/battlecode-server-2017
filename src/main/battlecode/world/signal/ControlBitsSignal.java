package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Signifies that the control bits of a robot have just changed.
 */
public class ControlBitsSignal extends Signal {

    private static final long serialVersionUID = -4229507739170593943L;

    private final int robotID;

    private final long controlBits;

    public ControlBitsSignal(int robotID, long controlBits) {
        this.robotID = robotID;
        this.controlBits = controlBits;
    }

    public int getRobotID() {
        return robotID;
    }

    public long getControlBits() {
        return controlBits;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private ControlBitsSignal() {
        this(0, 0L);
    }
}
