package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.common.MapLocation;

/**
 * Signifies that a robot cast a spell.
 *
 * @author npinsker
 */
public class CastSignal implements Signal {

	private static final long serialVersionUID = -2274815073063498123L;
	
	/**
     * TheID of the robot that attacked.
     */
    public final int robotID;
    
    /**
     * The location that the robot cast the spell
     */

    public final MapLocation targetLoc;

    public CastSignal(int robotID, MapLocation targetLoc) {
        this.robotID = robotID;
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
     * Returns the location that the robot cast the spell
     *
     * @return the location that the robot cast the spell
     */
    public MapLocation getTargetLoc() {
        return targetLoc;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private CastSignal() {
        this(0, null);
    }
}
