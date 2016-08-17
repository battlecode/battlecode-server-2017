package battlecode.world;

import battlecode.common.*;

/**
 * The representation of a tree used by the server.
 */
public class InternalTree {
    private final GameWorld gameWorld;

    private final int ID;
    private Team team;
    private double radius;
    private MapLocation location;
    private double health;

    private double containedBullets;
    private RobotType containedRobot;

    private int roundsAlive;

    /**
     * Used to avoid recreating the same TreeInfo object over and over.
     */
    private TreeInfo cachedTreeInfo;

    public InternalTree(GameWorld gw, int id, Team team, double radius, MapLocation center,
                        double containedBullets, RobotType containedRobot) {
        this.gameWorld = gw;

        this.ID = id;
        this.team = team;
        this.radius = radius;
        this.location = center;

        if(team == Team.NEUTRAL){
            this.health = GameConstants.NEUTRAL_TREE_HEALTH_RATE * radius;
        }else{
            this.health = .20 * GameConstants.BULLET_TREE_MAX_HEALTH;
        }

        this.containedBullets = containedBullets;
        this.containedRobot = containedRobot;

        this.roundsAlive = 0;
    }

}
