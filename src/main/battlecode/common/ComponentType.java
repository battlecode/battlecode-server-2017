package battlecode.common;

import static battlecode.common.ComponentClass.*;

public enum ComponentType
{
	//           WGT COS DEL RNG ANG POW
	SHIELD      (  1,  0,  0,  0,  0,  0,  ARMOR,ComponentController.class),
	HARDENED    (  5,  0,  0,  0,  0,  0,  ARMOR,ComponentController.class),
	REGEN       (  4,  0,  0,  0,  0,  0,  ARMOR,ComponentController.class),
	PLASMA      (  3,  0,  2,  0,  0,  0,  ARMOR,ComponentController.class),
	IRON        (  3,  0, 10,  0,  0,  0,  ARMOR,SpecialController.class),
	PLATING     (  1,  0,  0,  0,  0,  0,  ARMOR,ComponentController.class),
	SMG         (  1,  0,  2, 36, 90, .2, WEAPON,WeaponController.class),
	BLASTER     (  2,  0,  4, 16, 90,  3, WEAPON,WeaponController.class),
	CANNON      (  6,  0,  5,  9, 90,2.5, WEAPON,WeaponController.class),
	RAILGUN     (  6,  0,  4, 16, 90,6.5, WEAPON,WeaponController.class),
	HAMMER      (  2,  0,  1,  4, 90, .4, WEAPON,WeaponController.class),
	GLUEGUN     (  3,  0,  6, 16, 90,  0, WEAPON,WeaponController.class),
	MEDIC       (  4,  0,  4,  9,360,-.2, WEAPON,WeaponController.class),
	SATELLITE   (  7,  0,  0,100,360,  0, SENSOR,SensorController.class),
	TELESCOPE   (  4,  0,  0,144, 45,  0, SENSOR,SensorController.class),
	SIGHT       (  1,  0,  0,  9, 90,  0, SENSOR,SensorController.class),
	RADAR       (  2,  0,  0, 25,180,  0, SENSOR,SensorController.class),
	ANTENNA     (  2,  0,  0, 16,360,  0,   COMM,BroadcastController.class),
	DISH        (  4,  0,  0, 49,360,  0,   COMM,BroadcastController.class),
	NETWORK     (  7,  0,  0,144,360,  0,   COMM,BroadcastController.class),
	PROCESSOR   (  1,  0,  0,  0,  0,  0,   MISC,ComponentController.class),
	JUMP        (  3,  0, 25, 16,360,  0,   MISC,SpecialController.class),
	DUMMY       (  3,  0,  5, 25,360,  0,   MISC,SpecialController.class),
	BUG         (  2,  0,125, 25,180,  0, SENSOR,SensorController.class),
	DROPSHIP    (  4,  0,  8,  2,360,  0,   MISC,SpecialController.class),
	RECYCLER    ( 15,  0,  1,  0,  0,  0,BUILDER,BuilderController.class),
	FACTORY     ( 15,  0,  1,  0,  0,  0,BUILDER,BuilderController.class),
	CONSTRUCTOR (  6,  0,  1,  0,  0,  0,BUILDER,BuilderController.class),
	ARMORY      ( 15,  0,  1,  0,  0,  0,BUILDER,BuilderController.class),
	SMALL_MOTOR (  0,  0,  3,  2,360,  0,  MOTOR,MovementController.class),
	MEDIUM_MOTOR(  0,  0,  7,  2,360,  0,  MOTOR,MovementController.class),
	LARGE_MOTOR (  0,  0, 15,  2,360,  0,  MOTOR,MovementController.class),
	FLYING_MOTOR(  0,  0,  4,  2,360,  0,  MOTOR,MovementController.class),
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
