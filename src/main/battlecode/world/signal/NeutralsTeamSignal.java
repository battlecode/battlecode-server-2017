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
        for (InternalObject obj : objs) {
            InternalRobot ir = (InternalRobot) obj;
            if (ir.type != RobotType.PASTR && ir.type != RobotType.SOLDIER) {
                continue;
            }
            int captureRange = 0;
            if (ir.type == RobotType.PASTR) {
                captureRange = GameConstants.PASTR_RANGE;
            }
            MapLocation[] affected = MapLocation.getAllMapLocationsWithinRadiusSq(ir.getLocation(), captureRange);
            for (MapLocation ml : affected) {
                if (ml.x >= 0 && ml.x < width && ml.y >= 0 && ml.y < height) {
                    teams[ml.x][ml.y] |= (1 << ir.getTeam().ordinal());
                }
            }
        }
    }

    public int[][] getTeams() {
        return teams;
    }
}
