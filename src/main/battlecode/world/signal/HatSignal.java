package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

/**
 * Signifies that a robot just wore a hat
 *
 * @author yp
 */
public class HatSignal extends Signal {

	private static final long serialVersionUID = -2274815073063498123L;
	
	/**
     * TheID of the robot that attacked.
     */
    public final int robotID;
    
    public final int hat;

    
    public HatSignal(InternalRobot robot, int hat) {
        this.robotID = robot.getID();
        this.hat = hat;
    }

    /**
     * Returns the ID of the robot that just attacked.
     *
     * @return the messaging robot's ID
     */
    public int getRobotID() {
        return robotID;
    }
}
