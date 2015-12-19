package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.common.Team;

/**
 * Signifies that a robot has requested a debug indicator line be created.
 */
public class IndicatorLineSignal implements Signal {

    public final int robotID;
    public final Team team;
    public final MapLocation loc1, loc2;
    public final int red, green, blue;

    public IndicatorLineSignal(int robotID, Team team, MapLocation loc1, MapLocation loc2,
                               int red, int green, int blue) {
        this.robotID = robotID;
        this.team = team;
        this.loc1 = loc1;
        this.loc2 = loc2;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private IndicatorLineSignal() {
        this(0, null, null, null, 0, 0, 0);
    }
}
