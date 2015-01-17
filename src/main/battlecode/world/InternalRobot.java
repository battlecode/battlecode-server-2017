package battlecode.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.CommanderSkillType;
import battlecode.common.Team;
import battlecode.common.TerrainTile;
import battlecode.engine.GenericRobot;
import battlecode.engine.signal.Signal;
import battlecode.server.Config;
import battlecode.world.signal.AttackSignal;
import battlecode.world.signal.BashSignal;
import battlecode.world.signal.BroadcastSignal;
import battlecode.world.signal.DeathSignal;
import battlecode.world.signal.SelfDestructSignal;
import battlecode.world.signal.SpawnSignal;
import battlecode.world.signal.TransferSupplySignal;

public class InternalRobot extends InternalObject implements Robot, GenericRobot {

    public final RobotType type;

    protected volatile double myHealthLevel;
    protected volatile double mySupplyLevel;
    private double coreDelay;
    private double weaponDelay;
    private int missileCount;

    protected volatile long controlBits;

    int currentBytecodeLimit;
    private volatile int bytecodesUsed;
    protected volatile boolean hasBeenAttacked;
    private boolean healthChanged;
    private boolean missileCountChanged;
    private boolean didSelfDestruct;
    private boolean broadcasted;
    private volatile HashMap<Integer, Integer> broadcastMap;
    private int roundsAlive;
    private boolean justClearedBuilding;
    
    private ArrayList<Signal> supplyActions;
    private ArrayList<SpawnSignal> missileLaunchActions;
    private Signal movementSignal;
    private Signal attackSignal;
    private Signal castSignal;

    private int buildDelay;

    private static boolean upkeepEnabled = Config.getGlobalConfig().getBoolean("bc.engine.upkeep");
    private int myBuilder, myBuilding;

    @SuppressWarnings("unchecked")
    public InternalRobot(GameWorld gw, RobotType type, MapLocation loc, Team t,
                         boolean spawnedRobot, int buildDelay) {
        super(gw, loc, t);
        this.type = type;
        this.buildDelay = buildDelay;

        myHealthLevel = getMaxHealth();
        if (type.isBuildable() && buildDelay > 0) {
            myHealthLevel /= 2.0;
        }

        mySupplyLevel = 0.0;
        coreDelay = 0.0;
        weaponDelay = 0.0;
        missileCount = 0;

        controlBits = 0;

        currentBytecodeLimit = type.bytecodeLimit;
        bytecodesUsed = 0;
        hasBeenAttacked = false;
        healthChanged = true;
        missileCountChanged = true;
        
        didSelfDestruct = false;
        broadcasted = false;
        broadcastMap = new HashMap<Integer, Integer>();
        roundsAlive = 0;
        justClearedBuilding = false;
    
        supplyActions = new ArrayList<Signal>();
        missileLaunchActions = new ArrayList<SpawnSignal>();
        movementSignal = null;
        attackSignal = null;
        castSignal = null;

        myBuilder = -1;
        myBuilding = -1;

        // Update GameWorld stuff
        if (type == RobotType.COMMANDER) {
            myGameWorld.putCommander(this);
            myGameWorld.incrementCommandersSpawned(getTeam());
        }

        myGameWorld.incrementTotalRobotTypeCount(getTeam(), type);

        if (!type.isBuildable() || buildDelay == 0) {
            myGameWorld.incrementActiveRobotTypeCount(getTeam(), type);
        }

        myGameWorld.updateMapMemoryAdd(getTeam(), loc, type.sensorRadiusSquared);
    }

    // *********************************
    // ****** QUERY METHODS ************
    // *********************************

    public RobotInfo getRobotInfo() {
        MapLocation myBuilderLocation = null;
        if (myBuilder >= 0) {
            myBuilderLocation = myGameWorld.getRobotByID(myBuilder).getLocation();
        }
        MapLocation myBuildingLocation = null;
        if (myBuilding >= 0) {
            myBuildingLocation = myGameWorld.getRobotByID(myBuilding).getLocation();
        }

        return new RobotInfo(getID(), getTeam(), type, getLocation(), getCoreDelay(), getWeaponDelay(), getHealthLevel(), getSupplyLevel(), getXP(), getMissileCount(), myBuilderLocation, myBuildingLocation);
    }

