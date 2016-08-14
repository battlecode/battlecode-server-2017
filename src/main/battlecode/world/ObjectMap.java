package battlecode.world;

import battlecode.common.MapLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This class is used to hold information about what robots, trees, and bullets
 * are on the map.
 */
public class ObjectMap {

    private final int mapWidth;
    private final int mapHeight;
    private final MapLocation mapTopLeft;

    private final Map<Integer, InternalRobot> gameRobotsByID;
    private final Map<Integer, InternalTree> gameTreesByID;
    private final Map<Integer, InternalBullet> gameBulletsByID;

    private final Set<Integer>[][] treeLocations;
    private final Set<Integer>[][] robotLocations;
    private final Set<Integer>[][] bulletLocations;

    public ObjectMap(GameMap gm){
        this.mapWidth = gm.getWidth();
        this.mapHeight = gm.getHeight();
        this.mapTopLeft = gm.getOrigin();

        this.gameTreesByID = new LinkedHashMap<>();
        this.gameRobotsByID = new LinkedHashMap<>();
        this.gameBulletsByID = new LinkedHashMap<>();

        this.treeLocations = new Set[mapHeight][mapWidth];
        this.robotLocations = new Set[mapHeight][mapWidth];
        this.bulletLocations = new Set[mapHeight][mapWidth];
    }

    // ****************************
    // *** GETTING OBJECTS ********
    // ****************************

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
        int id = tree.getID();
        gameRobotsByID.put(id, tree);

        MapLocation loc = tree.getLocation();
        int xIndex = (int) Math.floor(loc.x - mapTopLeft.x);
        int yIndex = (int) Math.floor(loc.y - mapTopLeft.y);
        treeLocations[yIndex][xIndex].add(id);
    }

    public void spawnRobot(InternalRobot robot){
        int id = robot.getID();
        gameRobotsByID.put(id, robot);

        MapLocation loc = robot.getLocation();
        int xIndex = (int) Math.floor(loc.x - mapTopLeft.x);
        int yIndex = (int) Math.floor(loc.y - mapTopLeft.y);
        robotLocations[yIndex][xIndex].add(id);
    }

    public void spawnBullet(InternalBullet bullet){
        int id = bullet.getID();
        gamebulletsByID.put(id, bullet);

        MapLocation loc = bullet.getLocation();
        int xIndex = (int) Math.floor(loc.x - mapTopLeft.x);
        int yIndex = (int) Math.floor(loc.y - mapTopLeft.y);
        bulletLocations[yIndex][xIndex].add(id);
    }
}
