package battlecode.world;

import battlecode.common.*;
import battlecode.world.signal.DeathSignal;
import battlecode.world.signal.InternalSignal;
import battlecode.world.signal.TypeChangeSignal;

import java.util.ArrayList;
import java.util.Optional;

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
    private double weaponDelay;
    private double coreDelay;
    private double health;

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
     * @param buildDelay the build
     * @param parent the parent of the robot, if one exists
     */
    @SuppressWarnings("unchecked")
    public InternalRobot(GameWorld gw, int id, RobotType type, MapLocation loc, Team team) {
        this.ID = id;
        this.team = team;
        this.type = type;
        this.location = loc;
        this.weaponDelay = 0.0;
        this.coreDelay = 0.0;

        if(type == RobotType.ARCHON || type == RobotType.GARDENER){
            this.health = type.maxHealth;
        }else{
            this.health = .20 * type.maxHealth;
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

    public double getWeaponDelay() {
        return weaponDelay;
    }

    public double getCoreDelay() {
        return coreDelay;
    }

    public double getHealth() {
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


    // *********************************
    // ****** DELAYS METHODS ***********
    // *********************************

    public void addCoreDelay(double time) {
        coreDelay += time;
    }

    public void addWeaponDelay(double time) {
        weaponDelay += time;
    }

    public void setCoreDelayUpTo(double delay) {
        coreDelay = Math.max(coreDelay, delay);
    }

    public void setWeaponDelayUpTo(double delay) {
        weaponDelay = Math.max(weaponDelay, delay);
    }

    public void decrementDelays() {
        // Formula following the "Explanation of Delays" section of game specs
        // (Use previous bytecodes because current bytecode = 0)
        double amountToDecrement = 1.0 - (0.3 * Math.pow(
                Math.max(0.0,8000-this.type.bytecodeLimit+this.prevBytecodesUsed)/8000.0,1.5));
        
        weaponDelay-=amountToDecrement;
        coreDelay-=amountToDecrement;

        if (weaponDelay < 0.0) {
            weaponDelay = 0.0;
        }
        if (coreDelay < 0.0) {
            coreDelay = 0.0;
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
