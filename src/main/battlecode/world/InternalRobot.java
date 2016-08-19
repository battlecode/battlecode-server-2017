package battlecode.world;

import battlecode.common.*;

/**
 * The representation of a robot used by the server.
 */
public class InternalRobot {
    private final RobotControllerImpl controller;
    private final GameWorld gameWorld;

    private final int ID;
    private Team team;
    private RobotType type;
    private MapLocation location;
    private float weaponDelay;
    private float coreDelay;
    private float health;

    private long controlBits;
    private int bytecodesUsed;
    private int prevBytecodesUsed;

    private int roundsAlive;
    private int repairCount;
    private int shakeCount;
    private int waterCount;

    /**
     * Used to avoid recreating the same RobotInfo object over and over.
     */
    private RobotInfo cachedRobotInfo;

    /**
     * Create a new internal representation of a robot
     *
     * @param gw the world the robot exists in
     * @param type the type of the robot
     * @param loc the location of the robot
     * @param team the team of the robot
     */
    @SuppressWarnings("unchecked")
    public InternalRobot(GameWorld gw, int id, RobotType type, MapLocation loc, Team team) {
        this.ID = id;
        this.team = team;
        this.type = type;
        this.location = loc;
        this.weaponDelay = 0;
        this.coreDelay = 0;

        if(type == RobotType.ARCHON || type == RobotType.GARDENER){
            this.health = type.maxHealth;
        }else{
            this.health = .20F * type.maxHealth;
        }

        this.controlBits = 0;
        this.bytecodesUsed = 0;
        this.prevBytecodesUsed = 0;

        this.roundsAlive = 0;
        this.repairCount = 0;
        this.shakeCount = 0;
        this.waterCount = 0;

        this.gameWorld = gw;
        this.controller = new RobotControllerImpl(gameWorld, this);
    }

    // ******************************************
    // ****** GETTER METHODS ********************
    // ******************************************

    public RobotControllerImpl getController() {
        return controller;
    }

    public GameWorld getGameWorld() {
        return gameWorld;
    }

    public int getID() {
        return ID;
    }

    public Team getTeam() {
        return team;
    }

    public RobotType getType() {
        return type;
    }

    public MapLocation getLocation() {
        return location;
    }

    public float getWeaponDelay() {
        return weaponDelay;
    }

    public float getCoreDelay() {
        return coreDelay;
    }

    public float getHealth() {
        return health;
    }

    public long getControlBits() {
        return controlBits;
    }

    public int getBytecodesUsed() {
        return bytecodesUsed;
    }

    public int getPrevBytecodesUsed() {
        return prevBytecodesUsed;
    }

    public int getRoundsAlive() {
        return roundsAlive;
    }

    public int getRepairCount() {
        return repairCount;
    }

    public int getShakeCount() {
        return shakeCount;
    }

    public int getWaterCount() {
        return waterCount;
    }

    public RobotInfo getRobotInfo() {
        if (this.cachedRobotInfo != null
                && this.cachedRobotInfo.ID == ID
                && this.cachedRobotInfo.team == team
                && this.cachedRobotInfo.type == type
                && this.cachedRobotInfo.location.equals(location)
                && this.cachedRobotInfo.coreDelay == coreDelay
                && this.cachedRobotInfo.weaponDelay == weaponDelay
                && this.cachedRobotInfo.health == health) {
            return this.cachedRobotInfo;
        }
        return this.cachedRobotInfo = new RobotInfo(
                ID, team, type, location, coreDelay, weaponDelay, health);
    }

    // ******************************************
    // ****** UPDATE METHODS ********************
    // ******************************************

    private void keepMinHealth(){
        if(health < 0){
            this.health = 0;
        }
    }

    private void keepMaxHealth(){
        if(health > this.type.maxHealth){
            this.health = this.type.maxHealth;
        }
    }

    public void setBytecodesUsed(int bytecodesUsed){
        this.bytecodesUsed = bytecodesUsed;
    }

    public void damageRobot(float damage){
        this.health -= damage;
        keepMinHealth();
        killRobotIfDead();
    }

    public boolean killRobotIfDead(){
        if(this.health == 0){
            gameWorld.destroyRobot(this.ID);
            return true;
        }
        return false;
    }

    // *********************************
    // ****** DELAYS METHODS ***********
    // *********************************

    public void addCoreDelay(float time) {
        coreDelay += time;
    }

    public void addWeaponDelay(float time) {
        weaponDelay += time;
    }

    public void setCoreDelayUpTo(float delay) {
        coreDelay = Math.max(coreDelay, delay);
    }

    public void setWeaponDelayUpTo(float delay) {
        weaponDelay = Math.max(weaponDelay, delay);
    }

    public void decrementDelays() {
        // Formula following the "Explanation of Delays" section of game specs
        // (Use previous bytecodes because current bytecode = 0)
        float amountToDecrement = 1.0F - (0.3F * (float) Math.pow(
                Math.max(0,8000-this.type.bytecodeLimit+this.prevBytecodesUsed)/8000.0,1.5));
        
        weaponDelay-=amountToDecrement;
        coreDelay-=amountToDecrement;

        if (weaponDelay < 0.0) {
            weaponDelay = 0;
        }
        if (coreDelay < 0.0) {
            coreDelay = 0;
        }
    }

    // *********************************
    // ****** GAMEPLAY METHODS *********
    // *********************************

    // should be called at the beginning of every round
    public void processBeginningOfRound() {
    }

    public void processBeginningOfTurn() {
        decrementDelays();
        repairCount = 0;
    }

    public void processEndOfTurn() {
        this.prevBytecodesUsed = this.bytecodesUsed;
        roundsAlive++;
    }

    public void processEndOfRound() {}

    // *********************************
    // ****** MISC. METHODS ************
    // *********************************

    @Override
    public boolean equals(Object o) {
        return o != null && (o instanceof InternalRobot)
                && ((InternalRobot) o).getID() == ID;
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
