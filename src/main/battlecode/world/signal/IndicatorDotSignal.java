package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.common.Team;

/**
 * Signifies that a robot has requested a debug indicator dot be created.
 */
public class IndicatorDotSignal implements InternalSignal {

    public final int robotID;
    public final Team team;
    public final MapLocation location;
    public final int red, green, blue;

    public IndicatorDotSignal(int robotID, Team team, MapLocation location, int red, int green, int blue) {
        this.robotID = robotID;
        this.team = team;
        this.location = location;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private IndicatorDotSignal() {
        this(0, null, null, 0, 0, 0);
    }
}
