package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.common.MapLocation;

public class LocationSupplyChangeSignal extends Signal {

    private static final long serialVersionUID = -4229507739170593123L;

    public final MapLocation loc;
    public final double supply;

    public LocationSupplyChangeSignal(MapLocation loc, double supply) {
        this.loc = loc;
        this.supply = supply;
    }

    public MapLocation getLocation() {
        return loc;
    }

    public double getSupply() {
        return supply;
    }
}
