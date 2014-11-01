package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.common.MapLocation;

public class LocationOreChangeSignal extends Signal {

    private static final long serialVersionUID = -4229507739170593123L;

    public final MapLocation loc;
    public final int ore;

    public LocationOreChangeSignal(MapLocation loc, int ore) {
        this.loc = loc;
        this.ore = ore;
    }

    public MapLocation getLocation() {
        return loc;
    }

    public int getOre() {
        return ore;
    }
}
