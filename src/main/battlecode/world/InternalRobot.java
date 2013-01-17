package battlecode.world;

import java.util.HashMap;
import java.util.Map;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.Upgrade;
import battlecode.engine.GenericRobot;
import battlecode.engine.signal.Signal;
import battlecode.server.Config;
import battlecode.world.signal.AttackSignal;
import battlecode.world.signal.BroadcastSignal;
import battlecode.world.signal.CaptureSignal;
import battlecode.world.signal.DeathSignal;
import battlecode.world.signal.MineSignal;
import battlecode.world.signal.RegenSignal;
import battlecode.world.signal.ShieldSignal;
import battlecode.world.signal.SpawnSignal;

public class InternalRobot extends InternalObject implements Robot, GenericRobot {

    protected volatile double myEnergonLevel;
    protected volatile double myShieldLevel;
    protected volatile Direction myDirection;
    protected volatile boolean energonChanged = true;
    protected volatile boolean shieldChanged = true;
    protected volatile long controlBits;
    // is this used ever?
    protected volatile boolean hasBeenAttacked = false;
    private static boolean upkeepEnabled = Config.getGlobalConfig().getBoolean("bc.engine.upkeep");
    /**
     * first index is robot type, second is direction, third is x or y
     */
    private static final Map<RobotType, int[][][]> offsets = GameMap.computeVisibleOffsets();
    /**
     * number of bytecodes used in the most recent round
     */
    private volatile int bytecodesUsed = 0;
//    private List<Message> incomingMessageQueue;
//    now in gameworld, since each team has combined map memory
    protected GameMap.MapMemory mapMemory;
    public final RobotType type;

    private volatile int turnsUntilMovementIdle;
    private volatile int turnsUntilAttackIdle;
    private volatile HashMap<Integer, Integer> broadcastMap = new HashMap<Integer, Integer>();
    private boolean broadcasted = false;
    
    protected volatile boolean regen;
    private boolean upkeepPaid;
    
    private int researchRounds;
    private Upgrade researchUpgrade;
    
    private int miningRounds;
    private int defusingRounds;
    private MapLocation defusingLocation;
    private int capturingRounds;
    private RobotType capturingType;

    private Signal movementSignal;
    private Signal attackSignal;

