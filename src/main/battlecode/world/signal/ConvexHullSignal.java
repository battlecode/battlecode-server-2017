package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.common.MapLocation;
import battlecode.common.Team;

public class ConvexHullSignal extends Signal {

	private static final long serialVersionUID = -8277636175201362169L;
	
	private final Team team;
	private final MapLocation [][] hulls;
		
	public ConvexHullSignal(Team team, MapLocation [][] hulls) {
		this.team = team;
		this.hulls = hulls;
	}

	public MapLocation [][] getConvexHulls() {
		return hulls;
	}

	public Team getTeam() {
		return team;
	}
}
