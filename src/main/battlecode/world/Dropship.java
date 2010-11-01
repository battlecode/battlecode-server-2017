package battlecode.world;

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
		assertInactive();
		assertNotNull(loc);
		assertWithinRange(loc);
		InternalRobot ir = alliedRobotAt(loc,RobotLevel.ON_GROUND);
		activate();
		robot.addAction(new LoadSignal(robot,ir));
	}

	public void unload(Robot r, MapLocation loc) throws GameActionException {
		assertInactive();
		InternalRobot ir = castInternalRobot(r);
		assertCarrying(ir);
		assertCanUnload(loc);
		robot.addAction(new UnloadSignal(robot,ir,loc));
	}

	public boolean canUnload(MapLocation loc) {
		assertNotNull(loc);
		if(!checkWithinRange(loc)) return false;
		return gameWorld.canMove(RobotLevel.ON_GROUND,loc);
	}

	public InternalRobot [] robotsOnBoard() {
		return robot.robotsOnBoard();
	}

	public int spaceAvailable() {
		return robot.spaceAvailable();
	}

	public void assertCanUnload(MapLocation loc) throws GameActionException {
		if(!canUnload(loc))
			throw new GameActionException(GameActionExceptionType.CANT_MOVE_THERE,"You cannot unload a robot there.");
	}

	public void assertCarrying(InternalRobot r) throws GameActionException {
		if(r.container()!=robot)
			throw new GameActionException(GameActionExceptionType.OUT_OF_RANGE,"You are not carrying that robot.");
	}

}
