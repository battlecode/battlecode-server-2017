package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.common.RobotInfo;

/**
 * Signifies the RobotInfo associated with each robot.
 *
 * @author axc
 */
public class RobotInfoSignal extends Signal {

    private static final long serialVersionUID = 6617731214077155785L;

    private final int robotID;
    private final RobotInfo info;

    public RobotInfoSignal(int robotID, RobotInfo info) {
        this.robotID = robotID;
        this.info = info;
    }

    public int getID() {
        return this.robotID;
    }

    public RobotInfo getRobotInfo() {
        return this.info;
    }
}
