package battlecode.common;

public interface DropshipController extends ComponentController {

	/** Loads the ground robot at {@code loc}. */
	public void load(MapLocation loc) throws GameActionException;
	/** Unloads the robot {@code robot} at the location {@code loc}. */
	public void unload(Robot robot, MapLocation loc) throws GameActionException;
	public boolean canUnload(MapLocation loc);
	/** @methodcost */
	public Robot [] robotsOnBoard();
	public int spaceAvailable();

}
