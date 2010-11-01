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
public class RobotControllerImpl extends ControllerShared implements RobotController, GenericController {

    public RobotControllerImpl(GameWorld gw, InternalRobot r) {
        super(gw,r);
    }

    //*********************************
    //****** QUERY METHODS ********
    //*********************************
   
    /**
     * {@inheritDoc}
     */
    public Direction getDirection() {
        return robot.getDirection();
    }

    /**
     * {@inheritDoc}
     */
    public double getHitpoints() {
        return robot.getEnergonLevel();
    }

    /**
     * {@inheritDoc}
     */
    public double getMaxHp() {
        return robot.getMaxEnergon();
    }

    /**
     * {@inheritDoc}
     */
    public MapLocation getLocation() {
        return robot.getLocation();
    }

    /**
     * {@inheritDoc}
     */
    public Message getNextMessage() {
        return robot.dequeueIncomingMessage();
    }

    /**
     * {@inheritDoc}
     */
    public Message[] getAllMessages() {
        return robot.dequeueIncomingMessages();
    }

    /**
     * {@inheritDoc}
     */
    public Team getTeam() {
        return robot.getTeam();
    }

	public int getBytecodeLimit() {
		return robot.getBytecodeLimit();
	}

    public int getMapMinPoints() {
        return gameWorld.getGameMap().getMinPoints();
    }

	public Chassis getChassis() {
		return robot.getChassis(); 
	}

	public ComponentController [] components() {
		return robot.getComponentControllers();
	}

	public ComponentController [] newComponents() {
		return robot.getNewComponentControllers();
	}

	public ComponentController [] components(ComponentType type) {
		return robot.getComponentControllers(type);
	}

	public ComponentController [] components(ComponentClass cls) {
		return robot.getComponentControllers(cls);
	}
    
	//***********************************
    //****** ACTION METHODS *************
    //***********************************
    
	public void turnOff() {
		robot.setPower(false);
	}

	public boolean wasTurnedOff() {
		return robot.queryHasBeenOff();
	}

    /**
     * {@inheritDoc}
     */
    public void yield() {
        robot.changeEnergonLevel(YIELD_BONUS * RobotMonitor.getBytecodesUsedPercent() * robot.chassis.upkeep);
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
        gameWorld.notifyBreakpoint();
    }
	
	/*
	public void equip(Component c) throws GameActionException {
		InternalComponent ic = castInternalComponent(c);
		assertWithinRange(ic.getLocation(),2);
		if(!ic.canBeEquipped())
			throw new GameActionException(CANT_EQUIP_THAT,"You can't equip that component.");
		if(!robot.hasRoomFor(ic))
			throw new GameActionException(CANT_EQUIP_THAT,"There is not enough room in the chassis for that component.");
		robot.equip(ic);
	}
	*/

    //***********************************
    //****** SENSING METHODS *******
    //***********************************

    /**
     * {@inheritDoc}
     */
    public TerrainTile senseTerrainTile(MapLocation loc) {
        assertNotNull(loc);
        return robot.getMapMemory().recallTerrain(loc);
    }

    //************************************
    //******** MISC. METHODS **********
    //************************************
    /**
     * {@inheritDoc}
     */
    public void setIndicatorString(int stringIndex, String newString) {
        if (stringIndex >= 0 && stringIndex < NUMBER_OF_INDICATOR_STRINGS)
            (new IndicatorStringSignal(robot, stringIndex, newString)).accept(gameWorld);
    }

	public void setIndicatorStringFormat(int stringIndex, String format, Object ... args) {
		setIndicatorString(stringIndex,String.format(format,args));
	}

    /**
     * {@inheritDoc}
     */
    public long getControlBits() {
        return robot.getControlBits();
    }

    /**
     * {@inheritDoc}
     */
    public void addMatchObservation(String observation) {
        (new MatchObservationSignal(robot, observation)).accept(gameWorld);
    }

    /**
     * {@inheritDoc}
     */
    public void setTeamMemory(int index, long value) {
    	gameWorld.setArchonMemory(robot.getTeam(),index,value);
	}

    public long[] getTeamMemory() {
        return gameWorld.getOldArchonMemory()[robot.getTeam().ordinal()];
    }

	public int hashCode() {
		return robot.getID();
	}

}
