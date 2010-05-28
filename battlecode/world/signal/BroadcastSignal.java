package battlecode.world.signal;

import battlecode.world.InternalRobot;

/**
 * Signifies that a robot has broadcast a message.
 *
 * @author Matt
 */
public class BroadcastSignal extends Signal {
	
	private static final long serialVersionUID = 8603786984259160822L;
		
	/** TheID of the robot that broadcasted the message. */
	private final int robotID;

	/** 
	 * Creates a signal for a robot broadcast.
	 *
	 * @param robot the robot that broadcast the message
	 */
	public BroadcastSignal(InternalRobot robot) {
		this.robotID = robot.getID();
	}

	/**
	 * Returns the ID of the robot that just broadcast.
	 *
	 * @return the messaging robot's ID
	 */
	public int getRobotID() {
		return robotID;
	}
}
