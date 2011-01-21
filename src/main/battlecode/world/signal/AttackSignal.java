package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;
import battlecode.common.ComponentType;
import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;

/**
 * Signifies that a robot just attacked
 *
 * @author adamd
 */
public class AttackSignal extends Signal {
	
	private static final long serialVersionUID = 8064711239305833273L;

	/** TheID of the robot that attacked. */
	public final int robotID;
		
	/** The location that the robot attacked */
	public final MapLocation targetLoc;
		
	/** The height of the position that the robot attacked */
	public final RobotLevel targetHeight;

	/** The weapon that was used */
	public final ComponentType weaponType;

	public final double damage;

	/** 
	 * Creates a signal for a robot broadcast.
	 *
	 * @param robot the robot that attacked
	 * @param targetLoc the location that the robot attacked
	 */
	public AttackSignal(InternalRobot robot, ComponentType weapon, MapLocation targetLoc, RobotLevel targetHeight, double damage) {
		this.robotID = robot.getID();
		this.weaponType = weapon;
		this.targetLoc = targetLoc;
		this.targetHeight = targetHeight;
		this.damage = damage * (robot.getBuffs().getDamageDealtMultiplier()+1);
		//System.out.println("team: " + robot.getTeam() + " original damage: " + damage + " new damage: " + this.damage);
	}

	public AttackSignal(InternalRobot robot, ComponentType weapon, MapLocation targetLoc, RobotLevel targetHeight) {
		this(robot,weapon,targetLoc,targetHeight,weapon.attackPower);
	}

	/**
	 * Returns the ID of the robot that just broadcast.
	 *
	 * @return the messaging robot's ID
	 */
	public int getRobotID() {
		return robotID;
	}

	public ComponentType getWeaponType() {
		return weaponType;
	}
	
	/**
	 * Returns the location that the robot attacked
	 *
	 * @return the location that the robot attacked
	 */
	public MapLocation getTargetLoc() {
		return targetLoc;
	}
	
	/**
	 * Returns the height of the position that the robot attacked
	 *
	 * @return the height of the position that the robot attacked
	 */
	public RobotLevel getTargetHeight() {
		return targetHeight;
	}
}
