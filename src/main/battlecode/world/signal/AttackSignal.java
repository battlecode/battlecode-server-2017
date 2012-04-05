package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;
import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

/**
 * Signifies that a robot just attacked
 *
 * @author adamd
 */
public class AttackSignal extends Signal {

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
     * The height of the position that the robot attacked
     */
    public final RobotLevel targetHeight;

    /**
     * Creates a signal for a robot broadcast.
     *
     * @param robot     the robot that attacked
     * @param targetLoc the location that the robot attacked
     */
    public AttackSignal(InternalRobot robot, MapLocation targetLoc, RobotLevel targetHeight) {
        this.robotID = robot.getID();
        this.targetLoc = targetLoc;
        this.targetHeight = targetHeight;
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

    /**
     * Returns the height of the position that the robot attacked
     *
     * @return the height of the position that the robot attacked
     */
    public RobotLevel getTargetHeight() {
        return targetHeight;
    }
}
