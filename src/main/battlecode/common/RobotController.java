package battlecode.common;

/**
 * A RobotController allows contestants to make their robot sense and interact
 * with the game world. When a contestant's <code>RobotPlayer</code> is
 * constructed, it is passed an instance of <code>RobotController</code> that
 * controls the newly created robot.
 */
@SuppressWarnings("unused")
public interface RobotController {

    // *********************************
    // ****** GLOBAL QUERY METHODS *****
    // *********************************

    /**
     * Gets the number of rounds in the game. After this many rounds, if neither
     * team has destroyed the enemy HQ, then the tiebreakers will be used.
     *
     * @return the number of rounds in the game.
     *
     * @battlecode.doc.costlymethod
     */
    int getRoundLimit();

    /**
     * Gets the team's total parts.
     *
     * @return the team's total parts.
     *
     * @battlecode.doc.costlymethod
     */
    double getTeamParts();

    /**
     * Returns the zombie spawn schedule for a given round on the map. Only
     * works on zombie dens. NOT AVAILABLE TO COMPETITORS?
     *
     * @param round the round number
     * @return the zombie spawn schedule.
     *
     * @battlecode.doc.costlymethod
     */
    ZombieCount[] getZombieSpawnSchedule(int round);

    // *********************************
    // ****** UNIT QUERY METHODS *******
    // *********************************

    /**
     * Use this method to access your ID.
     *
     * @return the ID of the robot.
     *
     * @battlecode.doc.costlymethod
     */
    int getID();

    /**
     * Gets the Team of this robot.
     *
     * @return this robot's Team
     *
     * @battlecode.doc.costlymethod
     */
    Team getTeam();

    /**
     * Gets this robot's type (SOLDIER, HQ, etc.).
     *
     * @return this robot's type.
     *
     * @battlecode.doc.costlymethod
     */
    RobotType getType();

    /**
     * Gets the robot's current location.
     *
     * @return this robot's current location.
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation getLocation();

    /**
     * Returns the amount of core delay a robot has accumulated. If the result
     * is strictly less than 1, then the robot can perform a core action.
     *
     * @return the amount of core delay a robot has accumulated.
     *
     * @battlecode.doc.costlymethod
     */
    double getCoreDelay();

    /**
     * Returns the amount of weapon delay a robot has accumulated. If the result
     * is strictly less than 1, then the robot can attack.
     *
     * @return the number of weapon delay a robot has accumulated.
     *
     * @battlecode.doc.costlymethod
     */
    double getWeaponDelay();

    /**
     * Gets the robot's current health.
     *
     * @return this robot's current health.
     *
     * @battlecode.doc.costlymethod
     */
    double getHealth();

    // ***********************************
    // ****** GENERAL SENSOR METHODS *****
    // ***********************************

    // TODO: seems weird that onTheMap throws GameActionExceptions for things
    // that can't be sensed while senseRubble and senseParts return -1 for
    // thosee

    /**
     * Senses whether a MapLocation is on the map. Will throw an exception if
     * the location is not currently and has never been within sensor range.
     *
     * @param loc the location to check.
     * @return true if the location is on the map, and false if it is not.
     * @throws GameActionException if the location has never been within
     * sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    boolean onTheMap(MapLocation loc) throws GameActionException;

    /**
     * Senses the rubble at the given location. Returns -1 for a location
     * outside sensor range. Returns 0 for off map locations.
     *
     * @param loc
     *            the location to check.
     * @return the amount of rubble at the location
     *
     * @battlecode.doc.costlymethod
     */
    double senseRubble(MapLocation loc);
    
    /**
     * Senses the parts at the given location. Returns -1 for a location
     * outside sensor range. Returns 0 for off map locations.
     *
     * @param loc
     *            the location to check.
     * @return the amount of parts at the location
     *
     * @battlecode.doc.costlymethod
     */
    double senseParts(MapLocation loc);

    /**
     * Returns true if the given location is within the robot's sensor range, or
     * within the sensor range of some ally.
     *
     * @param loc
     *            the location to check.
     * @return whether the given location is within the robot's sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSenseLocation(MapLocation loc);

    /**
     * Returns whether there is a robot at the given location.
     *
     * @param loc
     *            the location to check.
     * @return whether there is a robot at the given location.
     * @throws GameActionException
     *             if <code>loc</code> is not within sensor range
     *             (CANT_SENSE_THAT).
     *
     * @battlecode.doc.costlymethod
     */
    boolean isLocationOccupied(MapLocation loc) throws GameActionException;

