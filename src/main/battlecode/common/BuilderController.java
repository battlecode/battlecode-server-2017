package battlecode.common;

public interface BuilderController extends ComponentController {

	public void build(ComponentType type) throws GameActionException;
	public void build(Chassis type) throws GameActionException;

}
