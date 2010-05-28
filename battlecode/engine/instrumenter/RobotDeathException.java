package battlecode.engine.instrumenter;

/**
 * An exception used to kill robot threads when their robot dies in the game.  In other words, when a robot
 * dies, a RobotDeathException is thrown in the robot's thread, and it propagates all the way up to RobotRunnable,
 * ending the robot's thread.  This must be an unchecked exception, since it could be thrown anywhere in the player's code.
 *
 * @author adamd
 */
public class RobotDeathException extends Error {

    static final long serialVersionUID = -7919557494710405575L;
	
	public RobotDeathException() {
		super();
		RobotMonitor.currentRobotData.thrownRobotDeathException = true;
	}
	
}