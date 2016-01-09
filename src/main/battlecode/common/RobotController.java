package battlecode.common;

import java.util.HashMap;

import battlecode.world.InternalRobot;

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
     * team has been destroyed, then the tiebreakers will be used.
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
     * Returns the current round number, where round 0 is the first round of the match.
     * @return the current round number, where 0 is the first round of the match.
     *
     * @battlecode.doc.costlymethod
     */
    int getRoundNum();

    /**
     * Returns a copy of the zombie spawn schedule for the game. Any attempts
     * to modify this zombie spawn schedule will not change the actual
     * schedule for the game.
     *
     * @return a copy of the zombie spawn schedule for the game.
     * @battlecode.doc.costlymethod
     */
    ZombieSpawnSchedule getZombieSpawnSchedule();

    /**
     * Returns the number of robots on your team, including your archons.
     *
     * @return the number of robots on your team, including your archon.s
     * @battlecode.doc.costlymethod
     */
    int getRobotCount();

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
     * @return this robot's Team.
     *
     * @battlecode.doc.costlymethod
     */
    Team getTeam();

    /**
     * Gets this robot's type (SOLDIER, ARCHON, etc.).
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
     * Core actions include building, activating, clearing rubble, and moving.
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
    
    /**
     * Gets the number of turns the robot will remain infected. If the robot dies
     * while this value is greater than zero, it will turn into a zombie. This is
     * the same thing as max(zombieInfectedTurns,viperInfectedTurns).
     * 
     * @return number of remaining infected turns.
     *
     * @battlecode.doc.costlymethod
     */
    int getInfectedTurns();
    
    /**
     * Gets the number of turns the robot will remain infected from a zombie's attack.
     * Unlike viperInfectedTurns, the unit WILL NOT take damage during these turns. The
     * unit will turn into a zombie if it dies while infected.
     * 
     * @return number of remaining zombie infected turns.
     *
     * @battlecode.doc.costlymethod
     */
    int getZombieInfectedTurns();
    
    /**
     * Gets the number of turns the robot will remain infected from a viper's attack.
     * Unlike zombieInfectedTurns, the unit WILL take damage during these turns. The
     * unit will turn into a zombie if it dies while infected.
     * 
     * @return number of remaining viper infected turns.
     *
     * @battlecode.doc.costlymethod
     */
    int getViperInfectedTurns();
    
    /**
     * Returns true if the robot is infected (either from a viper or a zombie). If the
     * robot dies while this is true, it will become a zombie.
     * 
     * @return true if the robot is infected.
     *
     * @battlecode.doc.costlymethod
     */
    boolean isInfected();

    /**
     * Returns the number of basic signals this robot has sent so far this turn.
     *
     * @return the number of basic signals this robot has sent so far this turn.
     *
     * @battlecode.doc.costlymethod
     */
    int getBasicSignalCount();

    /**
     * Returns the number of message signals this robot has sent so far this
     * turn.
     *
     * @return the number of message signals this robot has sent so far this
     * turn.
     *
     * @battlecode.doc.costlymethod
     */
    int getMessageSignalCount();

    // ***********************************
    // ****** GENERAL SENSOR METHODS *****
    // ***********************************

    /**
     * Determine if our robot can sense a location.
     *
     * @param loc the location to test.
     * @return whether it can sense the location.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSense(MapLocation loc);

    /**
     * Senses whether a MapLocation is on the map. Will throw an exception if
     * the location is not currently within sensor range.
     *
     * @param loc the location to check.
     * @return true if the location is on the map, and false if it is not.
     * @throws GameActionException if the location is not within sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    boolean onTheMap(MapLocation loc) throws GameActionException;

    /**
     * Senses the rubble at the given location. Returns -1 for a location
     * outside sensor range. Returns 0 for off map locations. If a location
     * is both outside sensor range and off map, -1 will be returned.
     *
     * @param loc the location to check.
     * @return the amount of rubble at the location.
     *
     * @battlecode.doc.costlymethod
     */
    double senseRubble(MapLocation loc);
    
    /**
     * Senses the parts at the given location. Returns -1 for a location
     * outside sensor range. Returns 0 for off map locations. If a location
     * is both outside sensor range and off map, -1 will be returned.
     *
     * @param loc the location to check.
     * @return the amount of parts at the location.
     *
     * @battlecode.doc.costlymethod
     */
    double senseParts(MapLocation loc);
    
    /**
     * Senses nearby MapLocations with nonzero parts within a certain radius.
     * Only MapLocations within the robot's sense radius will be returned.
     *
     * 
     * @param radiussquared
     *            return part locations this distance away from robot's location.
     *            If -1 is passed, locations from the robot's entire sensor radius
     *            are returned.
     * @return list of MapLocations that contain parts
     * 
     * @battlecode.doc.costlymethod
     */
    MapLocation[] sensePartLocations(int radiussquared);

    /**
     * Returns true if the given location is within the robot's sensor range.
     * This is the same as canSense().
     *
     * @param loc the location to check.
     * @return whether the given location is within the robot's sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSenseLocation(MapLocation loc);

    /**
     * Returns whether there is a robot (includes dens) at the given
     * location.
     *
     * @param loc the location to check.
     * @return whether there is a robot at the given location.
     * @throws GameActionException if the location is not within sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    boolean isLocationOccupied(MapLocation loc) throws GameActionException;

    /**
     * Returns the robot at the given location, or null if there is
     * no object there.
     *
     * @param loc the location to check.
     * @return the robot at the given location.
     * @throws GameActionException if the location is not within sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo senseRobotAtLocation(MapLocation loc)
            throws GameActionException;

    /**
     * Returns true if the given robot is within the robot's sensor range.
     * 
     * @param id the ID of the robot to query.
     * @return whether the given robot is within the robot's sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSenseRobot(int id);

    /**
     * Senses information about a particular robot given its ID.
     * 
     * @param id the ID of the robot to query.
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
     * @return array of RobotInfo objects, which contain information about
     * all the robots you sensed.
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
     * @return array of RobotInfo objects of all the robots you sensed.
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
     * @return array of RobotInfo objects of all the robots you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(int radiusSquared, Team team);

    /**
     * Returns all robots of a given team that can be sensed within a certain
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
     * @return array of RobotInfo objects of the robots you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(MapLocation center, int radiusSquared,
            Team team);
    
    /**
     * Returns all hostile (zombie or enemy team) robots that can be sensed
     * within a certain radius of a specified location.
     * 
     * @param center
     *            center of the given search radius.
     * @param radiusSquared
     *            return objects this distance away from the center. If -1 is
     *            passed, robots from the whole map are returned.
     * @return array of RobotInfo objects of the robots you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseHostileRobots(MapLocation center, int radiusSquared);

    // ***********************************
    // ****** READINESS METHODS **********
    // ***********************************

    /**
     * Returns whether the core delay is strictly less than 1 (whether the robot
     * can perform a core action in the given turn). If this is true, then
     * you can perform core actions, such as clearing rubble, moving,
     * activating, and building.
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
    // ****** RUBBLE METHODS *************
    // ***********************************

    /**
     * Clears rubble in the specified direction. If you clear a direction
     * that is off the map, nothing happens.
     *
     * @param dir
     *            the direction to clear rubble in.
     * @throws GameActionException if the robot has core delay or if you are
     * not allowed to clear rubble.
     *
     * @battlecode.doc.costlymethod
     */
    void clearRubble(Direction dir) throws GameActionException;

    // ***********************************
    // ****** MOVEMENT METHODS ***********
    // ***********************************

    /**
     * Tells whether this robot can move in the given direction, without taking
     * any sort of delays into account. Takes into account only the map
     * terrain (rubble), positions of other robots, and the current robot's type.
     * Does not take into account whether this robot is currently active (no
     * core delay), but will only return true for units that are capable of
     * movement. Returns false for the OMNI and NONE directions.
     *
     * @param dir
     *            the direction to move in.
     * @return true if there is nothing preventing this robot from moving in
     * the given direction; false otherwise (does not account for core delay).
     *
     * @battlecode.doc.costlymethod
     */
    boolean canMove(Direction dir);

    /**
     * Moves in the given direction.
     *
     * @param dir
     *            the direction to move in.
     * @throws GameActionException
     *             if the robot cannot move in this direction, such as due to
     *             having core delay, the target location being off the map,
     *             the robot not being one that is allowed to move, and the
     *             target destination being occupied with either another
     *             robot or rubble.
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
     *         attacking or if there is a unit on the target location.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canAttackLocation(MapLocation loc);

    /**
     * Attacks the given location. If the location is empty, nothing will
     * happen.
     *
     * @param loc
     *            the location to attack.
     * @throws GameActionException
     *             if the robot cannot attack the given square due to having
     *             weapon delay or the location being outside your attack range.
     *
     * @battlecode.doc.costlymethod
     */
    void attackLocation(MapLocation loc) throws GameActionException;

    // ***********************************
    // ****** BROADCAST METHODS **********
    // ***********************************

    /**
     * Retrieve the next message waiting in your incoming message queue. Also
     * removes the message from the queue.
     *
     * @return next Signal object in your queue, or null if your queue is
     *         empty.
     *
     * @battlecode.doc.costlymethod
     */
    Signal readSignal();
    
    /**
     * Retrieves an array of all the messages in your incoming message queue.
     * All messages will be removed from the queue. If there are no messages in
     * the queue, this method returns a zero-length array.
     *
     * @return all the Signals in your message queue
     *
     * @battlecode.doc.costlymethod
     */
    Signal[] emptySignalQueue();
    
    /**
     * Broadcasts a regular signal over a specific integer radius. The signal is
     * immediately added to the incoming message queues of all robots in
     * your broadcast range (except for the sending robot).
     *
     * @param radiusSquared the square of the radius over which the signal is
     *                      broadcasted.
     * @throws GameActionException if radius is negative.
     *
     * @battlecode.doc.costlymethod
     */
    void broadcastSignal(int radiusSquared)  throws GameActionException;
    
    /**
     * Broadcasts a message signal over a specific integer radius. The signal is
     * immediately added to the incoming message queues of all robots in
     * your broadcast range (except for the sending robot).
     *
     * @param message1 the first integer to broadcast.
     * @param message2 the second integer to broadcast.
     * @param radiusSquared the square of the radius over which the signal is
     *                      broadcasted.
     * @throws GameActionException if radius is negative or this robot 
     *      cannot send message signals.
     *
     * @battlecode.doc.costlymethod
     */
    void broadcastMessageSignal(int message1, int message2, int radiusSquared)
            throws GameActionException;

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************

    /**
     * Returns whether you have the parts and dependencies to build the given
     * robot, and this robot is a valid builder for the target robot.
     *
     * @param type
     *            the type to build.
     * @return whether the requirements to build are met.
     *
     * @battlecode.doc.costlymethod
     */
    boolean hasBuildRequirements(RobotType type);

    /**
     * Returns whether the robot can build a robot of the given type in the
     * given direction, without taking delays into account. Checks dependencies,
     * parts costs, whether the robot can build, and that the given direction is
     * not blocked. Does not check if a robot has sufficiently low coreDelay or
     * not.
     *
     * @param dir
     *            the direction to build in.
     * @param type
     *            the robot type to build.
     * @return whether it is possible to build a robot of the given type in
     *         the given direction.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canBuild(Direction dir, RobotType type);

    /**
     * Builds a robot in the given direction. The robot will initially
     * be inactive for a number of turns (during which this robot cannot move or
     * attack). After a number of turns, the robot will become active.
     *
     * @param dir
     *            the direction to build in.
     * @param type
     *            the type to build.
     * @throws GameActionException
     *             if the build is bad: if your robot cannot build, if you
     *             have coreDelay, if the direction is not a good build
     *             direction, or if the type you're building cannot be built
     *             by you.
     *
     * @battlecode.doc.costlymethod
     */
    void build(Direction dir, RobotType type) throws GameActionException;

    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************

    /**
     * Activates the neutral robot at the given location, converting it to a robot
     * of the same type but on your team. The robot will have a new ID. This
     * method increases your core as if it were a movement action, but does
     * not affect
     *
     * @param loc the location of the robot to activate.
     * @throws GameActionException if the location is out of range (needs to
     * be adjacent), if there is no robot there, if the robot is not a
     * neutral robot, or if you have core delay.
     *
     * @battlecode.doc.costlymethod
     */
    void activate(MapLocation loc) throws GameActionException;

    /**
     * Repairs the robot at the given location. The robot must be in attack
     * range. You can only repair once a turn.
     *
     * @param loc the location of the robot to repair.
     * @throws GameActionException if this robot is not an archon, the location 
     *  is out of range, if there is no robot there, if the robot is from the 
     *  wrong team, or if you already repaired this turn.
     *
     * @battlecode.doc.costlymethod
     */
    void repair(MapLocation loc) throws GameActionException;

    /**
     * Turret only. Transforms the turret into a TTM. Will increase both your
     * delays. It's okay to use this if you have delays though.
     *
     * @throws GameActionException if this robot is not a Turret.
     *
     * @battlecode.doc.costlymethod
     */
    void pack() throws GameActionException;

    /**
     * TTM only. Transforms the TTM into a turret. Will increase both your
     * delays. It's okay to use this if you have delays though.
     *
     * @throws GameActionException if this robot is not a TTM.
     *
     * @battlecode.doc.costlymethod
     */
    void unpack() throws GameActionException;

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

    // ***********************************
    // ******** TEAM MEMORY **************
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
     * be set manually by the user, so a robot can respond to them. To set
     * these bits, you must run the client in locksteop mode and right click
     * the units.
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
}
