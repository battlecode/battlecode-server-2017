package ZombiePlayer;
// This is intentionally not put in a Battlecode package, because our
// instrumenter explicitly disallows player classes to be part of a Battlecode
// package.

import battlecode.common.*;

import java.util.LinkedList;
import java.util.Queue;

public class ZombiePlayer {
    public static void run(RobotController rc) throws GameActionException{
        Direction[] dirs = { Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };
        Queue<RobotType> zombies = new LinkedList<>();
        while (true) {
            switch (rc.getType()) {
                case ZOMBIEDEN:
                    ZombieCount[] zSchedule = rc.getZombieSpawnSchedule(Clock.getRoundNum());
                    for (ZombieCount count : zSchedule) {
                        for (int i = 0; i < count.getCount(); ++i) {
                            zombies.add(count.getType());
                        }
                    }

                    while (!zombies.isEmpty()) {
                        boolean spawned = false;
                        RobotType next = zombies.peek();
                        int i = (int) (Math.random() * 8);

                        for (int j = 0; j < 8; ++j) {
                            if (rc.canBuild(dirs[(i + j) % 8], next)) {
                                rc.build(dirs[(i + j) % 8], next);
                                zombies.poll();
                                spawned = true;
                                break;
                            }
                        }
                        if (!spawned) break;
                        else rc.yield(); // this is silly but the engine doesn't support spawning multiple things in one round
                    }

                    rc.yield();
                    break;
                case STANDARDZOMBIE:
                case RANGEDZOMBIE:
                case FASTZOMBIE:
                case BIGZOMBIE:
                default:
                    RobotInfo[] nearbyRobots = rc.senseNearbyRobots(10000);
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
                        Direction preferredDirection = dirs[(int) (Math.random() * 8)];
                        if (closestRobot != null) {
                            preferredDirection = myLoc.directionTo(closestRobot.location);
                            // First, try to move if best direction toward target
                            if(rc.canMove(preferredDirection)) {
                                rc.move(preferredDirection);
                                rc.yield();
                                break;
                            }
                        }
                        
                        // If that was obstructed, randomly try either left or right 45 degress
                        int randomDirection = (int) (Math.random() * 2);
                        Direction newDirection;
                        if(randomDirection == 0){ // If zero, try rotating left first
                            newDirection = preferredDirection.rotateLeft();
                        } else {
                            newDirection = preferredDirection.rotateRight();
                        }
                        
                        if(rc.canMove(preferredDirection)) { // Try to move in the new rotated direction
                            rc.move(preferredDirection);
                            rc.yield();
                            break;
                        }
                        
                        if(randomDirection == 0){ // That didn't work, so try the other direction
                            newDirection = preferredDirection.rotateRight();
                        } else {
                            newDirection = preferredDirection.rotateLeft();
                        }
                        
                        if(rc.canMove(preferredDirection)) { // Try to move in the other rotated direction
                            rc.move(preferredDirection);
                            rc.yield();
                            break;
                        }
                        
                        // TODO: Try to clear rubble from original direction
                        
                        rc.yield();  // We couldn't do anything, so just yield
                    }

                    break;
            }
        }
    }
}
