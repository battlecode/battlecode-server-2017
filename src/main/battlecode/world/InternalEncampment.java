package battlecode.world;

import battlecode.common.Encampment;
import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;
import battlecode.common.Team;

public class InternalEncampment extends InternalObject implements Encampment {
	 
	public InternalEncampment(GameWorld gw, MapLocation loc) {
	    super(gw, loc, RobotLevel.ENCAMPMENT_LEVEL, Team.NEUTRAL);
	}

}

