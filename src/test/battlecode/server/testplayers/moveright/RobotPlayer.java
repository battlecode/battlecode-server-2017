// Javac will move this into the correct package in the build output
package battlecode.server.testplayers.moveright;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

@SuppressWarnings("unused")
public class RobotPlayer {
    public static void run(RobotController rc) throws GameActionException {
        if(rc.isCoreReady() && rc.canMove(Direction.getEast())){
            rc.move(Direction.getEast());
        }
    }
}
