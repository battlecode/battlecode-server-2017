package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.world.InternalRobot;

/**
 * Signifies that a neutral robot is being activated by an archon.
 *
 * @author adamd
 */
public class ActivationSignal implements Signal {

    private static final long serialVersionUID = -5655877873179895292L;

    /**
     * The activating robot's ID.
     */
    private final int robotID;

    /**
     * The location of the neutral robot being activated.
     */
    private final MapLocation loc;

    /**
     * Signal for an archon activating a neutral robot.
     *
     * @param robotID the ID of the robot who is doing the activating.
     * @param loc the location of the robot
     */
    public ActivationSignal(int robotID, MapLocation loc) {
        this.robotID = robotID;
        this.loc = loc;
    }

    public int getRobotID() {
        return robotID;
    }

    public MapLocation getLoc() {
        return loc;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private ActivationSignal() {
        this(0, null);
    }
}
