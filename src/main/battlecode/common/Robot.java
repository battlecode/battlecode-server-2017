package battlecode.common;

/**
 * A Robot instance represents a robot in the game world.
 * <p>
 * Use <code>equals</code> to determine if two Robot instances represent the same robot.
 *
 * @author Teh Devs
 * @see battlecode.common.RobotController
 */
public interface Robot extends GameObject {
    /**
     * Gives the Team (A, B, or neutral) to which this GameObject belongs
     * @return this GameObject's Team
     * @see battlecode.common.Team
     */
    public Team getTeam();

}
