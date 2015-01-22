package battlecode.common;


/**
 * A RobotController allows contestants to make their robot sense and interact
 * with the game world. When a contestant's <code>RobotPlayer</code> is
 * constructed, it is passed an instance of <code>RobotController</code> that
 * controls the newly created robot.
 */
public interface RobotController {

    // *********************************
    // ****** GLOBAL QUERY METHODS *****
    // *********************************

    /**
     * Gets the number of rounds in the game. After this many rounds, if neither team has destroyed the enemy HQ, then the tiebreakers will be used.
     *
     * @return the number of rounds in the game.
     */
    public int getRoundLimit();

    /**
     * Gets the team's total ore.
     *
     * @return the team's total ore.
     */
    public double getTeamOre();

    // *********************************
    // ****** UNIT QUERY METHODS *******
    // *********************************

    /**
     * Use this method to access your ID.
     *
     * @return the ID of the robot.
     */
    public int getID();

    /**
     * Gets the Team of this robot.
     *
     * @return this robot's Team
     */
    public Team getTeam();

    /**
     * Gets this robot's type (SOLDIER, HQ, etc.).
     *
     * @return this robot's type.
     */
    public RobotType getType();
    
    /**
     * Gets the robot's current location.
     *
     * @return this robot's current location.
     */
    public MapLocation getLocation();

    /**
     * Returns the amount of core delay a robot has accumulated. If the result is strictly less than 1, then the robot can perform a core action.
     *
     * @return the amount of core delay a robot has accumulated.
     */
    public double getCoreDelay();

    /**
     * Returns the amount of weapon delay a robot has accumulated. If the result is strictly less than 1, then the robot can attack.
     *
     * @return the number of weapon delay a robot has accumulated.
     */
    public double getWeaponDelay();

    /**
     * Gets the robot's current health.
     *
     * @return this robot's current health.
     */
    public double getHealth();

    /**
     * Gets the robot's current supply level.
     *
     * @return this robot's supply level.
     */
    public double getSupplyLevel();

    /**
     * Gets the experience a robot has. Only meaningful for COMMANDER.
     *
     * @return the number of XP the robot has.
     */
    public int getXP();

    /**
     * Returns how many missiles the robot has. Only useful for LAUNCHER.
     *
     * @return the number of missiles the robot has.
     */
    public int getMissileCount();

    /**
     * Returns whether this robot is currently building anything.
     *
     * @return whether this robot is currently building anything.
     */
    public boolean isBuildingSomething();

    // ***********************************
    // ****** GENERAL SENSOR METHODS *****
    // ***********************************
    
    /**
     * Returns location of the allied team's HQ (unconstrained by sensor range or distance).
     * @return the team's HQ location.
     */
    public MapLocation senseHQLocation();

    /**
     * Returns location of the enemy team's HQ (unconstrained by sensor range or distance).
     *
     * @return the enemy team's HQ location.
     */
    public MapLocation senseEnemyHQLocation();

    /**
     * Returns the locations of your own towers, unconstrained by sensor range or distance.
     * @return an array of the locations of your living towers.
    */
    public MapLocation[] senseTowerLocations();

    /**
     * Returns the locations of surviving enemy towers, unconstrained by sensor range or distance.
     * @return an array of the locations of living enemy towers.
    */
    public MapLocation[] senseEnemyTowerLocations();

    /**
     * Senses the terrain at the given location. Returns TerrainTile.UNKNOWN for a terrain tile that has never been in sensor range.
     *
     * @param loc the location to check.
     * @return the TerrainTile at the location.
     */
    public TerrainTile senseTerrainTile(MapLocation loc);

    /**
     * Returns true if the given location is within the robot's sensor range, or within the sensor range of some ally.
     *
     * @param loc the location to check.
     * @return whether the given location is within the robot's sensor range.
     */
    public boolean canSenseLocation(MapLocation loc);

    /**
     * Returns whether there is a robot at the given location.
     *
     * @param loc the location to check.
     * @return whether there is a robot at the given location.
     * @throws GameActionException if <code>loc</code> is not within sensor range (CANT_SENSE_THAT).
     */
    public boolean isLocationOccupied(MapLocation loc) throws GameActionException;

    /**
     * Returns the robot at the given location, or <code>null</code>
     * if there is no object there.
     *
     * @param loc the location to check.
     * @return the robot at the given location.
     * @throws GameActionException if <code>loc</code> is not within sensor range (CANT_SENSE_THAT).
     */
    public RobotInfo senseRobotAtLocation(MapLocation loc) throws GameActionException;

