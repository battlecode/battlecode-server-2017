package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.common.MapLocation;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LocationOreChangeSignal extends Signal {

    @JsonProperty("type")
    private String getTypeForJson() { return "LocationOreChange"; }

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
