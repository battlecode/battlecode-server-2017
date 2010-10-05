package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.common.RobotType;
import battlecode.world.InternalRobot;

/**
 * Signifies that a robot was just spawned
 *
 * @author adamd
 */
public class EvolutionSignal extends Signal {

	private static final long serialVersionUID = 1175336431784324131L;
	
	/** The new robot's ID */
	private final int robotID;
		
	/** The new RobotType of the robot */
	private final RobotType type;
	
	/** 
	 * Creates a signal for a robot that just evolved
	 *
	 * @param robot the robot that evolved (after it evolved)
	 */
	public EvolutionSignal(InternalRobot robot, RobotType type) {
		robotID = robot.getID();
		this.type = type;
	}

	public int getRobotID() {
		return robotID;
	}
		
	public RobotType getType() {
		return type;
	}

}
