package battlecode.common;

import static battlecode.common.RobotLevel.*;

public enum RobotType {
	//          LEVEL,    MXE MXF COS UPK MD MVC SR SANG AR AR  AA AD AP,  AIR GROUND
	ARCHON(     ON_GROUND,100,100, 0,-1  , 7, .1,36,360., 0, 0,  0, 5, 0,false,false),
	SOLDIER(    ON_GROUND, 40, 40,40,.15, 6, .3,10, 90., 0, 4, 90, 5, 6,true ,true );

	/** The robot's level. */
	public final RobotLevel level;

	/** The maximum amount of energon the robot can have. */
	public final double maxEnergon;

	/** The maximum amount of flux the robot can have. */
	public final double maxFlux;

	/** The amount of flux needed to spawn the robot. */
	public final double spawnCost;

	/** The amount of flux needed to keep the robot running for one turn. */
	public final double upkeep;

	/** The number of turns it takes the robot to move orthogonally. */
	public final int moveDelayOrthogonal;

	/**
	 * The number of turns it takes the robot to move diagonally.
	 * Equal to <code>(int)Math.round(moveDelayOrthogonal*Math.sqrt(2.))</code>.
	 */
	public final int moveDelayDiagonal;

	/** The cost in flux to move the robot one square. */
	public final double moveCost;

	/** The square of the maximum distance that the robot can sense. */
	public final int sensorRadiusSquared;

	/** The range of angles that the robot can sense. */ 
	public final double sensorAngle;

	/** The square of the maximum distance that the robot can attack. */
	public final int attackRadiusMaxSquared;
	
	/** The square of the minimum distance that the robot can attack. */
	public final int attackRadiusMinSquared;

	/** The range of angles that this robot can attack. */
	public final double attackAngle;

	/** The number of turns that it takes this robot to attack. */
	public final int attackDelay;

	/** The amount of damage that this robot does when it attacks. */
	public final double attackPower;

	/** Whether or not the robot can attack air units. */
	public final boolean canAttackAir;
	
	/** Whether or not the robot can attack ground units. */
	public final boolean canAttackGround;

	public final double sensorCosHalfTheta;
	public final double attackCosHalfTheta;

	/** Returns true if the robot can attack robots at the given level. */
	public boolean canAttack(RobotLevel level) {
		switch(level) {
		case ON_GROUND:
			return canAttackGround;
		case IN_AIR:
			return canAttackAir;
		default:
			return false;
		}
	}

	RobotType(RobotLevel level,
		double maxEnergon,
		double maxFlux,
		double spawnCost,
		double upkeep,
		int moveDelayOrthogonal,
		double moveCost,
		int sensorRadiusSquared,
		double sensorAngle,
		int attackRadiusMaxSquared,
		int attackRadiusMinSquared,
		double attackAngle,
		int attackDelay,
		double attackPower,
		boolean canAttackAir,
		boolean canAttackGround)
	{
		this.level=level;
		this.maxEnergon=maxEnergon;
		this.maxFlux=maxFlux;
		this.spawnCost=spawnCost;
		this.upkeep=upkeep;
		this.moveDelayOrthogonal=moveDelayOrthogonal;
		this.moveDelayDiagonal=(int)Math.round(moveDelayOrthogonal*Math.sqrt(2.));
		this.moveCost=moveCost;
		this.sensorRadiusSquared=sensorRadiusSquared;
		this.sensorAngle=sensorAngle;
		this.attackRadiusMaxSquared=attackRadiusMaxSquared;
		this.attackRadiusMinSquared=attackRadiusMinSquared;
		this.attackAngle=attackAngle;
		this.attackDelay=attackDelay;
		this.attackPower=attackPower;
		this.canAttackAir=canAttackAir;
		this.canAttackGround=canAttackGround;
		this.sensorCosHalfTheta=Math.cos(sensorAngle*Math.PI/360.);
		this.attackCosHalfTheta=Math.cos(attackAngle*Math.PI/360.);
	}
		
}

