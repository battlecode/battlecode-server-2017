package battlecode.world;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;

import java.util.*;

/**
 * This class is used to hold information about the robots, trees, and bullets
 * in the game world.
 */
public class ObjectInfo {
    private final float mapWidth;
    private final float mapHeight;
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

    public ObjectInfo(LiveMap gm){
        this.mapWidth = gm.getWidth();
        this.mapHeight = gm.getHeight();
        this.mapTopLeft = gm.getOrigin();

        this.gameTreesByID = new LinkedHashMap<>();
        this.gameRobotsByID = new LinkedHashMap<>();
        this.gameBulletsByID = new LinkedHashMap<>();

        this.treeLocations = new Set[(int) mapHeight][(int) mapWidth];
        this.robotLocations = new Set[(int) mapHeight][(int) mapWidth];
        this.bulletLocations = new Set[(int) mapHeight][(int) mapWidth];

        for(int i = 0; i < (int) mapHeight; i++){
            for(int j = 0; j < (int) mapWidth; j++){
                this.treeLocations[i][j] = new HashSet<>();
                this.robotLocations[i][j] = new HashSet<>();
                this.bulletLocations[i][j] = new HashSet<>();
            }
        }

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
        int xIndex = convertToXIndex(loc.x);
        int yIndex = convertToYIndex(loc.y);

        bulletLocations[yIndex][xIndex].remove(bullet.getID());

        int newXIndex = convertToXIndex(newLocation.x);
        int newYIndex = convertToYIndex(newLocation.y);

        bulletLocations[newYIndex][newXIndex].add(bullet.getID());
    }

    public void moveRobot(InternalRobot robot, MapLocation newLocation) {
        MapLocation loc = robot.getLocation();
        int xIndex = convertToXIndex(loc.x);
        int yIndex = convertToYIndex(loc.y);

        robotLocations[yIndex][xIndex].remove(robot.getID());

        int newXIndex = convertToXIndex(newLocation.x);
        int newYIndex = convertToYIndex(newLocation.y);

        robotLocations[newYIndex][newXIndex].add(robot.getID());

    }

    // ****************************
    // *** ADDING OBJECTS *********
    // ****************************

    public void spawnTree(InternalTree tree){
        incrementTreeCount(tree.getTeam());

        int id = tree.getID();
        gameTreesByID.put(id, tree);

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
        gameBulletsByID.put(id, bullet);

        MapLocation loc = bullet.getLocation();

        int xIndex = convertToXIndex(loc.x);
        int yIndex = convertToYIndex(loc.y);
        bulletLocations[yIndex][xIndex].add(id);
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
        InternalBullet b = getBulletByID(id);

        MapLocation loc = b.getLocation();

        int xIndex = convertToXIndex(loc.x);
        int yIndex = convertToYIndex(loc.y);
        gameBulletsByID.remove(id);
        bulletLocations[yIndex][xIndex].remove(id);
    }
    
    // ****************************
    // *** PLAYER METHODS *********
    // ****************************

    public InternalTree[] getAllTreesWithinRadius(MapLocation center, float radius){
        if (radius <= 0) {
            return new InternalTree[] { getTreeAtLocation(center) };
        }
        radius = (float) Math.ceil(radius);

        ArrayList<InternalTree> trees = new ArrayList<>();
        int minXPos = convertToXIndex(center.x - radius - GameConstants.NEUTRAL_TREE_MAX_RADIUS);
        int maxXPos = convertToXIndex(center.x + radius + GameConstants.NEUTRAL_TREE_MAX_RADIUS);
        int minYPos = convertToYIndex(center.y - radius - GameConstants.NEUTRAL_TREE_MAX_RADIUS);
        int maxYPos = convertToYIndex(center.y + radius + GameConstants.NEUTRAL_TREE_MAX_RADIUS);

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
        if (radius <= 0) {
            return new InternalRobot[] { getRobotAtLocation(center) };
        }
        radius = (float) Math.ceil(radius);

        ArrayList<InternalRobot> robots = new ArrayList<>();
        int minXPos = convertToXIndex(center.x - radius - GameConstants.MAX_ROBOT_RADIUS);
        int maxXPos = convertToXIndex(center.x + radius + GameConstants.MAX_ROBOT_RADIUS);
        int minYPos = convertToYIndex(center.y - radius - GameConstants.MAX_ROBOT_RADIUS);
        int maxYPos = convertToYIndex(center.y + radius + GameConstants.MAX_ROBOT_RADIUS);

        for (int x = minXPos; x <= maxXPos; x++) {
            for (int y = minYPos; y <= maxYPos; y++) {
                if(inBounds(x, y)){
                    for(int robotID : robotLocations[y][x]){
                        InternalRobot robot = getRobotByID(robotID);
                        if (robot == null) {
                            throw new RuntimeException("NULL ROBOT: "+robotID);
                        }
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
        if (radius <= 0) {
            return new InternalBullet[0];
        }
        radius = (float) Math.ceil(radius);

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
        int minXPos = convertToXIndex(loc.x - GameConstants.NEUTRAL_TREE_MAX_RADIUS);
        int maxXPos = convertToXIndex(loc.x + GameConstants.NEUTRAL_TREE_MAX_RADIUS);
        int minYPos = convertToYIndex(loc.y - GameConstants.NEUTRAL_TREE_MAX_RADIUS);
        int maxYPos = convertToYIndex(loc.y + GameConstants.NEUTRAL_TREE_MAX_RADIUS);

        for (int x = minXPos; x <= maxXPos; x++) {
            for (int y = minYPos; y <= maxYPos; y++) {
                if(inBounds(x,y)) {
                    for (int treeID : treeLocations[x][y]) {
                        InternalTree tree = gameTreesByID.get(treeID);
                        if (tree.getLocation().isWithinDistance(loc, tree.getRadius())) {
                            return tree;
                        }
                    }
                }
            }
        }
        return null;
    }

    public InternalRobot getRobotAtLocation(MapLocation loc){
        int minXPos = convertToXIndex(loc.x - GameConstants.NEUTRAL_TREE_MAX_RADIUS);
        int maxXPos = convertToXIndex(loc.x + GameConstants.NEUTRAL_TREE_MAX_RADIUS);
        int minYPos = convertToYIndex(loc.y - GameConstants.NEUTRAL_TREE_MAX_RADIUS);
        int maxYPos = convertToYIndex(loc.y + GameConstants.NEUTRAL_TREE_MAX_RADIUS);

        for (int x = minXPos; x <= maxXPos; x++) {
            for (int y = minYPos; y <= maxYPos; y++) {
                if(inBounds(x,y)) {
                    for (int robotID : robotLocations[x][y]) {
                        InternalRobot robot = gameRobotsByID.get(robotID);
                        if (robot.getLocation().isWithinDistance(loc, robot.getType().bodyRadius)) {
                            return robot;
                        }
                    }
                }
            }
        }
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
