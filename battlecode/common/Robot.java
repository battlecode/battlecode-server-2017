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
     * Gives the RobotLevel of this GameObject
     * @return this GameObject's RobotLevel
     * @see battlecode.common.RobotLevel
     */
    public RobotLevel getRobotLevel();
}
