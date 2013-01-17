package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

public class MinelayerSignal extends Signal {
	private static final long serialVersionUID = -5946471731326007868L;
	private final int robotID;
	private final boolean isLaying;
	private final MineAction action;
	private final MapLocation targetLoc;
	
	public enum MineAction {
		LAYING,
		LAYINGSTOP,
		DEFUSING,
		
	}
	
	public MinelayerSignal(InternalRobot robot, MineAction action, MapLocation target) {
		this.robotID = robot.getID();
		this.action = action;
		this.isLaying = action == MineAction.LAYING;
		this.targetLoc = target;
	}
	
	public int getRobotID() { return robotID; }
	public boolean isLaying() { return isLaying || action==MineAction.LAYING;}
	public boolean isDefusing()  {return !isLaying || action==MineAction.DEFUSING; }
	public boolean isStopping() { return !isLaying || action==MineAction.LAYINGSTOP; }
	public MapLocation getTarget() { return targetLoc; }

}
