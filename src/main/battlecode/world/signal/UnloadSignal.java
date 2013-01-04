package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

public class UnloadSignal extends Signal {

    public final int transportID;
    public final int passengerID;
    public final MapLocation unloadLoc;

    public UnloadSignal(InternalRobot transport, InternalRobot passenger, MapLocation loc) {
        transportID = transport.getID();
        passengerID = passenger.getID();
        unloadLoc = loc;
    }

}
