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
import battlecode.world.signal.BroadcastSignal;
import battlecode.world.signal.DeathSignal;
import battlecode.world.signal.DropSupplySignal;
import battlecode.world.signal.PickUpSupplySignal;
import battlecode.world.signal.ResearchSignal;
import battlecode.world.signal.SelfDestructSignal;
import battlecode.world.signal.SpawnSignal;
import battlecode.world.signal.TransferSupplySignal;

public class InternalRobot extends InternalObject implements Robot, GenericRobot {

    protected volatile double myEnergonLevel;
    protected volatile double myShieldLevel;
    protected volatile double mySupplyLevel;
    protected volatile Direction myDirection;
    protected volatile boolean energonChanged = true;
    protected volatile boolean shieldChanged = true;
    protected volatile long controlBits;
    // is this used ever?
    protected volatile boolean hasBeenAttacked = false;
    private static boolean upkeepEnabled = Config.getGlobalConfig().getBoolean("bc.engine.upkeep");
    /**
     * number of bytecodes used in the most recent round
     */
    private volatile int bytecodesUsed = 0;

    public final RobotType type;

    private volatile HashMap<Integer, Integer> broadcastMap = new HashMap<Integer, Integer>();
    private boolean broadcasted = false;
    
    protected volatile boolean regen;
    int currentBytecodeLimit;
    
    private int researchRounds;
    private Upgrade researchUpgrade;
    
    private int miningRounds;
    private int defusingRounds;
    private MapLocation defusingLocation;
    private int capturingRounds;
    private RobotType capturingType;

    private ArrayList<Signal> supplyActions;

    private Signal movementSignal;
    private Signal attackSignal;
    private Signal castSignal;

    public ResearchSignal researchSignal;

    private int roundsSinceLastDamage;
    private int roundsSinceLastSpawn;
    private int roundsAlive;

    private boolean didSelfDestruct;

    private double timeUntilMovement;
    private double timeUntilAttack;

    private int missileCount = 0;
    private int hatCount = 0;

    private int buildDelay;

    @SuppressWarnings("unchecked")
    public InternalRobot(GameWorld gw, RobotType type, MapLocation loc, Team t,
                         boolean spawnedRobot, int buildDelay) {
        super(gw, loc, t);
//        myDirection = Direction.values()[gw.getRandGen().nextInt(8)];
        this.type = type;
        this.buildDelay = buildDelay;

        myEnergonLevel = getMaxEnergon();
        if (type.isBuilding && type != RobotType.HQ && type != RobotType.TOWER) {
            myEnergonLevel /= 2.0;
        }
        myShieldLevel = 0.0;
        mySupplyLevel = 0.0;
        
        researchRounds = 0;
        researchUpgrade = null;
        
        miningRounds = 0;
        defusingRounds = 0;
        capturingRounds = 0;
        capturingType = null;;

        supplyActions = new ArrayList<Signal>();

        roundsSinceLastDamage = 0;
        roundsSinceLastSpawn = Integer.MAX_VALUE / 2;
        roundsAlive = 0;

        currentBytecodeLimit = type.bytecodeLimit;

        controlBits = 0;

        didSelfDestruct = false;
        
        timeUntilMovement = 0.0;
        timeUntilAttack = 0.0;

        missileCount = 0;
    }

    public RobotInfo getRobotInfo() {
        return new RobotInfo(getID(), getTeam(), type, getLocation(), getTimeUntilMovement(), getTimeUntilAttack(), getEnergonLevel(), getSupplyLevel(), getXP(), getCapturingType() != null, getCapturingType(), getCapturingRounds(), getMissileCount());
    }
    
    public void clearResearching() {
    	researchRounds = 0;
    	researchUpgrade = null;
    }
    
    public void setResearching(Upgrade upgrade) {
		researchRounds = upgrade.numRounds;
    	researchUpgrade = upgrade;
    }
    
    public int getResearchRounds() {
    	return researchRounds;
    }

    public void decrementMissileCount() {
        missileCount--;
    }

    public int getMissileCount() {
        return missileCount;
    }

    public void incrementHatCount() {
        hatCount++;
    }

    public int getHatCount() {
        return hatCount;
    }

    public void addAction(Signal s) {
        myGameWorld.visitSignal(s);
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
        if (timeUntilAttack >= 1 || timeUntilMovement >= 1) {
            // this means we need to pay upkeep
            if (getSupplyLevel() >= type.supplyUpkeep) {
                // can afford! all good
                decreaseSupplyLevel(type.supplyUpkeep);
                timeUntilAttack--;
                timeUntilMovement--;
            } else {
                // cannot afford, so we slow down the decrease
                timeUntilAttack -= 0.5;
                timeUntilMovement -= 0.5;
            }
        }

        if (timeUntilAttack < 0.0) {
            timeUntilAttack = 0.0;
        }
        if (timeUntilMovement < 0.0) {
            timeUntilMovement = 0.0;
        }
    }

    public double getTimeUntilMovement() {
        return timeUntilMovement;
    }

