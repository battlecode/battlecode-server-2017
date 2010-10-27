package battlecode.world;

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

	public Motor(InternalComponent component, InternalRobot robot) {
		super(component,robot);
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
		int delay = d.isDiagonal()?delayDiagonal:type().delay;
		component.activate(delay);
		robot.addAction(new MovementSignal(robot,robot.getLocation().add(d),
			d==robot.getDirection(),delay));
	}

	public void setDirection(Direction d) throws GameActionException {
		assertNotNull(d);
		assertInactive();
		component.activate(1);
		robot.addAction(new SetDirectionSignal(robot,d));
	}

	public boolean canMove(Direction d) {
		assertNotNull(d);
		return gameWorld.canMove(robot,d);
	}

	public void assertCanMove(Direction d) throws GameActionException {
		if(!gameWorld.canMove(robot,d))
			throw new GameActionException(GameActionExceptionType.CANT_MOVE_THERE, "Cannot move in the given direction: " + d);
	}

}
