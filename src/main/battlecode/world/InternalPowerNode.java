package battlecode.world;

import java.util.ArrayList;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.PowerNode;
import battlecode.common.RobotLevel;
import battlecode.common.Team;

public class InternalPowerNode extends InternalObject implements PowerNode {
	//capture is 0 to default, Team A increases, Team B decreases. It'll decrease overtime while
	//it is neutral. Once |capture| > 10, it switches team.
	private double capture = 0;
	//If not neutral, any attacks made to this decrease the HP over time. Once it is below 0, it becomes
	//neutral.
	private double health = GameConstants.MAX_NODE_HEALTH;
	
	//States: 0 (Neutral/No Connect), 1 (Team A Connected), 2 (Team B Connected), 3 (Team A&B Connected)
	//Maybe have different glow around it depending on connection
	public short state = 0;
		
	public InternalPowerNode(GameWorld gw, MapLocation loc, Team team) {
        super(gw, loc, RobotLevel.MINE, team);
    }
	
	public boolean connected(Team t)
	{
		ArrayList<MapLocation> adj = this.getGameWorld().getAdjacentNodes(getLocation());
		for(MapLocation ml : adj)
		{
			InternalPowerNode pn = this.getGameWorld().getObjectOfType(ml, RobotLevel.MINE, InternalPowerNode.class);
			if(pn.getTeam() == t)
				return true;
		}
		return false;
	}

	public MapLocation [] neighbors() {
		return myGameWorld.getAdjacentNodes(getLocation()).toArray(new MapLocation [0]);
	}
	
	public void takeDamage(double baseDamage)
	{
		double damage = baseDamage;
		if(this.getTeam() != Team.NEUTRAL)
			health -= damage;
		else
			health = GameConstants.MAX_NODE_HEALTH;
		
		if(health <= 0.0)
		{
			this.setTeam(Team.NEUTRAL);
			health = GameConstants.MAX_NODE_HEALTH;
			capture = 0;
		}
	}
	
	public boolean canCapture(Team captureTeam)
	{
		return connected(captureTeam);
	}
	
	public void capture(Team captureTeam)
	{
		if(this.getTeam() == Team.NEUTRAL)
		{
			if(captureTeam == Team.A && canCapture(Team.A))
				capture++;
			else if(captureTeam == Team.B && canCapture(Team.B))
				capture--;
			
			if(capture < GameConstants.NODE_CAPTURE_LIMIT * -1)
			{
				this.setTeam(Team.A);
				capture = 0;
				health = GameConstants.MAX_NODE_HEALTH;
			}
			else if(capture > GameConstants.NODE_CAPTURE_LIMIT)
			{
				this.setTeam(Team.B);
				capture = 0;
				health = GameConstants.MAX_NODE_HEALTH;
			}
		}
	}
}
