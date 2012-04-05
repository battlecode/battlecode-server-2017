package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

public class RegenSignal extends Signal {

    public final int robotID;

    public RegenSignal(InternalRobot r) {
        robotID = r.getID();
    }

}
