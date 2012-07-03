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
     * Returns an array of all Robots within sensor range. Returns a zero-length array if there are no nearby robots. 
     */
    public Robot[] senseNearbyRobots();

    /**
     * Returns true if <code>r</code> is within sensor range.
     */
    public boolean canSenseRobot(Robot r);
    
    /**
     * Sense the RobotInfo for the robot <code>r</code>.
     *
     * @throws GameActionException if <code>r</code> is not within sensor range (CANT_SENSE_THAT)
     */
    public RobotInfo senseRobotInfo(Robot r) throws GameActionException;
    
    /**
     * Sense the amount of flux at a location
     * 
     * @throws GameActionException if <code>loc</code> is not within sensor range
     */
    public int senseFluxAt(MapLocation loc) throws GameActionException;
    
    /**
     * Sense the amount of flux carried by a Transporter
     * 
     * Returns 0 if the robot is not a Transporter
     * Returns 0 if the robot is not an ally
     * @throws GameActionException if <code>r</code> is not within sensor range
     */
    public int senseFluxCarriedBy(Robot r) throws GameActionException;
    
    /**
     * Returns an array of MapLocations of all Rifts within sensor range. Returns a zero-length array if there are no nearby rifts. 
     */ 
    public MapLocation[] senseNearbyRifts();
    
    /**
     * Returns the MapLocation of the home base
     */
    public MapLocation senseNexusLocation();

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
    public boolean canMove(MapLocation loc);

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
     * @throws GameActionException if <code>loc</code> is out of range
     */
    public void attackSquare(MapLocation loc, RobotLevel height) throws GameActionException;
    
    // ***********************************
    //  **** FLUX CONSUMPTION METHODS ****
    // ***********************************
    
    /**
     * Returns the number of rounds until the robot becomes weakened from not consuming flux. 
     */
    public int roundsUntilWeakened();
    public boolean isWeakened();
    
    /**
     * Causes the robot to immediately consume one unit of flux from an adjacent MapLocation
     * @throws IllegalStateException if the robot is not a Soldier
     * @throws GameActionException if <code>loc</code> is not adjacent to the robot
     * @throws GameActionException if <code>loc</code> does not contain any flux
     */
    public void consumeFlux(MapLocation loc) throws GameActionException
    
    /**
     * Causes the robot to immediately consume one unit of flux from an adjacent allied Transporter
     * @throws GameActionException if <code>r</code> is not a friendly Transporter
     * @throws GameActionException if <code>r</code> is not adjacent or on top of the robot that called this method
     * @throws GameActionException if <code>r</code> does not contain any flux\
     */
    public void consumeFlux(Robot r) throws GameActionException

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
     * Each robot can only broadcast one message per round.
     *
     * @param msg the message you want to broadcast; cannot be <code>null</code>.
     * @throws GameActionException if this robot already has a message queued in the current round (ALREADY_ACTIVE).
     * @throws GameActionException if the Message is longer than 256 bytes. 
     */
    public void broadcast(Message msg) throws GameActionException;


    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************

    /**
     * Queues a spawn action to be performed at the end of this robot's turn. Spawn may only be called by the Nexus. Only one robot may be spawned per turn. When the action is executed, a robot will be created at the MapLocation loc. This MapLocation must not be occupied and must be adjacent (orthogonally or diagonally) to the Nexus. 
     * The new robot is created and starts executing bytecodes immediately, but
     * it will not be able to perform any actions for <code>GameConstants.WAKE_DELAY</code>
     * rounds.
     *
     * @param type the type of robot to spawn; cannot be null.
     * @throws GameActionException if this robot is not a Nexus
     * @throws GameActionException if <code>loc</code> is already occupied (CANT_MOVE_THERE)
     */
    public void spawn(RobotType type, MapLocation loc) throws GameActionException;
    
    /**
     * Generates 1 unit of flux at the robot's current location. GenerateFlux may only be called by the Nexus. When this action is executed, the amount of flux at the Nexus's MapLocation is increased by 1. This flux may be consumed by nearby Soldiers, or moved by a Transporter. 
     */
    public void generateFlux() throws GameActionException ;

    /**
     * Ends the current round.
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
