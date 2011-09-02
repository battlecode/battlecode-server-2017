package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.world.InternalPowerNode;
import battlecode.engine.signal.Signal;

public class NodeBirthSignal extends Signal
{

	public final int id;
	public final MapLocation location;

	public NodeBirthSignal(InternalPowerNode node) {
		id = node.getID();
		location = node.getLocation();
	}

}
