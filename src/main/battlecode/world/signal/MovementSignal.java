package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

/**
 * Signifies that a robot has moved.
 *
 * @author Matt
 */
public class MovementSignal extends Signal {

    private static final long serialVersionUID = -6853620834787044985L;

    /**
     * TheID of the robot that moved.
     */
    private final int robotID;

    /**
     * The robot's new location.
     */
    private final MapLocation newLoc;

    /**
     * Whether the robot got to the new location by moving forward or backward.
     */
    private final boolean isMovingForward;

    private final int delay;

    /**
     * Creates a signal for a robot broadcast.
     *
     * @param robot           the robot that broadcast the message
     * @param newLoc          the robot's new location
     * @param isMovingForward whether the robot got to the new location by moving forward or backward
     */
    public MovementSignal(InternalRobot robot, MapLocation newLoc, boolean isMovingForward) {
        this(robot, newLoc, isMovingForward, 0);
    }

    /**
     * Creates a signal for a robot broadcast.
     *
     * @param robot           the robot that broadcast the message
     * @param newLoc          the robot's new location
     * @param isMovingForward whether the robot got to the new location by moving forward or backward
     */
    public MovementSignal(InternalRobot robot, MapLocation newLoc, boolean isMovingForward, int delay) {
        this.robotID = robot.getID();
        this.newLoc = newLoc;
        this.isMovingForward = isMovingForward;
        this.delay = delay;
    }

    /**
     * Returns the ID of the robot that just broadcast.
     *
     * @return the messaging robot's ID
     */
    public int getRobotID() {
        return robotID;
    }

    /**
     * The robot's new location.
     *
     * @return the robot's new MapLocation
     */
    public MapLocation getNewLoc() {
        return newLoc;
    }

    /**
     * Whether the robot got to the new location by moving forward or backward.
     */
    public boolean isMovingForward() {
        return isMovingForward;
    }

    public int getDelay() {
        return delay;
    }
}
