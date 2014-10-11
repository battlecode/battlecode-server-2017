package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

/**
 * Signifies that a robot cast a spell.
 *
 * @author npinsker
 */
public class HatSignal extends Signal {

	private static final long serialVersionUID = -2274815073063498123L;
	
	/**
     * TheID of the robot that attacked.
     */
    public final int robotID;
    
    /**
     * The location that the robot cast the spell
     */
    public final MapLocation loc;

    public CastSignal(InternalRobot robot, MapLocation loc) {
        this.robotID = robot.getID();
        this.loc = loc;
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
     * Returns the location that the robot cast the spell
     *
     * @return the location that the robot cast the spell
     */
    public MapLocation getLoc() {
        return loc;
    }
}
