package battlecode.common;

public interface BuilderController extends ComponentController {

    /**
     * Queues an equip action to be beformed at the end of this robot's
     * turn.  When the action is executed, the robot at
     * location <code>loc</code> and height <code>level</code>
     * will be equipped with a component of type <code>type</code>.
     * The new component will not be able to perform any actions for
     * <code>GameConstants.WAKE_DELAY</code> rounds.
     *
     * @cooldown
     *
     * @throws IllegalArgumentException if this component cannot build a component of type <code>type</code>
     * @throws GameActionException if this component is already engaged in an action (ALREADY_ACTIVE)
     * @throws GameActionException if <code>loc</code> is not adjacent to this robot's location (OUT_OF_RANGE)
     * @throws GameActionException if there is no robot at the given location and height (NO_ROBOT_THERE)
     * @throws GameActionException if the robot at the given location and height cannot be equipped with the given type of component (WRONG_ROBOT_TYPE)
     * @throws GameActionException if there is not enough room in the robot's chassis for the component (NO_ROOM_IN_CHASSIS)
     * @throws GameActionException if there not enough resources to build a component of type <code>type</code> (NOT_ENOUGH_RESOURCES)
     */
    public void build(ComponentType type, MapLocation loc, RobotLevel level) throws GameActionException;

    /**
     * Queues a spawn action to be performed at the end of this robot's turn.
     * When the action is executed, a new robot will be created at
     * {@code loc}.  The square must not already be occupied.
     * The new robot is created and starts executing bytecodes immediately, but
     * it will not be able to perform any actions for <code>GameConstants.WAKE_DELAY</code>
     * rounds.
     *
     * @cooldown
     *
     * @throws IllegalArgumentException if this component cannot build a chassis of type <code>type</code>
     * @throws GameActionException if this component is already engaged in an action (ALREADY_ACTIVE)
     * @throws GameActionException if <code>loc</code> is not adjacent to this robot's location (OUT_OF_RANGE)
     * @throws GameActionException if <code>loc</code> is already occupied (CANT_MOVE_THERE)
     * @throws GameActionException if there not enough resources to build a chassis of type <code>type</code> (NOT_ENOUGH_RESOURCES)
     *
     * @param type
     *            the type of chassis to spawn; cannot be null.
     */
    public void build(Chassis type, MapLocation loc) throws GameActionException;

    public boolean canBuild(Direction dir, RobotLevel level);
}
