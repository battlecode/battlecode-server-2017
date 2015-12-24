package battlecode.world.signal;

import battlecode.common.MapLocation;

/**
 * Signifies that a robot just attacked
 *
 * @author adamd
 */
public final class AttackSignal implements Signal {

    private static final long serialVersionUID = 8064711239305833273L;

    /**
     * TheID of the robot that attacked.
     */
    public final int robotID;

    /**
     * The location that the robot attacked
     */
    public final MapLocation targetLoc;

    /**
     * Creates a signal for a robot attack.
     *
     * @param robotID   the ID of the robot that attacked
     * @param targetLoc the location that the robot attacked
     */
    public AttackSignal(int robotID, MapLocation targetLoc) {
        this.robotID = robotID;
        this.targetLoc = targetLoc;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private AttackSignal() {
        this(0, null);
    }

    /**
     * Returns the ID of the robot that just attacked.
     *
     * @return the messaging robot's ID
     */
    public int getRobotID() {
        return robotID;
    }

    /**
     * Returns the location that the robot attacked
     *
     * @return the location that the robot attacked
     */
    public MapLocation getTargetLoc() {
        return targetLoc;
    }
}
