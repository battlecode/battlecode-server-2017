package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

public class TurnOffSignal extends Signal {

    public final int robotID;
    public final boolean voluntary;

    public TurnOffSignal(InternalRobot r, boolean voluntary) {
        this.robotID = r.getID();
        this.voluntary = voluntary;
    }

}
