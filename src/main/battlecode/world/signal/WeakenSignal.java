package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

public class WeakenSignal extends Signal {

    public final int robotID;

    public WeakenSignal(InternalRobot r) {
        this.robotID = r.getID();
    }

}
