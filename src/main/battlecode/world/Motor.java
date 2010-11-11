package battlecode.world;

import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.MapLocation;
import battlecode.common.MovementController;
import battlecode.world.signal.MovementSignal;
import battlecode.world.signal.SetDirectionSignal;

public class Motor extends BaseComponent implements MovementController
{

	private static double sq2 = Math.sqrt(2.);

	int delayDiagonal;

	public Motor(ComponentType type, InternalRobot robot) {
		super(type,robot);
		delayDiagonal = (int)Math.round(type().delay*sq2);
	}

	public void moveForward() throws GameActionException {
		move(robot.getDirection());
	}

	public void moveBackward() throws GameActionException {
		move(robot.getDirection().opposite());
	}

	private void move(Direction d) throws GameActionException {
		assertInactive();
		assertCanMove(robot.getDirection());
		int delay = d.isDiagonal()?delayDiagonal:type.delay;
		activate(new MovementSignal(robot,robot.getLocation().add(d),
			d==robot.getDirection(),delay),delay);
	}

	public void setDirection(Direction d) throws GameActionException {
		assertValidDirection(d);
		assertInactive();
		activate(new SetDirectionSignal(robot,d),1);
	}

	public boolean canMove(Direction d) {
		assertValidDirection(d);
		return gameWorld.canMove(robot,d);
	}

	public void assertCanMove(Direction d) throws GameActionException {
		if(type==ComponentType.BUILDING_MOTOR)
			throw new IllegalStateException("Buildings cannot move.");
		if(!gameWorld.canMove(robot,d))
			throw new GameActionException(GameActionExceptionType.CANT_MOVE_THERE, "Cannot move in the given direction: " + d);
	}

	public void assertValidDirection(Direction d) {
		assertNotNull(d);
		if(d==Direction.NONE||d==Direction.OMNI)
			throw new IllegalArgumentException("You cannot move in the direction NONE or OMNI.");
	}

}
