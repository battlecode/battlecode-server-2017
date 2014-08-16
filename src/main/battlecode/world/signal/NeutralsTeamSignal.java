package battlecode.world.signal;

import java.util.Arrays;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.engine.signal.Signal;
import battlecode.world.InternalObject;
import battlecode.world.InternalRobot;

/**
 * TODO: describes which cow tiles are owned by which team
 *
 * @author axc
 */
public class NeutralsTeamSignal extends Signal {
    private final int[][] teams; // 0 = none, 1 = A, 2 = B, 3 = both
    public NeutralsTeamSignal(InternalObject[] objs, int width, int height) {
        this.teams = new int[width][height];
    }

    public int[][] getTeams() {
        return teams;
    }
}
