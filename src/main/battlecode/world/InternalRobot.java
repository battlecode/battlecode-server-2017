package battlecode.world;

import battlecode.common.*;
import battlecode.world.signal.DeathSignal;
import battlecode.world.signal.InternalSignal;
import battlecode.world.signal.TypeChangeSignal;

import java.util.ArrayList;
import java.util.Optional;

/**
 * The representation of a robot used by the server.
 *
 * Should only ever be created by GameWorld in the visitSpawnSignal method.
 */
public class InternalRobot {
    private RobotType type;
    private final int ID;
    private Team team;
    private MapLocation location;
    private final GameWorld gameWorld;
    private final RobotControllerImpl controller;
    private double maxHealth;
    private double healthLevel;
    private double attackPower;
    private double coreDelay;
    private double weaponDelay;
    private int zombieInfectedTurns;
    private int viperInfectedTurns;
    private long controlBits;
    private int currentBytecodeLimit;
    private int bytecodesUsed;
    private int prevBytecodesUsed;
    private boolean healthChanged;    
    private ArrayList<Signal> signalqueue;
    private int roundsAlive;
    private int buildDelay;
    private int repairCount;
    private int basicSignalCount;
    private int messageSignalCount;

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
    public InternalRobot(GameWorld gw, int id, RobotType type, MapLocation loc, Team team,
            int buildDelay, Optional<InternalRobot> parent) {

        this.ID = id;
        this.team = team;
        this.gameWorld = gw;
        this.location = loc;
        this.type = type;
        this.buildDelay = buildDelay;

        this.maxHealth = type.maxHealth(gw.getCurrentRound());
        this.healthLevel = maxHealth;
        this.attackPower = type.attackPower(gw.getCurrentRound());

        this.coreDelay = 0.0;
        this.weaponDelay = 0.0;
        this.zombieInfectedTurns = 0;
        this.viperInfectedTurns = 0;
        this.repairCount = 0;
        this.basicSignalCount = 0;
        this.messageSignalCount = 0;

        this.controlBits = 0;

        this.currentBytecodeLimit = type.bytecodeLimit;
        this.bytecodesUsed = 0;
        this.prevBytecodesUsed = 0;
        this.healthChanged = true;
        
        this.signalqueue = new ArrayList<Signal>();

        this.roundsAlive = 0;

        this.controller = new RobotControllerImpl(gameWorld, this);
    }

    @Override
    public boolean equals(Object o) {
        return o != null && (o instanceof InternalRobot)
                && ((InternalRobot) o).getID() == ID;
    }

    @Override
    public int hashCode() {
        return ID;
    }

    // *********************************
    // ****** QUERY METHODS ************
    // *********************************

    public RobotInfo getRobotInfo() {
        if (this.cachedRobotInfo != null
                && this.cachedRobotInfo.ID == ID
                && this.cachedRobotInfo.team == team
                && this.cachedRobotInfo.type == getType()
                && this.cachedRobotInfo.location.equals(location)
                && this.cachedRobotInfo.coreDelay == coreDelay
                && this.cachedRobotInfo.weaponDelay == weaponDelay
                && this.cachedRobotInfo.attackPower == attackPower
                && this.cachedRobotInfo.health == healthLevel
                && this.cachedRobotInfo.maxHealth == maxHealth
                && this.cachedRobotInfo.zombieInfectedTurns == zombieInfectedTurns
                && this.cachedRobotInfo.viperInfectedTurns == viperInfectedTurns) {
            return this.cachedRobotInfo;
        }
        return this.cachedRobotInfo = new RobotInfo(
                ID, team, getType(), location,
                coreDelay, weaponDelay, attackPower, healthLevel,
                maxHealth, zombieInfectedTurns, viperInfectedTurns
        );
    }

    public RobotControllerImpl getController() {
        return controller;
    }

    public int getRoundsAlive() {
        return roundsAlive;
    }

    public int getID() {
        return ID;
    }

    public Team getTeam() {
        return team;
    }

    public MapLocation getLocation() {
        return location;
    }

    public boolean exists() {
        return gameWorld.exists(this);
    }

    public int getRepairCount() {
        return repairCount;
    }
    
    public int getBasicSignalCount() {
        return basicSignalCount;
    }

    public int getMessageSignalCount() {
        return messageSignalCount;
    }
    
    public double getMaxHealth() {
        return maxHealth;
    }

    public double getAttackPower() {
        return attackPower;
    }

    // *********************************
    // ****** BASIC METHODS ************
    // *********************************

    public boolean isActive() {
        return !getType().isBuildable() || roundsAlive >= buildDelay;
    }

    public boolean canExecuteCode() {
        if (getHealthLevel() <= 0.0)
            return false;
        return isActive();
    }

    public void setBytecodesUsed(int numBytecodes) {
        bytecodesUsed = numBytecodes;
    }

    public int getBytecodesUsed() {
        return bytecodesUsed;
    }

    public int getBytecodeLimit() {
        return canExecuteCode() ? this.currentBytecodeLimit : 0;
    }

    public void setControlBits(long l) {
        controlBits = l;
    }

    public long getControlBits() {
        return controlBits;
    }

    public void clearHealthChanged() {
        healthChanged = false;
    }

    public boolean healthChanged() {
        return healthChanged;
    }

