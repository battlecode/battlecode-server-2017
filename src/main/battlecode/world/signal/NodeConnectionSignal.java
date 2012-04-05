package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.engine.signal.Signal;

public class NodeConnectionSignal extends Signal {

    public final MapLocation[][] connections;

    public NodeConnectionSignal(MapLocation[][] connections) {
        this.connections = connections;
    }

}
