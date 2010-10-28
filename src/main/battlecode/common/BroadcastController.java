package battlecode.common;

public interface BroadcastController extends ComponentController {

	public void broadcast(Message m) throws GameActionException;

}
