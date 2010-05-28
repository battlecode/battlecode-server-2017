package battlecode.world.signal;

import battlecode.common.MapLocation;

public class MineFluxSignal extends Signal {

	private static final long serialVersionUID = -2121996970526116211L;
	
	private final MapLocation[] locs;
	
	public MineFluxSignal(MapLocation[] locs) {
		this.locs = locs;
	}
	
	public MapLocation[] getLocs() {
		return locs;
	}

}
