package battlecode.world;

import battlecode.common.*;
import battlecode.engine.signal.Signal;
import battlecode.world.signal.BroadcastSignal;
import battlecode.world.signal.DeathSignal;
import battlecode.world.signal.TypeChangeSignal;

import java.util.HashMap;
import java.util.Optional;

/**
 * The representation of a robot used by the server.
 *
 * Should only ever be created by GameWorld in the visitSpawnSignal method.
 */
public class InternalRobot {
    public RobotType type;

    private final int ID;
    private Team team;

    // TODO remove volatiles?

    private volatile MapLocation location;
    private final GameWorld gameWorld;

    private final RobotControllerImpl controller;

    private volatile double healthLevel;
    private double coreDelay;
    private double weaponDelay;
    private int zombieInfectedTurns;
    private int viperInfectedTurns;

    private volatile long controlBits;

    int currentBytecodeLimit;
    private int bytecodesUsed;
    private boolean hasBeenAttacked;
    private boolean healthChanged;
    private boolean broadcasted;
    private volatile HashMap<Integer, Integer> broadcastMap;
    private int roundsAlive;

    private Signal movementSignal;
    private Signal attackSignal;

    private int buildDelay;

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

        this.healthLevel = getMaxHealth();

        this.coreDelay = 0.0;
        this.weaponDelay = 0.0;
        this.zombieInfectedTurns = 0;
        this.viperInfectedTurns = 0;

        this.controlBits = 0;

        this.currentBytecodeLimit = type.bytecodeLimit;
        this.bytecodesUsed = 0;
        this.hasBeenAttacked = false;
        this.healthChanged = true;

        this.broadcasted = false;
        this.broadcastMap = new HashMap<>();
        this.roundsAlive = 0;

        this.movementSignal = null;
        this.attackSignal = null;

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
                && this.cachedRobotInfo.type == type
                && this.cachedRobotInfo.location.equals(location)
                && this.cachedRobotInfo.coreDelay == coreDelay
                && this.cachedRobotInfo.weaponDelay == weaponDelay
                && this.cachedRobotInfo.health == healthLevel
                && this.cachedRobotInfo.zombieInfectedTurns == zombieInfectedTurns
                && this.cachedRobotInfo.viperInfectedTurns == viperInfectedTurns) {
            return this.cachedRobotInfo;
        }
        return this.cachedRobotInfo = new RobotInfo(
                ID, team, type, location,
                coreDelay, weaponDelay, healthLevel,
                zombieInfectedTurns, viperInfectedTurns
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

    // *********************************
    // ****** BASIC METHODS ************
    // *********************************

    public boolean isActive() {
        return !type.isBuildable() || roundsAlive >= buildDelay;
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

    public boolean hasBeenAttacked() {
        return hasBeenAttacked;
    }

    public void clearHealthChanged() {
        healthChanged = false;
    }

    public boolean healthChanged() {
        return healthChanged;
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
        if (attacker.type == RobotType.VIPER) {
            viperInfectedTurns = attacker.type.infectTurns;
        } else if (attacker.type.isZombie) {
            zombieInfectedTurns = attacker.type.infectTurns;
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
        healthChanged = true;
        if (baseAmount < 0) {
            changeHealthLevel(-baseAmount);
        } else {
            hasBeenAttacked = true;
            changeHealthLevel(-baseAmount);
        }
    }

    public void changeHealthLevel(double amount) {
        healthChanged = true;
        healthLevel += amount;
        if (healthLevel > getMaxHealth()) {
            healthLevel = getMaxHealth();
        }

        if (healthLevel <= 0) {
            gameWorld.visitDeathSignal(new DeathSignal(ID));
        }
    }

    public double getMaxHealth() {
        return type.maxHealth;
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

    public void addCooldownDelay(double delay) {
        coreDelay = Math.max(coreDelay, delay);
    }

    public void addLoadingDelay(double delay) {
        weaponDelay = Math.max(weaponDelay, delay);
    }

    public void decrementDelays() {
        weaponDelay--;
        coreDelay--;

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

    public void addBroadcast(int channel, int data) {
        broadcastMap.put(channel, data);
        broadcasted = true;
    }

    public Integer getQueuedBroadcastFor(int channel) {
        return broadcastMap.get(channel);
    }

    public boolean hasBroadcasted() {
        return broadcasted;
    }

    // *********************************
    // ****** ACTION METHODS ***********
    // *********************************

    public void activateMovement(Signal s, double attackDelay,
            double movementDelay) {
        movementSignal = s;
        addLoadingDelay(attackDelay);
        addCoreDelay(movementDelay);
    }

    public void activateAttack(Signal s, double attackDelay,
            double movementDelay) {
        attackSignal = s;
        addWeaponDelay(attackDelay);
        addCooldownDelay(movementDelay);
    }

    public void setLocation(MapLocation loc) {
        MapLocation oldloc = getLocation();
        gameWorld.notifyMovingObject(this, location, loc);
        location = loc;
        gameWorld.updateMapMemoryRemove(getTeam(), oldloc,
                type.sensorRadiusSquared);
        gameWorld
                .updateMapMemoryAdd(getTeam(), loc, type.sensorRadiusSquared);
    }

    public void suicide() {
        gameWorld.visitSignal((new DeathSignal(this.getID())));
    }
    
    public void transform(RobotType newType) {
        gameWorld.decrementRobotTypeCount(getTeam(), type);
        gameWorld.incrementRobotTypeCount(getTeam(), newType);
        type = newType;
        coreDelay += 10;
        weaponDelay += 10;

        gameWorld.visitSignal(new TypeChangeSignal(ID, newType));
    }

    // *********************************
    // ****** GAMEPLAY METHODS *********
    // *********************************

    // should be called at the beginning of every round
    public void processBeginningOfRound() {
    }

    public void processBeginningOfTurn() {
        decrementDelays(); // expends supply to decrement delays

        this.currentBytecodeLimit = type.bytecodeLimit;

        processBeingInfected();
    }

    public void processEndOfTurn() {
        roundsAlive++;

        // resetting stuff
        hasBeenAttacked = false;

        // broadcasts
        if (broadcasted)
            gameWorld.visitSignal(new BroadcastSignal(this.getID(), this.getTeam(), broadcastMap));

        broadcastMap = new HashMap<>();
        broadcasted = false;

        // perform attacks
        if (attackSignal != null) {
            gameWorld.visitSignal(attackSignal);
            attackSignal = null;
        }

        // perform movements (moving, spawning, mining)
        if (movementSignal != null) {
            gameWorld.visitSignal(movementSignal);
            movementSignal = null;
        }
    }

    public void processEndOfRound() {}

    // *********************************
    // ****** MISC. METHODS ************
    // *********************************

    @Override
    public String toString() {
        return String.format("%s:%s#%d", getTeam(), type, getID());
    }
}
