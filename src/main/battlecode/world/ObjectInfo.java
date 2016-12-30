package battlecode.world;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;

import gnu.trove.procedure.TIntProcedure;

import net.sf.jsi.SpatialIndex;
import net.sf.jsi.rtree.RTree;
import net.sf.jsi.Rectangle;
import net.sf.jsi.Point;

import java.util.*;

/**
 * This class is used to hold information about the robots, trees, and bullets
 * in the game world.
 */
public strictfp class ObjectInfo {
    private final float mapWidth;
    private final float mapHeight;
    private final MapLocation mapTopLeft;

    private final Map<Integer, InternalRobot> gameRobotsByID;
    private final Map<Integer, InternalTree> gameTreesByID;
    private final Map<Integer, InternalBullet> gameBulletsByID;

    private final SpatialIndex treeIndex;
    private final SpatialIndex robotIndex;
    private final SpatialIndex bulletIndex;

    private Map<Team, Map<RobotType, Integer>> robotTypeCount = new EnumMap<>(
            Team.class);
    private int[] robotCount = new int[3];
    private int[] treeCount = new int[3];

    public ObjectInfo(LiveMap gm){
        this.mapWidth = gm.getWidth();
        this.mapHeight = gm.getHeight();
        this.mapTopLeft = gm.getOrigin();

        this.gameTreesByID = new LinkedHashMap<>();
        this.gameRobotsByID = new LinkedHashMap<>();
        this.gameBulletsByID = new LinkedHashMap<>();

        treeIndex = new RTree();
        robotIndex = new RTree();
        bulletIndex = new RTree();

        treeIndex.init(null);
        robotIndex.init(null);
        bulletIndex.init(null);

        robotTypeCount.put(Team.A, new EnumMap<>(
                RobotType.class));
        robotTypeCount.put(Team.B, new EnumMap<>(
                RobotType.class));
    }

    // ****************************
    // *** GETTING OBJECTS ********
    // ****************************

    public int[] getTreeIDs(){
        return gameTreesByID.keySet().stream()
                .mapToInt(i -> i)
                .toArray();
    }

    public int[] getRobotIDs(){
        return gameRobotsByID.keySet().stream()
                .mapToInt(i -> i)
                .toArray();
    }

    public int[] getBulletIDs(){
        return gameBulletsByID.keySet().stream()
                .mapToInt(i -> i)
                .toArray();
    }

    public int getRobotTypeCount(Team team, RobotType type) {
        if (robotTypeCount.get(team).containsKey(type)) {
            return robotTypeCount.get(team).get(type);
        } else {
            return 0;
        }
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

    public InternalTree[] getAllTrees(){
        return gameTreesByID.values().toArray(new InternalTree[gameTreesByID.size()]);
    }

    public InternalRobot[] getAllRobots(){
        return gameRobotsByID.values().toArray(new InternalRobot[gameRobotsByID.size()]);
    }

    public InternalBullet[] getAllBullets(){
        return gameBulletsByID.values().toArray(new InternalBullet[gameBulletsByID.size()]);
    }

    public void moveBullet(InternalBullet bullet, MapLocation newLocation) {
        MapLocation loc = bullet.getLocation();

        bulletIndex.delete(fromPoint(loc),bullet.getID());
        bulletIndex.add(fromPoint(newLocation),bullet.getID());
    }

    public void moveRobot(InternalRobot robot, MapLocation newLocation) {
        MapLocation loc = robot.getLocation();

        bulletIndex.delete(fromPoint(loc),robot.getID());
        bulletIndex.add(fromPoint(newLocation),robot.getID());
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

        MapLocation loc = robot.getLocation();
        robotIndex.add(fromPoint(loc),robot.getID());
    }

    public void spawnBullet(InternalBullet bullet){
        int id = bullet.getID();
        gameBulletsByID.put(id, bullet);

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
        robotIndex.delete(fromPoint(loc),id);
    }

    public void destroyBullet(int id){
        InternalBullet b = getBulletByID(id);

        MapLocation loc = b.getLocation();
        gameBulletsByID.remove(id);
        bulletIndex.delete(fromPoint(loc),id);
    }
    
    // ****************************
    // *** PLAYER METHODS *********
    // ****************************

    public InternalTree[] getAllTreesWithinRadius(MapLocation center, float radius){

        float actualRadius = radius + GameConstants.NEUTRAL_TREE_MAX_RADIUS;

        ArrayList<InternalTree> returnTrees = new ArrayList<InternalTree>();

        treeIndex.nearestNUnsorted(
                new Point(center.x,center.y),   // Search from center
                new TIntProcedure() {          // Add each to a list
                    public boolean execute(int i) {
                        returnTrees.add(getTreeByID(i));
                        return true;
                    }
                },
                Integer.MAX_VALUE,
                actualRadius
        );

        return returnTrees.toArray(new InternalTree[returnTrees.size()]);
    }
    
    public InternalRobot[] getAllRobotsWithinRadius(MapLocation center, float radius){

        float actualRadius = radius + GameConstants.MAX_ROBOT_RADIUS;

        ArrayList<InternalRobot> returnRobots = new ArrayList<InternalRobot>();

        robotIndex.nearestNUnsorted(
                new Point(center.x,center.y),   // Search from center
                new TIntProcedure() {           // Add each to a list
                    public boolean execute(int i) {
                        returnRobots.add(getRobotByID(i));
                        return true;
                    }
                },
                Integer.MAX_VALUE,
                actualRadius
        );

        return returnRobots.toArray(new InternalRobot[returnRobots.size()]);
    }
    
    public InternalBullet[] getAllBulletsWithinRadius(MapLocation center, float radius){

        ArrayList<InternalBullet> returnBullets = new ArrayList<InternalBullet>();

        treeIndex.nearestNUnsorted(
                new Point(center.x,center.y),   // Search from center
                new TIntProcedure() {           // Add each to a list
                    public boolean execute(int i) {
                        returnBullets.add(getBulletByID(i));
                        return true;
                    }
                },
                Integer.MAX_VALUE,
                radius
        );

        return returnBullets.toArray(new InternalBullet[returnBullets.size()]);
    }
    
    public InternalTree getTreeAtLocation(MapLocation loc){

        // even though it only contains one element, arraylist is required to be accessed from inside TIntProcedure
        ArrayList<InternalTree> returnTrees = new ArrayList<InternalTree>();

        treeIndex.nearest(
                new Point(loc.x,loc.y),
                new TIntProcedure() {
                    public boolean execute(int i) {
                        InternalTree potentialTree = getTreeByID(i);
                        if (potentialTree.getLocation().isWithinDistance(loc,potentialTree.getRadius()))
                            returnTrees.add(potentialTree);
                        return false;   // Don't need any more results
                    }
                },
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

        treeIndex.nearest(
                new Point(loc.x,loc.y),
                new TIntProcedure() {
                    public boolean execute(int i) {
                        InternalRobot potentialRobot = getRobotByID(i);
                        if (potentialRobot.getLocation().isWithinDistance(loc,potentialRobot.getType().bodyRadius))
                            returnRobots.add(potentialRobot);
                        return false;   // Don't need any more results
                    }
                },
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
