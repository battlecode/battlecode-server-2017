package battlecode.common;

/**
 * A RobotController allows contestants to make their robot sense and interact
 * with the game world. When a contestant's <code>RobotPlayer</code> is
 * constructed, it is passed an instance of <code>RobotController</code> that
 * controls the newly created robot.
 * 
 * @author Teh Devs
 */
public interface RobotController {

    // *********************************
    // ****** QUERY METHODS ********
    // *********************************
    /**
     * Tells whether this robot can attack air units.
     *
     * @return true if this robot can attack air units, false otherwise
     */
    public boolean canAttackAir();

    /**
     * Tells whether this robot can attack ground units.
     *
     * @return true if this robot can attack ground units, false otherwise
     */
    public boolean canAttackGround();

    /**
     * Tells whether the given square is within range of this robot's attack.
     * Takes into account only the range of the attacking robot, and not whether
     * the robot is currently active or otherwise incapable of attacking.
     * <code>targetLoc</code> cannot be null.
     *
     * @return true if <code>targetLoc</code> is within attack range of this
     *         robot
     */
    public boolean canAttackSquare(MapLocation targetLoc);

    /**
     * Tells whether this robot can move in the given direction. Takes into
     * account only the map terrain and positions of other robots. Does not take
     * into account whether this robot is currently active or otherwise
     * incapable of moving. <code>dir</code> cannot be null.
     *
     * @return true if there are no robots or walls preventing this robot from
     *         moving in the given direction; false otherwise
     */
    public boolean canMove(Direction dir);

    /**
     * Tells whether this robot can currently sense the given GameObject (i.e.,
     * whether the given GameObject is in this robot's sensor range).
     *
     * @return true if this robot can currently sense the given GameObject;
     *         false otherwise
     * @throws GameActionException
     *             under any of the following circumstances. If more than one of
     *             the following applies, the
     *             <code>GameActionExceptionType</code> will be the first
     *             applicable one in the list.
     *             <li><code>obj</code> is not a valid
     *             <code>GameObject</code>
     *             (GameActionExceptionType.INVALID_OBJECT)</li>
     */
    public boolean canSenseObject(GameObject obj) throws GameActionException;

    /**
     * Tells whether this robot can currently sense the given MapLocation (i.e.,
     * whether the given MapLocation is in this robot's sensor range).
     * <code>loc</code> cannot be null.
     *
     * @return true if this robot can currently sense the given MapLocation;
     *         false otherwise
     */
    public boolean canSenseSquare(MapLocation loc);

    /**
     * Gets the direction this robot is currently facing.
     *
     * @return this robot's current Direction
     */
    public Direction getDirection();

    /**
     * Gets the current energon level of this robot.
     *
     * @return this robot's current energon level
     */
    public double getEnergonLevel();

    /**
     * Gets the current energon reserve level of this robot.
     *
     * @return this robot's current energon reserve level
     */
    public double getEnergonReserve();

    /**
     * Gets the eventual energon level (i.e., taking into account pending
     * transfers) of this robot.
     *
     * @return Math.min(energonLevel + energonReserve, maxEnergon)
     */
    public double getEventualEnergonLevel();

    /**
     * Gets the maximum energon level of this robot.
     *
     * @return this robot's maximum energon level
     */
    public double getMaxEnergonLevel();

    /**
     * Gets the energon production per round of this robot. If this robot is not
     * an Archon, this method returns 0.
     *
     * @return this robot's energon production
     */
    public double getEnergonProduction();

    /**
     * Gets the current location of this robot.
     *
     * @return this robot's current location
     */
    public MapLocation getLocation();

    /**
     * Retrieve the next message waiting in your incoming message queue. Also
     * removes the message from the queue.
     *
     * @return next Message object in your queue, or null if your queue is
     *         empty.
     */
    public Message getNextMessage();

    /**
     * Retrieves an array of all the messages in your incoming message queue.
     * All messages will be removed from the queue. If there are no messages in
     * the queue, this method returns a zero-length array.
     *
     * @return all the Messages in your message queue
     */
    public Message[] getAllMessages();

