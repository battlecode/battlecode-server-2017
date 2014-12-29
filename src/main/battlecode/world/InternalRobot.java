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
import battlecode.common.Upgrade;
import battlecode.engine.GenericRobot;
import battlecode.engine.signal.Signal;
import battlecode.server.Config;
import battlecode.world.signal.AttackSignal;
import battlecode.world.signal.BashSignal;
import battlecode.world.signal.BroadcastSignal;
import battlecode.world.signal.DeathSignal;
import battlecode.world.signal.DropSupplySignal;
import battlecode.world.signal.PickUpSupplySignal;
import battlecode.world.signal.ResearchSignal;
import battlecode.world.signal.SelfDestructSignal;
import battlecode.world.signal.SpawnSignal;
import battlecode.world.signal.TransferSupplySignal;

public class InternalRobot extends InternalObject implements Robot, GenericRobot {

    public final RobotType type;

    protected volatile double myHealthLevel;
    protected volatile double mySupplyLevel;
    private double timeUntilMovement;
    private double timeUntilAttack;
    private int missileCount;

    protected volatile long controlBits;

    int currentBytecodeLimit;
    private volatile int bytecodesUsed;
    protected volatile boolean hasBeenAttacked;
    private boolean didSelfDestruct;
    private boolean broadcasted;
    private volatile HashMap<Integer, Integer> broadcastMap;
    private int roundsAlive;
    
    private ArrayList<Signal> supplyActions;
    private ArrayList<SpawnSignal> missileLaunchActions;
    private Signal movementSignal;
    private Signal attackSignal;
    private Signal castSignal;
    public ResearchSignal researchSignal;

    private int buildDelay;

    @SuppressWarnings("unchecked")
    public InternalRobot(GameWorld gw, RobotType type, MapLocation loc, Team t,
                         boolean spawnedRobot, int buildDelay) {
        super(gw, loc, t);
        this.type = type;
        this.buildDelay = buildDelay;

        myHealthLevel = getMaxHealth();
        if (type.isBuilding && type != RobotType.HQ && type != RobotType.TOWER) {
            myHealthLevel /= 2.0;
        }

        mySupplyLevel = 0.0;
        timeUntilMovement = 0.0;
        timeUntilAttack = 0.0;
        missileCount = 0;

        controlBits = 0;

        currentBytecodeLimit = type.bytecodeLimit;
        bytecodesUsed = 0;
        hasBeenAttacked = false;
        didSelfDestruct = false;
        broadcasted = false;
        broadcastMap = new HashMap<Integer, Integer>();
        roundsAlive = 0;
    
        supplyActions = new ArrayList<Signal>();
        missileLaunchActions = new ArrayList<SpawnSignal>();
        movementSignal = null;
        attackSignal = null;
        castSignal = null;
        researchSignal = null;
    }

    // *********************************
    // ****** QUERY METHODS ************
    // *********************************

    public RobotInfo getRobotInfo() {
        return new RobotInfo(getID(), getTeam(), type, getLocation(), getTimeUntilMovement(), getTimeUntilAttack(), getHealthLevel(), getSupplyLevel(), getXP(), getMissileCount());
    }

    // *********************************
    // ****** BASIC METHODS ************
    // *********************************

