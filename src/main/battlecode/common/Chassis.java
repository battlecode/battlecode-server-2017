package battlecode.common;

public enum Chassis {
	;

	/** The weight that this chassis can support. */
	public final int weight;

	/** The movement delay of this chassis. */
	public final int moveDelay;

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

	Chassis(int weight, int moveDelay, double maxHp, double upkeep,
	        int cost, RobotLevel level, ComponentType motor) {
			this.weight = weight;
			this.moveDelay = moveDelay;
			this.maxHp = maxHp;
			this.upkeep = upkeep;
			this.cost = cost;
			this.level = level;
			this.motor = motor;
	}
}
