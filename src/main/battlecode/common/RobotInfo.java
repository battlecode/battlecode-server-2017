package battlecode.common;

/**
 * Struct that stores basic information that was 'sensed' of another Robot. This
 * info is ephemeral and there is no guarantee any of it will remain the same
 * between rounds.
 * 
 * @author Teh Devs
 */
public class RobotInfo {
	
	/** The robot that was sensed. */
    public final Robot robot;
	/** Location of this Robot. */
    public final MapLocation location;
    /** Hit points of this Robot. */
    public final double hitpoints;
    /** Maximum hit points of this Robot. */
    public final double maxHp;
    /** The direction this Robot is facing. */
    public final Direction direction;
	/**
	 * The components of the robot that was sensed, if the sensor is capable
	 * of sensing them.  Components can be sensed if: <ul>
	 * <li>the sensor is a satellite sensor, or</li>
	 * <li>the two robots are adjacent, and the robot being sensed is off.</li></ul>
	 * Otherwise, {@code components} will be {@code null}.
	 */
	public final Component [] components;

    public RobotInfo(Robot robot, MapLocation location,
	                 double hitpoints, double maxHp, Direction direction,
					 Component [] components) {
        super();
        this.robot = robot;
		this.location = location;
        this.hitpoints = hitpoints;
		this.maxHp = maxHp;
        this.direction = direction;
		this.components = components;
    }

	public int hashCode() {
		return robot.hashCode();
	}
}
