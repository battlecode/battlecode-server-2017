package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.world.InternalMine;
import battlecode.engine.signal.Signal;

public class MineBirthSignal extends Signal
{

	public final int id;
	public final MapLocation location;

	public MineBirthSignal(InternalMine mine) {
		id = mine.getID();
		location = mine.getLocation();
	}

}