    /**
     * Returns true if the given robot is within the robot's sensor range.
     * @param id the ID of the robot to query.
     * @return whether the given robot is within the robot's sensor range.
     */
    public boolean canSenseRobot(int id);

    /**
     * Senses information about a particular robot given its ID.
     * @param id the ID of the robot to query.
     * @return a RobotInfo object for the sensed robot.
     * @throws GameActionException if the robot cannot be sensed (for example, if it doesn't exist or is out of sight range).
     */
    public RobotInfo senseRobot(int id) throws GameActionException;

    /** 
     * Returns all robots that can be sensed on the map.
     * @return array of class type of game objects.
     */
    public RobotInfo[] senseNearbyRobots();

    /**
     * Returns all robots that can be sensed within a certain radius of the robot.
     * @param radiusSquared return objects this distance away from the center.
     * @return array of class type of game objects.
     */
    public RobotInfo[] senseNearbyRobots(int radiusSquared);
    
    /**
     * Returns all robots of a given team that can be sensed within a certain radius of the robot.
     * @param radiusSquared return objects this distance away from the center.
     * @param team filter game objects by the given team. If null is passed, objects from all teams are returned.
     * @return array of class type of game objects.
     */
    public RobotInfo[] senseNearbyRobots(int radiusSquared, Team team);
    
    /**
     * Returns all robots of a givin team that can be sensed within a certain radius of a specified location.
     *
     * @param center center of the given search radius.
     * @param radiusSquared return objects this distance away from the center.
     * @param team filter game objects by the given team. If null is passed, objects from all teams are returned.
     * @return array of class type of game objects.
     */
    public RobotInfo[] senseNearbyRobots(MapLocation center, int radiusSquared, Team team);

    // ***********************************
    // ****** READINESS METHODS **********
    // ***********************************

    /**
     * Returns whether the core delay is strictly less than 1 (whether the robot can perform a core action in the given turn).
     * @return whether the robot can perform a core action in this turn.
     */
    public boolean isCoreReady();

    /**
     * Returns whether the weapon delay is less than 1 (whether the robot can attack in the given turn).
     *
     * @return whether the robot is able to attack in the current turn.
     */
    public boolean isWeaponReady();

    // ***********************************
    // ****** MOVEMENT METHODS ***********
    // ***********************************

    /**
     * Returns whether a robot of the given type can move into the given location, without taking any sort of delays into account. Takes into account only the robot type and the terrain of the location, 
	 * and whether the location is occupied. Does not take into account any sort of core delays. Ignores whether the robot is capable of movement or not.
     * Always returns false for locations out of sight range.
     *
     * @param type the type of the robot.
     * @param loc the location to test.
     * @return true if a robot of the given type can be placed onto the given location on this turn.
     */
    public boolean isPathable(RobotType type, MapLocation loc);

    /**
     * Tells whether this robot can move in the given direction, without taking any sort of delays into account. Takes into
     * account only the map terrain, positions of other robots, and the current robot's type (MISSILE and DRONE can move over VOID).
     * Does not take into account whether this robot is currently active, but will only return true for units that are capable of movement.
     * Returns false for the OMNI and NONE directions.
     *
     * @param dir the direction to move in.
     * @return true if there are no robots or voids preventing this robot from
     *         moving in the given direction; false otherwise.
     */
    public boolean canMove(Direction dir);

    /**
     * Queues a move in the given direction to be performed at the end of this turn.
     *
     * @param dir the direction to move in.
     * @throws GameActionException if the robot cannot move in this direction.
     */
    public void move(Direction dir) throws GameActionException;

    // ***********************************
    // ****** ATTACK METHODS *************
    // ***********************************

    /**
     * Returns whether the given location is within the robot's attack range. Does not take into account whether the robot is currently attacking or has the delay to do so.
     *
     * @param loc the location to attempt to attack.
     * @return true if the given location is within this robot's attack range.
     * Does not take into account whether the robot is currently attacking.
     */
    public boolean canAttackLocation(MapLocation loc);

    /**   
     * Queues an attack on the given location to be performed at the end of this turn.
     *
     * @param loc the location to attack.
     * @throws GameActionException if the robot cannot attack the given square.
     */
    public void attackLocation(MapLocation loc) throws GameActionException;

    /**
     * Attacks all surrounding enemies (MISSILE only). Other robots can call this but will just result in the same result as disintegrate().
     * @throws GameActionException if the robot cannot explode.
     */
    public void explode() throws GameActionException;

    // ***********************************
    // ****** COMMANDER METHODS **********
    // ***********************************

    /**
     * Returns whether the team currently has a commander.
     *
     * @return whether the team has a commander.
     */
    public boolean hasCommander();

