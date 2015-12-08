package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.engine.signal.Signal;

/**
 * Signifies a change in parts on a tile.
 *
 * @author Matt
 */
public class PartsChangeSignal implements Signal {

    private static final long serialVersionUID = -6853692134787044985L;

    /**
     * The location of parts change.
     */
    private final MapLocation loc;

    /**
     * The new amount of parts.
     */
    private final double amount;

    public PartsChangeSignal(MapLocation loc, double amount) {
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
    private PartsChangeSignal() {
        this(null, 0);
    }
}
