package battlecode.world;

import battlecode.common.GameConstants;
import static battlecode.common.GameActionExceptionType.*;
import battlecode.common.*;
import battlecode.engine.GenericController;
import battlecode.engine.instrumenter.RobotMonitor;
import battlecode.engine.instrumenter.RobotDeathException;
import battlecode.engine.signal.Signal;
import battlecode.world.signal.*;
import java.util.Arrays;
import java.util.HashSet;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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
public class RobotControllerImpl extends ControllerShared implements RobotController, GenericController, GameConstants {

    public RobotControllerImpl(GameWorld gw, InternalRobot r) {
        super(gw, r);
    }

	public void assertHaveFlux(double amount) throws GameActionException {
		if(amount>robot.getFlux())
			throw new GameActionException(NOT_ENOUGH_FLUX,"You do not have enough flux to do that.");
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
    public double getEnergon() {
        return robot.getEnergonLevel();
    }

    public double getTeamResources() {
        return gameWorld.resources(getTeam());
    }

    /**
     * {@inheritDoc}
     */
    public double getMaxEnergon() {
        return robot.getMaxEnergon();
    }

	public double getFlux() {
		return robot.getFlux();
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


    public RobotType getType() {
        return robot.type;
    }

    //***********************************
    //****** ACTION METHODS *************
    //***********************************

	/**
     * {@inheritDoc}
     */
    public void yield() {
		int bytecodesBelowBase = BYTECODE_LIMIT - RobotMonitor.getBytecodesUsed();
		if(bytecodesBelowBase>0&&robot.type!=RobotType.ARCHON)
			robot.adjustFlux(GameConstants.YIELD_BONUS*bytecodesBelowBase/GameConstants.BYTECODE_LIMIT*GameConstants.UNIT_UPKEEP);	
        RobotMonitor.endRunner();
    }

	public void transferFlux(MapLocation loc, RobotLevel height, double amount) throws GameActionException {
		if(amount<=0||Double.isNaN(amount))
			throw new IllegalArgumentException("The amount of flux transferred must be positive.");
		assertHaveFlux(amount);
		assertWithinRange(loc,2);
		InternalRobot ir = robotOrException(loc,height);
		robot.adjustFlux(-amount);
		ir.adjustFlux(amount);
		gameWorld.addSignal(new TransferFluxSignal(robot,ir,amount));
	}

	public void spawn(RobotType type) throws GameActionException {
		// If we decide to let other robots spawn, then we should make
		// sure that air units can't spawn ground units.
		if(type==RobotType.ARCHON)
			throw new IllegalArgumentException("Archons may not be spawned.");
		if(robot.type!=RobotType.ARCHON)
			throw new IllegalStateException("Only archons can spawn.");
        assertNotNull(type);
        assertNotMoving();
        assertHaveFlux(type.spawnCost);
		MapLocation loc = getLocation().add(getDirection());
		if (!gameWorld.canMove(type.level, loc))
            throw new GameActionException(GameActionExceptionType.CANT_MOVE_THERE, "That square is occupied.");
		if (type==RobotType.TOWER) {
			InternalPowerNode node = gameWorld.getPowerNode(loc);
			if(node==null)
            	throw new GameActionException(GameActionExceptionType.CANT_MOVE_THERE, "A tower may only be built on top of a power node.");
			if(!node.connected(robot.getTeam()))
            	throw new GameActionException(GameActionExceptionType.CANT_MOVE_THERE, "That node is not connected to your power core.");
		}
        robot.adjustFlux(-type.spawnCost);
        robot.activateMovement(new SpawnSignal(loc, type, robot.getTeam(), robot),1);
    }

	public void regenerate() throws GameActionException {
		if(robot.type!=RobotType.SCOUT)
			throw new IllegalStateException("Only scouts can regenerate.");
		assertHaveFlux(REGEN_COST);
		robot.adjustFlux(-REGEN_COST);
		for(InternalRobot ir : gameWorld.getAllRobotsWithinRadiusDonutSq(getLocation(),RobotType.SCOUT.attackRadiusMaxSquared,RobotType.SCOUT.attackRadiusMinSquared)) {
			if(ir.getTeam()==robot.getTeam())
				ir.setRegen();
		}
		gameWorld.addSignal(new RegenSignal(robot));
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

    //***********************************
    //****** SENSING METHODS *******
    //***********************************
  
	public void assertCanSense(MapLocation loc) throws GameActionException {
		if(!checkCanSense(loc))
			throw new GameActionException(CANT_SENSE_THAT,"That location is not within the robot's sensor range.");
	}

	public void assertCanSense(InternalObject obj) throws GameActionException {
		if(!checkCanSense(obj))
			throw new GameActionException(CANT_SENSE_THAT,"That object is not within the robot's sensor range.");
	}

	public boolean checkCanSense(MapLocation loc) {
		MapLocation myLoc = getLocation();
		return myLoc.distanceSquaredTo(loc)<=robot.type.sensorRadiusSquared
			&& gameWorld.inAngleRange(myLoc,getDirection(),loc,robot.type.sensorCosHalfTheta);
	}

	public boolean checkCanSense(InternalObject obj) {
		return obj.exists() && checkCanSense(obj.getLocation());
	}

	public GameObject senseObjectAtLocation(MapLocation loc, RobotLevel height) throws GameActionException {
        assertNotNull(loc);
        assertNotNull(height);
        assertCanSense(loc);
        return gameWorld.getObject(loc, height);
    } 

    @SuppressWarnings("unchecked")
    public <T extends GameObject> T[] senseNearbyGameObjects(final Class<T> type) {
        Predicate<InternalObject> p = new Predicate<InternalObject>() {
            public boolean apply(InternalObject o) {
                return checkCanSense(o) && (type.isInstance(o)) && (!o.equals(robot));
            }
        };
        return Iterables.toArray((Iterable<T>) Iterables.filter(gameWorld.allObjects(), p), type);
    }

    public RobotInfo senseRobotInfo(Robot r) throws GameActionException {
        InternalRobot ir = castInternalRobot(r);
        assertCanSense(ir);
        return new RobotInfo(ir, ir.sensedLocation(), ir.getEnergonLevel(),
                ir.getFlux(), ir.getDirection(), ir.type, ir.getTeam(), ir.getRegen());
    }

    public MapLocation senseLocationOf(GameObject o) throws GameActionException {
        InternalObject io = castInternalObject(o);
        assertCanSense(io);
        return io.sensedLocation();
    }

    public boolean canSenseObject(GameObject o) {
        return checkCanSense(castInternalObject(o));
    }

    public boolean canSenseSquare(MapLocation loc) {
        return checkCanSense(loc);
    }

	public MapLocation [] senseAlliedArchons() {
		return gameWorld.getArchons(robot.getTeam());
	}

	public PowerNode [] senseAlliedPowerNodes() {
		return Iterables.toArray(gameWorld.getPowerNodesByTeam(robot.getTeam()),PowerNode.class);
	}

	public MapLocation [] senseCapturablePowerNodes() {
		return Lists.transform(gameWorld.getCapturableNodes(robot.getTeam()),Util.objectLocation).toArray(new MapLocation [0]); 
	}

	public boolean senseConnected(PowerNode p) {
		InternalPowerNode ip = castInternalPowerNode(p);
		return ip.connected(robot.getTeam());
	}
	
	public boolean senseOpponentConnected(PowerNode p) throws GameActionException {
		InternalPowerNode ip = castInternalPowerNode(p);
		assertCanSense(ip);
		return ip.connected(robot.getTeam().opponent());
	}

	public PowerNode sensePowerCore() {
		return gameWorld.getPowerCore(robot.getTeam());
	}

	/**
     * {@inheritDoc}
     */
    public TerrainTile senseTerrainTile(MapLocation loc) {
        assertNotNull(loc);
        return robot.getMapMemory().recallTerrain(loc);
    }
	
	// ***********************************
	// ****** MOVEMENT METHODS ********
	// ***********************************


	public int roundsUntilMovementIdle() {
		return robot.roundsUntilMovementIdle();
	}

	public boolean isMovementActive() {
		return roundsUntilMovementIdle()>0;
	}

	public void assertNotMoving() throws GameActionException {
		if(isMovementActive())
			throw new GameActionException(ALREADY_ACTIVE,"This robot is already moving.");
	}

	public void moveForward() throws GameActionException {
		move(robot.getDirection());
	}

	public void moveBackward() throws GameActionException {
		move(robot.getDirection().opposite());
	}

	private void move(Direction d) throws GameActionException {
		assertNotMoving();
		assertHaveFlux(robot.type.moveCost);
		assertCanMove(d);
		int delay = d.isDiagonal()?robot.type.moveDelayDiagonal:
			robot.type.moveDelayOrthogonal;
		robot.activateMovement(new MovementSignal(robot,getLocation().add(d),
			d==getDirection(),delay),delay);
		robot.adjustFlux(-robot.type.moveCost);
	}

	public void setDirection(Direction d) throws GameActionException {
		assertNotMoving();
		assertValidDirection(d);
		robot.activateMovement(new SetDirectionSignal(robot,d),1);
	}

	public boolean canMove(Direction d) {
		assertValidDirection(d);
		return gameWorld.canMove(robot.getRobotLevel(),getLocation().add(d));
	}

	public void assertCanMove(Direction d) throws GameActionException {
		if(!canMove(d))
			throw new GameActionException(GameActionExceptionType.CANT_MOVE_THERE, "Cannot move in the given direction: " + d);
	}

	protected void assertValidDirection(Direction d) {
        assertNotNull(d);
        if (d == Direction.NONE || d == Direction.OMNI)
            throw new IllegalArgumentException("You cannot move in the direction NONE or OMNI.");
    }
	
	// ***********************************
	// ****** ATTACK METHODS ********
	// ***********************************

	public int roundsUntilAttackIdle() {
		return robot.roundsUntilAttackIdle();
	}

	public boolean isAttackActive() {
		return robot.roundsUntilAttackIdle()>0;
	}

	protected void assertNotAttacking() throws GameActionException {
		if(isAttackActive())
			throw new GameActionException(ALREADY_ACTIVE,"This robot is already attacking.");
	}

	protected void assertCanAttack(MapLocation loc, RobotLevel height) throws GameActionException {
		if(!robot.type.canAttack(height))
			throw new GameActionException(OUT_OF_RANGE,"This robot can't attack robots at that height.");
		if(!canAttackSquare(loc))
			throw new GameActionException(OUT_OF_RANGE,"That location is out of this robot's attack range");
	}

	public boolean canAttackSquare(MapLocation loc) {
		assertNotNull(loc);
		return GameWorld.canAttackSquare(robot,loc);
	}

	public void attackSquare(MapLocation loc, RobotLevel height) throws GameActionException {
        assertNotAttacking();
		if(robot.type==RobotType.SCORCHER) {
			loc = null;
			height = null;
		}
		else {
        	assertNotNull(loc);
        	assertNotNull(height);
        	assertCanAttack(loc,height);
		}
        robot.activateAttack(new AttackSignal(robot, loc, height),robot.type.attackDelay);
		// if this robot killed itself, its turn should end
		if(robot.getEnergonLevel()<0) {
			throw new RobotDeathException();
		}
    }
    
	//************************************
    //******** BROADCAST METHODS **********
    //************************************

	public boolean hasBroadcasted() {
		return robot.hasBroadcasted();
	}
	
	public void broadcast(Message m) throws GameActionException {
        if(hasBroadcasted())
			throw new GameActionException(ALREADY_ACTIVE,"This robot has already broadcasted this turn.");
        assertNotNull(m);
		double cost = m.getFluxCost();
		assertHaveFlux(cost);
        robot.activateBroadcast(new BroadcastSignal(robot, BROADCAST_RADIUS_SQUARED, m));
		robot.adjustFlux(-cost);
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

}
