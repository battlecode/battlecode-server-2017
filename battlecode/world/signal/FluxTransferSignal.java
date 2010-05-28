package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;
import battlecode.world.InternalRobot;

/**
 * Signifies that a robot has transferred flux.
 */
public class FluxTransferSignal extends Signal {
	
	private static final long serialVersionUID = -488610493011448468L;

	/** The ID of the robot that just transferred flux. */
	private final int robotID;
		
	/** The location to which flux was just transferred */
	private final MapLocation targetLoc;
		
	/** The height to which flux was just transferred */
	private final RobotLevel targetHeight;
		
	/** The amount of flux that was transferred */
	private final double amount;

	/** 
	 * Creates a signal for a robot broadcast.
	 *
	 * @param robot the robot that transferred flux
	 * @param targetLoc the location to which the robot just transferred flux 
	 */
	public FluxTransferSignal(InternalRobot robot, MapLocation targetLoc, RobotLevel targetHeight, double amount) {
		this.robotID = robot.getID();
		this.targetLoc = targetLoc;
		this.targetHeight = targetHeight;
		this.amount = amount;
	}

	/**
	 * Returns the ID of the robot that just transferred flux.
	 *
	 * @return the transferring robot's ID
	 */
	public int getRobotID() {
		return robotID;
	}
	
	/**
	 * Returns the location to which flux was just transferred
	 *
	 * @return the location to which flux was just transferred
	 */
	public MapLocation getTargetLoc() {
		return targetLoc;
	}
	
	/**
	 * Returns the height to which flux was just transferred
	 *
	 * @return the height to which flux was just transferred
	 */
	public RobotLevel getTargetHeight() {
		return targetHeight;
	}
	
	/**
	 * Returns the amount of flux that was transferred
	 * 
	 * @retun the amount of flux thas was transferred
	 */
	public double getAmount() {
		return amount;
	}
}
