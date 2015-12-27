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
                        if (!spawned) break;
                        else Clock.yield(); // this is silly but the engine doesn't support spawning multiple things in one round
                    }

                    Clock.yield();
                    break;
                case STANDARDZOMBIE:
                case RANGEDZOMBIE:
                case FASTZOMBIE:
                case BIGZOMBIE:
                default:

            }
        }
    }
}