    @SuppressWarnings("unchecked")
    public InternalRobot(GameWorld gw, RobotType type, MapLocation loc, Team t,
                         boolean spawnedRobot) {
        super(gw, loc, type.level, t);
//        myDirection = Direction.values()[gw.getRandGen().nextInt(8)];
        this.type = type;

        myEnergonLevel = getMaxEnergon();
        myShieldLevel = 0.0;
        
        researchRounds = 0;
        researchUpgrade = null;
        
        miningRounds = 0;
        defusingRounds = 0;
        capturingRounds = 0;
        capturingType = null;;

//        incomingMessageQueue = new LinkedList<Message>();

        mapMemory = gw.getMapMemory(getTeam());
        saveMapMemory(null, loc, false);
        controlBits = 0;

//        if (spawnedRobot) {
//            turnsUntilMovementIdle = GameConstants.WAKE_DELAY;
//            turnsUntilAttackIdle = GameConstants.WAKE_DELAY;
//        }
        
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
    
//    public void scanForMines() {
//    	MapLocation base = getLocation();
//    	Team t = getTeam().opponent();
//    	for (int dx=-1; dx<=1; dx++) {
//    		for (int dy=-1; dy<=1; dy++) {
//    			MapLocation loc = base.add(dx, dy);
//    			if(myGameWorld.getMine(loc) == t) myGameWorld.addKnownMineLocation(getTeam(), loc);
//    		}
//    	}
//    	addAction(new ScanSignal(this));
//    }

    public void addAction(Signal s) {
        myGameWorld.visitSignal(s);
    }
    
    public Upgrade getResearchingUpgrade() {
    	return researchUpgrade;
    }
    
    public boolean canExecuteCode() {
    	if (getEnergonLevel() <= 0.0) return false;
    	return true;
    }

    @Override
    public void processBeginningOfRound() {
        super.processBeginningOfRound();
        
        // TODO we can do beginning of round damage/healing/etc here
    	// TODO also, resoruce generation is done here
        // TODO CORY FIX IT
        if (type == RobotType.HQ)
        	myGameWorld.adjustResources(getTeam(), GameConstants.HQ_POWER_PRODUCTION);
        else if (type == RobotType.GENERATOR)
        	myGameWorld.adjustResources(getTeam(), GameConstants.GENERATOR_POWER_PRODUCTION);
        else if (type == RobotType.SUPPLIER)
        	myGameWorld.adjustSpawnRate(getTeam(), GameConstants.HQ_SPAWN_DELAY_CONSTANT);
    }

    public void processBeginningOfTurn() {
    	
    	// TODO we can do beginning of turn damage/healing/etc here
        // TODO CORY FIX IT
//        if (type == RobotType.HQ)
//            HQProduction();
//        if (regen) {
//            changeEnergonLevel(GameConstants.REGEN_AMOUNT);
//            regen = false;
//        }
    	
        if (upkeepEnabled && canExecuteCode()) {
            upkeepPaid = myGameWorld.resources(getTeam()) > GameConstants.BYTECODE_LIMIT*GameConstants.POWER_COST_PER_BYTECODE + GameConstants.UNIT_POWER_UPKEEP;
            if (upkeepPaid)
                myGameWorld.adjustResources(getTeam(), -(GameConstants.BYTECODE_LIMIT*GameConstants.POWER_COST_PER_BYTECODE + GameConstants.UNIT_POWER_UPKEEP));
            else // we need to subtract energon
            {
            	this.takeDamage(GameConstants.UNIT_ENERGON_UPKEEP);
            	upkeepPaid = true;
            }
        } else
            upkeepPaid = true;
    }

//    public void HQProduction() {
//        int d, dmin = GameConstants.PRODUCTION_PENALTY_R2;
//        for (MapLocation l : myGameWorld.getArchons(getTeam())) {
//            d = getLocation().distanceSquaredTo(l);
//            if (d > 0 && d <= dmin)
//                dmin = d;
//        }
//        double prod = GameConstants.MIN_PRODUCTION + (GameConstants.MAX_PRODUCTION - GameConstants.MIN_PRODUCTION) * Math.sqrt(((double) dmin) / GameConstants.PRODUCTION_PENALTY_R2);
//        
//    }

    @Override
    public void processEndOfTurn() {
        super.processEndOfTurn();
        
        // autosend aggregated broadcast
        if (broadcasted) myGameWorld.visitSignal(new BroadcastSignal(this, broadcastMap));
        
    	broadcastMap = new HashMap<Integer, Integer>();
        broadcasted = false;
        
      	// quick hack to make mining work. move me out later
        if (type == RobotType.SOLDIER) {
        	if (miningRounds > 0) {
        		if (--miningRounds==0) {
        			myGameWorld.visitSignal(new MineSignal(getLocation(), getTeam(), MineSignal.ADD));
        			if(myGameWorld.hasUpgrade(getTeam(), Upgrade.PICKAXE)) {
        				for(Direction d: new Direction[]{Direction.NORTH, Direction.EAST, Direction.WEST, Direction.SOUTH}) {
        					myGameWorld.visitSignal(new MineSignal(getLocation().add(d), getTeam(), MineSignal.ADD));
        				}
        			}
        		}
        	} else if (defusingRounds > 0) {
        		if (--defusingRounds==0) {
        			myGameWorld.visitSignal(new MineSignal(defusingLocation, getTeam(), MineSignal.REMOVE));
        			defusingLocation = null;
        		}
        	} else if (capturingRounds > 0) {
        		if (--capturingRounds==0) {
        			myGameWorld.visitSignal(new SpawnSignal(getLocation(), capturingType, getTeam(), this));
        			capturingRounds = -1;
        			suicide();
        			return;
        		}
        	}
        }
        
        
        if (movementSignal != null) {
            myGameWorld.visitSignal(movementSignal);
            movementSignal = null;
        }
        
        if (!type.isEncampment)
    	{
    		Team mines = myGameWorld.getMine(getLocation());
    		if (mines!=null && mines!=getTeam()) {
    			
    			this.takeShieldedDamage(GameConstants.MINE_DAMAGE*GameConstants.MINE_DAMAGE_RATIO_ABSORBED_BY_SHIELD);
    			this.takeDamage(GameConstants.MINE_DAMAGE*(1.0-GameConstants.MINE_DAMAGE_RATIO_ABSORBED_BY_SHIELD));
    			
    			myGameWorld.addKnownMineLocation(getTeam(), getLocation());
    			if (myEnergonLevel <= 0.0)
    				return;
    		}
    	}
        
        if (attackSignal != null) {
        	myGameWorld.visitSignal(attackSignal);
        	attackSignal = null;
        }
        if (turnsUntilAttackIdle > 0)
            turnsUntilAttackIdle--;
        if (turnsUntilMovementIdle > 0)
            turnsUntilMovementIdle--;
        if (type == RobotType.HQ)
        {
        	if (researchRounds > 0)
            {
            	researchRounds--;
            	if (researchRounds==0)
            	{
            		myGameWorld.addUpgrade(getTeam(), researchUpgrade);
            		clearResearching();
            	}
            }
        }
       
        boolean nearbyAlly = false;
        boolean nearbyEnemy = false;
        
    	for(int i=0; i<8; i++) {
    		Robot nearby = myGameWorld.getRobot(this.getLocation().add(Direction.values()[i]),
    				RobotLevel.ON_GROUND);
    		if(nearby != null) {
    			if(nearby.getTeam() == getTeam()) nearbyAlly = true;
    			else if(nearby.getTeam() == getTeam().opponent()) nearbyEnemy = true;
        	}
    	}
        
        // Soldiers Automatically Attack
        if (type == RobotType.SOLDIER && nearbyEnemy && this.turnsUntilAttackIdle == 0) {
        	myGameWorld.visitSignal(new AttackSignal(this, getLocation(), RobotLevel.ON_GROUND));
        } // Medbays Auto Heal
        else if (type == RobotType.MEDBAY) {
        	if (nearbyAlly)
        		myGameWorld.visitSignal(new RegenSignal(this));
        	else
        		takeDamage(-this.type.attackPower, this);
        } // Shields Auto Shield
        else if (type == RobotType.SHIELDS) {
        	if (nearbyAlly)
        		myGameWorld.visitSignal(new ShieldSignal(this));
        	else
        		takeShieldedDamage(-this.type.attackPower);
        }
        
        // shield decay
        if (myShieldLevel > 0.0)
        {
        	shieldChanged = true;
        	myShieldLevel = Math.max(0.0, myShieldLevel-GameConstants.SHIELD_DECAY_RATE);
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

    public Direction getDirection() {
        return myDirection;
    }

//    there is no regen, healing by medbay is immediate
//    public void setRegen() {
//        if (type != RobotType.TOWER || !myGameWorld.timeLimitReached())
//            regen = true;
//    }

    public boolean getRegen() {
        return regen;
    }

    public void takeDamage(double baseAmount) {
        if (baseAmount < 0) {
            changeEnergonLevel(-baseAmount);
        } else {
            changeEnergonLevelFromAttack(-baseAmount);
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
        	if (source.type == RobotType.ARTILLERY)
        		takeShieldedDamage(amt);
        	else
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
        if (myShieldLevel > GameConstants.SHIELD_CAP) {
        	myShieldLevel = GameConstants.SHIELD_CAP;
        }
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

        if (myEnergonLevel <= 0) {
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
        return type.maxEnergon;
    }

    public void activateMovement(Signal s, int delay) {
        movementSignal = s;
        turnsUntilMovementIdle = delay;
    }
    
    public void activateAttack(Signal s, int delay) {
        attackSignal = s;
        turnsUntilAttackIdle = delay;
    }

    public void addBroadcast(int channel, int data) {
    	broadcastMap.put(channel, data);
        broadcasted = true;
    }
    
    public void activateMinelayer(Signal s, int delay) {
    	myGameWorld.visitSignal(s);
    	turnsUntilAttackIdle = delay;
    	turnsUntilMovementIdle = delay;
    	miningRounds = delay;
    }
    
    public void activateMinestop(Signal s, int delay) {
    	myGameWorld.visitSignal(s);
    	turnsUntilAttackIdle = delay;
    	turnsUntilMovementIdle = delay;
    	miningRounds = 0;
    }
    
    public void activateDefuser(Signal s, int delay, MapLocation target) {
    	myGameWorld.visitSignal(s);
    	turnsUntilAttackIdle = delay;
    	turnsUntilMovementIdle = delay;
    	defusingRounds = delay;
    	defusingLocation = target;
    }
    
    public void activateCapturing(CaptureSignal s, int delay) {
    	myGameWorld.visitSignal(s);
    	turnsUntilAttackIdle = delay;
    	turnsUntilMovementIdle = delay;
    	capturingRounds = delay;
    	capturingType = s.getType();
    }
    
    public int getMiningRounds() {
    	return miningRounds;
    }
    
    public int getDefusingRounds() { 
    	return defusingRounds;
    }
    
    public int roundsUntilAttackIdle() {
        return turnsUntilAttackIdle;
    }

    public int roundsUntilMovementIdle() {
        return turnsUntilMovementIdle;
    }

    public boolean hasBroadcasted() {
        return broadcasted;
    }
    
    public void setLocation(MapLocation loc) {
    	MapLocation oldloc = getLocation();
        super.setLocation(loc);
        saveMapMemory(oldloc, loc, true);
    }

    public void setDirection(Direction dir) {
        myDirection = dir;
//        saveMapMemory(getLocation());
    }

    public void suicide() {
        (new DeathSignal(this)).accept(myGameWorld);
    }

//    public void enqueueIncomingMessage(Message msg) {
//        incomingMessageQueue.add(msg);
//    }
//
//    public Message dequeueIncomingMessage() {
//        if (incomingMessageQueue.size() > 0) {
//            return incomingMessageQueue.remove(0);
//        } else {
//            return null;
//        }
//        // ~ return incomingMessageQueue.poll();
//    }
//
//    public Message[] dequeueIncomingMessages() {
//        Message[] result = incomingMessageQueue.toArray(new Message[incomingMessageQueue.size()]);
//        incomingMessageQueue.clear();
//        return result;
//    }
    
    public int getCapturingRounds() {
    	return capturingRounds;
    }
    
    public RobotType getCapturingType() {
    	return capturingType;
    }

    public GameMap.MapMemory getMapMemory() {
        return mapMemory;
    }

    // TODO this year all robots have 360 vision, probably can make this better
    public void saveMapMemory(MapLocation oldLoc, MapLocation newLoc,
                              boolean fringeOnly) {
    	int[][] myOffsets;
    	if (oldLoc == null)
    		myOffsets = offsets.get(type)[0];
    	else
    		myOffsets = offsets.get(type)[oldLoc.directionTo(newLoc).ordinal()];
        mapMemory.rememberLocations(newLoc, myOffsets[0], myOffsets[1]);
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
        return canExecuteCode() && upkeepPaid ? GameConstants.BYTECODE_LIMIT : 0;
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
        mapMemory = null;
        movementSignal = null;
        attackSignal = null;
    }
}