    /**
     * Casts Flash at the given location (COMMANDER only).
     *
     * @param loc the target location.
     * @throws GameActionException if the robot has not learned the spell or cannot cast at the given square.
     */
    public void castFlash(MapLocation loc) throws GameActionException;

    /**
     * Returns whether the robot has learned a skill (only relevant if used by a COMMANDER).
     *
     * @param skill the skill being checked.
     * @return whether the robot has that skill.
     * @throws GameActionException if there is no commander.
     */
    public boolean hasLearnedSkill(CommanderSkillType skill) throws GameActionException;

    /**
     * Returns the current cooldown of FLASH (COMMANDER only).
     *
     * @return the cooldown of FLASH.
     * @throws GameActionException if there is no commander.
     */
    public int getFlashCooldown() throws GameActionException;

    // ***********************************
    // ****** BROADCAST METHODS **********
    // ***********************************
    
    /**
     * Broadcasts a message to the global message board.
     *
     * @param channel the channel to write to, from 0 to <code>BROADCAST_MAX_CHANNELS</code>.
     * @param data one int's worth of data to write.
     * @throws GameActionException if the channel is invalid.
     */
    public void broadcast(int channel, int data) throws GameActionException;

    /**
     * Retrieves the message stored at the given radio channel.
     *
     * @param channel radio channel to query, from 0 to <code>BROADCAST_MAX_CHANNELS</code>.
     * @return data currently stored on the channel.
     * @throws GameActionException if the channel is invalid.
     */
    public int readBroadcast(int channel) throws GameActionException;

    // ***********************************
    // ****** SUPPLY METHODS *************
    // ***********************************

    /**
     * Transfers supplies to a robot in a nearby location (queued for the end of the turn). If you specify more supply than the robot has, all its supply will be transferred.
     *
     * @param amount the amount of supply to transfer.
     * @param loc the location to transfer the supply to.
     * @throws GameActionException if there is no one to transfer to, or if the distance is too far for a supply transfer.
     */
    public void transferSupplies(int amount, MapLocation loc) throws GameActionException;

    // ***********************************
    // ****** MINING METHODS *************
    // ***********************************

    /**
     * Returns whether the robot is able to mine, without taking delays into account. This only checks whether the robot is a mining unit. Does not check the core delay.
     * @return whether the robot is able to mine.
     */
    public boolean canMine();

    /**
     * Returns the amount of ore at a given location (to within sensor capabilities). If the location is out of sensor range, this returns the last known ore amount at that location. 
	 * If the location is off the map or is void, then 0 is returned. If the location has never been in sensor range, then -1 is returned.
     *
     * @param loc the MapLocation to sense ore at.
     * @return the amount of ore at a given location. If the location is out of sensor range, then the last known ore amount is returned.
     */
    public double senseOre(MapLocation loc);

    /**
     * Mines the current square for ore.
     *
     * @throws GameActionException if the current robot is not one that can collect ore
     * @throws GameActionException if there is currently movement delay and if the robot cannot mine
     */
    public void mine() throws GameActionException;

    // ***********************************
    // ****** LAUNCHER *******************
    // ***********************************

    /**
     * Returns whether the direction is valid for launching (LAUNCHER only). The location must be on the map and unoccupied, 
	 * and must not have already been launched to during this turn, and the launcher must not have moved already during this turn.
     *
     * @param dir the direction to check.
     * @return whether the direction is valid for launching.
     */
    public boolean canLaunch(Direction dir);

    /**
     * Launches a missile in the given direction (LAUNCHER only). You cannot launch if you have already moved in the given turn.
     *
     * @param dir the direction to launch a missile.
     * @throws GameActionException if not enough missiles or otherwise can't attack.
     */
    public void launchMissile(Direction dir) throws GameActionException;

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************

    /**
     * Gets the current progress of a dependency (relevant for building structures).
     *
     * @param type the dependency to check.
     * @return a DependencyProgress (DONE if the RobotType exists and is active on the map, INPROGRESS if the RobotType is not fully constructed, and NONE otherwise).
     */
    public DependencyProgress checkDependencyProgress(RobotType type);

    /**
     * Checks to make sure you have the ore requirements to spawn, and that the structure can actually spawn the specified RobotType.
     * @param type the type to check.
     * @return whether the spawn requirements are met.
     */
    public boolean hasSpawnRequirements(RobotType type);

    /**
     * Returns whether the spawn action is valid, without taking delays into account. Checks ore requirements, structure types, and that the given direction is not blocked.
     * @param dir the direction to spawn in.
     * @param type the type to spawn.
     * @return whether the spawn is valid.
     */
    public boolean canSpawn(Direction dir, RobotType type);

