package battlecode.common;

import static battlecode.common.ComponentClass.*;

public enum ComponentType
{
	//           WT CO DL RNG ANG POW
	SHIELD      ( 1, 0, 0,  0,  0,  0,ARMOR),
	HARDENED    ( 5, 0, 0,  0,  0,  0,ARMOR),
	REGEN       ( 4, 0, 0,  0,  0,  0,ARMOR),
	PLASMA      ( 3, 0, 2,  0,  0,  0,ARMOR),
	IRON        ( 3, 0,10,  0,  0,  0,ARMOR),
	PLATING     ( 1, 0, 0,  0,  0,  0,ARMOR),
	SATELLITE   ( 7, 0, 0,144,360,  0,SENSOR),
	SMALL_MOTOR ( 0, 0, 3,  2,360,  0,MOTOR),
	MEDIUM_MOTOR( 0, 0, 7,  2,360,  0,MOTOR),
	LARGE_MOTOR ( 0, 0,15,  2,360,  0,MOTOR),
	FLYING_MOTOR( 0, 0, 4,  2,360,  0,MOTOR),
	;

	/**
	 * The weight of the component.
	 */
	public final int weight;
	
	/**
	 * The cost to build the component.
	 */
	public final int cost;
	
	/**
	 * When the component is used, it takes this many
	 * turns before the component can be used again.
	 */
	public final int delay;

	/**
	 * The maximum distance at which this component's ability may
	 * be used.
	 */
	public final int range;

	/**
	 * The maximum angle at which this component's ability may
	 * be used.
	 */
	public final double angle;

	public final double cosHalfAngle;

	/**
	 * This component's attack power, if it is a weapon.
	 */
	public final double attackPower;

	/**
	 * The kind of component (armor, weapon, etc.)
	 */
	public final ComponentClass componentClass;
	
	ComponentType(int weight, int cost, int delay, int range, double angle,
		          double attackPower, ComponentClass componentClass) {
		this.weight = weight;
		this.cost = cost;
		this.delay = delay;
		this.range = range;
		this.angle = angle;
		this.cosHalfAngle = Math.cos(Math.toRadians(angle)/2.);
		this.attackPower = attackPower;
		this.componentClass = componentClass;
	}
}
