package battlecode.common;

public interface DropshipController extends ComponentController {

    /**
     * Loads the robot at {@code loc} and height {@code height}.
     *
     * @cooldown
     */
    public void load(MapLocation loc, RobotLevel height) throws GameActionException;
    
	/**
	 * @deprecated This function exists only for compatibility with
	 * versions 1.0.5 and earlier.
	 */
	@Deprecated
	public void load(MapLocation loc) throws GameActionException;

    /**
     * Unloads the robot {@code robot} at the location {@code loc}.
     *
     * @cooldown
     */
    public void unload(Robot robot, MapLocation loc) throws GameActionException;
   
	/**
	 * Returns true if {@code loc} is within this drophip's range and there is
	 * no robot at height {@code height}
	 */
    public boolean canUnload(MapLocation loc, RobotLevel height);
	
	/**
	 * @deprecated This function exists only for compatibility with
	 * versions 1.0.5 and earlier.
	 */
	@Deprecated
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
