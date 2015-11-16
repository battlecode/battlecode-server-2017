package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.engine.signal.Signal;

/**
 * Signifies that a robot just bashed.
 *
 * @author adamd
 */
public class BashSignal implements Signal {
    private static final long serialVersionUID = 8064711239305833273L;

    /**
     * The ID of the robot that bashed.
     */
    public final int robotID;

    /**
     * The location that the robot bashed.
     */
    public final MapLocation targetLoc;

    /**
     * Creates a signal for a robot bash.
     *
     * @param robotID   the id of the robot that attacked
     * @param targetLoc the location that the robot attacked
     */
    public BashSignal(int robotID, MapLocation targetLoc) {
        this.robotID = robotID;
        this.targetLoc = targetLoc;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private BashSignal() {
        this(0, null);
    }
}
