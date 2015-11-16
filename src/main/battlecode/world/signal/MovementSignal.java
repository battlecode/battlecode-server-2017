package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.engine.signal.Signal;

/**
 * Signifies that a robot has moved.
 *
 * @author Matt
 */
public class MovementSignal implements Signal {

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
    private final boolean movingForward;

    /**
     * The delay of the movement (?)
     */
    private final int delay;

    /**
     * Creates a signal for a robot movement.
     *
     * @param robotID         the ID of the robot that is moving
     * @param newLoc          the robot's new location
     * @param movingForward whether the robot got to the new location by moving forward or backward
     * @param delay           the delay of the movement
     */
    public MovementSignal(int robotID, MapLocation newLoc, boolean movingForward, int delay) {
        this.robotID = robotID;
        this.newLoc = newLoc;
        this.movingForward = movingForward;
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
    public boolean getMovingForward() {
        return movingForward;
    }

    /**
     * The delay of the movement (?)
     */
    public int getDelay() {
        return delay;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private MovementSignal() {
        this(0, null, false, 0);
    }
}
