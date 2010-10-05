package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.common.MapLocation;

public class MapOriginSignal extends Signal {

	private static final long serialVersionUID = 4810422629914561889L;
	
	private final MapLocation origin;
	
	public MapOriginSignal(MapLocation origin) {
		this.origin = origin;
	}
	
	public MapLocation getMapOrigin() {
		return origin;
	}

}
