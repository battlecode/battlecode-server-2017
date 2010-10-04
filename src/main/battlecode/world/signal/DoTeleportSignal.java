package battlecode.world.signal;

import battlecode.world.InternalRobot;
import battlecode.common.MapLocation;

public class DoTeleportSignal extends Signal {

	private static final long serialVersionUID = 350770075725L;
	
	private final int robotID;

	private final MapLocation teleportLoc;

	
	/** 
	 * Creates a signal for a robot that just changed its direction
	 *
	 * @param robot The robot that is teleporting
	 */
	public DoTeleportSignal(InternalRobot robot, MapLocation teleportLoc) {
		robotID = robot.getID();
		this.teleportLoc=teleportLoc;
	}

	public int getRobotID() {
		return robotID;
	}

	public MapLocation getTeleportLoc() {
		return teleportLoc;
	}

}
