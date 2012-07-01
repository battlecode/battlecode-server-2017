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
     * Gets the current energon of this robot.
     *
     * @return this robot's current energon level
     */
    public double getEnergon();

    /**
     * Gets the maximum energon of this robot.
     *
     * @return this robot's maximum energon level
     */
    public double getMaxEnergon();

    /**
     * Returns this robot's current flux level.
     */
    public double getFlux();

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
     * Gets the Team of this robot. Equivalent to
     * <code>this.getRobot().getTeam()</code>.
     *
     * @return this robot's Team
     * @see battlecode.common.Team
     */
    public Team getTeam();

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
    public RobotType getType();

    // ***********************************
    // ****** SENSOR METHODS ********
    // ***********************************

    /**
     * Returns the object at the given location and height, or <code>null</code>
     * if there is no object there.
     *
     * @throws GameActionException if <code>loc</code> is not within sensor range (CANT_SENSE_THAT)
     */
    public GameObject senseObjectAtLocation(MapLocation loc, RobotLevel height) throws GameActionException;

    /**
     * Sense objects of type <code>type</code> that are within this sensor's range.
     */
    public <T extends GameObject> T[] senseNearbyGameObjects(Class<T> type);

    /**
     * Sense the location of the object <code>o</code>.
     *
     * @throws GameActionException if <code>o</code> is not within sensor range (CANT_SENSE_THAT)
     */
    public MapLocation senseLocationOf(GameObject o) throws GameActionException;

    /**
     * Sense the RobotInfo for the robot <code>r</code>.
     *
     * @throws GameActionException if <code>r</code> is not within sensor range (CANT_SENSE_THAT)
     */
    public RobotInfo senseRobotInfo(Robot r) throws GameActionException;

    /**
     * Returns true if <code>o</code> is within sensor range.
     */
    public boolean canSenseObject(GameObject o);

    /**
     * Returns true if this robot can sense the location {@code loc}.
     */
    public boolean canSenseSquare(MapLocation loc);

    /**
     * Returns the locations of all the archons on the calling robot's team. The
     * length of the returned array is equal to the number of allied archons on
     * the map. The order of archons in the returned array is the same between
     * different calls to this method.
     *
     * @return the locations of all the allied archons
     */
    public MapLocation[] senseAlliedArchons();

    /**
     * Returns an array containing all of the power nodes owned by this robot's team.
     */
    public PowerNode[] senseAlliedPowerNodes();

    /**
     * Returns an array containing the locations of all of the nodes
     * that this team can capture.
     */
    public MapLocation[] senseCapturablePowerNodes();

    /**
     * Returns <code>true</code> if the node <code>p</code> is connected to
     * this robot's team's power core.  Note that <code>p</code> does not need to
     * be within sensor range.
     */
    public boolean senseConnected(PowerNode p);

    /**
     * Returns <code>true</code> if there is an allied tower at the power node <code>p</code>.
     * Note that <code>p</code> does not need to be within sensor range.
     */
    public boolean senseOwned(PowerNode p);

    /**
     * Returns <code>true</code> if the node <code>p</code> is connected to
     * this robot's team's opponent's power core.
     *
     * @throws GameActionException if <code>p</code> is not within this robot's sensor range (CANT_SENSE_THAT)
     */
    public boolean senseOpponentConnected(PowerNode p) throws GameActionException;

    /**
     * Returns this robot's team's power core.
     */
    public PowerNode sensePowerCore();

    // ***********************************
    // ****** MOVEMENT METHODS ********
    // ***********************************

    /**
     * Returns the number of rounds until this robot's movement cooldown reaches zero.
     */
    public int roundsUntilMovementIdle();

    /**
     * Returns true if this robot's movement cooldown is nonzero.
     */
    public boolean isMovementActive();

    /**
     * Queues a movement to be performed at the end of this robot's turn.
     * <p/>
     * When this action is executed, the robot will attempt to move to the given square.
     * If the move succeeds, this robot's new
     * location will immediately change to the destination square, but this robot
     * motor will not be able to move or change direction again for some number of rounds (<code>type().moveDelayOrthogonal</code> for
     * orthogonal movement and <code>type().moveDelayDiagonal</code> for
     * diagonal movement).
     *
     * @throws GameActionException if this robot is already moving (ALREADY_ACTIVE)
     * @throws GameActionException if this robot does not have enough flux to move (NOT_ENOUGH_FLUX)
     * @throws GameActionException if the destination terrain is not traversable by
     *                             this robot (CANT_MOVE_THERE)
     * @throws GameActionException if the destination is occupied by another {@code GameObject}
     *                             at the same height (CANT_MOVE_THERE)
     */
    public void move(MapLocation loc) throws GameActionException;

    /**
     * Tells whether this robot can move in the given direction. Takes into
     * account only the map terrain and positions of other robots. Does not take
     * into account whether this robot is currently active or otherwise
     * incapable of moving.
     *
     * @return true if there are no robots or walls preventing this robot from
     *         moving in the given direction; false otherwise
     */
    public boolean canMove(MapLocation loc) throws GameActionException;

    // ***********************************
    // ****** ATTACK METHODS *******
    // ***********************************

    /**
     * Returns the number of rounds until this robot's attack cooldown reaches zero.
     */
    public int roundsUntilAttackIdle();

    /**
     * Returns true if this robot's attack cooldown is nonzero.
     */
    public boolean isAttackActive();

    /**
     * Returns true if the given location is within this robot's attack range.
     * Does not take into account whether the robot is currently attacking.
     */
    public boolean canAttackSquare(MapLocation loc);

    /**
     * Attacks the given location and height.
     * If this robot is a <code>SCORCHER</code>, attacks every square in the robot's
     * attack range at ground level instead; <code>loc</code> and <code>height</code>
     * are ignored.
     *
     * @throws GameActionException if this robot's attack cooldown is not zero (ALREADY_ACTIVE)
     * @throws GameActionException if this robot is not a <code>SCORCHER</code> and it cannot attack the given height or location (OUT_OF_RANGE)
     */
    public void attackSquare(MapLocation loc, RobotLevel height) throws GameActionException;

    // ***********************************
    // ****** BROADCAST METHODS *******
    // ***********************************

    /**
     * Returns true if this robot has already broadcasted this turn.
     */
    public boolean hasBroadcasted();

    /**
     * Adds a message for your robot to broadcast. At the end of your
     * robot's execution block, if a broadcast has been set, the message is removed
     * and immediately added to the incoming message queues of all robots in
     * your broadcast range (except for the sending robot). Note that robots
     * are thus limited to sending at most one message per round.
     * </p>
     * <p/>
     * You are charged a small amount of flux for every message that you
     * broadcast. The cost of sending a message is equal to
     * <code>(GameConstants.BROADCAST_FIXED_COST +
     * GameConstants.BROADCAST_COST_PER_BYTE*sizeBytes)</code>
     * where <code>sizeBytes</code> is the size of the message, in bytes.
     * <p/>
     * <p/>
     * Each robot can only broadcast one message per round.
     *
     * @param msg the message you want to broadcast; cannot be <code>null</code>.
     * @throws GameActionException if this robot already has a message queued in the current round (ALREADY_ACTIVE).
     * @throws GameActionException if this robot does not have enough flux to pay for the broadcast (NOT_ENOUGH_FLUX).
     */
    public void broadcast(Message msg) throws GameActionException;


    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************

    /**
     * Queues a spawn action to be performed at the end of this robot's turn.
     * When the action is executed, a new robot will be created at
     * directly in front of this robot.  The square must not already be occupied.
     * The new robot is created and starts executing bytecodes immediately, but
     * it will not be able to perform any actions for <code>GameConstants.WAKE_DELAY</code>
     * rounds.
     *
     * @param type the type of robot to spawn; cannot be null.
     * @throws IllegalStateException if this robot is not an ARCHON
     * @throws GameActionException   if this robot is currently moving (ALREADY_ACTIVE)
     * @throws GameActionException   if this robot does not have enough flux to spawn a robot of type <code>type</code> (NOT_ENOUGH_FLUX)
     * @throws GameActionException   if <code>loc</code> is already occupied (CANT_MOVE_THERE)
     */
    public void spawn(RobotType type, MapLocation loc) throws GameActionException;

    /**
     * Transfers the specified amount of flux to the robot at location
     * <code>loc</code> and RobotLevel <code>height</code>. The robot
     * receiving the transfer must be adjacent to or in the same location as the
     * robot giving the transfer. Robots may not transfer more flux than they
     * currently have.
     *
     * @param amount the amount of flux to transfer to the specified robot
     * @param loc    the <code>MapLocation</code> of the robot to transfer to
     * @param height the <code>RobotLevel</code> of the robot to transfer to
     * @throws IllegalArgumentException if <code>amount</code> is negative, zero, or NaN.
     * @throws GameActionException      if the robot does not have <code>amount</code> flux (NOT_ENOUGH_FLUX).
     * @throws GameActionException      if <code>loc</code> is not the same as or adjacent to this robot's location (CANT_SENSE_THAT).
     * @throws GameActionException      if there is no robot at the given location and height (NO_ROBOT_THERE).
     */
    public void transferFlux(MapLocation loc, RobotLevel height, double amount) throws GameActionException;

    /**
     * Ends the current round.  This robot will receive a flux bonus of
     * <code>GameConstants.YIELD_BONUS * GameConstants.UNIT_UPKEEP * Clock.getBytecodesLeft() / GameConstants.BYTECODE_LIMIT</code>.
     * Never fails.
     */
    public void yield();

    /**
     * Kills your robot and ends the current round. Never fails.
     */
    public void suicide();

    /**
     * Causes your team to lose the game.  Mainly for testing purposes.
     */
    public void resign();

    /**
     * Causes each allied robot within its attack radius to regenerate
     * <code>GameConstants.REGEN_AMOUNT</code> energon at the beginning
     * of its next turn.  Each robot may only regenerate once per turn.
     * This action can only be activated by scouts and costs
     * <code>GameConstants.REGEN_COST</code> flux.
     *
     * @throws IllegalStateException if this robot is not a scout
     * @throws GameActionException   if this robot does not have <code>GameConstants.REGEN_COST</code> flux (NOT_ENOUGH_FLUX)
     */
    public void regenerate() throws GameActionException;

    // ***********************************
    // ******** MISC. METHODS *********
    // ***********************************

    /**
     * Sets one of this robot's 'indicator strings' for debugging purposes.
     * These strings are displayed in the client. This method has no effect on
     * gameplay (aside from the number of bytecodes executed to call this
     * method).
     *
     * @param stringIndex the index of the indicator string to set. Must satisfy
     *                    <code>stringIndex >= 0 && stringIndex < GameConstants.NUMBER_OF_INDICATOR_STRINGS</code>
     * @param newString   the value to which the indicator string should be set
     */
    public void setIndicatorString(int stringIndex, String newString);

    /**
     * Senses the terrain at <code>loc</code>, if <code>loc</code> was ever
     * within this robot's sensor range.  Otherwise, returns <code>null</code>.
     */
    public TerrainTile senseTerrainTile(MapLocation loc);

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
     * @param observation the observation you want to inject into the match file
     */
    public void addMatchObservation(String observation);

    /**
     * Sets the team's "memory", which is saved for the next game in the
     * match. The memory is an array of {@link GameConstants#TEAM_MEMORY_LENGTH}
     * longs.  If this method is called more than once with the same index
     * in the same game, the last call is what is saved for the
     * next game.
     *
     * @param index the index of the array to set
     * @param value the data that the team should remember for the next game
     * @throws java.lang.ArrayIndexOutOfBoundsException
     *          if {@code index} is less
     *          than zero or greater than or equal to {@link GameConstants#TEAM_MEMORY_LENGTH}
     * @see #getTeamMemory
     * @see #setTeamMemory(int, long, long)
     */
    public void setTeamMemory(int index, long value);

    /**
     * Sets this team's "memory". This function allows for finer control
     * than {@link #setTeamMemory(int, long)} provides.  For example,
     * if {@code mask == 0xFF} then only the eight least significant bits of
     * the memory will be set.
     *
     * @param index the index of the array to set
     * @param value the data that the team should remember for the next game
     * @param mask  indicates which bits should be set
     * @throws java.lang.ArrayIndexOutOfBoundsException
     *          if {@code index} is less
     *          than zero or greater than or equal to {@link GameConstants#TEAM_MEMORY_LENGTH}
     * @see #getTeamMemory
     * @see #setTeamMemory(int, long)
     */
    public void setTeamMemory(int index, long value, long mask);

    /**
     * Returns the team memory from the  last game of the match.
     * The return value is an array of length {@link GameConstants#TEAM_MEMORY_LENGTH}.
     * If setTeamMemory was not called in the last game, or there was no last game, the
     * corresponding long defaults to 0.
     *
     * @return the team memory from the the last game of the match
     * @see #setTeamMemory(int, long)
     * @see #setTeamMemory(int, long, long)
     */
    public long[] getTeamMemory();

    /**
     * If breakpoints are enabled, calling this method causes the game engine to
     * pause execution at the end of this round, until the user decides to
     * resume execution.
     */
    public void breakpoint();
}
