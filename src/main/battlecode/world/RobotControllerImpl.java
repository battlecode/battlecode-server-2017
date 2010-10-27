package battlecode.world;

import static battlecode.common.GameConstants.NUMBER_OF_INDICATOR_STRINGS;
import static battlecode.common.GameConstants.YIELD_BONUS;
import static battlecode.common.GameActionExceptionType.*;
import battlecode.common.*;
import battlecode.engine.GenericController;
import battlecode.engine.instrumenter.RobotMonitor;
import battlecode.engine.instrumenter.RobotDeathException;
import battlecode.engine.signal.Signal;
import battlecode.world.signal.*;

/*
TODO:
- tweak player
-specs & software page?
- performance tips
- figure out why it's not deterministic

- optimize
- non-constant java.util costs
- player's navigation class, and specs about it
- fix execute action hack w/ robotmonitor ??
- fix playerfactory/spawnsignal hack??
- better suicide() ??
- pare down GW, GWviewer methods; add engine.getallsignals?

- TEST energon transfer every round, with cap
- TEST better println tagging
- TEST optimized silencing
- TEST "control ints"
- TEST indicator strings
- TEST map offsets
- TEST tiebreaking conditions
- TEST senseNearbyUpgrades
- TEST no battery freeze
- TEST upgrades
- TEST upgrades on map
- TEST only one energonchangesignal / round / robot
- TEST exception polling for action queue
- TEST fix action timing
- TEST action queue
- TEST broadcast queue
- TEST emp detonate
- TEST locating enemy batteries -- return direction
- TEST InternalArchon
- TEST energon decay
- TEST new energon transfer
- TEST map parsing
- TEST run perl script to get robottypes
- TEST serializable stuff
- TEST getNextMessage arrays
- TEST responding to signals
- TEST clock
 */
public class RobotControllerImpl implements RobotController, GenericController {

    private final GameWorld myGameWorld;
    private final InternalRobot myRobot;

    public RobotControllerImpl(GameWorld gw, InternalRobot r) {
        myGameWorld = gw;
        myRobot = r;
    }

    //*********************************
    //****** QUERY METHODS ********
    //*********************************
   
    /**
     * {@inheritDoc}
     */
    public Direction getDirection() {
        return myRobot.getDirection();
    }

    /**
     * {@inheritDoc}
     */
    public double getHitpoints() {
        return myRobot.getEnergonLevel();
    }

    /**
     * {@inheritDoc}
     */
    public double getMaxEnergonLevel() {
        return myRobot.getMaxEnergon();
    }

    /**
     * {@inheritDoc}
     */
    public MapLocation getLocation() {
        return myRobot.getLocation();
    }

    /**
     * {@inheritDoc}
     */
    public Message getNextMessage() {
        return myRobot.dequeueIncomingMessage();
    }

    /**
     * {@inheritDoc}
     */
    public Message[] getAllMessages() {
        return myRobot.dequeueIncomingMessages();
    }

    /**
     * {@inheritDoc}
     */
    public Team getTeam() {
        return myRobot.getTeam();
    }

    /**
     * {@inheritDoc}
     */
    public InternalRobot getRobot() {
        return myRobot;
    }

    public int getMapMinPoints() {
        return myGameWorld.getGameMap().getMinPoints();
    }

	public Chassis getChassis() {
		return myRobot.getChassis(); 
	}

	public ComponentController [] components() {
		return myRobot.getComponentControllers();
	}

	public ComponentController [] components(ComponentType type) {
		return myRobot.getComponentControllers(type);
	}

	public ComponentController [] components(ComponentClass cls) {
		return myRobot.getComponentControllers(cls);
	}
    
	//***********************************
    //****** ACTION METHODS *************
    //***********************************
    
	public void turnOff() {
	}

	public boolean wasTurnedOff() {
		return myRobot.queryHasBeenOff();
	}

    /**
     * {@inheritDoc}
     */
    public void yield() {
        myRobot.changeEnergonLevel(YIELD_BONUS * RobotMonitor.getBytecodesUsedPercent() * myRobot.chassis.upkeep);
        RobotMonitor.endRunner();
    }

    /**
     * {@inheritDoc}
     */
    public void suicide() {
        throw new RobotDeathException();
    }

    /**
     * {@inheritDoc}
     */
    public void breakpoint() {
        myGameWorld.notifyBreakpoint();
    }

	public void equip(Component c) throws GameActionException {
		InternalComponent ic = castInternalComponent(c);
		assertWithinRange(ic.getLocation(),2);
		if(!ic.canBeEquipped())
			throw new GameActionException(CANT_EQUIP_THAT,"You can't equip that component.");
	}

    //***********************************
    //****** SENSING METHODS *******
    //***********************************

    /**
     * {@inheritDoc}
     */
    public TerrainTile senseTerrainTile(MapLocation loc) {
        assertNotNull(loc);
        return myRobot.getMapMemory().recallTerrain(loc);
    }

    //************************************
    //******** MISC. METHODS **********
    //************************************
    /**
     * {@inheritDoc}
     */
    public void setIndicatorString(int stringIndex, String newString) {
        if (stringIndex >= 0 && stringIndex < NUMBER_OF_INDICATOR_STRINGS)
            (new IndicatorStringSignal(myRobot, stringIndex, newString)).accept(myGameWorld);
    }

	public void setIndicatorStringFormat(int stringIndex, String format, Object ... args) {
		setIndicatorString(stringIndex,String.format(format,args));
	}

    /**
     * {@inheritDoc}
     */
    public long getControlBits() {
        return myRobot.getControlBits();
    }

    /**
     * {@inheritDoc}
     */
    public void addMatchObservation(String observation) {
        (new MatchObservationSignal(myRobot, observation)).accept(myGameWorld);
    }

    /**
     * {@inheritDoc}
     */
    public void setArchonMemory(long memory) throws GameActionException {
    	// what to do about this?
	}

    public long[] getOldArchonMemory() {
        return myGameWorld.getOldArchonMemory()[myRobot.getTeam().ordinal()];
    }


    //************************************
    //********* ASSERTIONS ************
    //************************************
    public static void assertNotNull(Object o) {
        if (o == null)
            throw new NullPointerException("Argument has an invalid null value");
    }

	public static InternalComponent castInternalComponent(Component c) throws GameActionException {
		assertNotNull(c);
		if(!(c instanceof InternalComponent))
            throw new GameActionException(INVALID_OBJECT, "Invalid Component (don't extend Component!)");
		return (InternalComponent)c;
	}

	public void assertWithinRange(MapLocation loc, int distance) throws GameActionException {
		if(myRobot.getLocation().distanceSquaredTo(loc)>distance)
			throw new GameActionException(CANT_SENSE_THAT,"That is too far away.");
	}
	
	public int hashCode() {
		return myRobot.getID();
	}

}