    /**
     * Queues a spawn action to be performed at the end of this robot's turn.
     * When the action is executed, a new unit will be created adjacent to the calling robot
     * in the given direction. The square must not already be occupied.
     * The new robot is created and starts executing bytecodes immediately.
     *
     * @param dir the direction to spawn the robot in.
     * @param type the robot type to spawn.
     * @throws GameActionException if the spawn is bad.
     */
    public void spawn(Direction dir, RobotType type) throws GameActionException;

    /**
     * Returns whether you have the ore and the dependencies to build the given robot, and that the robot can build structures.
     *
     * @param type the type to build.
     * @return whether the requirements to build are met.
     */
    public boolean hasBuildRequirements(RobotType type);

    /**
     * Returns whether the robot can build a structure of the given type in the given direction, without taking delays into account.
     * Checks dependencies, ore costs, whether the robot can build, and that the given direction is not blocked. Does not check if a robot has sufficiently low coreDelay or not.
     *
     * @param dir the direction to build in.
     * @param type the robot type to spawn.
     * @return whether it is possible to build a building of the given type in the given direction.
     */
    public boolean canBuild(Direction dir, RobotType type);
   
    /**
     * Builds a structure in the given direction, queued for the end of the turn. The structure will initially be inactive for a number of turns (during which this robot cannot move or attack). 
	 * After a number of turns, the structure will become active.
     *
     * @param dir the direction to bulid in.
     * @param type the type to build.
     * @throws GameActionException if the build is bad.
     */
    public void build(Direction dir, RobotType type) throws GameActionException;

    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************
    
    /**
     * Ends the current round.
     * Never fails.
     */
    public void yield();

    /**
     * Kills your robot and ends the current round. Never fails.
     */
    public void disintegrate();

    /**
     * Causes your team to lose the game. It's like typing "gg."
     */
    public void resign();

    // ***********************************
    // ******** MISC. METHODS ************
    // ***********************************

    /**
     * Sets the team's "memory", which is saved for the next game in the
     * match. The memory is an array of {@link GameConstants#TEAM_MEMORY_LENGTH}
     * longs.  If this method is called more than once with the same index
     * in the same game, the last call is what is saved for the
     * next game.
     *
     * @param index the index of the array to set.
     * @param value the data that the team should remember for the next game.
     * @throws java.lang.ArrayIndexOutOfBoundsException
     *          if {@code index} is less
     *          than zero or greater than or equal to {@link GameConstants#TEAM_MEMORY_LENGTH}.
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
     * @param index the index of the array to set.
     * @param value the data that the team should remember for the next game.
     * @param mask  indicates which bits should be set.
     * @throws java.lang.ArrayIndexOutOfBoundsException
     *          if {@code index} is less
     *          than zero or greater than or equal to {@link GameConstants#TEAM_MEMORY_LENGTH}.
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
     * @return the team memory from the the last game of the match.
     * @see #setTeamMemory(int, long)
     * @see #setTeamMemory(int, long, long)
     */
    public long[] getTeamMemory();

    // ***********************************
    // ******** DEBUG METHODS ************
    // ***********************************

    /**
     * Sets one of this robot's 'indicator strings' for debugging purposes.
     * These strings are displayed in the client. This method has no effect on
     * gameplay (aside from the number of bytecodes executed to call this
     * method).
     *
     * @param stringIndex the index of the indicator string to set. Must be between 0 and GameConstants.NUMBER_OF_INDICATOR_STRINGS.
     * @param newString  the value to which the indicator string should be set.
     */
    public void setIndicatorString(int stringIndex, String newString);

    /**
     * Draws a dot on the game map, for debugging purposes.
     * Press V in the client to toggle which team's indicator dots are displayed.
     *
     * @param loc the location to draw the dot.
     * @param red the red component of the dot's color.
     * @param green the green component of the dot's color.
     * @param blue the blue component of the dot's color.
     */
    public void setIndicatorDot(MapLocation loc, int red, int green, int blue);

    /**
     * Draws a line on the game map, for debugging purposes.
     * Press V in the client to toggle which team's indicator lines are displayed.
     *
     * @param from the location to draw the line from.
     * @param to the location to draw the line to.
     * @param red the red component of the line's color.
     * @param green the green component of the line's color.
     * @param blue the blue component of the line's color.
     */
    public void setIndicatorLine(MapLocation from, MapLocation to, int red, int green, int blue);

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
     * @param observation the observation you want to inject into the match file.
     */
    public void addMatchObservation(String observation);

    /**
     * If breakpoints are enabled, calling this method causes the game engine to
     * pause execution at the end of this round, until the user decides to
     * resume execution.
     */
    public void breakpoint();
}