    public double getTimeUntilAttack() {
        return timeUntilAttack;
    }
    
    public Upgrade getResearchingUpgrade() {
    	return researchUpgrade;
    }
    
    public boolean canExecuteCode() {
    	if (getEnergonLevel() <= 0.0) return false;
        if (type.isBuilding && type != RobotType.HQ && type != RobotType.TOWER && roundsAlive < buildDelay) return false;
    	return true;
    }

    public boolean isActive() {
        if (type.isBuilding && type != RobotType.HQ && type != RobotType.TOWER && roundsAlive < buildDelay) {
            return false;
        } else {
            return true;
        }
    }

    public void resetSpawnCounter() {
        roundsSinceLastSpawn = 0;
    }

    @Override
    public void processBeginningOfRound() {
        super.processBeginningOfRound();
    }

    public void processBeginningOfTurn() {
        decrementDelays(); // expends supply to decrement delays
    	
        if (type == RobotType.COMMANDER && ((InternalCommander)this).hasSkill(CommanderSkillType.REGENERATION)) {
           this.changeEnergonLevel(1); 
        }

        this.currentBytecodeLimit = type.bytecodeLimit;
        if (type.supplyUpkeep > 0) {
            // decide how many bytecodes we'll be allowed
            this.currentBytecodeLimit = Math.max(type.bytecodeLimit / 2, Math.min(type.bytecodeLimit, GameConstants.FREE_BYTECODES + (int) (mySupplyLevel * GameConstants.BYTECODES_PER_SUPPLY)));
        }
    }

    @Override
    public void processEndOfTurn() {
        super.processEndOfTurn();
        
        // autosend aggregated broadcast
        if (broadcasted) myGameWorld.visitSignal(new BroadcastSignal(this, broadcastMap));
        
    	broadcastMap = new HashMap<Integer, Integer>();
        broadcasted = false;

        // charge supply for bytecodes
        if (type.supplyUpkeep > 0) {
            double supplyNeeded = Math.max(getBytecodesUsed() - GameConstants.FREE_BYTECODES, 0) / (double) GameConstants.BYTECODES_PER_SUPPLY;
            decreaseSupplyLevel(supplyNeeded);
        }

        // supply actions
        for (Signal s : supplyActions) {
            myGameWorld.visitSignal(s);
        }
        supplyActions.clear();
       
        if (type != RobotType.HQ) { 
            roundsSinceLastDamage++;
        } else {
            roundsSinceLastSpawn++;
        }

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

        roundsAlive++;
        // after building is done, double health
        if (type.isBuilding && roundsAlive == buildDelay && type != RobotType.HQ && type != RobotType.TOWER) {
            changeEnergonLevel(getEnergonLevel());
            // increase robot count
            myGameWorld.incrementRobotTypeCount(getTeam(), type);
        } else if (!type.isBuilding && roundsAlive == 1) {
            myGameWorld.incrementRobotTypeCount(getTeam(), type);
        }

        if (roundsAlive % GameConstants.MISSILE_SPAWN_FREQUENCY == 0 && type == RobotType.LAUNCHER) {
            missileCount = Math.min(missileCount + 1, GameConstants.MAX_MISSILE_COUNT);
        }

        if (type == RobotType.MISSILE && roundsAlive >= 5) {
            suicide();
        }
        
        if (movementSignal != null) {
            myGameWorld.visitSignal(movementSignal);
            movementSignal = null;
        }

        if (researchSignal != null) {
            if (!myGameWorld.hasUpgrade(getTeam(), researchSignal.getUpgrade())) {
                myGameWorld.visitSignal(researchSignal);
            } else {
                researchSignal = null;
            }
        }
        
        if (attackSignal != null) {
        	myGameWorld.visitSignal(attackSignal);
        	attackSignal = null;
        }
    }

    @Override
    public void processEndOfRound() {
        super.processEndOfRound();
    }

    public double getEnergonLevel() {
        return myEnergonLevel;
    }
    
    public double getShieldLevel() {
    	return myShieldLevel;
    }

    public int getXP() {
        if (type == RobotType.COMMANDER) {
            System.out.println(this);
            System.out.println( (InternalCommander)this);
            return ((InternalCommander)this).getXP();
        }
        return 0;
    }

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

    public Direction getDirection() {
        return myDirection;
    }

    public boolean getRegen() {
        return regen;
    }