    /**
     * Gets the number of rounds before this robot's current attack ends, and
     * the robot can accept another attack action. Returns 0 iff
     * <code>this.isAttackActive() == false</code>.
     *
     * @return the number of rounds until this robot is attack idle
     */
    public int getRoundsUntilAttackIdle();

    /**
     * Gets the number of rounds before this robot's current movement ends, and
     * the robot can accept another movement action. Returns 0 iff
     * <code>this.isMovementActive() == false</code>.
     *
     * @return the number of rounds until this robot is movement idle
     */
    public int getRoundsUntilMovementIdle();

    /**
     * Gets the Team of this robot. Equivalent to
     * <code>this.getRobot().getTeam()</code>.
     *
     * @return this robot's Team
     * @see battlecode.common.Team
     */
    public Team getTeam();

    /**
     * Gets the amount of flux the calling robot currently has stored
     *
     * @return the amount of flux the calling robot currently has stored
     */
    public double getFlux();

    /**
     * Tells whether this robot is currently engaged in an attacking action.
     *
     * @return whether this robot is currently using its attack queue
     */
    public boolean isAttackActive();

    /**
     * Tells whether this robot is currently engaged in an moving action.
     *
     * @return whether this robot is currently using its movement queue
     */
    public boolean isMovementActive();

    /**
     * Use this method to access your robot.
     *
     * @return the Robot associated with this RobotController
     */
    public Robot getRobot();

    /**
     * Gets this robot's type.
     *
     * @return this robot's type.
     */
    public RobotType getRobotType();

    /**
     * Gets the minimum number of points needed to win a game on this map
     */
    public int getMapMinPoints();

    // ***********************************
    // ****** ACTION METHODS ********
    // ***********************************
    /**
     * Transfers the specified amount of energon to the robot at location
     * <code>loc</code> and RobotLevel <code>height</code>. The robot
     * receiving the transfer must be adjacent to or in the same location as the
     * robot giving the transfer. Robots may not transfer more energon than they
     * currently have. If the transfer would put the receiving robot's
     * energon reserve above ENERGON_RESERVE_SIZE, then its new energon reserve
     * level will be equal to ENERGON_RESERVE_SIZE, and the excess energon will
     * be lost.
     *
     * @param amount
     *            the amount of energon to transfer to the specified robot
     * @param loc
     *            the <code>MapLocation</code> of the robot to transfer to
     * @param level
     *            the <code>RobotLevel</code> of the robot to transfer to
     * @throws GameActionException
     *             under any of the following circumstances. If more than one of
     *             the following applies, the
     *             <code>GameActionExceptionType</code> will be the first
     *             applicable one in the list.
     *             <li><code>amount</code> is NaN, NEGATIVE_INFINITY, or
     *             POSITIVE_INFINITY
     *             (GameActionExceptionType.NOT_ENOUGH_ENERGON)</li>
     *             <li>The specified square is not adjacent to or the same as
     *             this robot's location
     *             (GameActionExceptionType.CANT_TRANSFER_ENERGON_THERE)</li>
     *             <li>There is no robot at the specified location and height
     *             (GameActionExceptionType.CANT_TRANSFER_ENERGON_THERE)</li>
     *             <li>This robot does not have <code>amount</code> energon,
     *             or <code>amount</code> is negative
     *             (GameActionExceptionType.NOT_ENOUGH_ENERGON)</li>
     *
     */
    public void transferUnitEnergon(double amount, MapLocation loc,
            RobotLevel level) throws GameActionException;

    /**
     * Transfers the specified amount of flux to the robot at location
     * <code>loc</code> and RobotLevel <code>height</code>. The robot
     * receiving the transfer must be adjacent to or in the same location as the
     * robot giving the transfer. Robots may not transfer more flux than they
     * currently have.
     *
     * @param amount
     *            the amount of flux to transfer to the specified robot
     * @param loc
     *            the <code>MapLocation</code> of the robot to transfer to
     * @param level
     *            the <code>RobotLevel</code> of the robot to transfer to
     * @throws GameActionException
     *             under any of the following circumstances. If more than one of
     *             the following applies, the
     *             <code>GameActionExceptionType</code> will be the first
     *             applicable one in the list.
     *             <li>The specified square is not adjacent to or the same as
     *             this robot's location
     *             (GameActionExceptionType.CANT_TRANSFER_FLUX_THERE)</li>
     *             <li>There is no robot at the specified location and height
     *             (GameActionExceptionType.CANT_TRANSFER_FLUX_THERE)</li>
     *             <li>This robot does not have <code>amount</code> flux,
     *             or <code>amount</code> is negative
     *             (GameActionExceptionType.NOT_ENOUGH_FLUX)</li>
     *
     */
    public void transferFlux(double  amount, MapLocation loc,
            RobotLevel level) throws GameActionException;

