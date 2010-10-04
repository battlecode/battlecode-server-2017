package battlecode.common;

/**
 * An ActionType represents a robot action that may occupy a robot for multiple rounds. A robot always
 * has a current action represented by exactly one ActionType. ActionTypes may have an attack cooldown,
 * a movement cooldown, both or neither.
 *
 * @author Teh Devs
 */
public enum ActionType {

    /** Indicates an inactive robot, that can initiate a new action. */
    IDLE(false, false),
    /** Indicates a robot that is moving to a different MapLocation.  The duration of this action is given
     * by <code>RobotType.moveDelayOrthogonal()</code> or <code>RobotType.moveDelayDiagonal()</code>.*/
    MOVING(false, true),
    /** Indicates a robot that is attacking.  The duration of this action is given
     * by <code>RobotType.attackDelay()</code>.*/
    ATTACKING(true, false),
    /** Indicates a 'parent' robot that is spawning a new robot.  The duration of this action is given
     * by <code>rt.spawnDelay()</code>, where <code>rt</code> is the <code>RobotType</code> of the child robot.*/
    SPAWNING(false, false),
    /** Indicates a robot that is transforming into a different <code>RobotType</code>.  The duration of this action is given
     * by <code>rt.spawnDelay()</code>, where <code>rt</code> is the <code>RobotType</code> of the new (transformed) robot.*/
    TRANSFORMING(true, true),
    /** Indicates a 'child' robot that is being spawned.  The duration of this action is given
     * by <code>rt.wakeDelay()</code>, where <code>rt</code> is the <code>RobotType</code> of the child robot.*/
    WAKING(true, true),
    /** Indicates a robot that is changing the direction it's facing.  The duration of this action is 1 round.*/
    SETTING_DIRECTION(false, true),
    /** Indicates a robot that is loading a block into its cargo.  The duration of this action is given
     * 	by <code>rt.loadDelay()</code>, where <code>rt</code> is the <code>RobotType</code> of the child robot.*/
    LOADING_BLOCK(false, true),
    /** Indicates a robot that is unloading a block from its cargo.  The duration of this action is given
     * 	by <code>rt.loadDelay()</code>, where <code>rt</code> is the <code>RobotType</code> of the child robot.*/
    UNLOADING_BLOCK(false, true),
    /** Indicates a robot that is draining energon from surrounding enemies.  The duration of this action is given by
     * <code>RobotType.attackDelay()</code>*/
    DRAINING(true, false),
    /** Indicates a robot that is burn flux in exchange for energon production. The duration of this action is 1 round,
     * but the effect lasts for GameConstants.FLUX_BURN_DURATION rounds.*/
    BURNING(false, false),
    /**
     * Indicates whether a turret is deploying
     */
    DEPLOYING(true, true),
    /**
     * Indicates whether a turret is undeploying
     */
    UNDEPLOYING(true, true),
    /**
     * Indicates that a robot is in the process of teleporting.
     */
    TELEPORTING(false, true);

    private ActionType(boolean hasAttackCooldown, boolean hasMovementCooldown) {
        this.hasAttackCooldown = hasAttackCooldown;
        this.hasMovementCooldown = hasMovementCooldown;
    }
    private boolean hasAttackCooldown, hasMovementCooldown;

    public boolean hasAttackCooldown() {
        return hasAttackCooldown;
    }

    public boolean hasMovementCooldown() {
        return hasMovementCooldown;
    }
}
