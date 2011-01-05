package battlecode.common;

public interface BugController extends SensorController, WeaponController {

    /**
	 * Returns the location of the bugged robot, or null no robot is bugged.
	 */
	public MapLocation buggedLocation();
}