    /**
     * Removes the action in the action queue. If an action is currently in
     * progress (e.g., the robot is currently moving), that action will not be
     * stopped.
     */
    public void clearAction();

    /**
     * Returns whether this robot has an action already queued in this round.
     *
     * @return true if and only if this robot has an action queued in the
     *         current round already.
     */
    public boolean hasActionSet();

    /**
     * Queues an air attack on the specified location to be performed at the end of this robot's turn.
     * If there is a Robot in the air in the given location when this action is
     * executed, it will feel some damage.
     * <p>
     * When the action is queued a GameActionException may result. The possible
     * <code>GameActionExceptionType</code>s are as follows. If more than one
     * of the following applies, the <code>GameActionExceptionType</code> will
     * be the first applicable one in the list.
     * <li>This robot is not within attack range of <code>loc</code>
     * (GameActionExceptionType.OUT_OF_ATTACK_RANGE)</li>
     * <li>This robot cannot attack ground
     * (GameActionExceptionType.OUT_OF_ATTACK_RANGE)</li>
     * <li>Attack has not sufficiently cooled down yet
     * (GameActionExceptionType.INSUFFICIENT_ATTACK_COOLDOWN)</li>
     * <li>An action has already been queued in this round
     * (GameActionExceptionType.DOUBLE_ACTION_ERROR)</li>
     *
     * @param targetLoc
     *            the location of the square you are attacking; cannot be null
     */
    public void attackAir(MapLocation targetLoc) throws GameActionException;

    /**
     * Queues a ground attack on the specified location to be performed at the
     * end of this robot's turn. If there is a Robot on the ground in the given
     * location when this action is executed, it will feel some damage.
     * <p>
     * When the action is queued a GameActionException may result. The possible
     * <code>GameActionExceptionType</code>s are as follows. If more than one
     * of the following applies, the <code>GameActionExceptionType</code> will
     * be the first applicable one in the list.
     * <li>This robot is not within attack range of <code>loc</code>
     * (GameActionExceptionType.OUT_OF_ATTACK_RANGE)</li>
     * <li>This robot cannot attack ground
     * (GameActionExceptionType.OUT_OF_ATTACK_RANGE)</li>
     * <li>Attack has not sufficiently cooled down yet
     * (GameActionExceptionType.INSUFFICIENT_ATTACK_COOLDOWN)</li>
     * <li>An action has already been queued in this round
     * (GameActionExceptionType.DOUBLE_ACTION_ERROR)</li>
     *
     * @param targetLoc
     *            the location of the square you are attacking; cannot be null
     */
    public void attackGround(MapLocation targetLoc)
            throws GameActionException;

    /**
     * Adds a message for your robot to broadcast. At the end of your
     * robot's execution block, if a broadcast has been set, the message is removed
     * and immediately added to the incoming message queues of all robots in
     * your broadcast range (except for the sending robot). Note that robots
     * are thus limited to sending at most one message per round.
     * </p>
     * <p>
     * You are charged a small amount of energon for every message that you
     * broadcast. The cost of sending a message is equal to
     * <code>(GameConstants.BROADCAST_FIXED_COST +
     * GameConstants.BROADCAST_COST_PER_BYTE*sizeBytes)</code>
     * where <code>sizeBytes</code> is the size of the message, in bytes.
     * <p>
     * <p>
     * Each robot can only have one message to broadcast in a given round at a time.
     * Attempts to queue two messages on a robot in the same round will cause
     * the robot to throw a GameActionException with <code>GameActionExceptionType.DOUBLE_ACTION_ERROR</code>
     *
     * @param msg
     *            the message you want to broadcast; cannot be <code>null</code>.
     * @throws GameActionException with <code>GameActionExceptionType.DOUBLE_ACTION_ERROR</code>
     *             if this robot already has a message queued in the current
     *             round.
     */
    public void broadcast(Message msg) throws GameActionException;

