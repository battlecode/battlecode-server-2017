package battlecode.common;

/**
 * Struct that stores basic information that was 'sensed' of another Robot. This
 * info is ephemeral and there is no guarantee any of it will remain the same
 * between rounds.
 *
 * @author Teh Devs
 */
public class RobotInfo {

    /**
     * The robot that was sensed.
     */
    public final Robot robot;
    /**
     * The location of this Robot.
     */
    public final MapLocation location;
    /**
     * The energon of this Robot.
     */
    public final double health;
    /**
     * The direction this Robot is facing.
     */
    public final Direction direction;
    /**
     * The type of this Robot.
     */
    public final RobotType type;
    /**
     * The team of this Robot.
     */
    public final Team team;
    /**
     * Action delay of the robot.
     */
    public final double actionDelay;
    /**
     * Whether the robot is constructing anything.
     */
    public final boolean isConstructing;
    /**
     * The type of building being constructed.
     */
    public final RobotType constructingType;
    /**
     * The number of constructing rounds remaining.
     */
    public final int constructingRounds;

    public RobotInfo(Robot robot, MapLocation location,
                     double hitpoints, Direction direction,
                     RobotType type, Team team, double actionDelay,
                     boolean isConstructing, RobotType constructingType, int constructingRounds) {
        super();
        this.robot = robot;
        this.location = location;
        this.health = hitpoints;
        this.direction = direction;
        this.type = type;
        this.team = team;
        this.actionDelay = actionDelay;

        this.isConstructing = isConstructing;
        this.constructingType = constructingType;
        this.constructingRounds = constructingRounds;
    }

    public int hashCode() {
        return robot.hashCode();
    }
}
