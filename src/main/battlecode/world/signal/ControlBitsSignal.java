package battlecode.world.signal;

import battlecode.engine.signal.Signal;

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
}