    /**
     * Clears any pending broadcast message for this robot.
     */
    public void clearBroadcast();

    /**
     * Returns whether your robot has a message queued for the end of this
     * round.
     *
     * @return true if and only if there is a message queued for the end of this
     *         round.
     */
    public boolean hasBroadcastMessage();

    /**
     * Gives the cost, in energon, of broadcasting the current set message.
     *
     * @return the cost of broadcasting the current set message
     */
    public double getBroadcastCost();

    /**
     * Queues a forward movement to be performed at the end of this robot's turn.
     * When this action is executed, the robot will attempt to move forward one
     * square in its current direction. If the move succeeds, this robot's new
     * location will immediately change to the destination square, but this robot
     * will be engaged in the ActionType.MOVING action for some number of rounds
     * (defined in this robot's RobotType).
     * <p>
     * When the action is queued a GameActionException may result. The possible
     * <code>GameActionExceptionType</code>s are as follows. If more than one
     * of the following applies, the <code>GameActionExceptionType</code> will
     * be the first applicable one in the list.
     * <li>The destination terrain is not traversable by this robot
     * (GameActionExceptionType.CANT_MOVE_THERE)</li>
     * <li>The destination square is occupied by another
     * <code>GameObject</code> at the same height
     * (GameActionExceptionType.CANT_MOVE_THERE)</li>
     * 	<li>Movement has not sufficiently cooled down yet
     * (GameActionExceptionType.INSUFFICIENT_MOVEMENT_COOLDOWN)</li>
     * <li>An action has already been queued in this round
     * (GameActionExceptionType.DOUBLE_ACTION_ERROR)</li>
     */
    public void moveForward() throws GameActionException;

    /**
     * Queues a backward movement to be performed at the end of this robot's turn.
     * Moving backward does not change this robot's direction.
     *
     * @see #moveForward()
     */
    public void moveBackward() throws GameActionException;

    /**
     * Queues a direction change to be performed at the end of this robot's turn.
     * When the action is executed, the robot will change its <code>Direction
     * </code> to the given one. This action has a duration of 1 round.
     * <p>
     * Note that the robot can only queue an action once per round. Attempting
     * to queue more than one in a given round will result in a
     * <code>GameActionException</code> with
     * <code>GameActionExceptionType.DOUBLE_ACTION_ERROR</code>.
     * <p>
     * When the action is queued a GameActionException may result. The possible
     * <code>GameActionExceptionType</code>s are as follows. If more than one
     * of the following applies, the <code>GameActionExceptionType</code> will
     * be the first applicable one in the list.
     * <li>Movement has not sufficiently cooled down yet
     * (GameActionExceptionType.INSUFFICIENT_MOVEMENT_COOLDOWN)</li>
     * <li><code>dir</code> is a non-direction direction, i.e. NONE or OMNI
     * (GameActionExceptionType.BAD_DIRECTION)</li>
     * 	<li>An action has already been queued in this round
     * (GameActionExceptionType.DOUBLE_ACTION_ERROR)</li>
     *
     * @param dir
     *            the Direction the robot should face; cannot be null
     */
    public void setDirection(Direction dir) throws GameActionException;

