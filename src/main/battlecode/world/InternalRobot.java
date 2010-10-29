package battlecode.world;

import static battlecode.common.GameConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import battlecode.common.Chassis;
import battlecode.common.ComponentClass;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.RobotLevel;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Robot;
import battlecode.common.Team;
import battlecode.engine.ErrorReporter;
import battlecode.engine.GenericRobot;
import battlecode.engine.signal.Signal;
import battlecode.server.Config;
import battlecode.world.signal.BroadcastSignal;
import battlecode.world.signal.DeathSignal;

public class InternalRobot extends InternalObject implements Robot, GenericRobot {

    private volatile double myEnergonLevel;
    protected volatile Direction myDirection;
    private volatile boolean energonChanged = true;
    protected volatile long controlBits;
    // is this used ever?
	protected volatile boolean hasBeenAttacked = false;
    private static boolean upkeepEnabled = Config.getGlobalConfig().getBoolean("bc.engine.upkeep-enabled");
    /** first index is robot type, second is direction, third is x or y */
    private static final Map<ComponentType,int[][][]> offsets = GameMap.computeVisibleOffsets();
    /** number of bytecodes used in the most recent round */
    private volatile int bytecodesUsed = 0;
    private List<Message> incomingMessageQueue;
    protected GameMap.MapMemory mapMemory;
    private InternalRobotBuffs buffs = new InternalRobotBuffs(this);
	public final Chassis chassis;
	/** all actions that have been performed in the current round */
	private List<Signal> actions;
	private volatile boolean on;
	private volatile boolean hasBeenOff;
	private volatile int cores;
	private volatile int platings;
	private volatile int weight;

	public static class ComponentSet extends ForwardingMultimap<ComponentClass,BaseComponent> {

		Multimap<ComponentClass,BaseComponent> backingMap = HashMultimap.create();

		protected Multimap<ComponentClass,BaseComponent> delegate() {
			return backingMap;
		}

		public boolean add(BaseComponent c) {
			return put(c.componentClass(),c);
		}

		public boolean remove(BaseComponent c) {
			return remove(c.componentClass(),c);
		}

		public boolean contains(BaseComponent c) {
			return containsEntry(c.componentClass(),c);
		}

	}

	public final ComponentSet components;

    public InternalRobotBuffs getBuffs() {
        return buffs;
    }

	@SuppressWarnings("unchecked")
    public InternalRobot(GameWorld gw, Chassis chassis, MapLocation loc, Team t,
            boolean spawnedRobot) {
        super(gw, loc, chassis.level, t);
        myDirection = Direction.values()[gw.getRandGen().nextInt(8)];
		this.chassis = chassis;

		myEnergonLevel = getMaxEnergon();
        
		incomingMessageQueue = new LinkedList<Message>();

        mapMemory = new GameMap.MapMemory(gw.getGameMap());
        saveMapMemory(null, loc, false);
        controlBits = 0;

		components = new ComponentSet();
		if(chassis.motor!=null)
			equip(new InternalComponent(myGameWorld,myLocation,chassis.motor));

		actions = new LinkedList<Signal>();

		on = true;
    }

	public boolean isOn() { return on; }

	public void setPower(boolean b) {
		on = b;
		if(!b) hasBeenOff=true;
	}

	public boolean queryHasBeenOff() {
		boolean tmp = hasBeenOff;
		hasBeenOff = false;
		return tmp;
	}

	public boolean hasRoomFor(InternalComponent c) {
		return c.type().weight + weight <= chassis.weight;	
	}

	public void equip(InternalComponent c) {
		BaseComponent controller;
		ComponentType type = c.type();
		switch(type) {
		case SHIELD:
		case HARDENED:
		case REGEN:
		case PLASMA:
		case PLATING:
		case PROCESSOR:
			controller = new BaseComponent(c,this);
			break;
		case SMG:
		case BLASTER:
		case CANNON:
		case RAILGUN:
		case HAMMER:
		case GLUEGUN:
		case MEDIC:
			controller = new Weapon(c,this);
			break;
		case SATELLITE:
		case TELESCOPE:
		case SIGHT:
		case RADAR:
			controller = new Sensor(c,this);
			break;
		case ANTENNA:
		case DISH:
		case NETWORK:
			controller = new Radio(c,this);
			break;
		case SMALL_MOTOR:
		case MEDIUM_MOTOR:
		case LARGE_MOTOR:
		case FLYING_MOTOR:
			controller = new Motor(c,this);
			break;
		default:
			controller = null;
		}
		c.setController(controller);
		components.add(controller);
		weight+=type.weight;
		if(type==ComponentType.PLATING)
			platings++;
		else if(type==ComponentType.PROCESSOR)
			cores++;
	}

