package battlecode.world;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
import battlecode.common.RobotLevel;
import battlecode.common.Team;

public class InternalMine extends InternalObject implements Mine
{
	private int roundsLeft = GameConstants.MINE_ROUNDS;

	public InternalMine(GameWorld gw, MapLocation loc) {
		super(gw,loc,RobotLevel.MINE,Team.NEUTRAL);	
	}

	public int getRoundsLeft() {
		return roundsLeft;
	}

	public double mine() {
		if(roundsLeft>0) {
			roundsLeft--;
			return GameConstants.MINE_RESOURCES;
		}
		else
			return GameConstants.MINE_DEPLETED_RESOURCES;
	}
}
