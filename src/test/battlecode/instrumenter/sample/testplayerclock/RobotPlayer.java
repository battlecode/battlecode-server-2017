package testplayerclock;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

/**
 * @author james
 */
public class RobotPlayer {
    public static void run(RobotController rc) throws GameActionException {
        rc.broadcast(0, 1);
        Clock.yield();
        rc.broadcast(0, 2);
        Clock.yield();
        rc.broadcast(0, 3);
    }
}