    // includes buffs
    public int getAttackRadiusSquared() {
        int base = type.attackRadiusSquared;
        if (type == RobotType.HQ && myGameWorld.getRobotTypeCount(getTeam(), RobotType.TOWER) >= 2) {
            return GameConstants.ATTACK_RADIUS_SQUARED_BUFFED_HQ;
        } else {
            return base;
        }
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

    public void takeDamage(double baseAmount) {
        if (baseAmount < 0) {
            changeEnergonLevel(-baseAmount);
        } else {
            if (baseAmount > 0) {
                roundsSinceLastDamage = 0;
            }
            // HQ has a tower boost
            double rate = 1.0;
            if (type == RobotType.HQ) {
                int towerCount = myGameWorld.getRobotTypeCount(getTeam(), RobotType.TOWER);
                if (towerCount >= 6) {
                    rate = 0.3;
                } else if (towerCount >= 4) {
                    rate = 0.5;
                } else if (towerCount >= 1) {
                    rate = 0.8;
                }
            }
            changeEnergonLevelFromAttack(-rate * baseAmount);
        }
    }
    
    public void takeShieldedDamage(double baseAmount) {
        if (baseAmount < 0) {
        	changeShieldLevel(-baseAmount);
        } else {
            double remainder = changeShieldLevelFromAttack(-baseAmount);
            changeEnergonLevelFromAttack(-remainder);
        }
    }

    public void takeDamage(double amt, InternalRobot source) {
        // uncomment this to test immortal base nodes
        //if(type==RobotType.TOWER&&myGameWorld.towerToNode(this).isPowerCore())
        //	return;
    	// make sure encampments don't take damage
        if (!(getTeam() == Team.NEUTRAL))
        {
        	//if (source.type == RobotType.ARTILLERY)
        	//	takeShieldedDamage(amt);
        	//else
                takeDamage(amt);
        }
    }
    
    public double changeShieldLevelFromAttack(double amount) {
        hasBeenAttacked = true;
        return changeShieldLevel(amount);
    }
    
    public void changeEnergonLevelFromAttack(double amount) {
        hasBeenAttacked = true;
        changeEnergonLevel(amount);
    }
    
    public double changeShieldLevel(double amount) {
        myShieldLevel += amount;
        /*if (myShieldLevel > GameConstants.SHIELD_CAP) {
        	myShieldLevel = GameConstants.SHIELD_CAP;
          }*/
        shieldChanged = true;

        if (myShieldLevel <= 0) {
        	double diff = -myShieldLevel;
        	myShieldLevel = 0;
        	return diff;
        }
        return 0.0;
    }

    public void changeEnergonLevel(double amount) {
        myEnergonLevel += amount;
        if (myEnergonLevel > getMaxEnergon()) {
            myEnergonLevel = getMaxEnergon();
        }
        energonChanged = true;

        if (myEnergonLevel <= 0 && getMaxEnergon() != Integer.MAX_VALUE) {
            processLethalDamage();
        }
    }

    public void processLethalDamage() {
        myGameWorld.notifyDied(this);
    }

    public boolean clearEnergonChanged() {
        boolean wasChanged = energonChanged;
        energonChanged = false;
        return wasChanged;
    }
    
    public boolean clearShieldChanged() {
        boolean wasChanged = shieldChanged;
        shieldChanged = false;
        return wasChanged;
    }

    public double getMaxEnergon() {
        return type.maxHealth;
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

    public double calculateAttackActionDelay(RobotType r) {
        if (r == RobotType.HQ && myGameWorld.getRobotTypeCount(getTeam(), RobotType.TOWER) >= 5) {
            return r.attackDelay / 2;
        }
        return r.attackDelay;
    }

    public void activateResearch(ResearchSignal s, double attackDelay, double movementDelay) {
        addLoadingDelay(attackDelay);
        addTimeUntilMovement(movementDelay);

        researchSignal = s;
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

    public void addBroadcast(int channel, int data) {
    	broadcastMap.put(channel, data);
        broadcasted = true;
    }

    public Integer getQueuedBroadcastFor(int channel) {
	return broadcastMap.get(channel);
    }
    
    public void activateMinelayer(Signal s, int delay) {
    	myGameWorld.visitSignal(s);
    	miningRounds = delay;
    }
    
    public void activateMinestop(Signal s, int delay) {
    	myGameWorld.visitSignal(s);
    	miningRounds = 0;
    }
    
    public void activateDefuser(Signal s, int delay, MapLocation target) {
    	myGameWorld.visitSignal(s);
    	defusingRounds = delay;
    	defusingLocation = target;
    }

    public int getMiningRounds() {
    	return miningRounds;
    }
    
    public int getDefusingRounds() { 
    	return defusingRounds;
    }

    public boolean hasBroadcasted() {
        return broadcasted;
    }
    
    public void setLocation(MapLocation loc) {
    	MapLocation oldloc = getLocation();
        super.setLocation(loc);
    }

    public void setDirection(Direction dir) {
        myDirection = dir;
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
    
    public int getCapturingRounds() {
    	return capturingRounds;
    }
    
    public RobotType getCapturingType() {
    	return capturingType;
    }

    public void setControlBits(long l) {
        controlBits = l;
    }

    public long getControlBits() {
        return controlBits;
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

    public boolean hasBeenAttacked() {
        return hasBeenAttacked;
    }

    @Override
    public String toString() {
        return String.format("%s:%s#%d", getTeam(), type, getID());
    }

    public void freeMemory() {
//        incomingMessageQueue = null;
        movementSignal = null;
        attackSignal = null;
    }
}
