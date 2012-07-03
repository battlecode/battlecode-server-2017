package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

public class LoadFluxSignal extends Signal {
    
    public final int transporterID;
    public final MapLocation loadLoc;
    
    public LoadFluxSignal(InternalRobot transporter, MapLocation loc) {
        transporterID = transporter.getID();
        loadLoc = loc;
    }
    
}
