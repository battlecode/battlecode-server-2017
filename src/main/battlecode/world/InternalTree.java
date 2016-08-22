package battlecode.world;

import battlecode.common.*;

/**
 * The representation of a tree used by the server.
 */
public class InternalTree {
    private final GameWorld gameWorld;

    private final int ID;
    private Team team;
    private float radius;
    private MapLocation location;
    private float health;

    private float containedBullets;
    private RobotType containedRobot;

    private int roundsAlive;

    /**
     * Used to avoid recreating the same TreeInfo object over and over.
     */
    private TreeInfo cachedTreeInfo;

    public InternalTree(GameWorld gw, int id, Team team, float radius, MapLocation center,
                        float containedBullets, RobotType containedRobot) {
        this.gameWorld = gw;

        this.ID = id;
        this.team = team;
        this.radius = radius;
        this.location = center;

        if(team == Team.NEUTRAL){
            this.health = GameConstants.NEUTRAL_TREE_HEALTH_RATE * radius;
        }else{
            this.health = .20F * GameConstants.BULLET_TREE_MAX_HEALTH;
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

    public float getRadius() {
        return radius;
    }

    public MapLocation getLocation() {
        return location;
    }

    public float getHealth() {
        return health;
    }

    public float getContainedBullets() {
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

    public void resetContainedBullets(){
        this.containedBullets = 0;
    }

    private void keepMinHealth(){
        if(health < 0){
            this.health = 0;
        }
    }

    private void keepMaxHealth(){
        if(health > 100){
            this.health = 100;
        }
    }

    public void damageTree(float damage, Team hitBy){
        this.health -= damage;
        keepMinHealth();
        killTreeIfDead(hitBy);
    }

    public void decayTree(){
        damageTree(1f, Team.NEUTRAL);
    }

    public void growTree(){
        this.health += 1;
        keepMaxHealth();
    }

    public void waterTree(){
        this.health += 10;
        keepMaxHealth();
    }

    public boolean killTreeIfDead(Team destroyedBy){
        if(health == 0){
            gameWorld.destroyTree(ID, destroyedBy);
            return true;
        }
        return false;
    }

    public float updateTree(){
        if(roundsAlive <= 80){
            growTree();
            return 0;
        }

        decayTree();
        return this.health * .1F;
    }

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
        return String.format("%s:#%d", getTeam(), getID());
    }

}
