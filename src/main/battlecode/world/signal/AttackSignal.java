package battlecode.world.signal;

import battlecode.common.MapLocation;
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
     * Creates a signal for a robot attack.
     *
     * @param robot
     *            the robot that attacked
     * @param targetLoc
     *            the location that the robot attacked
     */
    public AttackSignal(InternalRobot robot, MapLocation targetLoc) {
        this.robotID = robot.getID();
        this.targetLoc = targetLoc;
    }

    public AttackSignal(int ID, MapLocation targetLoc) {
        this.robotID = ID;
        this.targetLoc = targetLoc;
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
