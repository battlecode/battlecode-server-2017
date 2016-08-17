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

    // ******************************************
    // ****** GETTER METHODS ********************
    // ******************************************

    public GameWorld getGameWorld() {
        return gameWorld;
    }

    public int getID() {
        return ID;
    }

    public Team getTeam() {
        return team;
    }

    public double getRadius() {
        return radius;
    }

    public MapLocation getLocation() {
        return location;
    }

    public double getHealth() {
        return health;
    }

    public double getContainedBullets() {
        return containedBullets;
    }

    public RobotType getContainedRobot() {
        return containedRobot;
    }

    public int getRoundsAlive() {
        return roundsAlive;
    }

    public TreeInfo getTreeInfo() {
        if (this.cachedTreeInfo != null
                && this.cachedTreeInfo.ID == ID
                && this.cachedTreeInfo.team == team
                && this.cachedTreeInfo.radius == radius
                && this.cachedTreeInfo.location.equals(location)
                && this.cachedTreeInfo.containedBullets == containedBullets
                && this.cachedTreeInfo.containedRobot == containedRobot
                && this.cachedTreeInfo.health == health) {
            return this.cachedTreeInfo;
        }
        return this.cachedTreeInfo = new TreeInfo(
                ID, team, location, radius, health, containedBullets, containedRobot);
    }

    // ******************************************
    // ****** UPDATE METHODS ********************
    // ******************************************

    // *********************************
    // ****** MISC. METHODS ************
    // *********************************

    @Override
    public boolean equals(Object o) {
        return o != null && (o instanceof InternalTree)
                && ((InternalTree) o).getID() == ID;
    }

    @Override
    public int hashCode() {
        return ID;
    }

    @Override
    public String toString() {
        return String.format("%s:%s#%d", getTeam(), getType(), getID());
    }

}
