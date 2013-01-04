package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

public class MinelayerSignal extends Signal {
	private static final long serialVersionUID = -5946471731326007868L;
	private final int robotID;
	private boolean isLaying;
	
	public MinelayerSignal(InternalRobot robot, boolean isLaying) {
		this.robotID = robot.getID();
		this.isLaying = isLaying;
	}
	
	public int getRobotID() { return robotID; }
	public boolean isLaying() { return isLaying;}
	public boolean isDefusing()  {return !isLaying; }

}
