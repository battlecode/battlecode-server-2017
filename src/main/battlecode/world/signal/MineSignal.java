package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.engine.signal.Signal;

public class MineSignal extends Signal {
	
	
	private static final long serialVersionUID = -4303228382203951149L;
	
    private final MapLocation mineLoc;
    private final Team mineTeam;
    private final RobotType minerType;

    public MineSignal(MapLocation mineLoc, Team mineTeam, RobotType minerType) {
    	this.mineLoc = mineLoc;
    	this.mineTeam = mineTeam;
        this.minerType = minerType;
    }
    
    /** Location being mined */
    public MapLocation getMineLoc() { return mineLoc; }
    
    /** Mine owner */
    public Team getMineTeam() { return mineTeam; }

    /** Type of miner */
    public RobotType getMinerType() { return minerType; }
}
