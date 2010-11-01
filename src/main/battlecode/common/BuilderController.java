package battlecode.common;

public interface BuilderController extends ComponentController {

	public void build(ComponentType type, MapLocation loc, RobotLevel level) throws GameActionException;
	public void build(Chassis type, MapLocation loc) throws GameActionException;

}