    /**
     * Returns the robot at the given location, or <code>null</code> if there is
     * no object there.
     *
     * @param loc
     *            the location to check.
     * @return the robot at the given location.
     * @throws GameActionException
     *             if <code>loc</code> is not within sensor range
     *             (CANT_SENSE_THAT).
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo senseRobotAtLocation(MapLocation loc)
            throws GameActionException;

    /**
     * Returns true if the given robot is within the robot's sensor range.
     * 
     * @param id
     *            the ID of the robot to query.
     * @return whether the given robot is within the robot's sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSenseRobot(int id);

    /**
     * Senses information about a particular robot given its ID.
     * 
     * @param id
     *            the ID of the robot to query.
     * @return a RobotInfo object for the sensed robot.
     * @throws GameActionException
     *             if the robot cannot be sensed (for example, if it doesn't
     *             exist or is out of sight range).
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo senseRobot(int id) throws GameActionException;

    /**
     * Returns all robots that can be sensed on the map.
     * 
     * @return array of class type of game objects.
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots();

    /**
     * Returns all robots that can be sensed within a certain radius of the
     * robot.
     * 
     * @param radiusSquared
     *            return objects this distance away from the center. If -1 is
     *            passed, robots from the whole map are returned.
     * @return array of class type of game objects.
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(int radiusSquared);

    /**
     * Returns all robots of a given team that can be sensed within a certain
     * radius of the robot.
     * 
     * @param radiusSquared
     *            return objects this distance away from the center. If -1 is
     *            passed, robots from the whole map are returned.
     * @param team
     *            filter game objects by the given team. If null is passed, robots from
     *            any team are returned.
     * @return array of class type of game objects.
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(int radiusSquared, Team team);

    /**
     * Returns all robots of a givin team that can be sensed within a certain
     * radius of a specified location.
     *
     * @param center
     *            center of the given search radius.
     * @param radiusSquared
     *            return objects this distance away from the center. If -1 is
     *            passed, robots from the whole map are returned.
     * @param team
     *            filter game objects by the given team. If null is passed,
     *            objects from all teams are returned.
     * @return array of class type of game objects.
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(MapLocation center, int radiusSquared,
            Team team);

    // ***********************************
    // ****** READINESS METHODS **********
    // ***********************************

    /**
     * Returns whether the core delay is strictly less than 1 (whether the robot
     * can perform a core action in the given turn).
     * 
     * @return whether the robot can perform a core action in this turn.
     *
     * @battlecode.doc.costlymethod
     */
    boolean isCoreReady();

    /**
     * Returns whether the weapon delay is less than 1 (whether the robot can
     * attack in the given turn).
     *
     * @return whether the robot is able to attack in the current turn.
     *
     * @battlecode.doc.costlymethod
     */
    boolean isWeaponReady();

    // ***********************************
    // ****** REPAIR METHODS *************
    // ***********************************

    /**
     * Repairs the robot at the given location. The robot must be in attack
     * range.
     *
     * @param loc the location of the robot to repair.
     * @throws GameActionException if the location is out of range, if there
     *  is no robot there, if the robot is from the wrong team, or if you
     *  already repaired this turn.
     */
    void repair(MapLocation loc) throws GameActionException;

    // ***********************************
    // ****** RUBBLE METHODS *************
    // ***********************************

    /**
     * Queues a rubble clear in the given direction to be executed at the end
     * of the turn.
     *
     * @param dir
     *            the direction to clear rubble in.
     * @throws GameActionException
     *             if the robot cannot move in this direction.
     *
     * @battlecode.doc.costlymethod
     */
    void clearRubble(Direction dir) throws GameActionException;

    // ***********************************
    // ****** MOVEMENT METHODS ***********
    // ***********************************

    /**
     * Tells whether this robot can move in the given direction, without taking
     * any sort of delays into account. Takes into account only the map terrain,
     * positions of other robots, and the current robot's type. Does not take
     * into account whether this robot is currently active (no core delay), but
     * will only * return true for units that are capable of movement.
     * Returns false for the OMNI and NONE directions.
     *
     * @param dir
     *            the direction to move in.
     * @return true if there are no robots or voids preventing this robot from
     *         moving in the given direction; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canMove(Direction dir);

    /**
     * Queues a move in the given direction to be performed at the end of this
     * turn.
     *
     * @param dir
     *            the direction to move in.
     * @throws GameActionException
     *             if the robot cannot move in this direction.
     *
     * @battlecode.doc.costlymethod
     */
    void move(Direction dir) throws GameActionException;

