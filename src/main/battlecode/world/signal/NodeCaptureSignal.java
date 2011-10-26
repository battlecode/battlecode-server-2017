package battlecode.world.signal;

import battlecode.engine.signal.Signal;

import battlecode.common.Team;
import battlecode.world.InternalRobot;

public class NodeCaptureSignal extends Signal {

	public final int robotID;
	public final Team newTeam;

	public NodeCaptureSignal(InternalRobot robot, Team t) {
		robotID = robot.getID();
		newTeam = t;
	}

}
