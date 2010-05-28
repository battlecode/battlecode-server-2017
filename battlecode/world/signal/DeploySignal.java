package battlecode.world.signal;

import battlecode.world.InternalRobot;

public class DeploySignal extends Signal {

    static final long serialVersionUID = 6861936260819942952L;
    private final int robotID;

    public DeploySignal(InternalRobot r) {
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
