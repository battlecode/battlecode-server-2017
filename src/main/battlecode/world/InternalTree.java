package battlecode.world;

import battlecode.common.*;

/**
 * The representation of a tree used by the server.
 */
public strictfp class InternalTree {
    private final GameWorld gameWorld;

    private final int ID;
    private Team team;
    private float radius;
    private MapLocation location;
    private float health;
    private final float maxHealth;

    private int containedBullets;
    private RobotType containedRobot;

    private int roundsAlive;
    private boolean healthChanged;

    /**
     * Used to avoid recreating the same TreeInfo object over and over.
     */
    private TreeInfo cachedTreeInfo;

    public InternalTree(GameWorld gw, int id, Team team, float radius, MapLocation center,
                        int containedBullets, RobotType containedRobot) {
        this.gameWorld = gw;

        this.ID = id;
        this.team = team;
        this.radius = radius;
        this.location = center;

        if(team == Team.NEUTRAL){
            this.health = GameConstants.NEUTRAL_TREE_HEALTH_RATE * radius;
            this.maxHealth = GameConstants.NEUTRAL_TREE_HEALTH_RATE * radius;
        } else {
            this.health = .20F * GameConstants.BULLET_TREE_MAX_HEALTH;
            this.maxHealth = GameConstants.BULLET_TREE_MAX_HEALTH;
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

    public float getMaxHealth() {
        return maxHealth;
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
        if(health > getMaxHealth()){
            this.health = getMaxHealth();
        }
    }

    public void damageTree(float damage, Team hitBy, boolean fromChop){
        this.health -= damage;
        this.healthChanged = true;
        keepMinHealth();
        killTreeIfDead(hitBy,fromChop);
    }

    public void healTree(float healAmount){
        this.health += healAmount;
        this.healthChanged = true;
        keepMaxHealth();
    }

    public void decayTree(){
        damageTree(GameConstants.BULLET_TREE_DECAY_RATE, Team.NEUTRAL, false);
    }

    public void growTree(){
        healTree(GameConstants.BULLET_TREE_MAX_HEALTH*0.01f);
    }

    public void waterTree(){
        healTree(GameConstants.WATER_HEALTH_REGEN_RATE);
    }

    public boolean killTreeIfDead(Team destroyedBy, boolean fromChop){
        if(health == 0){
            gameWorld.destroyTree(ID, destroyedBy, fromChop);
            return true;
        }
        return false;
    }

    public float updateTree(){
        if(getTeam() == Team.NEUTRAL){
            return 0;
        }
        if(getRoundsAlive() <= 80){
            growTree();
            return 0;
        }

        float bulletIncome = this.health * GameConstants.BULLET_TREE_BULLET_PRODUCTION_RATE;
        decayTree();
        return bulletIncome;
    }

    public void processBeginningOfRound(){
        this.healthChanged = false;
    }

    public void processEndOfRound(){
        if(this.healthChanged){
            gameWorld.getMatchMaker().addHealthChanged(getID(), getHealth());
        }
        roundsAlive++;
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
