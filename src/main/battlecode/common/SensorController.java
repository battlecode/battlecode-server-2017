package battlecode.common;

public interface SensorController extends ComponentController {

	public Robot senseRobotAtLocation(MapLocation loc, RobotLevel height) throws GameActionException;
	public Robot [] senseNearbyRobots();
	public RobotInfo senseRobotInfo(Robot r) throws GameActionException;
	//public MapLocation senseLocationOf(GameObject o) throws GameActionException;
	public boolean canSenseObject(GameObject o) throws GameActionException;
	//public Component [] senseComponentsAtLocation(MapLocation loc) throws GameActionException;
	/** Senses nearby components that do not belong to any robot. To sense
	 * components of a robot, use {@link #senseRobotInfo}.*/
	//public Component [] senseNearbyComponents();

}