    public boolean canSense(MapLocation target) {
        if (type.sensorRadiusSquared == -1) {
            return true;
        }
        return location.distanceSquaredTo(target) <= type.sensorRadiusSquared;
    }

    // *********************************
    // ****** ZOMBIE METHODS ***********
    // *********************************

    public int getZombieInfectedTurns() {
        return zombieInfectedTurns;
    }
    
    public int getViperInfectedTurns() {
        return viperInfectedTurns;
    }
    
    public boolean isInfected() {
        return (zombieInfectedTurns > 0 || viperInfectedTurns > 0);
    }

    public void setInfected(InternalRobot attacker) {
        if (attacker.getType() == RobotType.VIPER) {
            viperInfectedTurns = attacker.getType().infectTurns;
        } else if (attacker.getType().isZombie) {
            zombieInfectedTurns = attacker.getType().infectTurns;
        }
    }

    public void processBeingInfected() { // TODO: Call this somewhere where it runs for each robot every turn
        if (viperInfectedTurns > 0) {
            takeDamage(GameConstants.VIPER_INFECTION_DAMAGE);
            viperInfectedTurns--;
        }
        if (zombieInfectedTurns > 0) {
            zombieInfectedTurns--;
        }
    }

    // *********************************
    // ****** HEALTH METHODS ***********
    // *********************************

    public double getHealthLevel() {
        return healthLevel;
    }

    public void takeDamage(double baseAmount) {
        assert baseAmount >= 0;

        changeHealthLevel(-baseAmount);
    }

    public void changeHealthLevel(double amount) {
        healthChanged = true;
        healthLevel += amount;
        if (healthLevel > maxHealth) {
            healthLevel = maxHealth;
        }

        if (healthLevel <= 0) {
            gameWorld.visitDeathSignal(new DeathSignal(ID));
        }
    }

    // *********************************
    // ****** DELAYS METHODS ***********
    // *********************************

    public double getCoreDelay() {
        return coreDelay;
    }

    public double getWeaponDelay() {
        return weaponDelay;
    }

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
        double amountToDecrement = 1.0 - (0.3 * Math.pow(Math.max(0.0,8000-this.currentBytecodeLimit+this.prevBytecodesUsed)/8000.0,1.5));
        
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
    // ****** BROADCAST METHODS ********
    // *********************************

    public void receiveSignal(Signal mess) {
        signalqueue.add(mess);
        if(signalqueue.size() > GameConstants.SIGNAL_QUEUE_MAX_SIZE) {
            signalqueue.remove(0);
        }
    }

    public Signal retrieveNextSignal() {
        if (signalqueue.size() == 0) {
            return null;
        }
        return signalqueue.remove(0);
    }

    public Signal[] retrieveAllSignals() {
        int numMessages = signalqueue.size();
        Signal[] queue = new Signal[numMessages];
        for (int i = 0; i < numMessages; i++) {
            queue[i] = signalqueue.remove(0);
        }
        return queue;
    }
    
    public void incrementBasicSignalCount() {
        basicSignalCount++;
    }
    
    public void incrementMessageSignalCount() {
        messageSignalCount++;
    }

    // *********************************
    // ****** ACTION METHODS ***********
    // *********************************

    public void activateCoreAction(InternalSignal s, double attackDelay, double
            movementDelay) {
        gameWorld.visitSignal(s);

        setWeaponDelayUpTo(attackDelay);
        addCoreDelay(movementDelay);
    }

    public void activateAttack(InternalSignal s, double attackDelay, double
            movementDelay) {
        gameWorld.visitSignal(s);

        addWeaponDelay(attackDelay);
        setCoreDelayUpTo(movementDelay);
    }

    public void setLocation(MapLocation loc) {
        gameWorld.notifyMovingObject(this, location, loc);
        location = loc;
    }

    public void suicide() {
        gameWorld.visitSignal((new DeathSignal(this.getID())));
    }
    
    public void transform(RobotType newType) {
        gameWorld.decrementRobotTypeCount(getTeam(), getType());
        gameWorld.incrementRobotTypeCount(getTeam(), newType);
        type = newType;
        coreDelay += GameConstants.TURRET_TRANSFORM_DELAY;
        weaponDelay += GameConstants.TURRET_TRANSFORM_DELAY;

        gameWorld.visitSignal(new TypeChangeSignal(ID, newType));
    }

    /**
     * Repairs the other robot. Assumes that all reprequisites are properly
     * checked: other is not null, you are an archon, the other robot is on
     * your own team, and you haven't already repaired this turn.
     *
     * @param other the robot to repair.
     */
    public void repair(InternalRobot other) {
        repairCount++;

        other.changeHealthLevel(GameConstants.ARCHON_REPAIR_AMOUNT);
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
        basicSignalCount = 0;
        messageSignalCount = 0;

        this.currentBytecodeLimit = getType().bytecodeLimit;
    }

    public void processEndOfTurn() {
        this.prevBytecodesUsed = this.bytecodesUsed;
        roundsAlive++;
        
        processBeingInfected();
    }

    public void processEndOfRound() {}

    // *********************************
    // ****** MISC. METHODS ************
    // *********************************

    @Override
    public String toString() {
        return String.format("%s:%s#%d", getTeam(), getType(), getID());
    }

    public RobotType getType() {
        return type;
    }
}