    public boolean canExecuteCode() {
    	if (getHealthLevel() <= 0.0) return false;
        if (type.isBuilding && type != RobotType.HQ && type != RobotType.TOWER && roundsAlive < buildDelay) return false;
    	return true;
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

    public boolean isActive() {
        if (type.isBuilding && type != RobotType.HQ && type != RobotType.TOWER && roundsAlive < buildDelay) {
            return false;
        } else {
            return true;
        }
    }

    public int getXP() {
        if (type == RobotType.COMMANDER) {
            System.out.println(this);
            System.out.println( (InternalCommander)this);
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

    // *********************************
    // ****** MISSILE METHODS **********
    // *********************************

    public void decrementMissileCount() {
        missileCount--;
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
        if (baseAmount < 0) {
            changeHealthLevel(-baseAmount);
        } else {
            // HQ has a tower boost
            double rate = 1.0;
            if (type == RobotType.HQ) {
                int towerCount = myGameWorld.getActiveRobotTypeCount(getTeam(), RobotType.TOWER);
                if (towerCount >= 6) {
                    rate = 0.3;
                } else if (towerCount >= 4) {
                    rate = 0.5;
                } else if (towerCount >= 1) {
                    rate = 0.8;
                }
            }
            changeHealthLevelFromAttack(-rate * baseAmount);
        }
    }
    
    public void takeDamage(double amt, InternalRobot source) {
        if (!(getTeam() == Team.NEUTRAL)) {
            takeDamage(amt);
        }
    }
    
    public void changeHealthLevelFromAttack(double amount) {
        hasBeenAttacked = true;
        changeHealthLevel(amount);
    }

    public void changeHealthLevel(double amount) {
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

    public double getTimeUntilMovement() {
        return timeUntilMovement;
    }

    public double getTimeUntilAttack() {
        return timeUntilAttack;
    }

    public void addTimeUntilMovement(double time) {
        timeUntilMovement += time;
    }

    public void addTimeUntilAttack(double time) {
        timeUntilAttack += time;
    }

    public void addCooldownDelay(double delay) {
        timeUntilMovement = Math.max(timeUntilMovement, delay);
    }

    public void addLoadingDelay(double delay) {
        timeUntilAttack = Math.max(timeUntilAttack, delay);
    }

    public void decrementDelays() {
		timeUntilAttack -= 0.5;
		timeUntilMovement -= 0.5;
		double maxDelay = Math.max(timeUntilAttack,timeUntilMovement);
		if (maxDelay > 0.0) {
			//fraction of upkeep that can be paid
			double supplyDelayReduction = Math.min(Math.min(0.5,getSupplyLevel()/(2*type.supplyUpkeep)),maxDelay);
			timeUntilAttack-=supplyDelayReduction;
			timeUntilMovement-=supplyDelayReduction;
			decreaseSupplyLevel(2*supplyDelayReduction*type.supplyUpkeep);
		}

        if (timeUntilAttack < 0.0) {
            timeUntilAttack = 0.0;
        }
        if (timeUntilMovement < 0.0) {
            timeUntilMovement = 0.0;
        }
    }

    public int getAttackDelayForType() {
        if (type == RobotType.HQ && myGameWorld.getActiveRobotTypeCount(getTeam(), RobotType.TOWER) >= 5) {
            return type.attackDelay / 2;
        }
        return type.attackDelay;
    }

    public int getMovementDelayForType() {
        if (type == RobotType.BASHER && myGameWorld.hasUpgrade(getTeam(), Upgrade.REGENERATIVEMACHINERY)) {
            return 1;
        } else {
            return type.movementDelay;
        }
    }

    public int getLoadingDelayForType() {
        if (type == RobotType.SOLDIER && myGameWorld.hasUpgrade(getTeam(), Upgrade.NEUROMORPHICS)) {
            return 0;
        } else {
            return type.loadingDelay;
        }
    }

    public int getCooldownDelayForType() {
        if (type == RobotType.SOLDIER && myGameWorld.hasUpgrade(getTeam(), Upgrade.NEUROMORPHICS)) {
            return 0;
        } else {
            return type.cooldownDelay;
        }
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

    public void dropSupply(int amount) {
        supplyActions.add(new DropSupplySignal(this, amount));
    }

    public void pickUpSupply(int amount) {
        supplyActions.add(new PickUpSupplySignal(this, amount));
    }

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
    // ****** RESEARCH METHODS *********
    // *********************************

    public void activateResearch(ResearchSignal s, double attackDelay, double movementDelay) {
        addLoadingDelay(attackDelay);
        addTimeUntilMovement(movementDelay);

        researchSignal = s;
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
        addTimeUntilMovement(movementDelay);
    }
    
    public void activateAttack(Signal s, double attackDelay, double movementDelay) {
        attackSignal = s;
        addTimeUntilAttack(attackDelay);
        addCooldownDelay(movementDelay);
    }

    public void setLocation(MapLocation loc) {
    	MapLocation oldloc = getLocation();
        super.setLocation(loc);
    }

    public void setSelfDestruct() {
        didSelfDestruct = true;
    }

    public void suicide() {
        if (didSelfDestruct) {
            (new SelfDestructSignal(this, getLocation())).accept(myGameWorld);
        }
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
        if (type.supplyUpkeep > 0) {
            // decide how many bytecodes we'll be allowed
            this.currentBytecodeLimit = Math.max(type.bytecodeLimit / 2, Math.min(type.bytecodeLimit, GameConstants.FREE_BYTECODES + (int) (mySupplyLevel * GameConstants.BYTECODES_PER_SUPPLY)));
        }
    }

    @Override
    public void processEndOfTurn() {
        super.processEndOfTurn();

        // resetting stuff
        hasBeenAttacked = false;
        
        // broadcasts
        if (broadcasted) myGameWorld.visitSignal(new BroadcastSignal(this, broadcastMap));
        
    	broadcastMap = new HashMap<Integer, Integer>();
        broadcasted = false;

        // remove supply from bytecode usage
        if (type.supplyUpkeep > 0) {
            double supplyNeeded = Math.max(getBytecodesUsed() - GameConstants.FREE_BYTECODES, 0) / (double) GameConstants.BYTECODES_PER_SUPPLY;
            decreaseSupplyLevel(supplyNeeded);
        }

        // perform supply actions
        for (Signal s : supplyActions) {
            myGameWorld.visitSignal(s);
        }
        supplyActions.clear();

        // supply decay
        if (type != RobotType.HQ && type != RobotType.SUPPLYDEPOT) {
            mySupplyLevel *= (1 - GameConstants.SUPPLY_DECAY);
        }

        // generate supply
        if ((type == RobotType.SOLDIER || type == RobotType.BASHER) && myGameWorld.hasUpgrade(getTeam(), Upgrade.CONTROLLEDECOPHAGY)) {
            if (type == RobotType.SOLDIER) {
                increaseSupplyLevel(5);
            } else if (type == RobotType.BASHER) {
                increaseSupplyLevel(10);
            }
        }
        if (type == RobotType.SUPPLYDEPOT) {
            increaseSupplyLevel(GameConstants.DEPOT_SUPPLY_GEN);
        }
		if (type == RobotType.HQ) {
			increaseSupplyLevel(GameConstants.HQ_SUPPLY_GEN);
		}

        // possibly convert building from inactive to active
        roundsAlive++;
        // after building is done, double health
        if (type.isBuilding && roundsAlive == buildDelay && type != RobotType.HQ && type != RobotType.TOWER) {
            changeHealthLevel(getHealthLevel());
            // increase robot count
            myGameWorld.incrementRobotTypeCount(getTeam(), type);
        } else if ((!type.isBuilding || type == RobotType.HQ || type == RobotType.TOWER) && roundsAlive == 1) {
            myGameWorld.incrementRobotTypeCount(getTeam(), type);
        }

        // produce missile
        if (roundsAlive % GameConstants.MISSILE_SPAWN_FREQUENCY == 0 && type == RobotType.LAUNCHER) {
            missileCount = Math.min(missileCount + 1, GameConstants.MAX_MISSILE_COUNT);
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
        
        // perform attacks
        if (attackSignal != null) {
        	myGameWorld.visitSignal(attackSignal);
        	attackSignal = null;
        }
        
        // perform movements (moving, spawning, mining)
        if (movementSignal != null) {
            myGameWorld.visitSignal(movementSignal);
            movementSignal = null;
        }

        // launch missiles
        for (SpawnSignal s : missileLaunchActions) {
            myGameWorld.visitSignal(s);
        }
        missileLaunchActions.clear();

        // perform research
        if (researchSignal != null) {
            myGameWorld.visitSignal(researchSignal);
            researchSignal = null;
        }

        // bashers should bash()
        if (type == RobotType.BASHER) {
            myGameWorld.visitSignal(new BashSignal(this, getLocation()));
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
