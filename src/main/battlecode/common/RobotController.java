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
     * team has been destroyed, then the tiebreakers will be used.
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
     * @return the current round number, where 0 is the first round of the
     * match.
     *
     * @battlecode.doc.costlymethod
     */
    int getRoundNum();

    /**
     * Gets the team's total bullet supply.
     *
     * @return the team's total bullet supply.
     *
     * @battlecode.doc.costlymethod
     */
    float getTeamBullets();

    /**
     * Gets the team's total victory points.
     *
     * @return the team's total victory points.
     *
     * @battlecode.doc.costlymethod
     */
    int getTeamVictoryPoints();

    /**
     * Gets the opponent's total victory points.
     *
     * @return the opponent's total victory points.
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
     * increasing y. Will return an empty list if you query for NEUTRAL.
     *
     * @param t the team whose archons you want to query the initial locations
     * for. Will return an empty list if you query for NEUTRAL.
     * @return a list of the INITIAL locations of the archons of that team, or
     * an empty list for team NEUTRAL.
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] getInitialArchonLocations(Team t);

    // *********************************
    // ****** UNIT QUERY METHODS *******
    // *********************************

    /**
     * Use this method to access your ID.
     *
     * @return the ID of this robot.
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
     * Gets this robot's current location.
     *
     * @return this robot's current location.
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation getLocation();

    /**
     * Gets this robot's current health.
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
     */
    int getAttackCount();
    
    /**
     * Returns the number of times the robot has moved this turn.
     * 
     * @return the number of times the robot has moved this turn.
     */
    int getMoveCount();

    // ***********************************
    // ****** GENERAL SENSOR METHODS *****
    // ***********************************

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
     * Senses whether a given circle is completely on the map. Will throw an exception if
     * the circle is not completely within sensor range.
     *
     * @param center the center of the circle to check.
     * @param radius the radius of the circle to check.
     * @return true if the circle is completely on the map, false otherwise.
     * @throws GameActionException if any portion of the given circle is not within sensor range.
     */
    boolean onTheMap(MapLocation center, float radius) throws GameActionException;

    /**
     * Returns true if the given location is within the robot's sensor range.
     *
     * @param loc the location to check.
     * @return whether the given location is within the robot's sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSenseLocation(MapLocation loc);

    /**
     * Returns true if any portion of the given circle is within the robot's sensor range.
     *
     * @param center the center of the circle to check.
     * @param radius the radius of the circle to check.
     * @return whether a portion of the circle is within the robot's sensor range.
     */
    boolean canSensePartOfCircle(MapLocation center, float radius);

    /**
     * Returns true if all of the given circle is within the robot's sensor range.
     *
     * @param center the center of the circle to check.
     * @param radius the radius of the circle to check.
     * @return whether all of the circle is within the robot's sensor range.
     */
    boolean canSenseAllOfCircle(MapLocation center, float radius);

    /**
     * Returns whether there is a robot or tree at the given location.
     *
     * @param loc the location to check.
     * @return whether there is a robot or tree at the given location.
     * @throws GameActionException if the location is not within sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    boolean isLocationOccupied(MapLocation loc) throws GameActionException;

    /**
     * Returns whether there is a tree at the given location.
     *
     * @param loc the location to check.
     * @return whether there is a tree at the given location.
     * @throws GameActionException if the location is not within sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    boolean isLocationOccupiedByTree(MapLocation loc) throws GameActionException;

    /**
     * Returns whether there is a robot at the given location.
     *
     * @param loc the location to check.
     * @return whether there is a robot at the given location.
     * @throws GameActionException if the location is not within sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    boolean isLocationOccupiedByRobot(MapLocation loc) throws GameActionException;

    /**
     * Returns whether there is any robot or tree within a given circle
     *
     * @param center the center of the circle to check.
     * @param radius the radius of the circle to check.
     * @return whether there is a robot or tree in the given circle.
     * @throws GameActionException if any portion of the given circle is not within sensor range.
     */
    boolean isCircleOccupied(MapLocation center, float radius) throws GameActionException;

    /**
     * Returns the tree at the given location, or null if there is no tree
     * there.
     *
     * @param loc the location to check.
     * @return the tree at the given location.
     * @throws GameActionException if the location is not within sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    TreeInfo senseTreeAtLocation(MapLocation loc) throws GameActionException;

    /**
     * Returns the robot at the given location, or null if there is no robot
     * there.
     *
     * @param loc the location to check.
     * @return the robot at the given location.
     * @throws GameActionException if the location is not within sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo senseRobotAtLocation(MapLocation loc) throws GameActionException;

    /**
     * Returns true if the given tree exists and any part of the given tree is
     * within this robot's sensor range.
     *
     * @param id the ID of the tree to query.
     * @return whether the given tree is within this robot's sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSenseTree(int id);

    /**
     * Returns true if the given robot exists and any part of the given robot is
     * within this robot's sensor range.
     *
     * @param id the ID of the robot to query.
     * @return whether the given robot is within this robot's sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSenseRobot(int id);

    /**
     * Returns true if the given bullet exists and if it is within this robot's
     * sensor range.
     *
     * @param id the ID of the bullet to query.
     * @return whether the given bullet is within this robot's sensor range.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSenseBullet(int id);

    /**
     * Senses information about a particular tree given its ID.
     *
     * @param id the ID of the tree to query.
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
     * @param id the ID of the robot to query.
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
     * @param id the ID of the bullet to query.
     * @return a BulletInfo object for the sensed bullet.
     * @throws GameActionException if the bullet cannot be sensed (for example,
     * if it doesn't exist or is out of sight range).
     *
     * @battlecode.doc.costlymethod
     */
    BulletInfo senseBullet(int id) throws GameActionException;

    /**
     * Returns all robots that can be sensed on the map.
     *
     * @return array of RobotInfo objects, which contain information about all
     * the robots you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots();

    /**
     * Returns all robots that can be sensed within a certain radius of this
     * robot.
     *
     * @param radius return robots this distance away from the center of
     * this robot. If -1 is passed, robots from the whole map are returned.
     * @return array of RobotInfo objects of all the robots you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(float radius);

    /**
     * Returns all robots of a given team that can be sensed within a certain
     * radius of this robot.
     *
     * @param radius return robots this distance away from the center of
     * this robot. If -1 is passed, robots from the whole map are returned.
     * @param team filter game objects by the given team. If null is passed,
     * robots from any team are returned.
     * @return array of RobotInfo objects of all the robots you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(float radius, Team team);

    /**
     * Returns all robots of a given team that can be sensed within a certain
     * radius of a specified location.
     *
     * @param center center of the given search radius.
     * @param radius return robots this distance away from the given center
     * location. If -1 is passed, robots from the whole map are returned.
     * @param team filter game objects by the given team. If null is passed,
     * objects from all teams are returned.
     * @return array of RobotInfo objects of the robots you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseNearbyRobots(MapLocation center, float radius, Team team);

    /**
     * Returns all trees that can be sensed on the map.
     *
     * @return array of TreeInfo objects, which contain information about all
     * the trees you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    TreeInfo[] senseNearbyTrees();

    /**
     * Returns all trees that can be sensed within a certain radius of this
     * robot.
     *
     * @param radius return trees this distance away from the center of
     * this robot. If -1 is passed, trees from the whole map are returned.
     * @return array of TreeInfo objects of all the trees you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    TreeInfo[] senseNearbyTrees(float radius);

    /**
     * Returns all trees of a given team that can be sensed within a certain
     * radius of this robot.
     *
     * @param radius return trees this distance away from the center of
     * this robot. If -1 is passed, trees from the whole map are returned.
     * @param team filter game objects by the given team. If null is passed,
     * robots from any team are returned.
     * @return array of TreeInfo objects of all the trees you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    TreeInfo[] senseNearbyTrees(float radius, Team team);

    /**
     * Returns all trees of a given team that can be sensed within a certain
     * radius of a specified location.
     *
     * @param center center of the given search radius.
     * @param radius return trees this distance away from given center
     * location. If -1 is passed, trees from the whole map are returned.
     * @param team filter game objects by the given team. If null is passed,
     * objects from all teams are returned.
     * @return array of TreeInfo objects of the trees you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    TreeInfo[] senseNearbyTrees(MapLocation center, float radius, Team team);

    /**
     * Returns all bullets that can be sensed on the map.
     *
     * @return array of BulletInfo objects, which contain information about all
     * the bullets you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    BulletInfo[] senseNearbyBullets();

    /**
     * Returns all bullets that can be sensed within a certain radius of this
     * robot.
     *
     * @param radius return bullets this distance away from the center of
     * this robot. If -1 is passed, bullets from the whole map are returned.
     * @return array of BulletInfo objects of all the bullets you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    BulletInfo[] senseNearbyBullets(float radius);

    /**
     * Returns all bullets that can be sensed within a certain
     * radius of a specified location.
     *
     * @param center center of the given search radius.
     * @param radius return bullets this distance away from the given center
     * location. If -1 is passed, bullets from the whole map are returned.
     * @return array of TreeInfo objects of the bullets you sensed.
     *
     * @battlecode.doc.costlymethod
     */
    BulletInfo[] senseNearbyBullets(MapLocation center, float radius);

    /**
     * Returns an array of all the robots that have broadcasted in the
     * last round (unconstrained by sensor range or distance).
     *
     * @return an array of all robots that have broadcasted in the last round.
     *
     * @battlecode.doc.costlymethod
     */
    RobotInfo[] senseBroadcastingRobots();

    /**
     * Returns an array of all the robots that have broadcasted in the
     * last round, filtered by team (unconstrained by sensor range or distance).
     * Will return an empty list if you query for NEUTRAL.
     *
     * @param t - filter robots by the given team.
     * @return an array of all robots of the given team that have
     * broadcasted in the last round.
     *
     * battlecode.doc.costlymethod
     */
    RobotInfo[] senseBroadcastingRobots(Team t);

    /**
     * Returns an array of all the locations of the robots that have
     * broadcasted in the last round (unconstrained by sensor range or distance)
     *
     * @return an array of all the locations of the robots that have
     * broadcasted in the last round.
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseBroadcastingRobotLocations();

    /**
     * Returns an array of all the locations of the robots that have broadcasted
     * in the last round, filtered by team (unconstrained by sensor range or distance).
     * Will return an empty list if you query for NEUTRAL.
     *
     * @param t - filter robots by the given team.
     * @return an array of all the locations of the robots of the
     * given team that have broadcasted in the last round.
     *
     * @battlecode.doc.costlymethod
     */
    MapLocation[] senseBroadcastingRobotLocations(Team t);


    // ***********************************
    // ****** MOVEMENT METHODS ***********
    // ***********************************
    
    /**
     * Returns true if the robot has moved this turn.
     * 
     * @return true if the robot has moved this turn.
     */
    boolean hasMoved();
    
    /**
     * Returns true if the robot has attacked this turn.
     * 
     * @return true if the robot has attacked this turn.
     */
    boolean hasAttacked();
    
    /**
     * Returns true if the robot's build cooldown has expired.
     * 
     * @return true if the robot's build cooldown has expired.
     */
    boolean isBuildReady();

    /**
     * Tells whether this robot can move one stride in the given direction,
     * without taking into account if they have already moved. Takes into account only
     * the positions of trees, positions of other robots, and the edge of the
     * game map. Does not take into account whether this robot is currently
     * active.  Note that one stride is equivalent to StrideRadius.
     *
     * @param dir the direction to move in.
     * @return true if there is nothing preventing this robot from moving one
     * stride in the given direction; false otherwise (does not account for
     * core delay).
     *
     * @battlecode.doc.costlymethod
     */
    boolean canMove(Direction dir);

    /**
     * Tells whether this robot can move scale strides in the given direction,
     * without taking into account if they have already moved. Takes into
     * account only the positions of trees, positions of other robots, and the
     * edge of the game map. Does not take into account whether this robot is
     * currently active. Note that one stride is equivalent to StrideRadius.
     *
     *
     * @param dir the direction to move in.
     * @param scale the scale of a stride you wish to check. Must be
     * from 0 to 1 (inclusive).
     * @return true if there is nothing preventing this robot from moving scale
     * strides in the given direction; false otherwise (does not account for
     * the robot having already moved that turn).
     *
     * @battlecode.doc.costlymethod
     */
    boolean canMove(Direction dir, float scale);
    
    /**
     * Tells whether this robot can move to the target MapLocation. If the location
     * is outside the robot's StrideRadius, the location is rescaled to be at the
     * StrideRadius. Takes into account only the positions of rees, positions of
     * other robots, and the edge of the game map. Does not take into accout whether
     * this robot is currently active.
     * 
     * @param center the MapLocation to move to.
     * @return true if there is nothing preventing this robot from moving to this
     * MapLocation (or in the direction of this MapLocation if it is too far);
     * false otherwise (does not account for the robot having already moved that turn).
     */
    boolean canMove(MapLocation center);
    
    /**
     * Moves one stride in the given direction. Note that one stride is equivalent
     * to 2*getType().bodyRadius in Euclidean distance.
     *
     * @param dir the direction to move in.
     * @throws GameActionException if the robot cannot move one stride in this
     * direction, such as already moved that turn, the target location being
     * off the map, and the target destination being occupied with either
     * another robot or a tree.
     *
     * @battlecode.doc.costlymethod
     */
    void move(Direction dir) throws GameActionException;

    /**
     * Moves scale strides in the given direction. Note that one stride is
     * equivalent to 2*getType().bodyRadius in Euclidean distance.
     *
     * @param dir the direction to move in.
     * @param scale the scale of a stride you wish to move. Must be be
     * from 0 to 1 (inclusive).
     * @throws GameActionException if the robot cannot move scale strides in this
     * direction, such as already moved that turn, the target location being
     * off the map, and the target destination being occupied with either
     * another robot or a tree.
     *
     * @battlecode.doc.costlymethod
     */
    void move(Direction dir, float scale) throws GameActionException;
    
    /**
     * Moves to the target MapLocation. If the target location is outside the robot's
     * StrideRadius, it is rescaled to be one StrideRadius away.
     * 
     * @param center the MapLocation to move to (or toward)
     * @throws GameActionException if the robot can not move to the target MapLocation,
     * such as already having moved that turn, the target location being off the map,
     * or a target destination being occupied with either another robot or a tree.
     */
    void move(MapLocation center) throws GameActionException;

    // ***********************************
    // ****** ATTACK METHODS *************
    // ***********************************

    /**
     * Strikes and deals damage to all other robots and trees within one stride of
     * this robot. Note that only Lumberjacks can perform this function.
     *
     * @throws GameActionException if the robot is not of type LUMBERJACK or
     * cannot attack due to having already attacked that turn.
     *
     * @battlecode.doc.costlymethod
     */
    void strike() throws GameActionException;

    /**
     * Tells whether there is enough bullets in your bullet supply to
     * fire a single shot and if the robot is of an appropriate type.
     *
     * @return true if there are enough bullets in the bullet supply and
     * this robot is of an appropriate type.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canSingleShot();

    /**
     * Tells whether there is enough bullets in your bullet supply to
     * fire a triad shot and if the robot is of an appropriate type.
     *
     * @return true if there are enough bullets in the bullet supply and
     * this robot is of an appropriate type.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canTriadShot();

    /**
     * Tells whether there is enough bullets in your bullet supply to
     * fire a pentad shot and if the robot is of an appropriate type.
     *
     * @return true if there are enough bullets in the bullet supply and
     * this robot is of an appropriate type.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canPentadShot();

    /**
     * Fires a single bullet in the direction dir at the cost of
     * GameConstants.SINGLE_SHOT_COST from your team's bullet supply. The speed
     * and damage of the bullet is determined from the type of this robot.
     *
     * @param dir the direction you wish to fire the bullet.
     * @throws GameActionException if this robot is not of a type that can
     * fire single shots (ARCHON, GARDENER, etc.), cannot attack due to having
     * already attacked, or for having insufficient bullets in the bullet supply.
     *
     * @battlecode.doc.costlymethod
     */
    void fireSingleShot(Direction dir) throws GameActionException;

    /**
     * Fires a three bullets with the center bullet in the direction dir and
     * with a spread of GameConstants.TRIAD_SPREAD_DEGREES degrees for the other
     * bullets.  This function costs GameConstants.TRIAD_SHOT_COST bullets from
     * your team's supply. The speed and damage of the bullets is determined
     * from the type of this robot.
     *
     * @param dir the direction you wish to fire the center bullet.
     * @throws GameActionException if this robot is not of a type that can
     * fire triad shots (ARCHON, GARDENER, etc.), cannot attack due to having
     * already attacked, or for having insufficient bullets in the bullet supply.
     *
     * @battlecode.doc.costlymethod
     */
    void fireTriadShot(Direction dir) throws GameActionException;

    /**
     * Fires a five bullets with the center bullet in the direction dir and
     * with a spread of GameConstants.PENTAD_SPREAD_DEGREES degrees for the other
     * bullets.  This function costs GameConstants.PENTAD_SHOT_COST bullets from
     * your team's supply. The speed and damage of the bullets is determined
     * from the type of this robot.
     *
     * @param dir the direction you wish to fire the center bullet.
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
     * Chops the target tree at location loc. This action counts as a movement.
     *
     * @param loc the location of the tree you wish to chop, does not
     * have to be the center of the tree
     * @throws GameActionException if the given location does not contain
     * a tree, if the tree (not location) is not within one stride of this
     * robot, or cannot perform action due to having already moved.
     *
     * @battlecode.doc.costlymethod
     */
    void chop(MapLocation loc) throws GameActionException;

    /**
     * Chops the target tree at location loc. This action counts as a movement.
     *
     * @param id the id of the tree you wish to chop.
     * @throws GameActionException if there isn't a tree with the given id,
     * if the tree (not location) is not within one stride of this robot,
     * or cannot perform action due to having already moved.
     *
     * @battlecode.doc.costlymethod
     */
    void chop(int id) throws GameActionException;

    /**
     * Shakes the target tree at location loc for all the bullets held within
     * the tree; these bullets will be added to your team's bullet supply.
     * Robots can only shake once per turn.
     *
     * @param loc the location of the tree you wish to shake, does not
     * have to be the center of the tree
     * @throws GameActionException if the given location does not contain
     * a tree, if the tree (not location) is not within one stride of this
     * robot, or if this robot has already shook a tree this turn
     *
     * @battlecode.doc.costlymethod
     */
    void shake(MapLocation loc) throws GameActionException;

    /**
     * Shakes the target tree at location loc for all the bullets held within
     * the tree; these bullets will be added to your team's bullet supply.
     * Robots can only shake once per turn.
     *
     * @param id the id of the tree you wish to shake.
     * @throws GameActionException if there isn't a tree with the given id,
     * if the tree (not location) is not within one stride of this robot,
     * or if this robot has already shook a tree this turn
     *
     * @battlecode.doc.costlymethod
     */
    void shake(int id) throws GameActionException;

    /**
     * Waters the target tree at location loc, healing
     * GameConstants.WATER_HEALTH_REGEN_RATE health to the tree.
     * Robots can only water once per turn and only with robots
     * of type GARDENER.
     *
     * @param loc the location of the tree you wish to water, does not
     * have to be the center of the tree
     * @throws GameActionException if the given location does not contain
     * a tree, if the tree (not location) is not within one stride of this
     * robot, or this robot is not of type GARDENER
     *
     * @battlecode.doc.costlymethod
     */
    void water(MapLocation loc) throws GameActionException;

    /**
     * Waters the target tree at location loc, healing
     * GameConstants.WATER_HEALTH_REGEN_RATE health to the tree.
     * Robots can only water once per turn and only with robots
     * of type GARDENER.
     *
     * @param id the id of the tree you wish to water.
     * @throws GameActionException if there isn't a tree with the given id,
     * if the tree (not location) is not within one stride of this robot,
     * or this robot is not of type GARDENER
     *
     * @battlecode.doc.costlymethod
     */
    void water(int id) throws GameActionException;

    /**
     * Determines whether or not this robot can water a tree, taking into
     * account how many times this robot has watered this turn and this
     * robot's type
     *
     * @return true if this robot can water a tree, false otherwise.
     */
    boolean canWater();

    /**
     * Determines whether or not this robot can shake a tree, taking into
     * account how many times this robot has shook this turn.
     *
     * @return true if this robot can shake a tree, false otherwise.
     */
    boolean canShake();

    /**
     * Determines whether or not there is a tree at location loc and, if so,
     * if the tree is within one stride of this robot and can therefore be
     * interacted with through chop(), shake(), or water().
     *
     * @param loc the location you wish to test
     * @return true if there is a tree located at loc and if said tree is
     * within one stride of this robot
     *
     * @battlecode.doc.costlymethod
     */
    boolean canInteractWithTree(MapLocation loc);

    /**
     * Determines whether or not there is a tree with the given id and, if so,
     * if the tree is within one stride of this robot and can therefore be
     * interacted with through chop(), shake(), or water().
     *
     * @param id the id of the tree you wish to test
     * @return true if there is a tree with the given id and if siad tree is
     * within a stride of this robot
     *
     * @battlecode.doc.costlymethod
     */
    boolean canInteractWithTree(int id);

    // ***********************************
    // ****** SIGNALING METHODS **********
    // ***********************************

    /**
     * Broadcasts a message to the team-shared array at index channel.
     * The data is not written until the end of the robot's turn.
     *
     * @param channel - the index to write to, from 0 to <code>BROADCAST_MAX_CHANNELS</code>
     * @param data - one int's worth of data to write
     * @throws GameActionException if the channel is invalid
     *
     * @battlecode.doc.costlymethod
     */
    void broadcast(int channel, int data) throws GameActionException;

    /**
     * Retrieves the message stored in the team-shared array at index channel.
     *
     * @param channel the index to query, from 0 to <code>BROADCAST_MAX_CHANNELS</code>
     * @return data currently stored on the channel
     * @throws GameActionException  if the channel is invalid
     *
     * @battlecode.doc.costlymethod
     */
    int readBroadcast(int channel) throws GameActionException;

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************

    /**
     * Returns whether you have the bullets and dependencies to build the given
     * robot, and this robot is a valid builder for the target robot.
     *
     * @param type the type to build.
     * @return whether the requirements to build are met.
     *
     * @battlecode.doc.costlymethod
     */
    boolean hasRobotBuildRequirements(RobotType type);

    /**
     * Returns whether you have the bullets and dependencies to build a
     * bullet tree, and this robot is a valid builder for a bullet tree.
     *
     * @return whether the requirements to build are met.
     *
     * @battlecode.doc.costlymethod
     */
    boolean hasTreeBuildRequirements();

    /**
     * Returns whether the robot can build a robot of the given type in the
     * given direction. Checks dependencies, cooldown turns remaining,
     * bullet costs, whether the robot can build, and that the given direction is
     * not blocked.
     *
     * @param dir the direction to build in.
     * @param type the robot type to build.
     * @return whether it is possible to build a robot of the given type in the
     * given direction.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canBuildRobot(RobotType type, Direction dir);

    /**
     * Returns whether the robot can build a bullet tree in the given direction.
     * Checks dependencies, cooldown turns remaining, bullet costs,
     * whether the robot can build, and that the given direction is
     * not blocked.
     *
     * @param dir the direction to build in.
     * @return whether it is possible to build a bullet tree in the
     * given direction.
     *
     * @battlecode.doc.costlymethod
     */
    boolean canPlantBulletTree(Direction dir);

    /**
     * Returns whether the robot can hire a gardener in the given direction.
     * Checks dependencies, cooldown turns remaining, bullet costs,
     * whether the robot can build, and that the given direction is
     * not blocked.
     * 
     * @param dir the direction to build in.
     * @return whether it is possible to hire a gardener in the given direction.
     */
    boolean canHireGardener(Direction dir);
    
    /**
     * Hires a Gardener in the given direction. This is a core action.
     *
     * @param dir the direction to spawn the GARDENER unit.
     * @throws GameActionException if the build is bad: if you don't have enough
     * bullets, if you have coreDelay, if the direction is not a good build
     * direction, or if you are not of type ARCHON.
     *
     * @battlecode.doc.costlymethod
     */
    void hireGardener(Direction dir) throws GameActionException;

    /**
     * Plants/Builds a robot of the given type in the given direction.
     *
     * @param dir the direction to spawn the unit.
     * @param type the type of robot to build
     * @throws GameActionException if the build is bad: if you don't have enough
     * bullets, if you have coreDelay, if the direction is not a good build
     * direction, or if you are not of type GARDENER.
     *
     * @battlecode.doc.costlymethod
     */
    void buildRobot(RobotType type, Direction dir) throws GameActionException;

    /**
     * Plants a bullet tree in the given direction. This is a core action.
     *
     * @param dir the direction to plant the bullet tree.
     * @throws GameActionException if the build is bad: if you don't have enough
     * bullets, if you have coreDelay, if the direction is not a good build
     * direction, or if you are not of type GARDENER.
     *
     * @battlecode.doc.costlymethod
     */
    void plantBulletTree(Direction dir) throws  GameActionException;

    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************

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
    void donate(int bullets) throws GameActionException;

    /**
     * Repairs the robot at the given location. The robot must be within one
     * stride. You can only repair once a turn. Note this action can only be
     * performed by ARCHONS and only works on robots, not trees.
     *
     * @param loc the location of the robot to repair.
     * @throws GameActionException if this robot is not an archon, the robot
     * is out of range, if there is no robot there, or if you already repaired
     * this turn.
     *
     * @battlecode.doc.costlymethod
     */
    void repair(MapLocation loc) throws GameActionException;

    /**
     * Repairs the robot with the given id. The robot must be within one
     * stride. You can only repair once a turn. Note this action can only be
     * performed by ARCHONS and only works on robots, not trees.
     *
     * @param id the id of the robot to repair.
     * @throws GameActionException if this robot is not an archon, the robot
     * is out of range, if there is no robot with the given id, or if you
     * already repaired this turn.
     *
     * @battlecode.doc.costlymethod
     */
    void repair(int id) throws GameActionException;

    /**
     * Determines whether or not this robot can repair a robot, taking into
     * account how many times this robot has repaired this turn and this
     * robot's type
     *
     * @return true if this robot can repair a robot, false otherwise.
     */
    boolean canRepair();

    /**
     * Determines whether or not there is a robot at location loc and, if so,
     * if the robot is within one stride of this robot.
     *
     * @param loc the location you wish to test
     * @return true if there is a robot located at loc and if said robot is
     * within one stride of this robot
     *
     * @battlecode.doc.costlymethod
     */
    boolean canInteractWithRobot(MapLocation loc);

    /**
     * Determines whether or not there is a robot with the given id and, if so,
     * if the robot is within one stride of this robot.
     *
     * @param id the id of the robot you wish to test
     * @return true if there is a robot with the given id and if siad robot is
     * within a stride of this robot
     *
     * @battlecode.doc.costlymethod
     */
    boolean canInteractWithRobot(int id);

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
     * bits, you must run the client in lockstep mode and right click the
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
