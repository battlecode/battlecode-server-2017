package battlecode.world;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;

import gnu.trove.procedure.TObjectProcedure;
import net.sf.jsi.SpatialIndex;
import net.sf.jsi.rtree.RTree;
import net.sf.jsi.Rectangle;
import net.sf.jsi.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;


/**
 * This class is used to hold information about the robots, trees, and bullets
 * in the game world.
 */
public strictfp class ObjectInfo {
    private final float mapWidth;
    private final float mapHeight;
    private final MapLocation mapTopLeft;

    private final TIntObjectHashMap<InternalRobot> gameRobotsByID;
    private final TIntObjectHashMap<InternalTree> gameTreesByID;
    private final TIntObjectHashMap<InternalBullet> gameBulletsByID;

    private final SpatialIndex treeIndex;
    private final SpatialIndex robotIndex;
    private final SpatialIndex bulletIndex;

    private final TIntArrayList dynamicBodyExecOrder;

    private Map<Team, Map<RobotType, Integer>> robotTypeCount = new EnumMap<>(
            Team.class);
    private int[] robotCount = new int[3];
    private int[] treeCount = new int[3];

    public ObjectInfo(LiveMap gm){
        this.mapWidth = gm.getWidth();
        this.mapHeight = gm.getHeight();
        this.mapTopLeft = gm.getOrigin();

        this.gameTreesByID = new TIntObjectHashMap<>();
        this.gameRobotsByID = new TIntObjectHashMap<>();
        this.gameBulletsByID = new TIntObjectHashMap<>();

        treeIndex = new RTree();
        robotIndex = new RTree();
        bulletIndex = new RTree();

        dynamicBodyExecOrder = new TIntArrayList();

        treeIndex.init(null);
        robotIndex.init(null);
        bulletIndex.init(null);

        robotTypeCount.put(Team.A, new EnumMap<>(
                RobotType.class));
        robotTypeCount.put(Team.B, new EnumMap<>(
                RobotType.class));
    }

    public int getRobotTypeCount(Team team, RobotType type) {
        if (robotTypeCount.get(team).containsKey(type)) {
            return robotTypeCount.get(team).get(type);
        } else {
            return 0;
        }
    }

    /**
     * Apply an operation for every bullet.
     * Return false to stop iterating.
     * If you call destroyBullet() on a bullet that hasn't been seen yet,
     * that bullet will be silently skipped.
     *
     * @param op a lambda (bullet) -> boolean
     */
    public void eachBullet(TObjectProcedure<InternalBullet> op) {
        // Trove doesn't throw errors when we delete a map entry while iterating
        // it just silently skips the entry later...
        // which is exactly the behaviour we want.
        gameBulletsByID.forEachValue(op);
    }

    /**
     * Apply an operation for every tree.
     * Return false to stop iterating.
     * If you call destroyTree() on a tree that hasn't been seen yet,
     * that tree will be silently skipped.
     *
     * @param op a lambda (bullet) -> void
     */
    public void eachTree(TObjectProcedure<InternalTree> op) {
        gameTreesByID.forEachValue(op);
    }

    /**
     * Apply an operation for every robot, ordered based on robot ID hash (effectively random).
     * Return false to stop iterating.
     * If you call destroyRobot() on a robot that hasn't been seen yet,
     * that robot will be silently skipped.
     *
     * @param op a lambda (bullet) -> void
     */
    public void eachRobot(TObjectProcedure<InternalRobot> op) {
        gameRobotsByID.forEachValue(op);
        //eachRobotBySpawnOrder(op);
    }

    /**
     * Apply an operation for every Bullet and Robot, in the order the
     * bodies should be updated. Robots update in spawn order, and
     * Bullets update immediately before the robot that fired them.
     * Return false to stop iterating.
     *
     * If a body is removed during iteration, the body is cleanly skipped.
     *
     * @param op a lambda (body) -> void
     */
    public void eachDynamicBodyByExecOrder(TObjectProcedure<InternalBody> op) {
        // We can't modify the ArrayList we are looping over
        int[] spawnOrderArray = dynamicBodyExecOrder.toArray();

        for (int id : spawnOrderArray) {
            // Check if body still exists.
            // This can produce bugs if a bullet and a robot can have the same ID.
            if (existsRobot(id)) {
                boolean returnedTrue = op.execute(gameRobotsByID.get(id));
                if (!returnedTrue) {
                    break;
                }
            } else if (existsBullet(id)) {
                boolean returnedTrue = op.execute(gameBulletsByID.get(id));
                if (!returnedTrue) {
                    break;
                }
            } else {
                // The body does not exist, so it was deleted in an earlier
                // iteration and should be skipped.
                continue;
            }
        }
    }

    /**
     * This allocates; prefer eachTree()
     */
    public Collection<InternalTree> trees() {
        return gameTreesByID.valueCollection();
    }

    /**
     * This allocates; prefer eachBullet()
     */
    public Collection<InternalBullet> bullets() {
        return gameBulletsByID.valueCollection();
    }

    /**
     * This allocates; prefer eachRobot()
     */
    public Collection<InternalRobot> robots() {
        return gameRobotsByID.valueCollection();
    }

    /**
     * This allocates; prefer eachTree()
     */
    public InternalTree[] treesArray() {
        return gameTreesByID.values(new InternalTree[gameTreesByID.size()]);
    }

    /**
     * This allocates; prefer eachBullet()
     */
    public InternalBullet[] bulletsArray() {
        return gameBulletsByID.values(new InternalBullet[gameBulletsByID.size()]);
    }

    /**
     * This allocates; prefer eachRobot()
     */
    public InternalRobot[] robotsArray() {
        return gameRobotsByID.values(new InternalRobot[gameRobotsByID.size()]);
    }

    public int getTreeCount(Team team) {
        return treeCount[team.ordinal()];
    }

    public int getRobotCount(Team team) {
        return robotCount[team.ordinal()];
    }

    public InternalTree getTreeByID(int id){
        return gameTreesByID.get(id);
    }

    public InternalRobot getRobotByID(int id){
        return gameRobotsByID.get(id);
    }

    public InternalBullet getBulletByID(int id){
        return gameBulletsByID.get(id);
    }

    public void moveBullet(InternalBullet bullet, MapLocation newLocation) {
        MapLocation loc = bullet.getLocation();

        bulletIndex.delete(fromPoint(loc),bullet.getID());
        bulletIndex.add(fromPoint(newLocation),bullet.getID());
    }

    public void moveRobot(InternalRobot robot, MapLocation newLocation) {
        MapLocation loc = robot.getLocation();

        robotIndex.delete(fromPoint(loc),robot.getID());
        robotIndex.add(fromPoint(newLocation),robot.getID());
    }

    // ****************************
    // *** ADDING OBJECTS *********
    // ****************************

    public void spawnTree(InternalTree tree){
        incrementTreeCount(tree.getTeam());

        int id = tree.getID();
        gameTreesByID.put(id, tree);

        MapLocation loc = tree.getLocation();
        treeIndex.add(fromPoint(loc),tree.getID());
    }

    public void spawnRobot(InternalRobot robot){
        incrementRobotCount(robot.getTeam());
        incrementRobotTypeCount(robot.getTeam(), robot.getType());

        int id = robot.getID();
        gameRobotsByID.put(id, robot);

        dynamicBodyExecOrder.add(id);

        MapLocation loc = robot.getLocation();
        robotIndex.add(fromPoint(loc),robot.getID());
    }

    public void spawnBullet(InternalBullet bullet, InternalRobot parent){
        int id = bullet.getID();
        gameBulletsByID.put(id, bullet);

        // We insert the bullet immediately before its parent (i.e. the robot
        // which fired it). This means that the bullet will first update immediately
        // before its parent next updates, and after any bullets previously fired
        // by this robot have updated again.
        int parentIndex = dynamicBodyExecOrder.indexOf(parent.getID());
        dynamicBodyExecOrder.insert(parentIndex, id);

        MapLocation loc = bullet.getLocation();
        bulletIndex.add(fromPoint(loc),bullet.getID());
    }

    // ****************************
    // *** EXISTS CHECKS **********
    // ****************************

    public boolean existsTree(int id){
        return gameTreesByID.containsKey(id);
    }

    public boolean existsRobot(int id){
        return gameRobotsByID.containsKey(id);
    }

    public boolean existsBullet(int id){
        return gameBulletsByID.containsKey(id);
    }

    // ****************************
    // *** DESTROYING OBJECTS *****
    // ****************************

    public void destroyTree(int id){
        InternalTree tree = getTreeByID(id);
        decrementTreeCount(tree.getTeam());

        MapLocation loc = tree.getLocation();
        gameTreesByID.remove(id);
        treeIndex.delete(fromPoint(loc),id);
    }

    public void destroyRobot(int id){
        InternalRobot robot = getRobotByID(id);
        decrementRobotCount(robot.getTeam());
        decrementRobotTypeCount(robot.getTeam(), robot.getType());

        MapLocation loc = robot.getLocation();
        gameRobotsByID.remove(id);
        dynamicBodyExecOrder.remove(id);
        robotIndex.delete(fromPoint(loc),id);
    }

    public void destroyBullet(int id){
        InternalBullet b = getBulletByID(id);

        MapLocation loc = b.getLocation();
        gameBulletsByID.remove(id);
        dynamicBodyExecOrder.remove(id);
        bulletIndex.delete(fromPoint(loc),id);
    }
    
    // ****************************
    // *** PLAYER METHODS *********
    // ****************************

    public InternalTree[] getAllTreesWithinRadius(MapLocation center, float radius){

        float searchRadius = radius + GameConstants.NEUTRAL_TREE_MAX_RADIUS;

        ArrayList<InternalTree> returnTrees = new ArrayList<InternalTree>();

        treeIndex.nearestN(
                new Point(center.x,center.y),   // Search from center
                new TIntProcedure() {          // Add each to a list
                    public boolean execute(int i) {
                        InternalTree potentialTree = getTreeByID(i);
                        if (potentialTree.getLocation().isWithinDistance(center,potentialTree.getRadius()+radius))
                            returnTrees.add(potentialTree);
                        return true;    // Keep searching for results
                    }
                },
                Integer.MAX_VALUE,
                searchRadius
        );

        return returnTrees.toArray(new InternalTree[returnTrees.size()]);
    }
    
    public InternalRobot[] getAllRobotsWithinRadius(MapLocation center, float radius){

        float searchRadius = radius + GameConstants.MAX_ROBOT_RADIUS;

        ArrayList<InternalRobot> returnRobots = new ArrayList<InternalRobot>();

        robotIndex.nearestN(
                new Point(center.x,center.y),   // Search from center
                new TIntProcedure() {           // Add each to a list
                    public boolean execute(int i) {
                        InternalRobot potentialRobot = getRobotByID(i);
                        if (potentialRobot.getLocation().isWithinDistance(center,potentialRobot.getType().bodyRadius+radius))
                            returnRobots.add(potentialRobot);
                        return true;   // Keep searching for results
                    }
                },
                Integer.MAX_VALUE,
                searchRadius
        );

        return returnRobots.toArray(new InternalRobot[returnRobots.size()]);
    }
    
    public InternalBullet[] getAllBulletsWithinRadius(MapLocation center, float radius){

        ArrayList<InternalBullet> returnBullets = new ArrayList<InternalBullet>();

        // Add each to a list
        bulletIndex.nearestN(
                new Point(center.x,center.y),   // Search from center
                i -> {
                    returnBullets.add(getBulletByID(i));
                    return true;
                },
                Integer.MAX_VALUE,
                radius
        );

        return returnBullets.toArray(new InternalBullet[returnBullets.size()]);
    }
    
    public InternalTree getTreeAtLocation(MapLocation loc){

        // even though it only contains one element, arraylist is required to be accessed from inside TIntProcedure
        ArrayList<InternalTree> returnTrees = new ArrayList<InternalTree>();

        treeIndex.nearestN(
                new Point(loc.x,loc.y),
                i -> {
                    InternalTree potentialTree = getTreeByID(i);
                    if (potentialTree.getLocation().isWithinDistance(loc,potentialTree.getRadius())) {
                        returnTrees.add(potentialTree);
                        return false;
                    }
                    return true;   // keep looking for results
                },
                Integer.MAX_VALUE,
                GameConstants.NEUTRAL_TREE_MAX_RADIUS  // Furthest distance
        );

        if(returnTrees.size() > 0)
            return returnTrees.get(0);
        else
            return null;
    }

    public InternalRobot getRobotAtLocation(MapLocation loc){

        // even though it only contains one element, arraylist is required to be accessed from inside TIntProcedure
        ArrayList<InternalRobot> returnRobots = new ArrayList<InternalRobot>();

        robotIndex.nearestN(
                new Point(loc.x,loc.y),
                new TIntProcedure() {
                    public boolean execute(int i) {
                        InternalRobot potentialRobot = getRobotByID(i);
                        if (potentialRobot.getLocation().isWithinDistance(loc,potentialRobot.getType().bodyRadius)) {
                            returnRobots.add(potentialRobot);
                            return false;
                        }
                        return true;   // keep looking for results
                    }
                },
                Integer.MAX_VALUE,
                GameConstants.MAX_ROBOT_RADIUS  // Furthest distance
        );

        if(returnRobots.size() > 0)
            return returnRobots.get(0);
        else
            return null;
    }

    public boolean isEmpty(MapLocation loc, float radius){
        return getAllTreesWithinRadius(loc, radius).length == 0 &&
                getAllRobotsWithinRadius(loc, radius).length == 0;
    }

    public boolean isEmptyExceptForRobot(MapLocation loc, float radius, InternalRobot robot){
        if (getAllTreesWithinRadius(loc, radius).length != 0)
            return false;
        InternalRobot[] robots = getAllRobotsWithinRadius(loc, radius);
        if (robots.length == 0) {
            return true;
        } else if (robots.length == 1) {
            return robot.equals(robots[0]);
        } else {
            return false;
        }
    }
    
    public boolean noRobotsExceptForRobot(MapLocation loc, float radius, InternalRobot robot){
        InternalRobot[] robots = getAllRobotsWithinRadius(loc, radius);
        if (robots.length == 0) {
            return true;
        } else if (robots.length == 1) {
            return robot.equals(robots[0]);
        } else {
            return false;
        }
    }

    private Rectangle fromPoint(float x, float y) {
        return new Rectangle(x,y,x,y);
    }

    private Rectangle fromPoint(Point p) {
        return new Rectangle(p.x,p.y,p.x,p.y);
    }

    private Rectangle fromPoint(MapLocation loc) {
        return new Rectangle(loc.x,loc.y,loc.x,loc.y);
    }

    // ****************************
    // *** PRIVATE METHODS ********
    // ****************************

    private void incrementRobotCount(Team team) {
        robotCount[team.ordinal()]++;
    }

    private void decrementRobotCount(Team team) {
        robotCount[team.ordinal()]--;
    }

    private void incrementTreeCount(Team team) {
        treeCount[team.ordinal()]++;
    }

    private void decrementTreeCount(Team team) {
        treeCount[team.ordinal()]--;
    }

    private void incrementRobotTypeCount(Team team, RobotType type) {
        if (robotTypeCount.get(team).containsKey(type)) {
            robotTypeCount.get(team).put(type,
                    robotTypeCount.get(team).get(type) + 1);
        } else {
            robotTypeCount.get(team).put(type, 1);
        }
    }

    private void decrementRobotTypeCount(Team team, RobotType type) {
        Integer currentCount = getRobotTypeCount(team, type);
        robotTypeCount.get(team).put(type,currentCount - 1);
    }

}