    // ***********************************
    // ****** ATTACK METHODS *************
    // ***********************************

    /**
     * Returns whether the given location is within the robot's attack range.
     * Does not take into account whether the robot is currently attacking or
     * has the delay to do so.
     *
     * @param loc
     *            the location to attempt to attack.
     * @return true if the given location is within this robot's attack range.
     *         Does not take into account whether the robot is currently
     *         attacking.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canAttackLocation(MapLocation loc);

    /**
     * Queues an attack on the given location to be performed at the end of this
     * turn.
     *
     * @param loc
     *            the location to attack.
     * @throws GameActionException
     *             if the robot cannot attack the given square.
     *
     * @battlecode.doc.costlymethod
     */
    void attackLocation(MapLocation loc) throws GameActionException;

    // ***********************************
    // ****** BROADCAST METHODS **********
    // ***********************************

    /**
     * Broadcasts a message to the global message board.
     *
     * @param channel
     *            the channel to write to, from 0 to
     *            <code>BROADCAST_MAX_CHANNELS</code>.
     * @param data
     *            one int's worth of data to write.
     * @throws GameActionException
     *             if the channel is invalid.
     *
     * @battlecode.doc.costlymethod
     */
    void broadcast(int channel, int data) throws GameActionException;

    /**
     * Retrieves the message stored at the given radio channel.
     *
     * @param channel
     *            radio channel to query, from 0 to
     *            <code>BROADCAST_MAX_CHANNELS</code>.
     * @return data currently stored on the channel.
     * @throws GameActionException
     *             if the channel is invalid.
     *
     * @battlecode.doc.costlymethod
     */
    int readBroadcast(int channel) throws GameActionException;

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************

    /**
     * Returns whether you have the parts and the dependencies to build the given
     * robot, and that the robot can build structures.
     *
     * @param type
     *            the type to build.
     * @return whether the requirements to build are met.
     *
     * @battlecode.doc.costlymethod
     */
    boolean hasBuildRequirements(RobotType type);

    /**
     * Returns whether the robot can build a structure of the given type in the
     * given direction, without taking delays into account. Checks dependencies,
     * parts costs, whether the robot can build, and that the given direction is
     * not blocked. Does not check if a robot has sufficiently low coreDelay or
     * not.
     *
     * @param dir
     *            the direction to build in.
     * @param type
     *            the robot type to spawn.
     * @return whether it is possible to build a building of the given type in
     *         the given direction.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canBuild(Direction dir, RobotType type);

    /**
     * Builds a structure in the given direction, queued for the end of the
     * turn. The structure will initially be inactive for a number of turns
     * (during which this robot cannot move or attack). After a number of turns,
     * the structure will become active.
     *
     * @param dir
     *            the direction to bulid in.
     * @param type
     *            the type to build.
     * @throws GameActionException
     *             if the build is bad.
     *
     * @battlecode.doc.costlymethod
     */
    void build(Direction dir, RobotType type) throws GameActionException;

    /**
     * If you are a ZOMBIEDEN, this method inflicts 10 damage to all surrounding
     * player-controlled units. It is called whenever the ZOMBIEDEN can't spawn
     * a new zombie because it is completely surrounded.
     */
    void spawnFail() throws GameActionException;
    
    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************

    /**
     * Kills your robot and ends the current round. Never fails.
     *
     * @battlecode.doc.costlymethod
     */
    void disintegrate();

    /**
     * Causes your team to lose the game. It's like typing "gg."
     *
     * @battlecode.doc.costlymethod
     */
    void resign();

    /**
     * Turret only. Transforms the turret into a TTM after a short delay.
     *
     * @throws GameActionException if this robot cannot pack
     *
     * @battlecode.doc.costlymethod
     */
    void pack() throws GameActionException;

    /**
     * TTM only. Transforms the TTM into a turret after a short delay.
     *
     * @throws GameActionException if this robot cannot unpack
     *
     * @battlecode.doc.costlymethod
     */
    void unpack() throws GameActionException;

    // ***********************************
    // ******** MISC. METHODS ************
    // ***********************************

