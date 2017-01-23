package battlecode.common;

/**
 * A RobotController allows contestants to make their robot sense and interact
 * with the game world. When a contestant's <code>RobotPlayer</code> is
 * constructed, it is passed an instance of <code>RobotController</code> that
 * controls the newly created robot.
 */
@SuppressWarnings("unused")
public strictfp interface RobotController {

    // *********************************
    // ****** GLOBAL QUERY METHODS *****
    // *********************************

    /**
     * Returns the number of rounds in the game. After this many rounds, if neither
     * team has been destroyed, tiebreakers will be used.
     *
     * @return the number of rounds in the game.
     *
     * @battlecode.doc.costlymethod
     */
    int getRoundLimit();

    /**
     * Returns the current round number, where round 0 is the first round of the
     * match.
     *
     * @return the current round number, where round 0 is the first round of the
     * match.
     *
     * @battlecode.doc.costlymethod
     */
    int getRoundNum();

    /**
     * Returns the team's total bullet supply.
     *
     * @return the team's total bullet supply.
     *
     * @battlecode.doc.costlymethod
     */
    float getTeamBullets();

    /**
     * Returns the team's total victory points.
     *
     * @return the team's total victory points.
     *
     * @battlecode.doc.costlymethod
     */
    int getTeamVictoryPoints();

    /**
     * Returns your opponent's total victory points.
     *
     * @return your opponent's total victory points.
     *
     * @battlecode.doc.costlymethod
     */
    int getOpponentVictoryPoints();

    /**
     * Returns the number of robots on your team, including your archons.
     * If this number ever reaches zero, the opposing team will automatically
     * win by destruction.
     *
     * @return the number of robots on your team, including your archons.
     *
     * @battlecode.doc.costlymethod
     */
    int getRobotCount();

    /**
     * Returns the number of trees on your team.
     *
     * @return the number of trees on your team.
     *
     * @battlecode.doc.costlymethod
     */
    int getTreeCount();

    /**
     * Returns a list of the INITIAL locations of the archons of a particular
     * team. The locations will be sorted by increasing x, with ties broken by
     * increasing y. Will return an empty list if you query for {@code Team.NEUTRAL}.
     *
     * @param t the team for which you want to query the initial archon
     * locations. Will return an empty list if you query for Team.NEUTRAL
     * @return a list of the INITIAL locations of the archons of that team, or
     * an empty list for Team.NEUTRAL.
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] getInitialArchonLocations(Team t);

    // *********************************
    // ****** UNIT QUERY METHODS *******
    // *********************************

    /**
     * Returns the ID of this robot.
     *
     * @return the ID of this robot.
     *
     * @battlecode.doc.costlymethod
     */
    int getID();

    /**
     * Returns this robot's Team.
     *
     * @return this robot's Team.
     *
     * @battlecode.doc.costlymethod
     */
    Team getTeam();

    /**
     * Returns this robot's type (SOLDIER, ARCHON, etc.).
     *
     * @return this robot's type.
     *
     * @battlecode.doc.costlymethod
     */
    RobotType getType();

    /**
     * Returns this robot's current location.
     *
     * @return this robot's current location.
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation getLocation();

    /**
     * Returns this robot's current health.
     *
     * @return this robot's current health.
     *
     * @battlecode.doc.costlymethod
     */
    float getHealth();
    
    /**
     * Returns the number of times the robot has attacked this turn.
     * 
     * @return the number of times the robot has attacked this turn.
     *
     * @battlecode.doc.costlymethod
     */
    int getAttackCount();
    
    /**
     * Returns the number of times the robot has moved this turn.
     * 
     * @return the number of times the robot has moved this turn.
     *
     * @battlecode.doc.costlymethod
     */
    int getMoveCount();

    // ***********************************
    // ****** GENERAL SENSOR METHODS *****
    // ***********************************

    /**
     * Senses whether a MapLocation is on the map. Will throw an exception if
     * the location is not currently within sensor range.
     *
     * @param loc the location to check
     * @return true if the location is on the map; false otherwise.
     * @throws GameActionException if the location is not within sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    boolean onTheMap(MapLocation loc) throws GameActionException;

    /**
     * Senses whether a given circle is completely on the map. Will throw an exception if
     * the circle is not completely within sensor range.
     *
     * @param center the center of the circle to check
     * @param radius the radius of the circle to check
     * @return true if the circle is completely on the map; false otherwise.
     * @throws GameActionException if any portion of the given circle is not within sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    boolean onTheMap(MapLocation center, float radius) throws GameActionException;

    /**
     * Senses whether the given location is within the robot's bullet sense range.
     *
     * @param loc the location to check
     * @return true if the given location is within the robot's bullet sense range, false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSenseBulletLocation(MapLocation loc);

    /**
     * Senses whether the given location is within the robot's sensor range.
     *
     * @param loc the location to check
     * @return true if the given location is within the robot's sensor range; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSenseLocation(MapLocation loc);

    /**
     * Senses whether a point at the given radius is within the robot's sensor range.
     *
     * @param radius the radius to check
     * @return true if the given location is within the robot's sensor range; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSenseRadius(float radius);

    /**
     * Senses whether any portion of the given circle is within the robot's sensor range.
     *
     * @param center the center of the circle to check
     * @param radius the radius of the circle to check
     * @return true if a portion of the circle is within the robot's sensor range; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSensePartOfCircle(MapLocation center, float radius);

    /**
     * Senses whether all of the given circle is within the robot's sensor range.
     *
     * @param center the center of the circle to check
     * @param radius the radius of the circle to check
     * @return true if all of the circle is within the robot's sensor range; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSenseAllOfCircle(MapLocation center, float radius);

    /**
     * Senses whether there is a robot or tree at the given location.
     *
     * @param loc the location to check
     * @return true if there is a robot or tree at the given location; false otherwise.
     * @throws GameActionException if the location is not within sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    boolean isLocationOccupied(MapLocation loc) throws GameActionException;

    /**
     * Senses whether there is a tree at the given location.
     *
     * @param loc the location to check.
     * @return true if there is a tree at the given location; false otherwise.
     * @throws GameActionException if the location is not within sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    boolean isLocationOccupiedByTree(MapLocation loc) throws GameActionException;

    /**
     * Senses whether there is a robot at the given location.
     *
     * @param loc the location to check
     * @return true if there is a robot at the given location; false otherwise.
     * @throws GameActionException if the location is not within sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    boolean isLocationOccupiedByRobot(MapLocation loc) throws GameActionException;

    /**
     * Senses whether there is any robot or tree within a given circle.
     *
     * @param center the center of the circle to check
     * @param radius the radius of the circle to check
     * @return true if there is a robot or tree in the given circle; false otherwise.
     * @throws GameActionException if any portion of the given circle is not within sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    boolean isCircleOccupied(MapLocation center, float radius) throws GameActionException;

    /**
     * Senses whether there is any robot or tree within a given circle, ignoring this robot
     * if it itself occupies the circle.
     *
     * @param center the center of the circle to check
     * @param radius the radius of the circle to check
     * @return true if there is a robot or tree in the given circle; false otherwise.
     * @throws GameActionException if any portion of the given circle is not within sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    boolean isCircleOccupiedExceptByThisRobot(MapLocation center, float radius) throws GameActionException;

    /**
     * Senses the tree at the given location, or null if there is no tree
     * there.
     *
     * @param loc the location to check
     * @return the tree at the given location.
     * @throws GameActionException if the location is not within sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    TreeInfo senseTreeAtLocation(MapLocation loc) throws GameActionException;

    /**
     * Senses the robot at the given location, or null if there is no robot
     * there.
     *
     * @param loc the location to check
     * @return the robot at the given location.
     * @throws GameActionException if the location is not within sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo senseRobotAtLocation(MapLocation loc) throws GameActionException;

    /**
     * Tests whether the given tree exists and any part of the given tree is
     * within this robot's sensor range.
     *
     * @param id the ID of the tree to query
     * @return true if the given tree is within this robot's sensor range; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSenseTree(int id);

    /**
     * Tests whether the given robot exists and any part of the given robot is
     * within this robot's sensor range.
     *
     * @param id the ID of the robot to query
     * @return true if the given robot is within this robot's sensor range; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSenseRobot(int id);

    /**
     * Tests the given bullet exists and it is within this robot's
     * sensor range.
     *
     * @param id the ID of the bullet to query
     * @return true if the given bullet is within this robot's sensor range; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSenseBullet(int id);

    /**
     * Senses information about a particular tree given its ID.
     *
     * @param id the ID of the tree to query
     * @return a TreeInfo object for the sensed tree.
     * @throws GameActionException if the tree cannot be sensed (for example,
     * if it doesn't exist or is out of sight range).
     *
     * @battlecode.doc.costlymethod
     */
    TreeInfo senseTree(int id) throws GameActionException;

    /**
     * Senses information about a particular robot given its ID.
     *
     * @param id the ID of the robot to query
     * @return a RobotInfo object for the sensed robot.
     * @throws GameActionException if the robot cannot be sensed (for example,
     * if it doesn't exist or is out of sight range).
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo senseRobot(int id) throws GameActionException;

    /**
     * Senses information about a particular bullet given its ID.
     *
     * @param id the ID of the bullet to query
     * @return a BulletInfo object for the sensed bullet.
     * @throws GameActionException if the bullet cannot be sensed (for example,
     * if it doesn't exist or is out of sight range).
     *
     * @battlecode.doc.costlymethod
     */
    BulletInfo senseBullet(int id) throws GameActionException;

    /**
     * Returns all robots within sense radius. The objects are returned in order of
     * increasing distance from your robot.
     *
     * @return sorted array of RobotInfo objects, which contain information about all
     * the robots you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots();

    /**
     * Returns all robots that can be sensed within a certain radius of this
     * robot. The objects are returned in order of increasing distance from
     * your robot.
     *
     * @param radius return robots this distance away from the center of
     * this robot. If -1 is passed, all robots within sense radius are returned.
     * @return sorted array of RobotInfo objects of all the robots you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(float radius);

    /**
     * Returns all robots of a given team that can be sensed within a certain
     * radius of this robot. The objects are returned in order of increasing distance
     * from your robot.
     *
     * @param radius return robots this distance away from the center of
     * this robot. If -1 is passed, all robots within sense radius are returned
     * @param team filter game objects by the given team. If null is passed,
     * robots from any team are returned
     * @return sorted array of RobotInfo objects of all the robots you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(float radius, Team team);

    /**
     * Returns all robots of a given team that can be sensed within a certain
     * radius of a specified location. The objects are returned in order of
     * increasing distance from the specified center.
     *
     * @param center center of the given search radius
     * @param radius return robots this distance away from the given center
     * location. If -1 is passed, all robots within sense radius are returned
     * @param team filter game objects by the given team. If null is passed,
     * objects from all teams are returned
     * @return sorted array of RobotInfo objects of the robots you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(MapLocation center, float radius, Team team);

    /**
     * Returns all trees within sense radius. The objects are returned in order
     * of increasing distance from your robot.
     *
     * @return sorted array of TreeInfo objects, which contain information about all
     * the trees you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    TreeInfo[] senseNearbyTrees();

    /**
     * Returns all trees that can be sensed within a certain radius of this
     * robot. The objects are returned in order of increasing distance from
     * your robot.
     *
     * @param radius return trees this distance away from the center of
     * this robot. If -1 is passed, all trees within sense radius are returned
     * @return sorted array of TreeInfo objects of all the trees you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    TreeInfo[] senseNearbyTrees(float radius);

    /**
     * Returns all trees of a given team that can be sensed within a certain
     * radius of this robot. The objects are returned in order of increasing distance
     * from your robot.
     *
     * @param radius return trees this distance away from the center of
     * this robot. If -1 is passed, all trees within sense radius are returned
     * @param team filter game objects by the given team. If null is passed,
     * robots from any team are returned
     * @return sorted array of TreeInfo objects of all the trees you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    TreeInfo[] senseNearbyTrees(float radius, Team team);

    /**
     * Returns all trees of a given team that can be sensed within a certain
     * radius of a specified location. The objects are returned in order of
     * increasing distance from the specified center.
     *
     * @param center center of the given search radius
     * @param radius return trees this distance away from given center
     * location. If -1 is passed, all trees within sense radius are returned
     * @param team filter game objects by the given team. If null is passed,
     * objects from all teams are returned
     * @return sorted array of TreeInfo objects of the trees you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    TreeInfo[] senseNearbyTrees(MapLocation center, float radius, Team team);

    /**
     * Returns all bullets within bullet sense radius. The objects are returned in 
     * order of increasing distance from your robot.
     *
     * @return sorted array of BulletInfo objects, which contain information about all
     * the bullets you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    BulletInfo[] senseNearbyBullets();

    /**
     * Returns all bullets that can be sensed within a certain radius of this
     * robot. The objects are returned in order of increasing distance from
     * your robot.
     *
     * @param radius return bullets this distance away from the center of
     * this robot. If -1 is passed, bullets from the whole map are returned
     * @return sorted array of BulletInfo objects of all the bullets you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    BulletInfo[] senseNearbyBullets(float radius);

    /**
     * Returns all bullets that can be sensed within a certain
     * radius of a specified location. The objects are returned in order of
     * increasing distance from the specified center.
     *
     * @param center center of the given search radius
     * @param radius return bullets this distance away from the given center
     * location. If -1 is passed, all bullets within bullet sense radius are returned
     * @return sorted array of TreeInfo objects of the bullets you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    BulletInfo[] senseNearbyBullets(MapLocation center, float radius);

    /**
     * Returns an array of all the locations of the robots that have
     * broadcasted in the last round (unconstrained by sensor range or distance).
     *
     * @return an array of all the locations of the robots that have
     * broadcasted in the last round.
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseBroadcastingRobotLocations();

    // ***********************************
    // ****** MOVEMENT METHODS ***********
    // ***********************************
    
    /**
     * Returns whether the robot has moved this turn.
     * 
     * @return true if the robot has moved this turn; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean hasMoved();
    
    /**
     * Returns whether the robot has attacked this turn.
     * 
     * @return true if the robot has attacked this turn; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean hasAttacked();
    
    /**
     * Returns whether the robot's build cooldown has expired.
     * 
     * @return true if the robot's build cooldown has expired; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean isBuildReady();

    /**
     * Returns the number of cooldown turns remaining before this unit can build() again.
     * When this number is 0, isBuildReady() is true.
     *
     * @return the number of cooldown turns remaining before this unit can build() again.
     *
     * @battlecode.doc.costlymethod
     */
    int getBuildCooldownTurns();

    /**
     * Tells whether this robot can move one stride in the given direction,
     * without taking into account if they have already moved. Takes into account only
     * the positions of trees, positions of other robots, and the edge of the
     * game map. Does not take into account whether this robot is currently
     * active. Note that one stride is equivalent to this robot's {@code strideRadius}.
     *
     * @param dir the direction to move in
     * @return true if there is no external obstruction to prevent this robot
     * from moving one stride in the given direction; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canMove(Direction dir);

    /**
     * Tests whether this robot can move {@code distance} units in the given
     * direction, without taking into account if they have already moved. Takes into
     * account only the positions of trees, positions of other robots, and the
     * edge of the game map. Does not take into account whether this robot is
     * currently active. Note that one stride is equivalent to this robot's
     * {@code strideRadius}.
     *
     * @param dir the direction to move in
     * @param distance the distance of a move you wish to check. Must be
     * in [0, RobotType.strideRadius]
     * @return true if there is no external obstruction to prevent this robot
     * from moving distance in the given direction; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canMove(Direction dir, float distance);
    
    /**
     * Tests whether this robot can move to the target MapLocation. If
     * the location is outside the robot's {@code strideRadius}, the location
     * is rescaled to be at the {@code strideRadius}. Takes into account only
     * the positions of trees, other robots, and the edge of the game map. Does
     * not take into account whether this robot is currently active.
     * 
     * @param center the MapLocation to move to
     * @return true if there is no external obstruction to prevent this robot
     * from moving to this MapLocation (or in the direction of this MapLocation
     * if it is too far); false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canMove(MapLocation center);
    
    /**
     * Moves one stride in the given direction. Note that one stride is equivalent
     * to this robot's {@code strideRadius}.
     *
     * @param dir the direction to move in
     * @throws GameActionException if the robot cannot move one stride in this
     * direction, such as already moved that turn, the target location being
     * off the map, and the target destination being occupied with either
     * another robot or a tree.
     *
     * @battlecode.doc.costlymethod
     */
    void move(Direction dir) throws GameActionException;

    /**
     * Moves distance in the given direction. If the distance exceeds the robot's
     * {@code strideRadius}, it is rescaled to {@code strideRadius}.
     *
     * @param dir the direction to move in
     * @param distance the distance to move in that direction
     * @throws GameActionException if the robot cannot move distance in this
     * direction, such as already moved that turn, the target location being
     * off the map, and the target destination being occupied with either
     * another robot or a tree.
     *
     * @battlecode.doc.costlymethod
     */
    void move(Direction dir, float distance) throws GameActionException;
    
    /**
     * Moves to the target MapLocation. If the target location is outside the robot's
     * {@code strideRadius}, it is rescaled to be {@code strideRadius} away.
     * 
     * @param center the MapLocation to move to (or toward)
     * @throws GameActionException if the robot can not move to the target MapLocation,
     * such as already having moved that turn, the target location being off the map,
     * or a target destination being occupied with either another robot or a tree.
     *
     * @battlecode.doc.costlymethod
     */
    void move(MapLocation center) throws GameActionException;

    // ***********************************
    // ****** ATTACK METHODS *************
    // ***********************************

    /**
     * Tests whether a robot is able to strike this turn. This takes into accout
     * the robot's type, and if the robot has attacked this turn.
     *
     * @return true if the robot is able to strike this turn; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canStrike();

    /**
     * Strikes and deals damage to all other robots and trees within
     * {@link GameConstants#LUMBERJACK_STRIKE_RADIUS} of this robot. Note that only Lumberjacks
     * can perform this function.
     *
     * @throws GameActionException if the robot is not of type LUMBERJACK or
     * cannot attack due to having already attacked that turn.
     *
     * @battlecode.doc.costlymethod
     */
    void strike() throws GameActionException;

    /**
     * Tests whether there are enough bullets in your bullet supply to
     * fire a single shot, the robot is of an appropriate type, and the
     * robot has not attacked in the current turn.
     *
     * @return true if there are enough bullets in the bullet supply,
     * this robot is of an appropriate type, and the robot hasn't attacked
     * this turn; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canFireSingleShot();

    /**
     * Tests whether there are enough bullets in your bullet supply to
     * fire a triad shot, the robot is of an appropriate type, and the
     * robot has not attacked in the current turn.
     *
     * @return true if there are enough bullets in the bullet supply,
     * this robot is of an appropriate type, and the robot hasn't attacked
     * this turn; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canFireTriadShot();

    /**
     * Tests whether there is enough bullets in your bullet supply to
     * fire a pentad shot, the robot is of an appropriate type, and the
     * robot has not attacked in the current turn.
     *
     * @return true if there are enough bullets in the bullet supply,
     * this robot is of an appropriate type, and the robot hasn't attacked
     * this turn; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canFirePentadShot();

    /**
     * Fires a single bullet in the direction dir at the cost of
     * {@link GameConstants#SINGLE_SHOT_COST} from your team's bullet supply. The speed
     * and damage of the bullet is determined from the type of this robot.
     *
     * @param dir the direction you wish to fire the bullet
     * @throws GameActionException if this robot is not of a type that can
     * fire single shots (ARCHON, GARDENER, etc.), cannot attack due to having
     * already attacked, or for having insufficient bullets in the bullet supply.
     *
     * @battlecode.doc.costlymethod
     */
    void fireSingleShot(Direction dir) throws GameActionException;

    /**
     * Fires a three bullets with the center bullet in the direction dir and
     * with a spread of {@link GameConstants#TRIAD_SPREAD_DEGREES} degrees for the other
     * bullets.  This function costs {@link GameConstants#TRIAD_SHOT_COST} bullets from
     * your team's supply. The speed and damage of the bullets is determined
     * from the type of this robot.
     *
     * @param dir the direction you wish to fire the center bullet
     * @throws GameActionException if this robot is not of a type that can
     * fire triad shots (ARCHON, GARDENER, etc.), cannot attack due to having
     * already attacked, or for having insufficient bullets in the bullet supply.
     *
     * @battlecode.doc.costlymethod
     */
    void fireTriadShot(Direction dir) throws GameActionException;

    /**
     * Fires a five bullets with the center bullet in the direction dir and
     * with a spread of {@link GameConstants#PENTAD_SPREAD_DEGREES} degrees for the other
     * bullets.  This function costs {@link GameConstants#PENTAD_SHOT_COST} bullets from
     * your team's supply. The speed and damage of the bullets is determined
     * from the type of this robot.
     *
     * @param dir the direction you wish to fire the center bullet
     * @throws GameActionException if this robot is not of a type that can
     * fire pentad shots (ARCHON, GARDENER, etc.), cannot attack due to having
     * already attacked, or for having insufficient bullets in the bullet supply.
     *
     * @battlecode.doc.costlymethod
     */
    void firePentadShot(Direction dir) throws GameActionException;

    // ***********************************
    // ****** TREE METHODS ***************
    // ***********************************

    /**
     * Tests whether the robot can chop a tree at the given location.
     * Checks if the location is within {@link GameConstants#INTERACTION_DIST_FROM_EDGE},
     * the robot's type, if a tree exists, and if the robot hasn't attacked this turn.
     *
     * @param loc The location of the tree to chop
     * @return true if this robot can chop the tree; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canChop(MapLocation loc);

    /**
     * Tests whether the robot can chop a tree with the given ID. Checks if the tree is within
     * {@link GameConstants#INTERACTION_DIST_FROM_EDGE}, the robot's type,
     * if a tree exists, and if the robot hasn't attacked this turn.
     *
     * @param id The ID of the tree to chop
     * @return true if this robot can chop the tree; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canChop(int id);

    /**
     * Chops the tree at the given location. This action counts as an attack.
     *
     * @param loc the location of the tree to chop
     * @throws GameActionException if the given location does not contain
     * a tree, the specified tree is not within {@link GameConstants#INTERACTION_DIST_FROM_EDGE}
     * of this robot, this robot is not of type LUMBERJACK, or this robot has already attacked
     * this turn.
     *
     * @battlecode.doc.costlymethod
     */
    void chop(MapLocation loc) throws GameActionException;

    /**
     * Chops the tree with the given ID. This action counts as an attack.
     *
     * @param id the ID of the tree you wish to chop
     * @throws GameActionException if there isn't a tree with the given id,
     * the specified tree is not within one stride of this robot, this robot
     * is not of type LUMBERJACK, or this robot has already attacked this turn.
     *
     * @battlecode.doc.costlymethod
     */
    void chop(int id) throws GameActionException;

    /**
     * Tests whether this robot can shake a tree at the given location. Checks
     * if the tree is within {@link GameConstants#INTERACTION_DIST_FROM_EDGE},
     * if a tree exists, and if the robot hasn't shaken this turn.
     *
     * @param loc The location of the tree to shake
     * @return true if this robot can shake the tree; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canShake(MapLocation loc);

    /**
     * Tests whether this robot can shake a tree with the given ID. Checks if the
     * tree is within {@link GameConstants#INTERACTION_DIST_FROM_EDGE},
     * if a tree exists, and if the robot hasn't shaken this turn.
     *
     * @param id The ID of the tree to shake
     * @return true if this robot can shake the tree; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canShake(int id);

    /**
     * Shakes the tree at the given location for all the bullets held within
     * the tree; these bullets will be added to your team's bullet supply.
     * Robots can only shake once per turn.
     *
     * @param loc the location of the tree to shake
     * @throws GameActionException if the given location does not contain
     * a tree, if the tree (not location) is not within {@link GameConstants#INTERACTION_DIST_FROM_EDGE}
     * of this robot, or if this robot has already shaken a tree this turn.
     *
     * @battlecode.doc.costlymethod
     */
    void shake(MapLocation loc) throws GameActionException;

    /**
     * Shakes the tree with the given ID for all the bullets held within
     * the tree; these bullets will be added to your team's bullet supply.
     * Robots can only shake once per turn.
     *
     * @param id the ID of the tree to shake
     * @throws GameActionException if there isn't a tree with the given id,
     * if the tree (not location) is not within one stride of this robot,
     * or if this robot has already shaken a tree this turn
     *
     * @battlecode.doc.costlymethod
     */
    void shake(int id) throws GameActionException;

    /**
     * Tests whether this robot can water a tree at the given location. Checks robot
     * stride radius, the robot's type, if a tree exists, and if the robot hasn't
     * watered this turn.
     *
     * @param loc The location of the tree to water
     * @return true if this robot can water the tree; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canWater(MapLocation loc);

    /**
     * Tests whether this robot can water a tree with the given ID. Checks that the tree is within
     * {@link GameConstants#INTERACTION_DIST_FROM_EDGE}, the robot's type, if a tree exists, and
     * if the robot hasn't watered this turn.
     *
     * @param id The ID of a tree to check.
     * @return true if this robot can water a tree; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canWater(int id);

    /**
     * Waters the target tree at the given location, restoring
     * {@code WATER_HEALTH_REGEN_RATE} health to the tree.
     * Robots can only water once per turn and only with robots
     * of type GARDENER.
     *
     * @param loc the location of the tree you wish to water
     * @throws GameActionException if the given location does not contain
     * a tree, the tree is not within {@link GameConstants#INTERACTION_DIST_FROM_EDGE} of this robot,
     * this robot is not of type GARDENER, or this robot has already
     * watered a tree.
     *
     * @battlecode.doc.costlymethod
     */
    void water(MapLocation loc) throws GameActionException;

    /**
     * Waters the target tree with the given ID, restoring
     * {@link GameConstants#WATER_HEALTH_REGEN_RATE} health to the tree.
     * Robots can only water once per turn and only with robots
     * of type GARDENER.
     *
     * @param id the ID of the tree you wish to water.
     * @throws GameActionException if there isn't a tree with the given id,
     * the tree is not within {@link GameConstants#INTERACTION_DIST_FROM_EDGE} of this robot,
     * this robot is not of type GARDENER, or this robot has already
     * watered a tree.
     *
     * @battlecode.doc.costlymethod
     */
    void water(int id) throws GameActionException;

    /**
     * Tests whether this robot can water a tree, taking into
     * account how many times this robot has watered this turn and this
     * robot's type.
     *
     * @return true if this robot can water a tree; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canWater();

    /**
     * Tests whether this robot can shake a tree, taking into
     * account how many times this robot has shaken this turn.
     *
     * @return true if this robot can shake a tree; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canShake();

    /**
     * Tests whether there is a tree at the given location and, if so,
     * if the tree is within one stride of this robot and can therefore be
     * interacted with through chop(), shake(), or water().
     *
     * @param loc the location you wish to test
     * @return true if there is a tree located at loc and if said tree is
     * within {@link GameConstants#INTERACTION_DIST_FROM_EDGE} of this robot.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canInteractWithTree(MapLocation loc);

    /**
     * Tests whether there is a tree with the given ID and, if so,
     * if the tree is within one stride of this robot and can therefore be
     * interacted with through chop(), shake(), or water().
     *
     * @param id the ID of the tree you wish to test
     * @return true if there is a tree with id and if said tree is
     * within {@link GameConstants#INTERACTION_DIST_FROM_EDGE} of this robot.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canInteractWithTree(int id);

    // ***********************************
    // ****** SIGNALING METHODS **********
    // ***********************************

    /**
     * Broadcasts an integer to the team-shared array at index channel.
     * The data is not written until the end of the robot's turn.
     *
     * @param channel the index to write to, from 0 to <code>BROADCAST_MAX_CHANNELS</code>
     * @param data one int of data to write
     * @throws GameActionException if the channel is invalid
     *
     * @see #broadcastInt(int channel, int data)
     *
     * @battlecode.doc.costlymethod
     */
    void broadcast(int channel, int data) throws GameActionException;

    /**
     * Retrieves the integer stored in the team-shared array at index channel.
     *
     * @param channel the index to query, from 0 to <code>BROADCAST_MAX_CHANNELS</code>
     * @return the data currently stored on the channel, interpreted as an int.
     * @throws GameActionException  if the channel is invalid
     *
     * @see #readBroadcastInt(int channel)
     *
     * @battlecode.doc.costlymethod
     */
    int readBroadcast(int channel) throws GameActionException;

    /**
     * Broadcasts a boolean to the team-shared array at index channel.
     * The data is not written until the end of the robot's turn.
     *
     * @param channel the index to write to, from 0 to <code>BROADCAST_MAX_CHANNELS</code>
     * @param data one int of data to write
     * @throws GameActionException if the channel is invalid
     *
     * @battlecode.doc.costlymethod
     */
    void broadcastBoolean(int channel, boolean data) throws GameActionException;

    /**
     * Retrieves the boolean stored in the team-shared array at index channel.
     *
     * @param channel the index to query, from 0 to <code>BROADCAST_MAX_CHANNELS</code>
     * @return the data currently stored on the channel, interpreted as a boolean.
     * @throws GameActionException  if the channel is invalid
     *
     * @battlecode.doc.costlymethod
     */
    boolean readBroadcastBoolean(int channel) throws GameActionException;

    /**
     * Broadcasts an int to the team-shared array at index channel.
     * The data is not written until the end of the robot's turn.
     *
     * @param channel the index to write to, from 0 to <code>BROADCAST_MAX_CHANNELS</code>
     * @param data one int of data to write
     * @throws GameActionException if the channel is invalid
     *
     * @see #broadcast(int channel, int data)
     *
     * @battlecode.doc.costlymethod
     */
    void broadcastInt(int channel, int data) throws GameActionException;

    /**
     * Retrieves the int stored in the team-shared array at index channel.
     *
     * @param channel the index to query, from 0 to <code>BROADCAST_MAX_CHANNELS</code>
     * @return the data currently stored on the channel, interpreted as an int.
     * @throws GameActionException  if the channel is invalid
     *
     * @see #readBroadcast(int channel)
     *
     * @battlecode.doc.costlymethod
     */
    int readBroadcastInt(int channel) throws GameActionException;

    /**
     * Broadcasts a float to the team-shared array at index channel.
     * The data is not written until the end of the robot's turn.
     *
     * @param channel the index to write to, from 0 to <code>BROADCAST_MAX_CHANNELS</code>
     * @param data one float of data to write
     * @throws GameActionException if the channel is invalid
     *
     * @battlecode.doc.costlymethod
     */
    void broadcastFloat(int channel, float data) throws GameActionException;

    /**
     * Retrieves the float stored in the team-shared array at index channel.
     *
     * @param channel the index to query, from 0 to <code>BROADCAST_MAX_CHANNELS</code>
     * @return the data currently stored on the channel, interpreted as a float.
     * @throws GameActionException  if the channel is invalid
     *
     * @battlecode.doc.costlymethod
     */
    float readBroadcastFloat(int channel) throws GameActionException;

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************

    /**
     * Tests whether you have the bullets and dependencies to build the given
     * robot, and this robot is a valid builder for the target robot.
     *
     * @param type the type of robot to build
     * @return true if the requirements to build the given robot are met; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean hasRobotBuildRequirements(RobotType type);

    /**
     * Tests whether you have the bullets and dependencies to build a
     * bullet tree, and this robot is a valid builder for a bullet tree.
     *
     * @return true if the requirements to plant a tree are met; false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    boolean hasTreeBuildRequirements();

    /**
     * Tests whether the robot can build a robot of the given type in the
     * given direction. Checks cooldown turns remaining, bullet costs,
     * whether the robot can build, and that the given direction is
     * not blocked.
     *
     * @param dir the direction to build in
     * @param type the type of robot to build
     * @return whether it is possible to build a robot of the given type in the
     * given direction.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canBuildRobot(RobotType type, Direction dir);

    /**
     * Builds a robot of the given type in the given direction.
     *
     * @param dir the direction to spawn the unit
     * @param type the type of robot to build
     * @throws GameActionException if you don't have enough bullets, if
     * the robot is still in build cooldown, if the direction is not a
     * good build direction, or if this robot is not of an appropriate type.
     *
     * @battlecode.doc.costlymethod
     */
    void buildRobot(RobotType type, Direction dir) throws GameActionException;

    /**
     * Tests whether the robot can build a bullet tree in the given direction.
     * Checks cooldown turns remaining, bullet costs, whether the robot can
     * plant, and that the given direction is not blocked
     *
     * @param dir the direction to build in
     * @return whether it is possible to build a bullet tree in the
     * given direction.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canPlantTree(Direction dir);

    /**
     * Plants a bullet tree in the given direction.
     *
     * @param dir the direction to plant the bullet tree
     * @throws GameActionException if you don't have enough bullets, if
     * the robot is still in build cooldown, if the direction is not a good build
     * direction, or if this robot is not of an appropriate type.
     *
     * @battlecode.doc.costlymethod
     */
    void plantTree(Direction dir) throws  GameActionException;

    /**
     * Tests whether the robot can hire a Gardener in the given direction.
     * Checks cooldown turns remaining, bullet costs, whether the robot can
     * hire, and that the given direction is not blocked.
     * 
     * @param dir the direction to build in
     * @return whether it is possible to hire a gardener in the given direction.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canHireGardener(Direction dir);
    
    /**
     * Hires a Gardener in the given direction.
     *
     * @param dir the direction to spawn the Gardener
     * @throws GameActionException if you don't have enough bullets, if
     * the robot is still in build cooldown, if the direction is not a good build
     * direction, or if this robot is not of an appropriate type.
     *
     * @battlecode.doc.costlymethod
     */
    void hireGardener(Direction dir) throws GameActionException;

    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************

    /**
     * Returns the current cost of a victory point in bullets. This varies based
     * on the round number, and is equal to {@link GameConstants#VP_BASE_COST} +
     * NumRounds * {@link GameConstants#VP_INCREASE_PER_ROUND}.
     *
     * @return the current cost of a victory point in bullets
     *
     * @battlecode.doc.costlymethod
     */
    float getVictoryPointCost();

    /**
     * Donates the given amount of bullets to the reforestation fund in
     * exchange for one victory point per ten bullets donated.  Note there
     * are no fractions of victory points, meaning, for example, donating
     * 11 bullets will only result in 1 victory point, not 1.1 victory points.
     *
     * @param bullets the amount of bullets you wish to donate
     * @throws GameActionException if you have less bullets in your bullet
     * supply than the amount of bullet you wish to donate.
     *
     * @battlecode.doc.costlymethod
     */
    void donate(float bullets) throws GameActionException;

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
    // **** INDICATOR STRING METHODS *****
    // ***********************************

    /**
     * Draw a dot on the game map for debugging purposes.
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
     * Draw a line on the game map for debugging purposes.
     *
     * @param startLoc the location to draw the line from.
     * @param endLoc the location to draw the line to.
     * @param red the red component of the line's color.
     * @param green the green component of the line's color.
     * @param blue the blue component of the line's color.
     *
     * @battlecode.doc.costlymethod
     */
    void setIndicatorLine(MapLocation startLoc, MapLocation endLoc, int red, int green, int blue);

    // ***********************************
    // ******** TEAM MEMORY **************
    // ***********************************

    /**
     * Sets the team's "memory", which is saved for the next game in the match.
     * The memory is an array of {@link GameConstants#TEAM_MEMORY_LENGTH} longs.
     * If this method is called more than once with the same index in the same
     * game, the last call is what is saved for the next game.
     *
     * @param index the index of the array to set
     * @param value the data that the team should remember for the next game
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
     * @param index the index of the array to set
     * @param value the data that the team should remember for the next game
     * @param mask indicates which bits should be set
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
     * Gets this robot's 'control bits' for debugging purposes. These bits can
     * be set manually by the user, so a robot can respond to them. To set these
     * bits, you must run the client in lockstep mode and right click the
     * units.
     *
     * @return this robot's control bits.
     *
     * @battlecode.doc.costlymethod
     */
    long getControlBits();

}
