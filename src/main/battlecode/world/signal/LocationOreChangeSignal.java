package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.engine.signal.Signal;

/**
 * Represents that the amoun
 */
public class LocationOreChangeSignal implements Signal {

    private static final long serialVersionUID = -4229507739170593123L;

    public final MapLocation loc;
    public final double ore;

    public LocationOreChangeSignal(MapLocation loc, double ore) {
        this.loc = loc;
        this.ore = ore;
    }

    public MapLocation getLoc() {
        return loc;
    }

    public double getOre() {
        return ore;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private LocationOreChangeSignal() {
        this(null, 0);
    }
}
