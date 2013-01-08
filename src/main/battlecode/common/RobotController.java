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
     * @return this robot's current energon level
     */
    public double getEnergon();
    
    /**
     * @return this robot's shields
     */
    public double getShields();
    
    /**
     * @return total amount of resources remaining
     */
    public double getTeamPower();

    /**
     * @return this robot's current location
     */
    public MapLocation getLocation();

    /**
     * @return the current map's width
     */
    public int getMapWidth();
   
    /**
     * @return the current map's height
     */
    public int getMapHeight();
    

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
     * Gets this robot's type (SOLDIER, HQ, etc.)
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
    public GameObject senseObjectAtLocation(MapLocation loc) throws GameActionException;

    /** 
     * Returns an array of _all_ visible game objects
     * @see #senseNearbyGameObjects(Class, MapLocation, int, Team)
     */
    public <T extends GameObject> T[] senseNearbyGameObjects(Class<T> type);

    /**
     * Returns all game objects of a given type nearby the robot
     * @see #senseNearbyGameObjects(Class, MapLocation, int, Team)
     */
    public <T extends GameObject> T[] senseNearbyGameObjects(Class<T> type, int radiusSquared);
    
    /**
     * Returns all game objects of a given type nearby the robot of a given team
     * @see #senseNearbyGameObjects(Class, MapLocation, int, Team)
     */
    public <T extends GameObject> T[] senseNearbyGameObjects(Class<T> type, int radiusSquared, Team team);
    
    
    /**
     * Senses all game objects of a given type within a given search area specified by the parameters
     * @param type - type of game object to sense, eg: Robot.class
     * @param center - center of the given search radius
     * @param radiusSquared - return objects this distance away from the center
     * @param team - filter game objects by the given team. If null is passed, objects from all teams are returned
     * @return array of class type of game objects
     */
    public <T extends GameObject> T[] senseNearbyGameObjects(Class<T> type, MapLocation center, int radiusSquared, Team team);
    
    
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
     * @return true if GameObject <code>o</code> is within a robot's personal sensor range
     */
    public boolean canSenseObject(GameObject o);

    /**
     * Returns true if this robot can sense the location {@code loc}.
     */
    public boolean canSenseSquare(MapLocation loc);

    /**
     * @return array of map location containing all encampment squares on the map
     * @see #senseEncampmentSquares(MapLocation, int, Team)
     */
    public MapLocation[] senseAllEncampmentSquares();
    
    /**
     * @return array of all encampment squares owned by the allied team
     * @see #senseEncampmentSquares(MapLocation, int, Team)
     */
    public MapLocation[] senseAlliedEncampmentSquares();
    
    /**
     * Returns all encampment squares filtered on the given search area
     * 
     * Allows a team-based filter which can be one of the following parameters:
     * <ul>
     * <li>Null - Senses _all_ encampments on the map
     * <li>Neutral - Senses all encampments that not owned by the sensing robot's team
     * <li>Allied Team - Senses all encampments owned by the allied team
     * </ui>
     * Note that you cannot sense all enemy-owned encampments
     * 
     * @param center - location to search for encampment squares
     * @param radiusSquared - radius around the center to search for encampment squares
     * @param team - team filter (null, allied team, or neutral team, see usage above)
     * @return Array of map locations containing found encampment squares satisfying the criteria
     * @throws GameActionException - attempting to search all enemy encampment squares
     */
    public MapLocation[] senseEncampmentSquares(MapLocation center, int radiusSquared, Team team) throws GameActionException;

    /**
     * Sense whether a mine exists at a given location
     * Returns either the TEAM of the mine or null if there is no mine
     * @param location to scan
     * @return team the mine belongs to or null if there is no mine there
     */
    public Team senseMine(MapLocation location);
   
    /**
     * Returns all mines within a given search radius specified by the parameters
     * @param center - center of the search area
     * @param radiusSquared - radius around the center to include mines
     * @param team - only return mines of this team. If null is passed, all team's mines are returned
     * @return An array of MapLocations containing mine locations
     */
    public MapLocation[] senseMineLocations(MapLocation center, int radiusSquared, Team team) throws GameActionException;

    /**
     * @return location of this robot's HQ
     */
    public MapLocation senseHQLocation();

    /**
     * @return location of the enemy team's HQ
     */
    public MapLocation senseEnemyHQLocation();
    
    /**
     * Senses the enemy team's NUKE research progress - only HQ can do this
     * @return true if the enemy team's NUKE is halfways researched.
     * @throws GameActionException if not HQ
     */
    public boolean senseEnemyNukeHalfDone() throws GameActionException;
    
    /**
     * Checks if the given map location is an encampment square.
     * Returns true if an encampement can be built on the square regardless of whether
     * there already exists an encampment on the square
     */
    public boolean senseEncampmentSquare(MapLocation loc);

    // ***********************************
    // ****** MOVEMENT METHODS ********
    // ***********************************

    /**
     * @return the number of rounds until this robot's action cooldown ends, or 0 if it is already active.
     */
    public int roundsUntilActive();

    /**
     * @return true if this robot is active. If a robot is active, it can move, mine, defuse, capture, and attack.
     */
    public boolean isActive();

    /**
     * Move in the given direction if possible.
     * @param dir
     * @throws GameActionException if the robot cannot move in this direction
     */
    public void move(Direction dir) throws GameActionException;

    /**
     * Tells whether this robot can move in the given direction. Takes into
     * account only the map terrain and positions of other robots. Does not take
     * into account whether this robot is currently active or otherwise
     * incapable of moving.
     *
     * @return true if there are no robots or walls preventing this robot from
     *         moving in the given direction; false otherwise
     */
    public boolean canMove(Direction dir);

    // ***********************************
    // ****** ATTACK METHODS *******
    // ***********************************

    /**
     * ARTILLERY only
     * @return true if the given location is within this robot's attack range.
     * Does not take into account whether the robot is currently attacking
     */
    public boolean canAttackSquare(MapLocation loc);

    /**
     * ARTILLERY only
     * Attacks the given location and height.
     */
    public void attackSquare(MapLocation loc) throws GameActionException;

    // ***********************************
    // ****** BROADCAST METHODS *******
    // ***********************************
    
    /**
     * Broadcasts a message to the global message board.
     * The data is not written until the end of the robot's turn
     * @param channel - the channel to write to, from 0 to <code>MAX_RADIO_CHANNELS</code>
     * @param data - one int's worth of data to write
     * @throws GameActionException
     */
    public void broadcast(int channel, int data) throws GameActionException;

    /**
     * Retrieves the message stored at the given radio channel
     * @param channel - radio channel to query, from 0 to <code>MAX_RADIO_CHANNELS</code>
     * @return data currently stored on the channel
     * @throws GameActionException 
     */
    public int readBroadcast(int channel) throws GameActionException;

    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************

    /**
     *
     * Queues a spawn action to be performed at the end of this robot's turn.
     * When the action is executed, a new robot will be created at
     * directly in front of this robot.  The square must not already be occupied.
     * The new robot is created and starts executing bytecodes immediately
     *
     * @param dir the direction to spawn robot in; cannot be null.
     * @throws IllegalStateException if this robot is not an ARCHON
     * @throws GameActionException   if this robot is currently moving (ALREADY_ACTIVE)
     * @throws GameActionException   if <code>loc</code> is already occupied (CANT_MOVE_THERE)
     */
    public void spawn(Direction dir) throws GameActionException;
   
    
    /**
     * Checks whether a given upgrade has been researched and is available.
     * @param upgrade
     */
    public boolean hasUpgrade(Upgrade upgrade);
    
    
    /**
     * SOLDIER only
     * Lays a mine underneath a robot. A robot cannot move until the mine is laid
     * 
     * @throws GameActionException
     */
    public void layMine() throws GameActionException;
    
    /**
     * SOLDIER only
     * Defuses a mine on the target square. A robot cannot move until the defusion is complete.
     * 
     * @throws GameActionException
     */
    public void defuseMine(MapLocation loc) throws GameActionException;
    
    /**
     * Captures the encampment soldier is standing on. 
     * Immediately kills the soldier and encampment, and spawns an encampment robot of the given type
     * @param type
     * @throws GameActionException
     */
    public void captureEncampment(RobotType type) throws GameActionException;
    
    /**
     * Checks how much it costs to start a capture
     */
    public double senseCaptureCost();
   
    /**
     * Researches the given upgrade for a turn.
     * Will only work if the robot is an HQ
     * @param upgrade
     * @throws GameActionException
     */
    public void researchUpgrade(Upgrade upgrade) throws GameActionException;
    
    /**
     * Checks the total number of rounds a given research has been researched
     * Will only work if the robot is an HQ
     * @param upgrade
     * @return the number of rounds that have been spent upgrading
     * @throws GameActionException
     */
    public int checkResearchProgress(Upgrade upgrade) throws GameActionException;
    
    
    /**
     * Ends the current round.  This robot will receive a power bonus of
     * <code>GameConstants.POWER_COST_PER_BYTECODE * (GameConstants.BYTECODE_LIMIT
		 * - RobotMonitor.getBytecodesUsed())</code>.
     * Never fails.
     */
    public void yield();

    /**
     * Kills your robot and ends the current round. Never fails.
     */
    public void suicide();

    /**
     * Causes your team to lose the game. It's like typing "gg."
     */
    public void resign();

    // ***********************************
    // ******** MISC. METHODS *********
    // ***********************************
    
    /**
     * Puts a hat on the robot. To be monetized in a future DLC
     */
    public void wearHat();

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
