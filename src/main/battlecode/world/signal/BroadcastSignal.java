package battlecode.world.signal;

import battlecode.common.Message;
import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

/**
 * Signifies that a robot has broadcast a message.
 *
 * @author Matt
 */
public class BroadcastSignal extends Signal {
	
	private static final long serialVersionUID = 8603786984259160822L;
		
	/** TheID of the robot that broadcasted the message. */
	public final int robotID;
	public final int range;
	public transient Message message;

	/** 
	 * Creates a signal for a robot broadcast.
	 *
	 * @param robot the robot that broadcast the message
	 */
	public BroadcastSignal(InternalRobot robot, int range) {
		this.robotID = robot.getID();
		this.range = range;
	}

	public BroadcastSignal(InternalRobot robot, int range, Message message) {
		this(robot,range);
		this.message = message;
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
