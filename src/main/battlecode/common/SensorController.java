package battlecode.common;

public interface SensorController extends ComponentController {

	public GameObject senseObjectAtLocation(MapLocation loc, RobotLevel height) throws GameActionException;
	public <T> T [] senseNearbyGameObjects(Class<T> type);
	public MapLocation senseLocationOf(GameObject o) throws GameActionException;
	public RobotInfo senseRobotInfo(Robot r) throws GameActionException;
	public MineInfo senseMineInfo(Mine r) throws GameActionException;
	public boolean canSenseObject(GameObject o) throws GameActionException;
}
