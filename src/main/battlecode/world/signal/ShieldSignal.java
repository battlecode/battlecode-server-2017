package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

public class ShieldSignal extends Signal {

	private static final long serialVersionUID = -6281189788931319768L;
	public final int robotID;

    public ShieldSignal(InternalRobot r) {
        robotID = r.getID();
    }

}
