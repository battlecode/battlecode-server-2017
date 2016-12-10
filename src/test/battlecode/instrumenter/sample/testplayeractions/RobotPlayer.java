package testplayeractions;

import battlecode.common.*;

/**
 * A RobotPlayer for testing that uses all of the methods in RobotController.
 *
 * @author james
 */
@SuppressWarnings("unused")
public class RobotPlayer {
    public static void run(RobotController rc) throws GameActionException {
        rc.resign();
        rc.senseNearbyRobots();
        rc.setTeamMemory(0, 0);

        System.out.println("I shouldn't overflow!");
    }
}
