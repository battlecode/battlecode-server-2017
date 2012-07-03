package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

public class UnloadFluxSignal extends Signal {

    public final int transporterID;
    public final MapLocation unloadLoc;

    public UnloadFluxSignal(InternalRobot transporter, MapLocation loc) {
        transporterID = transporter.getID();
        unloadLoc = loc;
    }

}
