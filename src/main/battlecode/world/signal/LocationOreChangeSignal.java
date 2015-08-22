package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.engine.signal.Signal;

public class LocationOreChangeSignal extends Signal {

    private static final long serialVersionUID = -4229507739170593123L;

    public final MapLocation loc;
    public final double ore;

    public LocationOreChangeSignal(MapLocation loc, double ore) {
        this.loc = loc;
        this.ore = ore;
    }

    public MapLocation getLocation() {
        return loc;
    }

    public double getOre() {
        return ore;
    }
}
