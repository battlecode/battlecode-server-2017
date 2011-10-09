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
     * Gets the current hit points of this robot.
     *
     * @return this robot's current energon level
     */
    public double getHitpoints();

    /**
     * Gets the maximum hit points of this robot.
     *
     * @return this robot's maximum energon level
     */
    public double getMaxHp();

    public double getTeamResources();

    /**
     * Gets the current location of this robot.
     *
     * @return this robot's current location
     *
     */
    public MapLocation getLocation();

    /**
     * Gets the direction this robot is currently facing.
     *
     * @return this robot's current Direction
     */
    public Direction getDirection();

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
     * Sense objects of type <code>type</code> that are within this sensor's range.
     */
    public <T extends GameObject> T[] senseNearbyGameObjects(Class<T> type);

    /**
     * Sense the location of the object <code>o</code>
     */
    public MapLocation senseLocationOf(GameObject o) throws GameActionException;

    /**
     * Sense the RobotInfo for the robot <code>r</code>.
     */
    public RobotInfo senseRobotInfo(Robot r) throws GameActionException;

    public boolean canSenseObject(GameObject o);

	/**
	 * Returns true if this robot can sense the location {@code loc}.
	 */
    public boolean canSenseSquare(MapLocation loc);

	// ***********************************
    // ****** MOVEMENT METHODS ********
    // ***********************************

	
	public int roundsUntilMovementIdle();

	public boolean isMovementActive();
    
	/**
     * Queues a forward movement to be performed at the end of this robot's turn.
     *
     * When this action is executed, the robot will attempt to move forward one
     * square in its current direction. If the move succeeds, this robot's new
     * location will immediately change to the destination square, but this robot's
     * motor will be engaged for some number of rounds ({@code type().delay} for
     * orthogonal movement and {@code Math.round(type().delay*Math.sqrt(2))} for
     * diagonal movement).
     *
     * @cooldown <code>type().delay</code> if orthogonal, <code>Math.round(type().delay*Math.sqrt(2.))</code> if diagonal
     *
     * @throws GameActionException if the robot is already moving (ALREADY_ACTIVE)
     * @throws GameActionException if the destination terrain is not traversable by
     * this robot (CANT_MOVE_THERE)
     * @throws GameActionException if the destination is occupied by another {@code GameObject}
     * at the same height (CANT_MOVE_THERE)
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
     * When the action is executed, the robot will change its direction.
     *
     * @cooldown 1
     *
     * @param dir
     *            the direction the robot should face
     * @throws GameActionException if this component is already in use (ALREADY_ACTIVE)
     */
    public void setDirection(Direction dir) throws GameActionException;

    /**
     * Tells whether this robot can move in the given direction. Takes into
     * account only the map terrain and positions of other robots. Does not take
     * into account whether this robot is currently active or otherwise
     * incapable of moving.
     *
     * @return true if there are no robots or walls preventing this robot from
     *         moving in the given direction; false otherwise
     *
     */
    public boolean canMove(Direction dir);

	// ***********************************
    // ****** ATTACK METHODS *******
    // ***********************************

	public int roundsUntilAttackIdle();

	public boolean isAttackActive();

	public boolean canAttackSquare(MapLocation loc);

 	/**
     * Attacks the given location and height.
     *
     * @cooldown
     */
    public void attackSquare(MapLocation loc, RobotLevel height) throws GameActionException;

    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************

    /**
     * Ends the current round.  If your player used fewer than
	 * BYTECODE_LIMIT_BASE bytecodes this round, then it will
	 * receive a flux bonus of
     * <code>GameConstants.YIELD_BONUS * chassis.upkeep * (BYTECODE_LIMIT_BASE - (bytecodes_used)) / BYTECODE_LIMIT_BASE</code>.
     * Never fails.
     */
    public void yield();

    /**
     * Kills your robot and ends the current round. Never fails.
     */
    public void suicide();

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
     *
     */
    public void setIndicatorString(int stringIndex, String newString);

    /**
     * Senses the terrain at loc, if loc was ever within the range of any
     * of this robot's sensors.
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
     * @param observation
     *            the observation you want to inject into the match file
     */
    public void addMatchObservation(String observation);

    /**
     * Sets the team's "memory", which is saved for the next game in the
     * match. The memory is an array of {@link GameConstants#TEAM_MEMORY_LENGTH}
     * longs.  If this method is called more than once with the same index
     * in the same game, the last call is what is saved for the
     * next game.
     *
     * @param index
     *		the index of the array to set
     * @param value
     * 		the data that the team should remember for the next game
     *
     * @throws java.lang.ArrayIndexOutOfBoundsException if {@code index} is less
     * than zero or greater than {@link GameConstants#TEAM_MEMORY_LENGTH}
     *
     * @see #getTeamMemory
     * @see #setTeamMemory(int,long)
     */
    public void setTeamMemory(int index, long value);

    /**
     * Sets this team's "memory". This function allows for finer control
     * than {@link #setTeamMemory(int,long)} provides.  For example,
     * if {@code mask == 0xFF} then only the eight least significant bits of
     * the memory will be set.
     *
     * @param index
     *		the index of the array to set
     * @param value
     * 		the data that the team should remember for the next game
     * @param mask
     * 		indicates which bits should be set
     *
     * @throws java.lang.ArrayIndexOutOfBoundsException if {@code index} is less
     * than zero or greater than {@link GameConstants#TEAM_MEMORY_LENGTH}
     *
     * @see #getTeamMemory
     * @see #setTeamMemory(int,long,long)
     */
    public void setTeamMemory(int index, long value, long mask);

    /**
     * Returns the team memory from the  last game of the match.
     * The return value is an array of length {@link GameConstants#TEAM_MEMORY_LENGTH}.
     * If setTeamMemory was not called in the last game, or there was no last game, the
     * corresponding long defaults to 0.
     *
     * @return the team memory from the the last game of the match
     *
     * @see #setTeamMemory(int,long)
     * @see #setTeamMemory(int,long,long)
     */
    public long[] getTeamMemory();

    /**
     * If breakpoints are enabled, calling this method causes the game engine to
     * pause execution at the end of this round, until the user decides to
     * resume execution.
     */
    public void breakpoint();
}
