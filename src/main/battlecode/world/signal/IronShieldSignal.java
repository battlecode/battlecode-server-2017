package battlecode.world.signal;

import battlecode.world.InternalRobot;
import battlecode.engine.signal.Signal;

public class IronShieldSignal extends Signal {

	public final int robotID;

	public IronShieldSignal(InternalRobot robot) {
		robotID = robot.getID();
	}

}