    /**
     * Queues a spawn action to be performed at the end of this robot's turn.
     * When the action is executed, a new robot will be created directly in
     * front of this robot. The square directly in front of you must be empty,
     * i.e., a robot of the new type must be able to move into that square from
     * the parent robot's location. The new robot is created and starts
     * executing bytecodes immediately, but parent and child will be engaged in
     * ActionType.SPAWNING and ActionType.WAKING, respectively, for some number
     * of rounds.
     * <p>
     * When the action is queued a GameActionException may result. The possible
     * <code>GameActionExceptionType</code>s are as follows. If more than one
     * of the following applies, the <code>GameActionExceptionType</code> will
     * be the first applicable one in the list.
     * <li>Tried to spawn with a non-archon unit
     * (GameActionExceptionType.ARCHONS_ONLY)</li>
     * <li>The given robot type cannot be spawned, e.g., it is an evolved unit
     * type (GameActionExceptionType.CANT_SPAWN_THAT)</li>
     * <li>You do not have enough energon to spawn the given robot type
     * (GameActionExceptionType.NOT_ENOUGH_ENERGON)</li>
     * <li>You do not have enough flux to spawn the given robot type
     * (GameActionExceptionType.NOT_ENOUGH_ENERGON)</li>
     * <li>You could not move into the square in front of you, if you were a
     * robot of type <code>type</code>
     * (GameActionExceptionType.CANT_SPAWN_THERE)</li>
     * <li>You are not in the spawn radius of one of your own towers
     * (GameActionExceptionType.OUTSIDE_SPAWN_RADIUS)</li>
     * <li>An action has already been queued in this round
     * (GameActionExceptionType.DOUBLE_ACTION_ERROR)</li>
     *
     * @param type
     *            the type of robot to spawn in the square directly in front of
     *            you; cannot be null.
     */
    public void spawn(RobotType type) throws GameActionException;

    /**
     * Queues a transformation action to be performed at the end of this robot's turn.
     * When this action is executed, this robot transforms into a different robot type.
     * The change takes place instantly, but this robot will be engaged in
     * ActionType.TRANSFORMING for some number of rounds.
     * <p>
     * When the action is queued a GameActionException may result. The possible
     * <code>GameActionExceptionType</code>s are as follows. If more than one
     * of the following applies, the <code>GameActionExceptionType</code> will
     * be the first applicable one in the list.
     * <li>This unit's current robot type precludes it from evolving into the
     * given robot type (GameActionExceptionType.CANT_EVOLVE_INTO_THAT)</li>
     * <li>This robot does not have enough energon to evolve into the given
     * robot type (GameActionExceptionType.NOT_ENOUGH_ENERGON)</li>
     * <li>An action has already been queued in this round
     * (GameActionExceptionType.DOUBLE_ACTION_ERROR)</li>
     *
     * @param type
     *            the type of robot to evolve into; cannot be null.
     */
    public void transform(RobotType type) throws GameActionException;

    /**
     * Ends the current round and gives your robot a slight energon bonus, given
     * by
     * <code>GameConstants.YIELD_BONUS * energonUpkeep * (fraction of unused bytecodes in the round)</code>.
     * Never fails.
     */
    public void yield();

    /**
     * Kills your robot and ends the current round. Never fails.
     */
    public void suicide();

    /**
     * Queues a turret deploying action to be performed at the end of this robot's
     * turn.
     * When the action is queued a GameActionException may result. The possible
     * <code>GameActionExceptionType</code>s are as follows. If more than one
     * of the following applies, the <code>GameActionExceptionType</code> will
     * be the first applicable one in the list.
     * <li>The unit is not a TURRET
     * (GameActionExceptionType.TURRETS_ONLY)</li>
     * <li>The unit is aready deployed
     * (GameActionExceptionType.CANT_DEPLOY)</li>
     * <li>An action has already been queued in this round
     * (GameActionExceptionType.DOUBLE_ACTION_ERROR)</li>
     *
     */
    public void deploy() throws GameActionException;

    /**
     * Queues a turret undeploying action to be performed at the end of this robot's
     * turn.
     * When the action is queued a GameActionException may result. The possible
     * <code>GameActionExceptionType</code>s are as follows. If more than one
     * of the following applies, the <code>GameActionExceptionType</code> will
     * be the first applicable one in the list.
     * <li>The unit is not a TURRET
     * (GameActionExceptionType.TURRETS_ONLY)</li>
     * <li>The unit is not deployed
     * (GameActionExceptionType.CANT_DEPLOY)</li>
     * <li>An action has already been queued in this round
     * (GameActionExceptionType.DOUBLE_ACTION_ERROR)</li>
     *
     */
    public void undeploy() throws GameActionException;

    /**
     * Returns true if this robot is a turret that is deployed and false otherwise.
     *
     */
    public boolean isDeployed();

    public boolean isTeleporting();

