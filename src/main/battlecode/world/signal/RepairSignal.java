package battlecode.world.signal;

import battlecode.common.MapLocation;

/**
 * Signifies that a neutral robot is being activated by an archon.
 */
public class RepairSignal implements InternalSignal {

    private static final long serialVersionUID = -5655877873179895292L;

    /**
     * The activating robot's ID.
     */
    private final int robotID;

    /**
     * The ID of the robot being repaired.
     */
    private final int repairedRobotID;

    /**
     * InternalSignal for an archon repairing a robot.
     *
     * @param robotID the ID of the robot who is doing the activating.
     * @param repairedRobotID the ID of the robot being repaired.
     */
    public RepairSignal(int robotID, int repairedRobotID) {
        this.robotID = robotID;
        this.repairedRobotID = repairedRobotID;
    }

    public int getRobotID() {
        return robotID;
    }

    public int getRepairedRobotID() {
        return repairedRobotID;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private RepairSignal() {
        this(0, 0);
    }
}
