package battlecode.world;

import battlecode.common.BroadcastController;
import battlecode.common.GameActionException;
import battlecode.common.Message;
import battlecode.world.signal.BroadcastSignal;

public class Radio extends BaseComponent implements BroadcastController {

	public Radio(InternalComponent component, InternalRobot robot) {
		super(component,robot);
	}

	public void broadcast(Message m) throws GameActionException {
		assertEquipped();
		assertInactive();
		assertNotNull(m);
		component.activate();
		robot.addAction(new BroadcastSignal(robot,type().range,m));
	}

}