    // ***********************************
    // ****** SENSING METHODS *******
    // ***********************************
    /**
     * Sense any Robots IN THE AIR at this location.
     *
     * @param loc
     *            the location of the square you are sensing; cannot be null
     * @return the Robot at this location IN THE AIR, or null if there is none.
     * @throws GameActionException
     *             under any of the following circumstances. If more than one of
     *             the following applies, the
     *             <code>GameActionExceptionType</code> will be the first
     *             applicable one in the list.
     *             <li>You are not within sensor range of <code>loc</code>
     *             (GameActionExceptionType.CANT_SENSE_THAT)</li>
     */
    public Robot senseAirRobotAtLocation(
            MapLocation loc)
            throws GameActionException;

    /**
     * Sense any Robots ON THE GROUND at this location.
     *
     * @param loc
     *            the location of the square you are sensing; cannot be null
     * @return the Robot at this location ON THE GROUND, or null if there is
     *         none.
     * @throws GameActionException
     *             under any of the following circumstances. If more than one of
     *             the following applies, the
     *             <code>GameActionExceptionType</code> will be the first
     *             applicable one in the list.
     *             <li>You are not within sensor range of <code>loc</code>
     *             (GameActionExceptionType.CANT_SENSE_THAT)</li>
     */
    public Robot senseGroundRobotAtLocation(MapLocation loc)
            throws GameActionException;

    /**
     * Sense any FluxDeposits at this location.
     *
     * @param loc
     *            the location of the square you are sensing; cannot be null
     * @return the FluxDeposit at this location, or null if there is
     *         none.
     * @throws GameActionException
     *             under any of the following circumstances. If more than one of
     *             the following applies, the
     *             <code>GameActionExceptionType</code> will be the first
     *             applicable one in the list.
     *             <li>You are not within sensor range of <code>loc</code>
     *             (GameActionExceptionType.CANT_SENSE_THAT)</li>
     */
    /**
     * Get an array of all Robots IN THE AIR in your current sensor range,
     * excepting the calling robot. The order of the objects returned is
     * undefined. If there are no other objects in your sensor range, a
     * zero-length array is returned.
     *
     * @return array of {@link battlecode.common.Robot} objects in the air
     *         within sensor range, except for the calling robot
     */
    public Robot[] senseNearbyAirRobots();

    /**
     * Get an array of all Robots ON THE GROUND in your current sensor range,
     * excepting for the calling robot. The order of the objects returned is
     * undefined. If there are no other objects in your sensor range, a
     * zero-length array is returned.
     *
     * @return array of all {@link battlecode.common.Robot} objects on the
     *         ground within sensor range, except for the calling robot
     */
    public Robot[] senseNearbyGroundRobots();

    /**
     * Sense the total height at <code>loc</code> which is the sum of the
     * immutable TerrainTile height and the number of blocks.
     *
     * @return the total height of the location
     * @throws GameActionException
     *             under any of the following circumstances. If more than one of
     *             the following applies, the
     *             <code>GameActionExceptionType</code> will be the first
     *             applicable one in the list.
     *             <li>You are not within sensor range of <code>loc</code>
     *             (GameActionExceptionType.CANT_SENSE_THAT)</li>
     */
    public int senseHeightOfLocation(MapLocation loc) throws GameActionException;

    /**
     * Sense the terrain tile at a given MapLocation. If the given location is
     * <i>or has ever been</i> within sensor range of this robot, this method
     * gives the terrain at that location, as described in
     * {@link battlecode.common.TerrainTile}. If the given location has never
     * been within sensor range of this robot, this method returns
     * <code>null</code>.
     *
     * @return the terrain at the given location, if the robot has seen that
     *         location; null otherwise
     */
    public TerrainTile senseTerrainTile(MapLocation loc);

    /**
     * Sense the flux stored at a given MapLocation.
     *
     * @return the amount of flux stored at <code>MapLocation loc</code>
     * @throws GameActionException
     *             under any of the following circumstances. If more than one of
     *             the following applies, the
     *             <code>GameActionExceptionType</code> will be the first
     *             applicable one in the list.
     *             <li>You are not within sensor range of <code>MapLocation loc</code>
     *             (GameActionExceptionType.CANT_SENSE_THAT)</li>
     */
    public int senseFluxAtLocation(MapLocation loc) throws GameActionException;

