package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.engine.signal.Signal;

/**
 * Signifies a change in rubble on a tile.
 *
 * @author Matt
 */
public class RubbleChangeSignal implements Signal {

    private static final long serialVersionUID = -6853620834724044985L;

    /**
     * The location of rubble change.
     */
    private final MapLocation loc;

    /**
     * The new amount of rubble.
     */
    private final double amount;

    public RubbleChangeSignal(MapLocation loc, double amount) {
        this.loc = loc;
        this.amount = amount;
    }

    public MapLocation getLoc() {
        return loc;
    }

    public double getAmount() {
        return amount;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private RubbleChangeSignal() {
        this(null, 0);
    }
}
