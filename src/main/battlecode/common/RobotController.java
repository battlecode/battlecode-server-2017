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
    public double getMaxEnergonLevel();

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
     * Gets this robot's chassis type.
     *
     * @return this robot's chassis type.
     */
    public Chassis getChassis();

	public ComponentController [] components();

	public ComponentController [] components(ComponentType type);

	public ComponentController [] components(ComponentClass cls);

    /**
     * Gets the minimum number of points needed to win a game on this map
     */
    public int getMapMinPoints();

    // ***********************************
    // ****** ACTION METHODS ********
    // ***********************************

	public void equip(Component component) throws GameActionException;

	public void turnOff();

	/**
	 * Returns true if the robot has been turned off since the last call
	 * to {@code wasTurnedOff()}.
	 */
	public boolean wasTurnedOff();

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
	 * Equivalent to setIndicatorString(stringIndex,String.format(format,args)).
	 */
	public void setIndicatorStringFormat(int stringIndex, String format, Object ... args);

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

}
