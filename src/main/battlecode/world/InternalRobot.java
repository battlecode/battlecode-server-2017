package battlecode.world;

import battlecode.common.*;
import battlecode.schema.Action;

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
    private int currentBytecodeLimit;
    private int bytecodesUsed;
    private int prevBytecodesUsed;

    private int roundsAlive;
    private int repairCount;
    private int shakeCount;
    private int waterCount;

    private boolean healthChanged = false;

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
        this.currentBytecodeLimit = type.bytecodeLimit;
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

    // **********************************
    // ****** CHECK METHODS *************
    // **********************************

    public boolean canSenseLocation(MapLocation toSense){
        return this.location.distanceTo(toSense) <= this.type.sensorRadius;
    }

    public boolean canInteractWithLocation(MapLocation toInteract){
        return this.location.distanceTo(toInteract) <= 2*this.type.bodyRadius;
    }

    // ******************************************
    // ****** UPDATE METHODS ********************
    // ******************************************

    public void setLocation(MapLocation loc){
        this.location = loc;
    }

    public void incrementWaterCount(){
        this.waterCount++;
    }

    public void incrementShakeCount(){
        this.shakeCount++;
    }

    public void incrementRepairCount() {
        this.repairCount++;
    }

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

    public void repairRobot(float healAmount){
        this.health += healAmount;
        this.healthChanged = true;
        keepMaxHealth();
    }

    public void damageRobot(float damage){
        this.health -= damage;
        this.healthChanged = true;
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
        this.healthChanged = false;
    }

    public void processBeginningOfTurn() {
        decrementDelays();
        repairCount = 0;
        waterCount = 0;
        shakeCount = 0;
        if(getRoundsAlive() < 20){
            this.repairRobot(.04f * getType().maxHealth);
        }
        this.currentBytecodeLimit = getType().bytecodeLimit;
    }

    public void processEndOfTurn() {
        this.prevBytecodesUsed = this.bytecodesUsed;
        roundsAlive++;
    }

    public void processEndOfRound() {
        if(this.healthChanged){
            gameWorld.getMatchMaker().addHealthChanged(getID(), getHealth());
        }
    }

    // *********************************
    // ****** BYTECODE METHODS *********
    // *********************************

    public boolean canExecuteCode() {
        if (getHealth() <= 0.0)
            return false;
        return roundsAlive >= 20;
    }

    public void setBytecodesUsed(int numBytecodes) {
        bytecodesUsed = numBytecodes;
    }

    public int getBytecodeLimit() {
        return canExecuteCode() ? this.currentBytecodeLimit : 0;
    }

    // *********************************
    // ****** VARIOUS METHODS **********
    // *********************************

    public void suicide(){
        gameWorld.destroyRobot(getID());

        gameWorld.getMatchMaker().addAction(getID(), Action.DIE_SUICIDE, 0);
    }

    // *****************************************
    // ****** MISC. METHODS ********************
    // *****************************************

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
