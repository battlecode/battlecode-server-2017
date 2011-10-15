package battlecode.world;

import static battlecode.common.GameConstants.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Robot;
import battlecode.common.Team;
import battlecode.common.RobotType;
import battlecode.engine.GenericRobot;
import battlecode.engine.signal.Signal;
import battlecode.server.Config;
import battlecode.world.signal.DeathSignal;
import battlecode.world.signal.TurnOffSignal;

public class InternalRobot extends InternalObject implements Robot, GenericRobot {

    /**
     * Robots that are inside a transport are considered to be at this
     * location, so that no one but the dropship will be able to sense them.
     */
    private volatile double myEnergonLevel;
	private volatile double flux;
    protected volatile Direction myDirection;
    private volatile boolean energonChanged = true;
    protected volatile long controlBits;
    // is this used ever?
    protected volatile boolean hasBeenAttacked = false;
    private static boolean upkeepEnabled = Config.getGlobalConfig().getBoolean("bc.engine.upkeep-enabled");
    /** first index is robot type, second is direction, third is x or y */
    private static final Map<RobotType, int[][][]> offsets = GameMap.computeVisibleOffsets();
    /** number of bytecodes used in the most recent round */
    private volatile int bytecodesUsed = 0;
    private List<Message> incomingMessageQueue;
    protected GameMap.MapMemory mapMemory;
    public final RobotType type;

	private int turnsUntilMovementIdle;
	private int turnsUntilAttackIdle;
	private boolean broadcasted;
	private boolean upkeepPaid;

	private Signal movementSignal;

	private InternalRobotBuffs buffs;

    public InternalRobotBuffs getBuffs() {
        return buffs;
    }

    @SuppressWarnings("unchecked")
    public InternalRobot(GameWorld gw, RobotType type, MapLocation loc, Team t,
            boolean spawnedRobot) {
        super(gw, loc, type.level, t);
        myDirection = Direction.values()[gw.getRandGen().nextInt(8)];
        this.type = type;

        myEnergonLevel = getMaxEnergon();

        incomingMessageQueue = new LinkedList<Message>();

        mapMemory = new GameMap.MapMemory(gw.getGameMap());
        saveMapMemory(null, loc, false);
        controlBits = 0;

		buffs = new InternalRobotBuffs(this);

    }

	public void addAction(Signal s) {
        myGameWorld.visitSignal(s);
    }

    @Override
    public void processBeginningOfRound() {
        super.processBeginningOfRound();
        buffs.processBeginningOfRound();
    }

    public void processBeginningOfTurn() {
		if(upkeepEnabled) {
			upkeepPaid = flux>=type.upkeep;
			if(upkeepPaid)
				flux-=type.upkeep;
		}
		else
			upkeepPaid = true;
    }

    @Override
    public void processEndOfTurn() {
        super.processEndOfTurn();
		myGameWorld.removeDead();
		if(movementSignal!=null) {
			myGameWorld.visitSignal(movementSignal);
			movementSignal=null;
		}
		if(turnsUntilAttackIdle>0)
			turnsUntilAttackIdle--;
		if(turnsUntilMovementIdle>0)
			turnsUntilMovementIdle--;
		broadcasted = false;
    }

    @Override
    public void processEndOfRound() {
        super.processEndOfRound();
        buffs.processEndOfRound();

        if (myEnergonLevel <= 0) {
            suicide();
        }
    }

    public double getEnergonLevel() {
        return myEnergonLevel;
    }

	public double getFlux() {
		return flux;
	}

	public boolean payFlux(double amt) {
		if(amt<flux)
			return false;
		else {
			flux-=amt;
			return true;
		}
	}

	public void adjustFlux(double amt) {
		flux+=amt;
		if(flux>=type.maxFlux)
			flux=type.maxFlux;
	}

    public Direction getDirection() {
        return myDirection;
    }

    public void takeDamage(double baseAmount) {
        if (baseAmount < 0) {
            changeEnergonLevel(-baseAmount);
        }
		else {
            changeEnergonLevelFromAttack(-baseAmount);
        }
    }

    public void changeEnergonLevelFromAttack(double amount) {
        hasBeenAttacked = true;
        changeEnergonLevel(amount * (buffs.getDamageReceivedMultiplier() + 1));
    }

    public void changeEnergonLevel(double amount) {
        myEnergonLevel += amount;
        if (myEnergonLevel > getMaxEnergon()) {
            myEnergonLevel = getMaxEnergon();
        }
        energonChanged = true;

        if (myEnergonLevel <= 0) {
            myGameWorld.notifyDied(this);
        }
    }

    public boolean clearEnergonChanged() {
        if (energonChanged) {
            energonChanged = false;
            return true;
        } else {
            return false;
        }
    }

    public double getMaxEnergon() {
        return type.maxEnergon;
    }

	public void activateMovement(Signal s, int delay) {
		movementSignal = s;
		turnsUntilMovementIdle = delay;
	}

	public void activateAttack(Signal s, int delay) {
		myGameWorld.visitSignal(s);
		turnsUntilAttackIdle = delay;
	}
	
	public void activateBroadcast(Signal s) {
		myGameWorld.visitSignal(s);
		broadcasted = true;
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
		super.setLocation(loc);
		saveMapMemory(loc);
	}

    public void setDirection(Direction dir) {
        myDirection = dir;
    }

    public void suicide() {
        (new DeathSignal(this)).accept(myGameWorld);
    }

    public void enqueueIncomingMessage(Message msg) {
        incomingMessageQueue.add(msg);
    }

    public Message dequeueIncomingMessage() {
        if (incomingMessageQueue.size() > 0) {
            return incomingMessageQueue.remove(0);
        } else {
            return null;
        }
        // ~ return incomingMessageQueue.poll();
    }

    public Message[] dequeueIncomingMessages() {
        Message[] result = incomingMessageQueue.toArray(new Message[incomingMessageQueue.size()]);
        incomingMessageQueue.clear();
        return result;
    }

    public GameMap.MapMemory getMapMemory() {
        return mapMemory;
    }

    public void saveMapMemory(MapLocation oldLoc, MapLocation newLoc,
            boolean fringeOnly) {
        saveMapMemory(newLoc);
    }

    public void saveMapMemory(MapLocation newLoc) {
        int[][] myOffsets = offsets.get(type)[myDirection.ordinal()];
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
        return upkeepPaid?BYTECODE_LIMIT_BASE:0;
    }

    public boolean hasBeenAttacked() {
        return hasBeenAttacked;
    }

    @Override
    public String toString() {
        return String.format("%s:%s#%d", getTeam(), type, getID());
    }

    public void freeMemory() {
        incomingMessageQueue = null;
        mapMemory = null;
        buffs = null;
    }
}
