package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

public class RegenSignal extends Signal {

	private static final long serialVersionUID = 8581954905914270494L;
	public final int robotID;

    public RegenSignal(InternalRobot r) {
        robotID = r.getID();
    }

}
