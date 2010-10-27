package battlecode.world;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import battlecode.common.Component;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.SensorController;

import java.util.Set;

public class Sensor extends BaseComponent implements SensorController {

	public Sensor(InternalComponent component, InternalRobot robot) {
		super(component,robot);
	}

	public Robot senseRobotAtLocation(MapLocation loc, RobotLevel height) throws GameActionException {
		assertEquipped();
		assertNotNull(loc);
		assertNotNull(height);
		assertWithinRange(loc);
		return gameWorld.getObjectOfType(loc,height,InternalRobot.class);
	}

	public Robot [] senseNearbyRobots() {
		assertEquipped();
		Predicate<GameObject> p = Predicates.and(objectWithinRangePredicate(),Util.isRobot);
		return (Robot [])Iterables.toArray(Iterables.filter(gameWorld.allObjects(),p),GameObject.class);
	}

	public RobotInfo senseRobotInfo(Robot r) throws GameActionException {
		assertEquipped();
		InternalRobot ir = castInternalRobot(r);
		assertWithinRange(ir);
		Component [] components;
		if(type()==ComponentType.SATELLITE||GameWorld.canStealComponent(robot,ir))
			components = ir.getComponents();
		else
			components = null;
		return new RobotInfo(ir,ir.getLocation(),ir.getEnergonLevel(),ir.getMaxEnergon(),
			ir.getDirection(),ir.isOn(),components);
		
	}

	public MapLocation senseLocationOf(GameObject o) throws GameActionException {
		assertEquipped();
		InternalObject io = castInternalObject(o);
		assertWithinRange(io);
		return io.getLocation();
	}

	public InternalComponent [] senseNearbyComponents() {
		assertEquipped();
		return Iterables.toArray(gameWorld.getLooseComponents(locWithinRangePredicate()),InternalComponent.class);
	}

	public InternalComponent [] senseComponentsAtLocation(MapLocation loc) throws GameActionException {
		assertEquipped();
		assertNotNull(loc);
		assertWithinRange(loc);
		return gameWorld.getComponentsAt(loc).toArray(new InternalComponent [0]);
	}

	public boolean canSenseObject(GameObject o) {
		assertEquipped();
		return checkWithinRange(castInternalObject(o));
	}

}
