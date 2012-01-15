package battlecode.world;

import java.util.ArrayList;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.PowerNode;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.world.signal.NodeBirthSignal;

public class InternalPowerNode extends InternalObject implements PowerNode {
	
	private boolean [] connected = new boolean [2];
	private boolean isCore;
		
	public InternalPowerNode(GameWorld gw, MapLocation loc, boolean core) {
        super(gw, loc, RobotLevel.POWER_NODE, Team.NEUTRAL);
		isCore=core;
		gw.addSignal(new NodeBirthSignal(loc));
    }

	public void setConnected(Team t, boolean b) {
		connected[t.ordinal()]=b;
	}

	public boolean connected(Team t) {
		return connected[t.ordinal()];
	}

	public boolean isPowerCore() {
		return isCore;
	}

	public Team powerCoreTeam() {
		if(isPowerCore())
			return getControllingTeam();
		else
			return null;
	}

	public MapLocation [] neighbors() {
		return myGameWorld.getAdjacentNodes(getLocation()).toArray(new MapLocation [0]);
	}

	public Team getControllingTeam() {
		InternalRobot tower = myGameWorld.nodeToTower(this);
		if(tower==null)
			return Team.NEUTRAL;
		else
			return tower.getTeam();
	}

}
