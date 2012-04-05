package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.engine.signal.Signal;

public class NodeBirthSignal extends Signal {

    public final MapLocation location;

    public NodeBirthSignal(MapLocation loc) {
        location = loc;
    }

}
