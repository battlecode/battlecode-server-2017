package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;
import battlecode.common.MapLocation;
import battlecode.common.CommanderSkillType;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Signifies that a robot cast a spell.
 *
 * @author npinsker
 */
public class CastSignal extends Signal {

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
