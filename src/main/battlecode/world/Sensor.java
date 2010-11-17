package battlecode.world;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
import battlecode.common.MineInfo;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.SensorController;

import java.util.Set;

public class Sensor extends BaseComponent implements SensorController {

	public Sensor(ComponentType type, InternalRobot robot) {
		super(type,robot);
	}

	public GameObject senseObjectAtLocation(MapLocation loc, RobotLevel height) throws GameActionException {
		assertEquipped();
		assertNotNull(loc);
		assertNotNull(height);
		assertWithinRange(loc);
		return gameWorld.getObject(loc,height);
	}

	@SuppressWarnings("unchecked")
	public <T extends GameObject> T [] senseNearbyGameObjects(Class<T> type) {
		Predicate<GameObject> p = Predicates.and(objectWithinRangePredicate(),Predicates.instanceOf(type),Predicates.not(Predicates.equalTo(robot)));
		return (T [])Iterables.toArray(Iterables.filter(gameWorld.allObjects(),p),GameObject.class);
	}
	
	public RobotInfo senseRobotInfo(Robot r) throws GameActionException {
		assertEquipped();
		InternalRobot ir = castInternalRobot(r);
		assertWithinRange(ir);
		ComponentType [] components;
		if(type()==ComponentType.SATELLITE)
			components = ir.getComponentTypes();
		else
			components = null;
		MapLocation loc;
		if(ir.container()!=null)
			loc = ir.container().getLocation();
		else
			loc = ir.getLocation();
		return new RobotInfo(ir,ir.getLocation(),ir.getEnergonLevel(),ir.getMaxEnergon(),
			ir.getDirection(),ir.isOn(),components);
		
	}

	public MineInfo senseMineInfo(Mine m) throws GameActionException {
		InternalMine im = castInternalMine(m);
		assertWithinRange(im);
		return new MineInfo(im,im.getRoundsLeft());
	}

	public MapLocation senseLocationOf(GameObject o) throws GameActionException {
		InternalObject io = castInternalObject(o);
		assertWithinRange(io);
		return io.getLocation();
	}

	public boolean canSenseObject(GameObject o) {
		assertEquipped();
		return checkWithinRange(castInternalObject(o));
	}

}
