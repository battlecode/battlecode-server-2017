package battlecode.common;

import static battlecode.common.RobotLevel.*;
import static battlecode.common.ComponentType.*;

public enum Chassis {
	//       WGT  HP UPK COS
	LIGHT   (  6, 12, .5,  0,ON_GROUND,   SMALL_MOTOR),
	MEDIUM  (  9, 20, .8,  0,ON_GROUND,  MEDIUM_MOTOR),
	HEAVY   ( 14, 40,1.0,  0,ON_GROUND,   LARGE_MOTOR),
	FLYING  (  4,  5, .9,  0,   IN_AIR,  FLYING_MOTOR),
	BUILDING( 18, 30, .4,  0,ON_GROUND,BUILDING_MOTOR),
	;

	/** The weight that this chassis can support. */
	public final int weight;

	/** The maximum hit points of this chassis. */
	public final double maxHp;

	/** The upkeep cost of this chassis. */
	public final double upkeep;

	/** The cost to build this chassis. */
	public final int cost;

	/** Determines whether or not this chassis is flying. */
	public final RobotLevel level;

	/** The type of motor this chassis uses. */
	public final ComponentType motor;

	@Deprecated
	public boolean isAirborne() { return level == RobotLevel.IN_AIR; }

	Chassis(int weight, double maxHp, double upkeep,
	        int cost, RobotLevel level, ComponentType motor) {
			this.weight = weight;
			this.maxHp = maxHp;
			this.upkeep = upkeep;
			this.cost = cost;
			this.level = level;
			this.motor = motor;
	}
}
