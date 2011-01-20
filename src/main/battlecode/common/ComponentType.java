package battlecode.common;

import static battlecode.common.ComponentClass.*;

public enum ComponentType
{
	//              WGT COS DEL RNG ANG POW
	SHIELD         (  1, 10,  0,  0,  0,  0,  ARMOR,ComponentController.class),
	HARDENED       (  3, 23,  0,  0,  0,  0,  ARMOR,ComponentController.class),
	REGEN          (  4, 10,  0,  0,  0,  0,  ARMOR,ComponentController.class),
	PLASMA         (  2, 16,  2,  0,  0,  0,  ARMOR,ComponentController.class),
	IRON           (  2, 20, 10,  0,  0,  0,  ARMOR,IronController.class),
	PLATING        (  1,  8,  0,  0,  0,  0,  ARMOR,ComponentController.class),
	SMG            (  1,  7,  2, 36, 90, .6, WEAPON,WeaponController.class),
	BLASTER        (  2, 18,  4, 16, 90,  3, WEAPON,WeaponController.class),
	RAILGUN        (  5, 25,  5, 25, 90,8.0, WEAPON,WeaponController.class),
	HAMMER         (  2, 16,  1,  4, 90,2.5, WEAPON,WeaponController.class),
	BEAM           (  4, 17,  2, 36, 90,  0, WEAPON,WeaponController.class),
	MEDIC          (  4, 13,  1, 16,360,-.4, WEAPON,WeaponController.class),
	SATELLITE      (  6, 25,  0,100,360,  0, SENSOR,SensorController.class),
	TELESCOPE      (  4, 11,  0,144, 45,  0, SENSOR,SensorController.class),
	SIGHT          (  1,  5,  0,  9, 90,  0, SENSOR,SensorController.class),
	RADAR          (  3, 15,  0, 36,180,  0, SENSOR,SensorController.class),
	ANTENNA        (  1,  5,  0, 36,360,  0,   COMM,BroadcastController.class),
	DISH           (  2, 14,  0, 64,360,  0,   COMM,BroadcastController.class),
	NETWORK        (  4, 25,  0,144,360,  0,   COMM,BroadcastController.class),
	PROCESSOR      (  1,  3,  0,  0,  0,  0,   MISC,ComponentController.class),
	JUMP           (  3,  8, 25, 16,360,  0,   MISC,JumpController.class),
	DUMMY          (  3, 10, 13, 25,360,  0,   MISC,BuilderController.class),
	BUG            (  2, 12,125, 25,180,  0,   MISC,BugController.class),
	DROPSHIP       (  4, 16,  3,  2,360,  0,   MISC,DropshipController.class),
	RECYCLER       ( 15, 70,  1,  2,360,  0,BUILDER,BuilderController.class),
	FACTORY        ( 15, 70,  1,  2,360,  0,BUILDER,BuilderController.class),
	CONSTRUCTOR    (  4, 40,  1,  2,360,  0,BUILDER,BuilderController.class),
	ARMORY         ( 15, 70,  1,  2,360,  0,BUILDER,BuilderController.class),
	SMALL_MOTOR    (  0,  0,  4,  2,360,  0,  MOTOR,MovementController.class),
	MEDIUM_MOTOR   (  0,  0,  7,  2,360,  0,  MOTOR,MovementController.class),
	LARGE_MOTOR    (  0,  0, 10,  2,360,  0,  MOTOR,MovementController.class),
	FLYING_MOTOR   (  0,  0,  5,  2,360,  0,  MOTOR,MovementController.class),
	BUILDING_MOTOR (  0,  0,  1,  2,360,  0,  MOTOR,MovementController.class),
	BUILDING_SENSOR(  0,  0,  0,  2,360,  0, SENSOR,SensorController.class),
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
	 * The square of the maximum distance at which this component's
	 * ability may be used.
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

	/**
	 * The interface that is used to control this component.
	 */
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
