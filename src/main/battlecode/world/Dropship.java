package battlecode.world;

import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.DropshipController;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotLevel;

import battlecode.world.signal.LoadSignal;
import battlecode.world.signal.UnloadSignal;

public class Dropship extends BaseComponent implements DropshipController {
	
	public Dropship(InternalRobot robot) {
		super(ComponentType.DROPSHIP,robot);
	}

	public void load(MapLocation loc) throws GameActionException {
		load(loc,RobotLevel.ON_GROUND);
	}

	public void load(MapLocation loc, RobotLevel height) throws GameActionException {
		assertInactive();
		assertNotNull(loc);
		assertWithinRange(loc);
		InternalRobot ir = alliedRobotAt(loc,height);
		assertNotBuilding(ir);
		assertSpaceAvailable(ir.getChassis());
		if(ir==robot) {
			throw new GameActionException(GameActionExceptionType.NO_ROOM_IN_CHASSIS,"This robot cannot transport itself!");
		}
		activate(new LoadSignal(robot,ir));
	}

	public void unload(Robot r, MapLocation loc) throws GameActionException {
		assertInactive();
		InternalRobot ir = castInternalRobot(r);
		assertCarrying(ir);
		assertCanUnload(loc);
		activate(new UnloadSignal(robot,ir,loc));
	}
	
	public boolean canUnload(MapLocation loc) {
		return canUnload(loc,RobotLevel.ON_GROUND);
	}

	public boolean canUnload(MapLocation loc, RobotLevel height) {
		assertNotNull(loc);
		if(!checkWithinRange(loc)) return false;
		return gameWorld.canMove(height,loc);
	}

	public InternalRobot [] robotsOnBoard() {
		return robot.robotsOnBoard();
	}

	public int spaceAvailable() {
		return robot.spaceAvailable();
	}

	public void assertSpaceAvailable(Chassis newChassis) throws GameActionException {
		if(newChassis==Chassis.BUILDING)
			throw new GameActionException(GameActionExceptionType.NO_ROBOT_THERE,"You can't transport a building.");
		if(spaceAvailable()<newChassis.weight)
			throw new GameActionException(GameActionExceptionType.INSUFFICIENT_ROOM_IN_CARGO,"You don't have space for that robot.");
	}

	public void assertCanUnload(MapLocation loc) throws GameActionException {
		if(!canUnload(loc))
			throw new GameActionException(GameActionExceptionType.CANT_MOVE_THERE,"You cannot unload a robot there.");
	}

	public void assertCarrying(InternalRobot r) throws GameActionException {
		if(r.container()!=robot)
			throw new GameActionException(GameActionExceptionType.OUT_OF_RANGE,"You are not carrying that robot.");
	}

	public void assertNotBuilding(InternalRobot r) throws GameActionException {
		if(r.getChassis()==Chassis.BUILDING)
			throw new GameActionException(GameActionExceptionType.WRONG_ROBOT_TYPE,"You cannot transport a building.");
	}

}
