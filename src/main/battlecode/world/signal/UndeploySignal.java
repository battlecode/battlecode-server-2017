package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

public class UndeploySignal extends Signal {

    static final long serialVersionUID = 6861936260819942284L;
    private final int robotID;

    public UndeploySignal(InternalRobot r) {
        robotID = r.getID();
    }

    /**
     * Returns the ID of the robot that is draining
     *
     * @return the messaging robot's ID
     */
    public int getRobotID() {
        return robotID;
    }
}