	public void unequip(BaseComponent c) {
		c.getComponent().setController(null);
		components.remove(c);
		ComponentType type = c.type();
		weight-=type.weight;
		if(type==ComponentType.PLATING)
			platings--;
		else if(type==ComponentType.PROCESSOR)
			cores--;
	}

	public void addAction(Signal s) {
		actions.add(s);
	}

	public Chassis getChassis() {
		return chassis;
	}

	public InternalComponent [] getComponents() {
		return Iterables.toArray(Iterables.transform(components.values(),Util.controllerToComponent),InternalComponent.class);
	}

	public BaseComponent [] getComponentControllers() {
		return components.values().toArray(new BaseComponent [0]);
	}

	public BaseComponent [] getComponentControllers(ComponentClass cl) {
		return components.get(cl).toArray(new BaseComponent [0]);
	}

	public BaseComponent [] getComponentControllers(final ComponentType t) {
		Predicate<BaseComponent> p = new Predicate<BaseComponent>() {
			public boolean apply(BaseComponent c) {
				return c.type()==t;
			}
		};
		Iterable<BaseComponent> filtered = Iterables.filter(components.get(t.componentClass),p);
		return Iterables.toArray(filtered,BaseComponent.class);
	}

    @Override
    public void processBeginningOfRound() {
        super.processBeginningOfRound();
        buffs.processBeginningOfRound();

		for(BaseComponent c : components.values()) {
			c.component.processBeginningOfRound();
		}
    }

    public void processBeginningOfTurn() {
	}

    @Override
    public void processEndOfTurn() {
        super.processEndOfTurn();
		for(BaseComponent c : components.values()) {
			c.getComponent().processEndOfTurn();
		}
		for(Signal s : actions) {
			myGameWorld.visitSignal(s);
		}
		actions.clear();
	}

    @Override
    public void processEndOfRound() {
        super.processEndOfRound();

        if (upkeepEnabled) {
        	// TODO: upkeep
		}

		// TODO: regeneration

        buffs.processEndOfRound();
        
		if (myEnergonLevel <= 0) {
            suicide();
        }
    }

    public double getEnergonLevel() {
        return myEnergonLevel;
    }

    public Direction getDirection() {
        return myDirection;
    }
	
	public void takeDamage(double baseAmount) {
		// TODO: iron (use buffs)
		if(baseAmount<0) changeEnergonLevel(-baseAmount);
		boolean haveHardened = false;
		double minDamage = Math.min(SHIELD_MIN_DAMAGE, baseAmount);
		for(BaseComponent c : components.get(ComponentClass.ARMOR)) {
			switch(c.type()) {
				case SHIELD:
					baseAmount-=SHIELD_DAMAGE_REDUCTION;
					break;
				case HARDENED:
					haveHardened=true;
					break;
				case PLASMA:
					if(!c.getComponent().isActive()) {
						c.getComponent().activate();
						return;
					}
					break;
			}
		}
		if(haveHardened&&baseAmount>HARDENED_MAX_DAMAGE)
			changeEnergonLevelFromAttack(-HARDENED_MAX_DAMAGE);
		else
			changeEnergonLevelFromAttack(-Math.max(minDamage,baseAmount));		
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
        return chassis.maxHp + platings * PLATING_HP_BONUS; 
    }

    public void setDirection(Direction dir) {
        myDirection = dir;
        saveMapMemory(getLocation(), getLocation(), false);
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
		for(BaseComponent c : components.get(ComponentClass.SENSOR)) {
        	int[][] myOffsets = offsets.get(c.type())[myDirection.ordinal()];
        	mapMemory.rememberLocations(newLoc, myOffsets[0], myOffsets[1]);
		}
    }

    @Override
    public void setLocation(MapLocation newLoc) {
        MapLocation oldLoc = getLocation();
        super.setLocation(newLoc);
        saveMapMemory(oldLoc, newLoc, false);
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
		if(!on) return 0;
		return BYTECODE_LIMIT_BASE + BYTECODE_LIMIT_ADDON * cores;
	}


    public boolean hasBeenAttacked() {
        return hasBeenAttacked;
    }

    @Override
    public String toString() {
        return String.format("%s:%s#%d",getTeam(),chassis,getID());
    }

    public void freeMemory() {
        incomingMessageQueue = null;
        mapMemory = null;
        buffs = null;
    }
}
