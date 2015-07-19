package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.common.Team;
import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IndicatorDotSignal extends Signal {

    @JsonProperty("type")
    private String getTypeForJson() { return "IndicatorDot"; }

    public final int robotID;
    public final Team team;
    public final MapLocation location;
    public final int red, green, blue;

    public IndicatorDotSignal(InternalRobot robot, MapLocation loc, int r, int g, int b) {
        robotID = robot.getID();
        team = robot.getTeam();
        location = loc;
        red = r;
        green = g;
        blue = b;
    }
}
