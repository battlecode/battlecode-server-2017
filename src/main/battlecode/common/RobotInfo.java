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
    public final double energon;
    /**
     * The type of this Robot.
     */
    public final RobotType type;
    /**
     * The team of this Robot.
     */
    public final Team team;
    
    public final int roundsUntilAttackIdle;
    public final int roundsUntilMovementIdle;
    public final int roundsUntilWeakened;

    public RobotInfo(Robot robot, MapLocation location,
                     double hitpoints, RobotType type, Team team, 
                     int roundsUntilAttackIdle, int roundsUntilMovementIdle) {
        super();
        this.robot = robot;
        this.location = location;
        this.energon = hitpoints;
        this.type = type;
        this.team = team;
        this.roundsUntilAttackIdle = roundsUntilAttackIdle;
        this.roundsUntilMovementIdle = roundsUntilMovementIdle;
    }

    public int hashCode() {
        return robot.hashCode();
    }
}
