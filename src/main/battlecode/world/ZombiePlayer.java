package ZombiePlayer;
// This is intentionally not put in a Battlecode package, because our
// instrumenter explicitly disallows player classes to be part of a Battlecode
// package.

import battlecode.common.*;

import java.util.LinkedList;
import java.util.Queue;

// TODO: clean up this file a lot... could use some helper methods
public class ZombiePlayer {
    public static void run(RobotController rc) throws GameActionException{
        Direction[] dirs = { Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };
        Queue<RobotType> zombies = new LinkedList<>();
        while (true) {
            switch (rc.getType()) {
                case ZOMBIEDEN:
                    ZombieCount[] zSchedule = rc.getZombieSpawnSchedule(rc.getRoundNum());
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
                        if (!spawned) {
                            rc.spawnFail();
                            Clock.yield();
                        }
                        else Clock.yield(); // this is silly but the engine doesn't support spawning multiple things in one round
                    }

                    Clock.yield();
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
                    if (rc.isWeaponReady() && closestRobot != null && rc
                            .canAttackLocation
                            (closestRobot.location)) {
                        rc.attackLocation(closestRobot.location);
                        Clock.yield();
                    }
                    
                    // Else, try to move closer
                    else if (rc.isCoreReady()) {
                        Direction preferredDirection = dirs[(int) (Math.random() * 8)];
                        if (closestRobot != null) {
                            preferredDirection = myLoc.directionTo(closestRobot.location);
                            // First, try to move if best direction toward target
                            if (rc.canMove(preferredDirection)) {
                                rc.move(preferredDirection);
                                Clock.yield();
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

                        // Try to move in the new rotated direction
                        if(rc.canMove(newDirection)) {
                            rc.move(newDirection);
                            Clock.yield();
                            break;
                        }

                        // That didn't work, so try the other direction
                        Direction otherDirection;
                        if (randomDirection == 0) {
                            otherDirection = preferredDirection.rotateRight();
                        } else {
                            otherDirection = preferredDirection.rotateLeft();
                        }

                        // Try to move in the other rotated direction
                        if (rc.canMove(otherDirection)) {
                            rc.move(otherDirection);
                            Clock.yield();
                            break;
                        }

                        // Try to clear rubble instead
                        MapLocation target1 = rc.getLocation().add
                                (preferredDirection);
                        if (!rc.isLocationOccupied(target1) && rc.onTheMap
                                (target1) && rc.senseRubble
                                (target1) >= GameConstants
                                .RUBBLE_OBSTRUCTION_THRESH) {
                            rc.clearRubble(preferredDirection);
                            Clock.yield();
                            break;
                        }

                        MapLocation target2 = rc.getLocation().add
                                (newDirection);
                        if (!rc.isLocationOccupied(target2) && rc.onTheMap
                                (target1) && rc
                                .senseRubble
                                (target2) >= GameConstants
                                .RUBBLE_OBSTRUCTION_THRESH) {
                            rc.clearRubble(newDirection);
                            Clock.yield();
                            break;
                        }

                        MapLocation target3 = rc.getLocation().add
                                (otherDirection);
                        if (!rc.isLocationOccupied(target3) && rc.onTheMap
                                (target3) & rc
                                .senseRubble
                                (target3) >= GameConstants
                                .RUBBLE_OBSTRUCTION_THRESH) {
                            rc.clearRubble(otherDirection);
                            Clock.yield();
                            break;
                        }

                        Clock.yield();  // We couldn't do anything, so just yield
                    }

                    break;
            }
        }
    }
}
