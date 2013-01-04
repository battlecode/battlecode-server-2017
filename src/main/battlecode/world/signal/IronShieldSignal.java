package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

public class IronShieldSignal extends Signal {

    public final int robotID;

    public IronShieldSignal(InternalRobot robot) {
        robotID = robot.getID();
    }

}
