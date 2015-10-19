package ZombiePlayer;
// This is intentionally not put in a Battlecode package, because our
// instrumenter explicitly disallows player classes to be part of a Battlecode
// package.

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public class ZombiePlayer {
    public static void run(RobotController rc) throws GameActionException{
        switch (rc.getType()) {
        case ZOMBIEDEN:
            // TODO: Add zombie den behavior
            break;
        case STANDARDZOMBIE:
        case RANGEDZOMBIE:
        case FASTZOMBIE:
        case BIGZOMBIE:
        default:
            RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1);
            RobotInfo closestRobot = null;
            int closestDist = Integer.MAX_VALUE;
            MapLocation myLoc = rc.getLocation();

            for (RobotInfo robot : nearbyRobots) {
                if(!robot.team.isPlayer())      // Don't go for other Zombies
                    continue;
                int testDist = myLoc.distanceSquaredTo(robot.location);
                if (testDist < closestDist) {
                    closestRobot = robot;
                    closestDist = testDist;
                }
            }
            
            // If target is in range, attack it and end turn
            if (closestRobot != null && rc.canAttackLocation(closestRobot.location)) {
                rc.attackLocation(closestRobot.location);
                rc.yield();
            }
            
            // Else, try to move closer
            if(rc.isCoreReady()){
                Direction preferredDirection = myLoc.directionTo(closestRobot.location);
                // First, try to move if best direction toward target
                if(rc.canMove(preferredDirection)) {
                    rc.move(preferredDirection);
                    rc.yield();
                }
                
                
                // If that was obstructed, randomly try either left or right 45 degress
                int randomDirection = 0; // TODO: Replace with an actual random direction after figuring out how to access game's rng
                Direction newDirection;
                if(randomDirection == 0){ // If zero, try rotating left first
                    newDirection = preferredDirection.rotateLeft();
                } else {
                    newDirection = preferredDirection.rotateRight();
                }
                
                if(rc.canMove(preferredDirection)) { // Try to move in the new rotated direction
                    rc.move(preferredDirection);
                    rc.yield();
                }
                
                if(randomDirection == 0){ // That didn't work, so try the other direction
                    newDirection = preferredDirection.rotateRight();
                } else {
                    newDirection = preferredDirection.rotateLeft();
                }
                
                if(rc.canMove(preferredDirection)) { // Try to move in the other rotated direction
                    rc.move(preferredDirection);
                    rc.yield();
                }
                
                // TODO: Try to clear rubble from original direction
                
                rc.yield();  // We couldn't do anything, so just yield
            }

            break;
        }
    }
}
