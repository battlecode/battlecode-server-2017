package battlecode.world.signal;

import battlecode.world.InternalRobot;
import battlecode.world.InternalComponent;
import battlecode.engine.signal.Signal;

public class EquipSignal extends Signal {

	public final int robotID;
	public final int componentID;
	
	public EquipSignal(InternalRobot robot, InternalComponent component) {
		robotID = robot.getID();
		componentID = component.getID();
	}
}