    /**
     * Returns the number of points currently held by the team of the given robot.
     * The given robot must be in sensor range and it must be an archon.
     *
     * @return the number of points held by the given archon's team
     * @throws GameActionException
     *             under any of the following circumstances. If more than one of
     *             the following applies, the
     *             <code>GameActionExceptionType</code> will be the first
     *             applicable one in the list.
     *             <li>You are not within sensor range of <code>r</code>
     *             (GameActionExceptionType.CANT_SENSE_THAT)</li>
     *             <li>The robot <code>r</code> is not an archon.
     *             (GameActionExceptionType.CANT_SENSE_THAT)</li>
     */
    public double senseTeamPoints(Robot r) throws GameActionException;

    /**
     * Returns the locations of all the archons on the calling robot's team. The
     * length of the returned array is equal to the number of allied archons on
     * the map. The order of archons in the returned array is the same between
     * different calls to this method.
     *
     * @return the locations of all the allied archons
     */
    public MapLocation[] senseAlliedArchons();

    // ***********************************
    // ******** MISC. METHODS *********
    // ***********************************
    /**
     * Sets one of this robot's 'indicator strings' for debugging purposes.
     * These strings are displayed in the client. This method has no affect on
     * gameplay (aside from the number of bytecodes executed to call this
     * method).
     *
     * @param stringIndex
     *            the index of the indicator string to set. Must satisfy
     *            <code>stringIndex >= 0 && stringIndex < GameConstants.NUMBER_OF_INDICATOR_STRINGS</code>
     * @param newString
     *            the value to which the indicator string should be set
     */
    public void setIndicatorString(int stringIndex, String newString);

    /**
     * Gets this robot's 'control bits' for debugging purposes. These bits can
     * be set manually by the user, so a robot can respond to them.
     *
     * @return this robot's control bits
     */
    public long getControlBits();

    /**
     * Adds a custom observation to the match file, such that when it is analyzed, this observation will appear.
     *
     * @param observation
     *            the observation you want to inject into the match file
     */
    public void addMatchObservation(String observation);

    /**
     * Sets this robot's "memory", which is saved for the next game in the
     * match. This method can only be called by an archon. If this method is
     * called more than once in a game, the last call is what is saved for the
     * next game.
     *
     * @param memory
     *            the data that this archon should remember for the next game
     * @throws GameActionException
     *             under any of the following circumstances. If more than one of
     *             the following applies, the
     *             <code>GameActionExceptionType</code> will be the first
     *             applicable one in the list.
     *             <li>The calling robot is not an archon
     *             (GameActionExceptionType.ARCHONS_ONLY)</li>
     */
    public void setArchonMemory(long memory) throws GameActionException;

    /**
     * Returns the archon memory from the archons in the last game of the match.
     * The return value is an array of length 8. If an archon did not call
     * setArchonMemory in the last game, or there was no last game, the
     * corresponding long defaults to 0.
     *
     * @return the archon memory from the archons in the last game of the match
     */
    public long[] getOldArchonMemory();

    /**
     * If breakpoints are enabled, calling this method causes the game engine to
     * pause execution at the end of this round, until the user decides to
     * resume execution.
     */
    public void breakpoint();

    /**
     * Senses basic information about a given Robot if it is within sensor
     * range.
     *
     * @param r
     *            Robot to sense information about
     * @return RobotInfo of the Robot <tt>r</tt>
     * @throws GameActionException
     *             under any of the following circumstances. If more than one of
     *             the following applies, the
     *             <code>GameActionExceptionType</code> will be the first
     *             applicable one in the list.
     *             <li><code>Robot r</code> is not a valid
     *             <code>GameObject</code>
     *             (GameActionExceptionType.INVALID_OBJECT)</li>
     *             <li><code>Robot r</code> no longer exists
     *             (GameActionExceptionType.CANT_SENSE_THAT)</li>
     *             <li>You are not within sensor range of <code>Robot r</code>
     *             (GameActionExceptionType.CANT_SENSE_THAT)</li>
     *
     * @see RobotInfo
     */
    public RobotInfo senseRobotInfo(Robot r) throws GameActionException;

