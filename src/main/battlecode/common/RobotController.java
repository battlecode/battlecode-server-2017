package battlecode.common;

import java.util.Map;

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

    int getRoundLimit();

    int getCurrentRound();

    double getTeamBullets();

    int getTeamVictoryPoints();

    int getOpponentVictoryPoints();

    int getRobotCount();

    int getTreeCount();

    MapLocation[] getInitialArchonLocations(Team t);

    // *********************************
    // ****** UNIT QUERY METHODS *******
    // *********************************

    int getID();

    Team getTeam();

    RobotType getType();

    MapLocation getLocation();

    double getHealth();

    double getCoreDelay();

    double getWeaponDelay();

    // ***********************************
    // ****** GENERAL SENSOR METHODS *****
    // ***********************************

    boolean onTheMap(MapLocation loc);

    boolean canSenseLocation(MapLocation loc);

    boolean isLocationOccupied(MapLocation loc);

    TreeInfo senseTreeAtLocation(MapLocation loc);

    RobotInfo senseRobotAtLocation(MapLocation loc);

    boolean canSenseTree(int id);

    boolean canSenseRobot(int id);

    TreeInfo senseTree(int id);

    RobotInfo senseRobot(int id);

    RobotInfo[] senseNearbyRobots();

    RobotInfo[] senseNearbyRobots(int radius);

    RobotInfo[] senseNearbyRobots(int radius, Team team);

    RobotInfo[] senseNearbyRobots(MapLocation center, int radius, Team team);

    TreeInfo[] senseNearbyTrees();

    TreeInfo[] senseNearbyTrees(int radius);

    TreeInfo[] senseNearbyTrees(int radius, Team team);

    TreeInfo[] senseNearbyTrees(MapLocation center, int radius, Team team);

    BulletInfo[] senseNearbyBullets();

    BulletInfo[] senseNearbyBullets(int radius);

    BulletInfo[] senseNearbyBullets(MapLocation center, int radius);

    RobotInfo[] senseBroadcastingRobots();

    RobotInfo[] senseBroadcastingRobots(Team t);

    RobotInfo[] senseBroadcastingRobotLocations();

    RobotInfo[] senseBroadcastingRobotLocations(Team t);


    // ***********************************
    // ****** READINESS METHODS **********
    // ***********************************

    boolean isCoreReady();

    boolean isWeaponReady();

    // ***********************************
    // ****** MOVEMENT METHODS ***********
    // ***********************************

    boolean canMove(Direction dir);

    boolean canMove(Direction dir, double scale);

    void move(Direction dir);

    void move(Direction dir, double scale);

    // ***********************************
    // ****** ATTACK METHODS *************
    // ***********************************

    void strike();

    void fireSingleShot(Direction dir);

    void fireTriadShot(Direction dir);

    void firePentadShot(Direction dir);

    // ***********************************
    // ****** TREE METHODS ***************
    // ***********************************

    void cut(MapLocation loc);

    void shake(MapLocation loc);

    void water(MapLocation loc);

    boolean canInteractWithTree(MapLocation loc);

    // ***********************************
    // ****** SIGNALING METHODS **********
    // ***********************************

    /**
     * Broadcasts a message to the global message board.
     * The data is not written until the end of the robot's turn.
     * @param channel - the channel to write to, from 0 to <code>BROADCAST_MAX_CHANNELS</code>
     * @param data - one int's worth of data to write
     * @throws GameActionException if the channel is invalid
     */
    public void broadcast(int channel, int data) throws GameActionException;

    /**
     * Retrieves the message stored at the given radio channel.
     * @param channel - radio channel to query, from 0 to <code>BROADCAST_MAX_CHANNELS</code>
     * @return data currently stored on the channel
     * @throws GameActionException  if the channel is invalid
     */
    public int readBroadcast(int channel) throws GameActionException;

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************

    boolean hasBuildRequirements(RobotType type);

    boolean canBuild(RobotType type, Direction dir);

    void hire(Direction dir);

    void plant(RobotType type, Direction dir);

    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************

    void donate(int bullets);

    /**
     * Repairs the robot at the given location. The robot must be within the
     * archons action radius. You can only repair once a turn.
     *
     * @param loc the location of the robot to repair.
     * @throws GameActionException if this robot is not an archon, the location
     * is out of range, if there is no robot there, if the robot is from the
     * wrong team, or if you already repaired this turn.
     *
     * @battlecode.doc.costlymethod
     */
    void repair(MapLocation loc) throws GameActionException;

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
     * @param index the index of the array to set.
     * @param value the data that the team should remember for the next game.
     * @throws java.lang.ArrayIndexOutOfBoundsException if {@code index} is less
     * than zero or greater than or equal to
     * {@link GameConstants#TEAM_MEMORY_LENGTH}.
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
     * @param index the index of the array to set.
     * @param value the data that the team should remember for the next game.
     * @param mask indicates which bits should be set.
     * @throws java.lang.ArrayIndexOutOfBoundsException if {@code index} is less
     * than zero or greater than or equal to
     * {@link GameConstants#TEAM_MEMORY_LENGTH}.
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
     * @param stringIndex the index of the indicator string to set. Must be
     * between 0 and GameConstants.NUMBER_OF_INDICATOR_STRINGS.
     * @param newString the value to which the indicator string should be set.
     *
     * @battlecode.doc.costlymethod
     */
    void setIndicatorString(int stringIndex, String newString);

    /**
     * Draws a dot on the game map, for debugging purposes. Press V in the
     * client to toggle which team's indicator dots are displayed.
     *
     * @param loc the location to draw the dot.
     * @param red the red component of the dot's color.
     * @param green the green component of the dot's color.
     * @param blue the blue component of the dot's color.
     *
     * @battlecode.doc.costlymethod
     */
    void setIndicatorDot(MapLocation loc, int red, int green, int blue);

    /**
     * Draws a line on the game map, for debugging purposes. Press V in the
     * client to toggle which team's indicator lines are displayed.
     *
     * @param from the location to draw the line from.
     * @param to the location to draw the line to.
     * @param red the red component of the line's color.
     * @param green the green component of the line's color.
     * @param blue the blue component of the line's color.
     *
     * @battlecode.doc.costlymethod
     */
    void setIndicatorLine(MapLocation from, MapLocation to, int red,
                          int green, int blue);

    /**
     * Gets this robot's 'control bits' for debugging purposes. These bits can
     * be set manually by the user, so a robot can respond to them. To set these
     * bits, you must run the client in locksteop mode and right click the
     * units.
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
     * @param observation the observation you want to inject into the match
     * file.
     *
     * @battlecode.doc.costlymethod
     */
    void addMatchObservation(String observation);

}
