package battlecode.world.signal;

import battlecode.world.InternalRobot;
import battlecode.common.MapLocation;

public class StartTeleportSignal extends Signal {

	private static final long serialVersionUID = 35077007146L;
	
	private final int robotID;

	private final int fromTeleporterID;
	private final int toTeleporterID;
	private final MapLocation teleportLoc;

	
	/** 
	 * Creates a signal for a robot that just changed its direction
	 *
	 * @param robot The robot that is teleporting
	 */
	public StartTeleportSignal(InternalRobot robot, InternalRobot fromTeleporter, InternalRobot toTeleporter, MapLocation teleportLoc) {
		robotID = robot.getID();
		this.fromTeleporterID=fromTeleporter.getID();
		this.toTeleporterID=toTeleporter.getID();
		this.teleportLoc=teleportLoc;
	}

	public int getRobotID() {
		return robotID;
	}
	
	public int getFromTeleporterID() {
		return fromTeleporterID;
	}

	public int getToTeleporterID() {
		return toTeleporterID;
	}

	public MapLocation getTeleportLoc() {
		return teleportLoc;
	}

}
