package battlecode.world;

import battlecode.common.*;

public class InternalWorker extends InternalRobot {

	private int numBlocks = 0;
	
	public InternalWorker(GameWorld gw, RobotType type, MapLocation loc, Team t, boolean wakeDelay) {
		super(gw, type, loc, t, wakeDelay);
	}
	
	public void addBlock() {
		if (numBlocks < 0)
			numBlocks++;
	}
	
	public void removeBlock() {
		if (numBlocks > 0)
			numBlocks--;
	}
	
	public int getNumBlocks() {
		return numBlocks;
	}

}