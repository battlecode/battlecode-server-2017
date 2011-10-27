package battlecode.world;

import java.util.ArrayList;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.PowerNode;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.world.signal.NodeCaptureSignal;

public class InternalPowerNode extends InternalRobot implements PowerNode {
	//capture is 0 to default, Team A increases, Team B decreases. It'll decrease overtime while
	//it is neutral. Once |capture| > 10, it switches team.
	private int capture = 0;
	
	private boolean [] connected = new boolean [3];
		
	public InternalPowerNode(GameWorld gw, MapLocation loc, Team team) {
        super(gw, RobotType.POWER_NODE, loc, team, false);
    }

	public void setConnected(Team t, boolean b) {
		connected[t.ordinal()]=b;
	}

	public boolean connected(Team t) {
		return connected[t.ordinal()];
	}

	public void setTeam(Team t) {
		Team oldTeam = getTeam();
		super.setTeam(t);
		myGameWorld.teamChanged(this,oldTeam,t);
		regen = false;
		if(t==Team.NEUTRAL)
			capture = 0;
	}

	public void processLethalDamage() {
		setTeam(Team.NEUTRAL);
		myEnergonLevel = type.maxEnergon;
	}

	public boolean isNeutral() {
		return getTeam() == Team.NEUTRAL;
	}

	public void takeDamage(double amt, InternalRobot source) {
		if(connected(source.getTeam()))
			super.takeDamage(amt,source);
	}

	public void processBeginningOfRound() {
		super.processBeginningOfRound();
		if(isNeutral()) {
			InternalRobot ir = (InternalRobot)myGameWorld.getObject(getLocation(),RobotLevel.ON_GROUND);
			if(ir!=null)
				capture(ir.getTeam());
		}
		else {
			if(regen) {
				changeEnergonLevel(GameConstants.REGEN_AMOUNT);
			}
			if(!connected(getTeam()))
				takeDamage(GameConstants.DISCONNECTED_NODE_DAMAGE);
		}
		regen = false;
	}

	public int loyalty() {
		return capture;
	}

	public void setRegen() {
		if(!myGameWorld.timeLimitReached())
			super.setRegen();
	}

	public MapLocation [] neighbors() {
		return myGameWorld.getAdjacentNodes(getLocation()).toArray(new MapLocation [0]);
	}
	
	public void capture(Team captureTeam)
	{
		if(this.getTeam() == Team.NEUTRAL)
		{
			if(captureTeam == Team.A && connected(Team.A))
				capture++;
			else if(captureTeam == Team.B && connected(Team.B))
				capture--;
			
			if(capture <= GameConstants.NODE_CAPTURE_LIMIT * -1)
			{
				this.setTeam(Team.B);
			}
			else if(capture >= GameConstants.NODE_CAPTURE_LIMIT)
			{
				this.setTeam(Team.A);
			}
		}
	}
}
