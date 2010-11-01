package battlecode.world;

import battlecode.common.BroadcastController;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.Message;
import battlecode.world.signal.BroadcastSignal;

public class Radio extends BaseComponent implements BroadcastController {

	public Radio(ComponentType type, InternalRobot robot) {
		super(type,robot);
	}

	public void broadcast(Message m) throws GameActionException {
		assertInactive();
		assertNotNull(m);
		activate();
		robot.addAction(new BroadcastSignal(robot,type().range,m));
	}

}
