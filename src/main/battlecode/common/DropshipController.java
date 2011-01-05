package battlecode.common;

public interface DropshipController extends ComponentController {

    /**
     * Loads the ground robot at {@code loc}.
     *
     * @cooldown
     */
    public void load(MapLocation loc) throws GameActionException;

    /**
     * Unloads the robot {@code robot} at the location {@code loc}.
     *
     * @cooldown
     */
    public void unload(Robot robot, MapLocation loc) throws GameActionException;

    public boolean canUnload(MapLocation loc);

	/**
	 * Returns the robots that aren currently loaded into the dropship.
	 */
    public Robot[] robotsOnBoard();

	/**
	 * Returns the amount of space available in the dropship.
	 */
    public int spaceAvailable();
}