    /**
     * Sets the team's "memory", which is saved for the next game in the match.
     * The memory is an array of {@link GameConstants#TEAM_MEMORY_LENGTH} longs.
     * If this method is called more than once with the same index in the same
     * game, the last call is what is saved for the next game.
     *
     * @param index
     *            the index of the array to set.
     * @param value
     *            the data that the team should remember for the next game.
     * @throws java.lang.ArrayIndexOutOfBoundsException
     *             if {@code index} is less than zero or greater than or equal
     *             to {@link GameConstants#TEAM_MEMORY_LENGTH}.
     * @see #getTeamMemory
     * @see #setTeamMemory(int, long, long)
     *
     * @battlecode.doc.costlymethod
     */
    void setTeamMemory(int index, long value);

    /**
     * Sets this team's "memory". This function allows for finer control than
     * {@link #setTeamMemory(int, long)} provides. For example, if
     * {@code mask == 0xFF} then only the eight least significant bits of the
     * memory will be set.
     *
     * @param index
     *            the index of the array to set.
     * @param value
     *            the data that the team should remember for the next game.
     * @param mask
     *            indicates which bits should be set.
     * @throws java.lang.ArrayIndexOutOfBoundsException
     *             if {@code index} is less than zero or greater than or equal
     *             to {@link GameConstants#TEAM_MEMORY_LENGTH}.
     * @see #getTeamMemory
     * @see #setTeamMemory(int, long)
     *
     * @battlecode.doc.costlymethod
     */
    void setTeamMemory(int index, long value, long mask);

    /**
     * Returns the team memory from the last game of the match. The return value
     * is an array of length {@link GameConstants#TEAM_MEMORY_LENGTH}. If
     * setTeamMemory was not called in the last game, or there was no last game,
     * the corresponding long defaults to 0.
     *
     * @return the team memory from the the last game of the match.
     * @see #setTeamMemory(int, long)
     * @see #setTeamMemory(int, long, long)
     *
     * @battlecode.doc.costlymethod
     */
    long[] getTeamMemory();

    // ***********************************
    // ******** DEBUG METHODS ************
    // ***********************************

    /**
     * Sets one of this robot's 'indicator strings' for debugging purposes.
     * These strings are displayed in the client. This method has no effect on
     * gameplay (aside from the number of bytecodes executed to call this
     * method).
     *
     * @param stringIndex
     *            the index of the indicator string to set. Must be between 0
     *            and GameConstants.NUMBER_OF_INDICATOR_STRINGS.
     * @param newString
     *            the value to which the indicator string should be set.
     *
     * @battlecode.doc.costlymethod
     */
    void setIndicatorString(int stringIndex, String newString);

    /**
     * Draws a dot on the game map, for debugging purposes. Press V in the
     * client to toggle which team's indicator dots are displayed.
     *
     * @param loc
     *            the location to draw the dot.
     * @param red
     *            the red component of the dot's color.
     * @param green
     *            the green component of the dot's color.
     * @param blue
     *            the blue component of the dot's color.
     *
     * @battlecode.doc.costlymethod
     */
    void setIndicatorDot(MapLocation loc, int red, int green, int blue);

    /**
     * Draws a line on the game map, for debugging purposes. Press V in the
     * client to toggle which team's indicator lines are displayed.
     *
     * @param from
     *            the location to draw the line from.
     * @param to
     *            the location to draw the line to.
     * @param red
     *            the red component of the line's color.
     * @param green
     *            the green component of the line's color.
     * @param blue
     *            the blue component of the line's color.
     *
     * @battlecode.doc.costlymethod
     */
    void setIndicatorLine(MapLocation from, MapLocation to, int red,
            int green, int blue);

    /**
     * Gets this robot's 'control bits' for debugging purposes. These bits can
     * be set manually by the user, so a robot can respond to them.
     *
     * @return this robot's control bits
     *
     * @battlecode.doc.costlymethod
     */
    long getControlBits();

    /**
     * Adds a custom observation to the match file, such that when it is
     * analyzed, this observation will appear.
     *
     * @param observation
     *            the observation you want to inject into the match file.
     *
     * @battlecode.doc.costlymethod
     */
    void addMatchObservation(String observation);

    /**
     * Returns the current round number, where round 0 is the first round of the match.
     * @return the current round number, where 0 is the first round of the match.
     *
     * @battlecode.doc.costlymethod
     */
    int getRoundNum();
}