    public int getRoundsAlive() {
        return roundsAlive;
    }

    // *********************************
    // ****** BASIC METHODS ************
    // *********************************

    public boolean isActive() {
        return !type.isBuildable() || roundsAlive >= buildDelay;
    }

    public boolean canExecuteCode() {
    	if (getHealthLevel() <= 0.0) return false;
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

    public boolean movedThisTurn() {
        return this.movementSignal != null;
    }

    public int getXP() {
        if (type == RobotType.COMMANDER) {
            return ((InternalCommander)this).getXP();
        }
        return 0;
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

    public void clearMissileCountChanged() {
        missileCountChanged = false;
    }

    public boolean missileCountChanged() {
        return missileCountChanged;
    }

    public void setMyBuilding(int id) {
        myBuilding = id;
    }

    public int getMyBuilding() {
        return myBuilding;
    }

    public void setMyBuilder(int id) {
        myBuilder = id;
    }

    public int getMyBuilder() {
        return myBuilder;
    }

    public void clearBuilding() {
        myBuilding = -1;
        myBuilder = -1;
        justClearedBuilding = true;
    }

    public void clearBuildingAndFree() {
        clearBuilding();
        justClearedBuilding = false;

        int amountToDecrement = (int) coreDelay;
        coreDelay -= amountToDecrement;
        weaponDelay -= amountToDecrement;

        if (weaponDelay < 0) {
            weaponDelay = 0;
        }
    }

    // *********************************
    // ****** MISSILE METHODS **********
    // *********************************

    public void decrementMissileCount() {
        missileCount--;
        missileCountChanged = true;
    }

    public int getMissileCount() {
        return missileCount;
    }

    public boolean canLaunchMissileAtLocation(MapLocation loc) {
        for (SpawnSignal s : missileLaunchActions) {
            if (s.getLoc().equals(loc)) {
                return false;
            }
        }
        return true;
    }

    public void launchMissile(MapLocation loc) {
        missileLaunchActions.add(new SpawnSignal(loc, RobotType.MISSILE, getTeam(), this, 0));
    }

    // *********************************
    // ****** HEALTH METHODS ***********
    // *********************************

    public double getHealthLevel() {
        return myHealthLevel;
    }

    public void takeDamage(double baseAmount) {
        healthChanged = true;
        if (baseAmount < 0) {
            changeHealthLevel(-baseAmount);
        } else {
            // HQ has a tower boost
            double rate = 1.0;
            if (type == RobotType.HQ) {
                int towerCount = myGameWorld.getActiveRobotTypeCount(getTeam(), RobotType.TOWER);
                if (towerCount >= 6) {
                    rate = GameConstants.HQ_BUFFED_DAMAGE_RATIO_LEVEL_3;
                } else if (towerCount >= 4) {
                    rate = GameConstants.HQ_BUFFED_DAMAGE_RATIO_LEVEL_2;
                } else if (towerCount >= 1) {
                    rate = GameConstants.HQ_BUFFED_DAMAGE_RATIO_LEVEL_1;
                }
            }
            changeHealthLevelFromAttack(-rate * baseAmount);
        }
    }
    
    public void takeDamage(double amt, InternalRobot source) {
        if (!(getTeam() == Team.NEUTRAL)) {
            healthChanged = true;
            takeDamage(amt);
        }
    }
    
    public void changeHealthLevelFromAttack(double amount) {
        healthChanged = true;
        hasBeenAttacked = true;
        changeHealthLevel(amount);
    }

    public void changeHealthLevel(double amount) {
        healthChanged = true;
        myHealthLevel += amount;
        if (myHealthLevel > getMaxHealth()) {
            myHealthLevel = getMaxHealth();
        }

        if (myHealthLevel <= 0 && getMaxHealth() != Integer.MAX_VALUE) {
            processLethalDamage();
        }
    }

    public void processLethalDamage() {
        myGameWorld.notifyDied(this);
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
        if (type.supplyUpkeep > 0 && upkeepEnabled && myBuilding < 0 && !justClearedBuilding) {
            weaponDelay -= 0.5;
            coreDelay -= 0.5;
            double maxDelay = Math.max(weaponDelay,coreDelay);
            if (maxDelay > 0.0) {
                //fraction of upkeep that can be paid
                double supplyDelayReduction = Math.min(Math.min(0.5,getSupplyLevel()/(2*type.supplyUpkeep)),maxDelay);
                weaponDelay-=supplyDelayReduction;
                coreDelay-=supplyDelayReduction;
                decreaseSupplyLevel(2*supplyDelayReduction*type.supplyUpkeep);
            }
        } else {
            weaponDelay--;
            coreDelay--;
        }

        justClearedBuilding = false;

        if (weaponDelay < 0.0) {
            weaponDelay = 0.0;
        }
        if (coreDelay < 0.0) {
            coreDelay = 0.0;
        }
    }

    public int getAttackDelayForType() {
        if (type == RobotType.HQ && myGameWorld.getActiveRobotTypeCount(getTeam(), RobotType.TOWER) >= 5) {
            return GameConstants.HQ_BUFFED_ATTACK_DELAY;
        }
        return type.attackDelay;
    }

    public int getMovementDelayForType() {
        return type.movementDelay;
    }

    public int getLoadingDelayForType() {
        return type.loadingDelay;
    }

    public int getCooldownDelayForType() {
        return type.cooldownDelay;
    }

    public double calculateMovementActionDelay(MapLocation from, MapLocation to, TerrainTile terrain) {
        double base = 1;
        if (from.distanceSquaredTo(to) <= 1) {
            base = getMovementDelayForType();
        } else {
            base = getMovementDelayForType() * 1.4;
        }
        return base;
    }

    // *********************************
    // ****** SUPPLY METHODS ***********
    // *********************************

    public void transferSupply(int amount, InternalRobot target) {
        supplyActions.add(new TransferSupplySignal(this, target, amount));
    }

    public double getSupplyLevel() {
        return mySupplyLevel;
    }

    public void decreaseSupplyLevel(double dec) {
        mySupplyLevel -= dec;
        if (mySupplyLevel < 0) {
            mySupplyLevel = 0;
        }
    }

    public void increaseSupplyLevel(double inc) {
        mySupplyLevel += inc;
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

    public void addAction(Signal s) {
        myGameWorld.visitSignal(s);
    }

    public void activateMovement(Signal s, double attackDelay, double movementDelay) {
        movementSignal = s;
        addLoadingDelay(attackDelay);
        addCoreDelay(movementDelay);
    }
    
    public void activateAttack(Signal s, double attackDelay, double movementDelay) {
        attackSignal = s;
        addWeaponDelay(attackDelay);
        addCooldownDelay(movementDelay);
    }

    public void setLocation(MapLocation loc) {
    	MapLocation oldloc = getLocation();
        super.setLocation(loc);
        myGameWorld.updateMapMemoryRemove(getTeam(), oldloc, type.sensorRadiusSquared);
        myGameWorld.updateMapMemoryAdd(getTeam(), loc, type.sensorRadiusSquared);
    }

    public void setSelfDestruct() {
        didSelfDestruct = true;
    }

    public void unsetSelfDestruct() {
        didSelfDestruct = false;
    }

    public boolean didSelfDestruct() {
        return didSelfDestruct;
    }

    public void suicide() {
        (new DeathSignal(this)).accept(myGameWorld);
    }

    // *********************************
    // ****** GAMEPLAY METHODS *********
    // *********************************

    @Override
    public void processBeginningOfRound() {
        super.processBeginningOfRound();
    }

    public void processBeginningOfTurn() {
        decrementDelays(); // expends supply to decrement delays

        this.currentBytecodeLimit = type.bytecodeLimit;
        if (type.supplyUpkeep > 0 && upkeepEnabled) {
            // decide how many bytecodes we'll be allowed
            this.currentBytecodeLimit = Math.max(type.bytecodeLimit / 2, Math.min(type.bytecodeLimit, GameConstants.FREE_BYTECODES + (int) (mySupplyLevel * GameConstants.BYTECODES_PER_SUPPLY)));
        }
    }

    @Override
    public void processEndOfTurn() {
        super.processEndOfTurn();

        roundsAlive++;
		
        // resetting stuff
        hasBeenAttacked = false;

        // remove supply from bytecode usage
        if (type.supplyUpkeep > 0 && upkeepEnabled) {
            double supplyNeeded = Math.max(getBytecodesUsed() - GameConstants.FREE_BYTECODES, 0) / (double) GameConstants.BYTECODES_PER_SUPPLY;
            decreaseSupplyLevel(supplyNeeded);
        }

        // broadcasts
        if (broadcasted) myGameWorld.visitSignal(new BroadcastSignal(this, broadcastMap));
        
    	broadcastMap = new HashMap<Integer, Integer>();
        broadcasted = false;

        // perform supply actions
        for (Signal s : supplyActions) {
            myGameWorld.visitSignal(s);
        }
        supplyActions.clear();

        // supply decay
        if (type != RobotType.HQ && type != RobotType.SUPPLYDEPOT) {
            mySupplyLevel *= (1 - GameConstants.SUPPLY_DECAY);
        }
        
        // perform attacks
        if (attackSignal != null) {
        	myGameWorld.visitSignal(attackSignal);
        	attackSignal = null;
        }

        // launch missiles
        for (SpawnSignal s : missileLaunchActions) {
            myGameWorld.visitSignal(s);
        }
        missileLaunchActions.clear();
        
        // perform movements (moving, spawning, mining)
        if (movementSignal != null) {
            myGameWorld.visitSignal(movementSignal);
            movementSignal = null;
        }

        // bashers should bash()
        if (type == RobotType.BASHER) {
            myGameWorld.visitSignal(new BashSignal(this, getLocation()));
        }

        // produce missile
        if (type == RobotType.LAUNCHER && weaponDelay < 1 && missileCount + 1 <= GameConstants.MISSILE_MAX_COUNT) {
            missileCount++;
            addWeaponDelay(GameConstants.MISSILE_SPAWN_FREQUENCY);
            missileCountChanged = true;
        }
    	
        // commander regen
        if (type == RobotType.COMMANDER && ((InternalCommander)this).hasSkill(CommanderSkillType.REGENERATION)) {
           this.changeHealthLevel(GameConstants.REGEN_RATE); 
        }
		
        // missiles should die automatically
		if (type == RobotType.MISSILE && roundsAlive >= GameConstants.MISSILE_LIFESPAN) {
			setSelfDestruct();
            suicide();
        }
		
        // generate supply
		if (type == RobotType.HQ) {
            int numSupplyDepots = myGameWorld.getActiveRobotTypeCount(getTeam(), RobotType.SUPPLYDEPOT);
			increaseSupplyLevel(GameConstants.SUPPLY_GEN_BASE * (GameConstants.SUPPLY_GEN_MULTIPLIER + Math.pow(numSupplyDepots, GameConstants.SUPPLY_GEN_EXPONENT)));
		}

        // possibly convert building from inactive to active
        // after building is done, double health
        if (type.isBuildable() && roundsAlive == buildDelay) {
            changeHealthLevel(getHealthLevel());
            // increase robot count
            myGameWorld.incrementActiveRobotTypeCount(getTeam(), type);
            if (myBuilder >= 0) {
                myGameWorld.getRobotByID(myBuilder).clearBuilding();
            }
            clearBuilding();
        }
    }

    @Override
    public void processEndOfRound() {
        super.processEndOfRound();
    }

    // *********************************
    // ****** MISC. METHODS ************
    // *********************************

    @Override
    public String toString() {
        return String.format("%s:%s#%d", getTeam(), type, getID());
    }

    public void freeMemory() {
        movementSignal = null;
        attackSignal = null;
    }
}
