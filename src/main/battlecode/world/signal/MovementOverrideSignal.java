package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.engine.signal.Signal;

/**
 * Signifies that a robot was moved by a user override -- i.e., it should warp to the new location without an animation
 *
 * @author Matt
 */
public class MovementOverrideSignal extends Signal {

    private static final long serialVersionUID = -5689176900158693078L;

    /**
     * TheID of the robot that moved.
     */
    private final int robotID;

    /**
     * The robot's new location.
     */
    private final MapLocation newLoc;


    /**
     * Creates a signal for a robot movement override.
     *
     * @param robot  the ID of the robot that was moved by the user
     * @param newLoc the robot's new location
     */
    public MovementOverrideSignal(int robotID, MapLocation newLoc) {
        this.robotID = robotID;
        this.newLoc = newLoc;
    }

    /**
     * Returns the ID of the robot that just moved.
     *
     * @return the moved robot's ID
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
}
