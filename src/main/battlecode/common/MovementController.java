package battlecode.common;

public interface MovementController extends ComponentController {

	public void moveForward() throws GameActionException;
	public void moveBackward() throws GameActionException;
	public void setDirection(Direction d) throws GameActionException;
	public boolean canMove(Direction d);

}
