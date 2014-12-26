package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.common.Team;
import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

public class IndicatorLineSignal extends Signal {

    public final int robotID;
    public final Team team;
    public final MapLocation loc1, loc2;
    public final int red, green, blue;

    public IndicatorLineSignal(InternalRobot robot, MapLocation l1, MapLocation l2, int r, int g, int b) {
        robotID = robot.getID();
        team = robot.getTeam();
        loc1 = l1;
        loc2 = l2;
        red = r;
        green = g;
        blue = b;
    }
}
