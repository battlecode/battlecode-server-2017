package battlecode.world;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;

import java.util.*;

/**
 * This class is used to hold information about the robots, trees, and bullets
 * in the game world.
 */
public class ObjectInfo {
    private static final float MAX_TREE_RADIUS = 5;
    private static final float MAX_ROBOT_RADIUS = 2;

    private final int mapWidth;
    private final int mapHeight;
    private final MapLocation mapTopLeft;

    private final Map<Integer, InternalRobot> gameRobotsByID;
    private final Map<Integer, InternalTree> gameTreesByID;
    private final Map<Integer, InternalBullet> gameBulletsByID;

    private final Set<Integer>[][] treeLocations;
    private final Set<Integer>[][] robotLocations;
    private final Set<Integer>[][] bulletLocations;

    private Map<Team, Map<RobotType, Integer>> robotTypeCount = new EnumMap<>(
            Team.class);
    private int[] robotCount = new int[3];
    private int[] treeCount = new int[3];

    public ObjectInfo(GameMap gm){
        this.mapWidth = gm.getWidth();
        this.mapHeight = gm.getHeight();
        this.mapTopLeft = gm.getOrigin();

        this.gameTreesByID = new LinkedHashMap<>();
        this.gameRobotsByID = new LinkedHashMap<>();
        this.gameBulletsByID = new LinkedHashMap<>();

        this.treeLocations = new Set[mapHeight][mapWidth];
        this.robotLocations = new Set[mapHeight][mapWidth];
        this.bulletLocations = new Set[mapHeight][mapWidth];

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

    // ****************************
    // *** ADDING OBJECTS *********
    // ****************************

    public void spawnTree(InternalTree tree){
        incrementTreeCount(tree.getTeam());

        int id = tree.getID();
        gameRobotsByID.put(id, tree);

        MapLocation loc = tree.getLocation();
        int xIndex = convertToXIndex(loc.x);
        int yIndex = convertToYIndex(loc.y);
        treeLocations[yIndex][xIndex].add(id);
    }

    public void spawnRobot(InternalRobot robot){
        incrementRobotCount(robot.getTeam());
        incrementRobotTypeCount(robot.getTeam(), robot.getType());

        int id = robot.getID();
        gameRobotsByID.put(id, robot);

        MapLocation loc = robot.getLocation();
        int xIndex = convertToXIndex(loc.x);
        int yIndex = convertToYIndex(loc.y);
        robotLocations[yIndex][xIndex].add(id);
    }

    public void spawnBullet(InternalBullet bullet){
        int id = bullet.getID();
        gamebulletsByID.put(id, bullet);

        MapLocation loc = bullet.getLocation();
        int xIndex = convertToXIndex(loc.x);
        int yIndex = convertToYIndex(loc.y);
        bulletLocations[yIndex][xIndex].add(id);
    }

    // ****************************
    // *** DESTROYING OBJECTS *****
    // ****************************

    public void destroyTree(int id){
        InternalTree tree = getTreeByID(id);
        decrementTreeCount(tree.getTeam());
        
        MapLocation loc = tree.getLocation();
        int xIndex = convertToXIndex(loc.x);
        int yIndex = convertToYIndex(loc.y);
        
        gameTreesByID.remove(id);
        treeLocations[yIndex][xIndex].remove(id);
    }

    public void destroyRobot(int id){
        InternalRobot robot = getRobotByID(id);
        decrementRobotCount(robot.getTeam());
        decrementRobotTypeCount(robot.getTeam(), robot.getType());

        MapLocation loc = robot.getLocation();
        int xIndex = convertToXIndex(loc.x);
        int yIndex = convertToYIndex(loc.y);

        gameRobotsByID.remove(id);
        robotLocations[yIndex][xIndex].remove(id);
    }

    public void destroyBullet(int id){
        MapLocation loc = getBulletByID(id).getLocation();
        int xIndex = convertToXIndex(loc.x);
        int yIndex = convertToYIndex(loc.y);

        gameBulletsByID.remove(id);
        bulletLocations[yIndex][xIndex].remove(id);
    }
    
    // ****************************
    // *** PLAYER METHODS *********
    // ****************************

    public boolean onTheMap(MapLocation loc, float radius){
        return (onTheMap(loc.translate(-radius, 0)) &&
                onTheMap(loc.translate(radius, 0)) &&
                onTheMap(loc.translate(0, -radius)) &&
                onTheMap(loc.translate(0, radius)));
    }

    public boolean onTheMap(MapLocation loc){
        return onTheMap(loc.x, loc.y);
    }

    public boolean onTheMap(float x, float y){
        return inBounds(x - mapTopLeft.x, y - mapTopLeft.y);
    }

    public InternalTree[] getAllTreesWithinRadius(MapLocation center, float radius){
        ArrayList<InternalTree> trees = new ArrayList<>();
        int minXPos = convertToXIndex(center.x - radius - MAX_TREE_RADIUS);
        int maxXPos = convertToXIndex(center.x + radius + MAX_TREE_RADIUS);
        int minYPos = convertToYIndex(center.y - radius - MAX_TREE_RADIUS);
        int maxYPos = convertToYIndex(center.y + radius + MAX_TREE_RADIUS);

        for (int x = minXPos; x <= maxXPos; x++) {
            for (int y = minYPos; y <= maxYPos; y++) {
                if(inBounds(x, y)){
                    for(int treeID : treeLocations[y][x]){
                        InternalTree tree = getTreeByID(treeID);
                        if(center.isWithinDistance(tree.getLocation(), radius)){
                            trees.add(tree);
                        }
                    }
                }
            }
        }
        return trees.toArray(new InternalTree[trees.size()]);
    }
    
    public InternalRobot[] getAllRobotsWithinRadius(MapLocation center, float radius){
        ArrayList<InternalRobot> robots = new ArrayList<>();
        int minXPos = convertToXIndex(center.x - radius - MAX_ROBOT_RADIUS);
        int maxXPos = convertToXIndex(center.x + radius + MAX_ROBOT_RADIUS);
        int minYPos = convertToYIndex(center.y - radius - MAX_ROBOT_RADIUS);
        int maxYPos = convertToYIndex(center.y + radius + MAX_ROBOT_RADIUS);

        for (int x = minXPos; x <= maxXPos; x++) {
            for (int y = minYPos; y <= maxYPos; y++) {
                if(inBounds(x, y)){
                    for(int robotID : robotLocations[y][x]){
                        InternalRobot robot = getRobotByID(robotID);
                        if(center.isWithinDistance(robot.getLocation(), radius)){
                            robots.add(robot);
                        }
                    }
                }
            }
        }
        return robots.toArray(new InternalRobot[robots.size()]);
    }
    
    public InternalBullet[] getAllBulletsWithinRadius(MapLocation center, float radius){
        ArrayList<InternalBullet> bullets = new ArrayList<>();
        int minXPos = convertToXIndex(center.x - radius);
        int maxXPos = convertToXIndex(center.x + radius);
        int minYPos = convertToYIndex(center.y - radius);
        int maxYPos = convertToYIndex(center.y + radius);

        for (int x = minXPos; x <= maxXPos; x++) {
            for (int y = minYPos; y <= maxYPos; y++) {
                if(inBounds(x, y)){
                    for(int bulletID : bulletLocations[y][x]){
                        InternalBullet bullet = getBulletByID(bulletID);
                        if(center.isWithinDistance(bullet.getLocation(), radius)){
                            bullets.add(bullet);
                        }
                    }
                }
            }
        }
        return bullets.toArray(new InternalBullet[bullets.size()]);
    }
    
    public InternalTree getTreeAtLocation(MapLocation loc){
        InternalTree[] trees = getAllTreesWithinRadius(loc, 0);
        return trees.length == 0 ? null : trees[0];
    }

    public InternalRobot getRobotAtLocation(MapLocation loc){
        InternalRobot[] robots = getAllRobotsWithinRadius(loc, 0);
        return robots.length == 0 ? null : robots[0];
    }

    public boolean isEmpty(MapLocation loc, float radius){
        return  onTheMap(loc, radius) &&
                getAllTreesWithinRadius(loc, radius).length == 0 &&
                getAllRobotsWithinRadius(loc, radius).length == 0;
    }

    // ****************************
    // *** PRIVATE METHODS ********
    // ****************************

    private boolean inBounds(float x, float y){
        return (x >= 0 && x < mapWidth &&
                y >= 0 && y < mapHeight);
    }

    private int convertToXIndex(double x){
        return (int) Math.floor(x - mapTopLeft.x);
    }

    private int convertToYIndex(double y){
        return (int) Math.floor(y - mapTopLeft.y);
    }

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
