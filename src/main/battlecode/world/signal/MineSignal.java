package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.common.Team;
import battlecode.engine.signal.Signal;

public class MineSignal extends Signal {
	
	public static boolean ADD = true;
	public static boolean REMOVE = false;
	
	
	private static final long serialVersionUID = -4303228382203951149L;
	
    private final MapLocation mineLoc;
    private final Team mineTeam;
    private final boolean birth;

    public MineSignal(MapLocation mineLoc, Team mineTeam, boolean birth) {
    	this.mineLoc = mineLoc;
    	this.mineTeam = mineTeam;
    	this.birth = birth;
    }
    
    /** Location being mined */
    public MapLocation getMineLoc() { return mineLoc; }
    
    /** Mine owner */
    public Team getMineTeam() { return mineTeam; }
    
    public boolean shouldAdd() { return birth; }
    public boolean shouldRemove() { return !birth; }
    
}
