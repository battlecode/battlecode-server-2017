package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

/**
 * Signifies that a robot is capturing
 *
 * @author yp
 */
public class CaptureSignal extends Signal {


    /**
	 * 
	 */
	private static final long serialVersionUID = -1724238075125375688L;

	/**
     * The new robot's ID
     */
    private final int robotID;

    /**
     * The parent robot's ID, or 0 if there was no parent
     */
    private final int parentID;

    /**
     * The new robot's location
     */
    private final MapLocation loc;

    /**
     * The type of the new robot
     */
    private final RobotType type;

    /**
     * The new robot's team
     */
    private final Team team;
    
    /**
     * If the robot has handling
     */
    private final boolean hasHandling;


    /**
     * Creates a capture signal for capture which is starting
     */
    public CaptureSignal(MapLocation loc, RobotType type, Team team, boolean handling, InternalRobot parent) {
        this.loc = loc;
        this.type = type;
        this.team = team;
        robotID = 0;
        if (parent == null)
            parentID = 0;
        else
            parentID = parent.getID();
//        dir = null;
        hasHandling = handling;
    }

    public int getRobotID() {
        return robotID;
    }

    public MapLocation getLoc() {
        return loc;
    }

    public RobotType getType() {
        return type;
    }

    public Team getTeam() {
        return team;
    }

    public int getParentID() {
        return parentID;
    }
    
    public boolean getHandling() {
    	return hasHandling;
    }
}