    /**
     * Returns the location of all teleporters, assuming this robot is
     * within range of a teleporter.
     *
     * @return MapLocation of all allied teleporters <tt>o</tt>
     * @throws GameActionException
     *             under any of the following circumstances. If more than one of
     *             the following applies, the
     *             <code>GameActionExceptionType</code> will be the first
     *             applicable one in the list.
     *             <li>This robot is not a teleporter.
     *             (GameActionExceptionType.NOT_A_TELEPORTER)</li>
     *
     */
    public MapLocation[] senseAlliedTeleporters() throws GameActionException;

    /**
     * Senses whether it is possible to teleport to a given location.
     *
     * @param toTeleporter
     *            The teleporter to which the robot is to be sent
     * @param teleportLoc
     *            The location to which this robot is to be sent
     * @param level
     *            The level of the robot to be teleported
     * @return  <tt>True if it is possible to teleport to teleportLoc at level height.</tt>
     * @throws GameActionException
     *             under any of the following circumstances. If more than one of
     *             the following applies, the
     *             <code>GameActionExceptionType</code> will be the first
     *             applicable one in the list.
     *             This robot is not a teleporter.
     *             (GameActionExceptionType.NOT_A_TELEPORTER)</li>
     *             <li>There is no allied teleporter at<code>MapLocation toTeleporter</code>
     *             (GameActionExceptionType.NOT_A_TELEPORTER)</li>
     *             <li><code>MapLocation teleportLoc</code>is not within
     *             teleport range of
     *             <code>MapLocation toTeleporter</code>
     *             (GameActionExceptionType.CANT_SENSE_THAT)</li>
     */
    public boolean canTeleport(MapLocation toTeleporter, MapLocation teleportLoc, RobotLevel level) throws GameActionException;

    /**
     * Teleports this robot.  The robot will be immobile for
     * <code>GameConstants.TELEPORT_DELAY</code> turns, after
     * which it will be teleported, assuming both teleporters
     * still exist and the teleport square is not occupied
     *
     * @param r
     *            The Robot to be teleported;
     * @param toTeleporter
     *            The teleporter to which this robot is to be sent
     * @param teleportLoc
     *            The location to which this robot is to be sent
     * @throws GameActionException
     *             under any of the following circumstances. If more than one of
     *             the following applies, the
     *             <code>GameActionExceptionType</code> will be the first
     *             applicable one in the list.
     *             <li>This robot is not a teleporter.
     *             (GameActionExceptionType.NOT_A_TELEPORTER)</li>
     *             <code>Robot r</code> is not on this robot's team
     *             (GameActionExceptionType.CANT_TELEPORT_THAT)</li>
     *             <code>Robot r</code>
     *             (GameActionExceptionType.CANT_TELEPORT_THAT)</li>
     *             <li>You are not within teleport range of
     *             <code>MapLocation fromTeleporter</code>
     *             (GameActionExceptionType.CANT_SENSE_THAT)</li>
     *             <li>There is no teleporter at
     *             <code>MapLocation fromTeleporter</code>
     *             (GameActionExceptionType.NOT_A_TELEPORTER)</li>
     *             <li>There is no teleporter at<code>MapLocation toTeleporter</code>
     *             (GameActionExceptionType.NOT_A_TELEPORTER)</li>
     *             <li><code>MapLocation teleportLoc</code>is not within
     *             teleport range of
     *             <code>MapLocation toTeleporter</code>
     *             (GameActionExceptionType.CANT_SENSE_THAT)</li>
     */
    public void teleport(Robot r, MapLocation toTeleporter, MapLocation teleportLoc) throws GameActionException;

    /**
     * Sets the current Aura of the building
     * @param t the type of aura to set to.
     * @throws GameActionException
     * under any of the following circumstances. If more than one of
     *             the following applies, the
     *             <code>GameActionExceptionType</code> will be the first
     *             applicable one in the list.
     *             <li>This robot is not a teleporter.
     *             (GameActionExceptionType.AURAS_ONLY)</li>
     */
    public void setAura(AuraType t) throws GameActionException;

    /**
     * Gets the current aura of the building
     * @return The last aura the building used.
     */
    public AuraType getLastAura();
}
