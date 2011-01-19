package battlecode.world;

import static battlecode.common.GameConstants.BYTECODE_LIMIT_BASE;
import static battlecode.common.GameConstants.NUMBER_OF_INDICATOR_STRINGS;
import static battlecode.common.GameConstants.YIELD_BONUS;
import static battlecode.common.GameActionExceptionType.*;
import battlecode.common.*;
import battlecode.engine.GenericController;
import battlecode.engine.instrumenter.RobotMonitor;
import battlecode.engine.instrumenter.RobotDeathException;
import battlecode.engine.signal.Signal;
import battlecode.world.signal.*;
import java.util.Arrays;

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

    protected MapLocation locThisTurn;
    protected Direction dirThisTurn;

    public RobotControllerImpl(GameWorld gw, InternalRobot r) {
        super(gw, r);
        r.setRC(this);
    }

    //*********************************
    //****** QUERY METHODS ********
    //*********************************
    /**
     * {@inheritDoc}
     */
    public Direction getDirection() {
        return dirThisTurn;
    }

    /**
     * {@inheritDoc}
     */
    public double getHitpoints() {
        return robot.getEnergonLevel();
    }

    public double getTeamResources() {
        return gameWorld.resources(getTeam());
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
        return locThisTurn;
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


    public Chassis getChassis() {
        return robot.getChassis();
    }

    public ComponentController[] components() {
        return robot.getComponentControllers();
    }

    public ComponentController[] newComponents() {
        return robot.getNewComponentControllers();
    }

    public ComponentController[] components(ComponentType type) {
        return robot.getComponentControllers(type);
    }

    public ComponentController[] components(ComponentClass cls) {
        return robot.getComponentControllers(cls);
    }

    //***********************************
    //****** ACTION METHODS *************
    //***********************************

    public void turnOff() {
		gameWorld.visitSignal(new TurnOffSignal(robot,true));
    	RobotMonitor.endRunner();
	}

    public boolean wasTurnedOff() {
        return robot.queryHasBeenOff();
    }

	public void turnOn(MapLocation loc, RobotLevel height) throws GameActionException {
		assertNotNull(loc);
		assertRobotHeight(height);
		assertWithinRange(loc,2);
		InternalRobot ir = gameWorld.getRobot(loc,height);
		assertSameTeam(ir);
		gameWorld.visitSignal(new TurnOnSignal(ir,robot,false));
	}

	/**
     * {@inheritDoc}
     */
    public void yield() {
		int bytecodesBelowBase = BYTECODE_LIMIT_BASE - RobotMonitor.getBytecodesUsed();
		if(bytecodesBelowBase>0)
			gameWorld.adjustResources(robot.getTeam(),YIELD_BONUS*bytecodesBelowBase/BYTECODE_LIMIT_BASE*robot.chassis.upkeep);	
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

    public void setIndicatorStringFormat(int stringIndex, String format, Object... args) {
        setIndicatorString(stringIndex, String.format(format, args));
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
        gameWorld.setArchonMemory(robot.getTeam(), index, value);
    }

    public void setTeamMemory(int index, long value, long mask) {
        gameWorld.setArchonMemory(robot.getTeam(), index, value, mask);
    }

    public long[] getTeamMemory() {
		long [] arr = gameWorld.getOldArchonMemory()[robot.getTeam().ordinal()];
		return Arrays.copyOf(arr,arr.length);
    }

    public int hashCode() {
        return robot.getID();
    }

    public void processBeginningOfTurn() {
        if ((!robot.inTransport())&&!(robot.getLocation().equals(locThisTurn) && robot.getDirection().equals(dirThisTurn))) {
            robot.saveMapMemory(locThisTurn, robot.getLocation(), false);
            locThisTurn = robot.getLocation();
            dirThisTurn = robot.getDirection();
        }
    }
}
