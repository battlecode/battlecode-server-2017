package battlecode.world;

import battlecode.common.*;
import battlecode.schema.Action;

/**
 * The representation of a robot used by the server.
 */
public strictfp class InternalRobot implements InternalBody {
    private final RobotControllerImpl controller;
    private final GameWorld gameWorld;

    private final int ID;
    private Team team;
    private RobotType type;
    private MapLocation location;
    private float health;

    private long controlBits;
    private int currentBytecodeLimit;
    private int bytecodesUsed;
    private int prevBytecodesUsed;

    private int roundsAlive;
    private int repairCount;
    private int shakeCount;
    private int waterCount;
    private int attackCount;
    private int moveCount;
    
    private int buildCooldownTurns;

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

        this.health = type.getStartingHealth();

        this.controlBits = 0;
        this.currentBytecodeLimit = type.bytecodeLimit;
        this.bytecodesUsed = 0;
        this.prevBytecodesUsed = 0;

        this.roundsAlive = 0;
        this.repairCount = 0;
        this.shakeCount = 0;
        this.waterCount = 0;
        this.attackCount = 0;
        this.moveCount = 0;
        
        this.buildCooldownTurns = 0;

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
    
    public int getAttackCount() {
        return attackCount;
    }
    
    public int getMoveCount() {
        return moveCount;
    }
    
    public int getBuildCooldownTurns() {
        return buildCooldownTurns;
    }

    public RobotInfo getRobotInfo() {
        if (this.cachedRobotInfo != null
                && this.cachedRobotInfo.ID == ID
                && this.cachedRobotInfo.team == team
                && this.cachedRobotInfo.type == type
                && this.cachedRobotInfo.location.equals(location)
                && this.cachedRobotInfo.health == health
                && this.cachedRobotInfo.attackCount == attackCount
                && this.cachedRobotInfo.moveCount == moveCount) {
            return this.cachedRobotInfo;
        }
        return this.cachedRobotInfo = new RobotInfo(
                ID, team, type, location, health, attackCount, moveCount);
    }

    // **********************************
    // ****** CHECK METHODS *************
    // **********************************

    public boolean canSenseBulletLocation(MapLocation toSense) {
        return this.location.distanceTo(toSense) <= this.type.bulletSightRadius;
    }

    public boolean canSenseLocation(MapLocation toSense){
        return this.location.distanceTo(toSense) <= this.type.sensorRadius;
    }

    public boolean canSenseRadius(float radius) {
        return radius <= this.type.sensorRadius;
    }

    public boolean canInteractWithLocation(MapLocation toInteract){
        return this.location.distanceTo(toInteract) <= (this.type.bodyRadius + GameConstants.INTERACTION_DIST_FROM_EDGE);
    }

    public boolean canInteractWithCircle(MapLocation center, float radius) {
        return this.location.distanceTo(center) <= (this.type.bodyRadius + radius + GameConstants.INTERACTION_DIST_FROM_EDGE);
    }

    // ******************************************
    // ****** UPDATE METHODS ********************
    // ******************************************

    public void setLocation(MapLocation loc){
        this.gameWorld.getObjectInfo().moveRobot(this, loc);
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
    
    public void incrementAttackCount() {
        this.attackCount++;
    }
    
    public void incrementMoveCount() {
        this.moveCount++;
    }
    
    public void setBuildCooldownTurns(int newTurns) {
        this.buildCooldownTurns = newTurns;
    }

    public void repairRobot(float healAmount){
        this.health = Math.min(this.health + healAmount, this.type.maxHealth);
        if(health > this.type.maxHealth){
            this.health = this.type.maxHealth;
        }
        this.healthChanged = true;
    }

    public void damageRobot(float damage){
        this.health = Math.max(this.health - damage, 0);
        this.healthChanged = true;
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
    // ****** GAMEPLAY METHODS *********
    // *********************************

    // should be called at the beginning of every round
    public void processBeginningOfRound() {
        this.healthChanged = false;
    }

    public void processBeginningOfTurn() {
        attackCount = 0;
        moveCount = 0;
        repairCount = 0;
        waterCount = 0;
        shakeCount = 0;
        if(buildCooldownTurns > 0) {
            buildCooldownTurns--;
        }
        if(getRoundsAlive() < 20 && this.type.isBuildable()){
            this.repairRobot(.04f * getType().maxHealth);
        }
        this.currentBytecodeLimit = getType().bytecodeLimit;
    }

    public void processEndOfTurn() {
        gameWorld.getMatchMaker().addBytecodes(ID, this.bytecodesUsed);
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
        if(type.isBuildable())
            return roundsAlive >= 20;
        return true;
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

        gameWorld.getMatchMaker().addAction(getID(), Action.DIE_SUICIDE, -1);
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
