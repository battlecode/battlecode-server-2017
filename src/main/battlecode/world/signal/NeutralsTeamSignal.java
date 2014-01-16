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
        int[][] ids = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                ids[i][j] = Integer.MAX_VALUE;
            }
        }

        for (InternalObject obj : objs) {
            InternalRobot ir = (InternalRobot) obj;
            if (ir.type == RobotType.PASTR) {
                MapLocation[] affected = MapLocation.getAllMapLocationsWithinRadiusSq(ir.getLocation(), GameConstants.PASTR_RANGE);
                for (MapLocation ml : affected) {
                    if (ml.x >= 0 && ml.x < width && ml.y >= 0 && ml.y < height) {
                        ids[ml.x][ml.y] = Math.min(ids[ml.x][ml.y], ir.getID());
                    }
                }
            }
        }

        for (InternalObject obj : objs) {
            InternalRobot ir = (InternalRobot) obj;
            if (ir.type != RobotType.PASTR && ir.type != RobotType.SOLDIER) {
                continue;
            }
            if (ir.type == RobotType.SOLDIER && ids[ir.getLocation().x][ir.getLocation().y] < Integer.MAX_VALUE) {
                continue;
            }
            int captureRange = 0;
            if (ir.type == RobotType.PASTR) {
                captureRange = GameConstants.PASTR_RANGE;
            }
            MapLocation[] affected = MapLocation.getAllMapLocationsWithinRadiusSq(ir.getLocation(), captureRange);
            for (MapLocation ml : affected) {
                if (ml.x >= 0 && ml.x < width && ml.y >= 0 && ml.y < height && (ir.type == RobotType.SOLDIER || ir.type == RobotType.PASTR && ir.getID() == ids[ml.x][ml.y])) {
                    teams[ml.x][ml.y] = (1 << ir.getTeam().ordinal());
                }
            }
        }
    }

    public int[][] getTeams() {
        return teams;
    }
}
