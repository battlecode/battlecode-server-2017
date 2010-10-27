package battlecode.common;

import static battlecode.common.ComponentClass.*;

public enum ComponentType
{
	//           WT CO DL RNG ANG POW
	SHIELD      ( 1, 0, 0,  0,  0,  0, ARMOR,ComponentController.class),
	HARDENED    ( 5, 0, 0,  0,  0,  0, ARMOR,ComponentController.class),
	REGEN       ( 4, 0, 0,  0,  0,  0, ARMOR,ComponentController.class),
	PLASMA      ( 3, 0, 2,  0,  0,  0, ARMOR,ComponentController.class),
	IRON        ( 3, 0,10,  0,  0,  0, ARMOR,SpecialController.class),
	PLATING     ( 1, 0, 0,  0,  0,  0, ARMOR,ComponentController.class),
	SATELLITE   ( 7, 0, 0,100,360,  0,SENSOR,SensorController.class),
	SMALL_MOTOR ( 0, 0, 3,  2,360,  0, MOTOR,MovementController.class),
	MEDIUM_MOTOR( 0, 0, 7,  2,360,  0, MOTOR,MovementController.class),
	LARGE_MOTOR ( 0, 0,15,  2,360,  0, MOTOR,MovementController.class),
	FLYING_MOTOR( 0, 0, 4,  2,360,  0, MOTOR,MovementController.class),
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

	public final Class<? extends ComponentController> controller;

	ComponentType(int weight, int cost, int delay, int range, double angle,
		          double attackPower, ComponentClass componentClass,
				  Class<? extends ComponentController> controller) {
		this.weight = weight;
		this.cost = cost;
		this.delay = delay;
		this.range = range;
		this.angle = angle;
		this.cosHalfAngle = Math.cos(Math.toRadians(angle)/2.);
		this.attackPower = attackPower;
		this.componentClass = componentClass;
		this.controller = controller;
	}
}
