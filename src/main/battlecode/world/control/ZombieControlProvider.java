package battlecode.world.control;

import battlecode.common.*;
import battlecode.server.ErrorReporter;
import battlecode.world.GameWorld;
import battlecode.world.InternalRobot;
import battlecode.common.ZombieSpawnSchedule;

import java.util.*;

/**
 * The control provider for zombies.
 * Doesn't use instrumentation or anything, just plain-old logic.
 *
 * @author james
 */
/**
 * @author nmccoy
 *
 */
public class ZombieControlProvider implements RobotControlProvider {

    /**
     * The directions a zombie cares about.
     */
    private static final Direction[] DIRECTIONS = {
            Direction.NORTH,
            Direction.NORTH_EAST,
            Direction.EAST,
            Direction.EAST,
            Direction.SOUTH_EAST,
            Direction.SOUTH,
            Direction.SOUTH_WEST,
            Direction.WEST,
            Direction.NORTH_WEST
    };

    /**
     * The types & order to spawn zombie robots in.
     */
    private static final RobotType[] ZOMBIE_TYPES = {
            RobotType.STANDARDZOMBIE,
            RobotType.RANGEDZOMBIE,
            RobotType.FASTZOMBIE,
            RobotType.BIGZOMBIE
    };

    /**
     * The world we're operating in.
     */
    private GameWorld world;

    /**
     * The queues of zombies to spawn for each den.
     */
    private final Map<Integer, Map<RobotType, Integer>> denQueues;

    /**
     * An rng based on the world seed.
     */
    private Random random;

    /**
     * Create a ZombieControlProvider.
     */
    public ZombieControlProvider() {
        this.denQueues = new HashMap<>();
    }

    @Override
    public void matchStarted(GameWorld world) {
        assert this.world == null;

        this.world = world;
        this.random = new Random(world.getMapSeed());
    }

    @Override
    public void matchEnded() {
        assert this.world != null;

        this.world = null;
        this.random = null;
        this.denQueues.clear();
    }

    @Override
    public void roundStarted() {}

    @Override
    public void roundEnded() {}

    @Override
    public void robotSpawned(InternalRobot robot) {
        if (robot.getType() == RobotType.ZOMBIEDEN) {
            // Create the spawn queue for this robot
            final Map<RobotType, Integer> spawnQueue = new HashMap<>();
            // Initialize all zombie types in the queue to 0
            for (RobotType type : ZOMBIE_TYPES) {
                spawnQueue.put(type, 0);
            }
            // Store it in denQueues
            denQueues.put(robot.getID(), spawnQueue);
        }
    }

    @Override
    public void robotKilled(InternalRobot robot) {}

    @Override
    public void runRobot(InternalRobot robot) {
        if (robot.getType() == RobotType.ZOMBIEDEN) {
            processZombieDen(robot);
        } else if (robot.getType().isZombie) {
            processZombie(robot);
        } else {
            // We're somehow controlling a non-zombie robot.
            // ...
            // Kill it.
            robot.getController().disintegrate();
        }
    }

    /**
     * Run the logic for a zombie den.
     *
     * @param den the zombie den.
     */
    private void processZombieDen(InternalRobot den) {
        assert den.getType() == RobotType.ZOMBIEDEN;

        final RobotController rc = den.getController();
        final Map<RobotType, Integer> spawnQueue = denQueues.get(rc.getID());

        final ZombieSpawnSchedule zSchedule = world.getGameMap().getZombieSpawnSchedule(den.getLocation());
        
        // Update the spawn queue with the values from this round.
        for (ZombieCount count : zSchedule.getScheduleForRound(world.getCurrentRound())) {
            final int currentCount = spawnQueue.get(count.getType());
            spawnQueue.put(count.getType(), currentCount + count.getCount());
        }

        // Spawn as many available robots as possible
        spawnAllPossible(rc,spawnQueue);
        
        // Now we've tried every direction. If we still have things in queue, damage surrounding robots
        RobotType next = null;
        for (RobotType type : ZOMBIE_TYPES) {
            if (spawnQueue.get(type) != 0) {
                next = type;
            }
        }
        if (next != null) {
            // There are still things in queue, so attack all locations
            for (int dirOffset=0; dirOffset < DIRECTIONS.length; dirOffset++) {
                final InternalRobot block = world.getObject(rc.getLocation().add(DIRECTIONS[dirOffset]));
                if (block != null && block.getTeam() != Team.ZOMBIE) {
                    block.takeDamage(GameConstants.DEN_SPAWN_PROXIMITY_DAMAGE);
                }
            }
            
            // Now spawn in remaining locations
            spawnAllPossible(rc,spawnQueue);
        }
    }
    
    /**
     * Spawn as of the queued robots as space allows
     * @param rc a robotcontroller
     * @param spawnQueue the queue of robots to be spawned
     */
    private void spawnAllPossible(RobotController rc, Map<RobotType, Integer> spawnQueue) {
        // Walk around the den, attempting to spawn zombies.
        // We choose a random direction to start spawning so that we don't prefer to spawn zombies
        // to the north.
        final int startingDirection = random.nextInt(DIRECTIONS.length);
        for (int dirOffset = 0; dirOffset < DIRECTIONS.length; dirOffset++) {
            final Direction dir = DIRECTIONS[(startingDirection + dirOffset) % DIRECTIONS.length];
            // Pull the next zombie type to spawn from the queue
            RobotType next = null;
            for (RobotType type : ZOMBIE_TYPES) {
                if (spawnQueue.get(type) != 0) {
                    next = type;
                }
            }
            if (next == null) {
                break;
            }

            // Check if we can build in this location
            if (rc.canBuild(dir, next)) {
                try {
                    // We can!
                    rc.build(dir, next);
                    spawnQueue.put(next, spawnQueue.get(next) - 1);
                } catch (GameActionException e) {
                    ErrorReporter.report(e, true);
                }
            }
        }
    }

    private void processZombie(InternalRobot zombie) {
        assert zombie.getType().isZombie;

        final RobotController rc = zombie.getController();

        RobotInfo closestRobot = world.getNearestPlayerControlled(rc.getLocation());

        try {
            if (closestRobot != null && rc.canAttackLocation(closestRobot.location)) {
                // If target is in range, attack it and end turn
                if (rc.isWeaponReady()) {
					rc.attackLocation(closestRobot.location);
				}
                return;
            }
            if (!rc.isCoreReady()) {
                // We can't do anything.
                return;
            }

            // Else, try to move closer
            Direction preferredDirection;
            if (closestRobot != null) {
                preferredDirection = rc.getLocation().directionTo(closestRobot.location);
                // First, try to move if best direction toward target
                if (rc.canMove(preferredDirection)) {
                    rc.move(preferredDirection);
                    return;
                }
            } else {
                preferredDirection = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
            }

            // If that was obstructed, randomly try either left or right 45 degress
            final Direction nextDirection;
            final boolean newLeft = random.nextBoolean();
            if (newLeft) {
                nextDirection = preferredDirection.rotateLeft();
            } else {
                nextDirection = preferredDirection.rotateRight();
            }

            // Try to move in the new rotated direction
            if (rc.canMove(nextDirection)) {
                rc.move(nextDirection);
                return;
            }

            // That didn't work, so try the other direction
            final Direction finalDirection;
            if (newLeft) {
                finalDirection = preferredDirection.rotateRight();
            } else {
                finalDirection = preferredDirection.rotateLeft();
            }

            // Try to move in the other rotated direction
            if (rc.canMove(finalDirection)) {
                rc.move(finalDirection);
                return;
            }

            // Try to clear rubble instead

            final MapLocation preferredTarget = rc.getLocation().add(preferredDirection);
            if (!rc.isLocationOccupied(preferredTarget) && rc.onTheMap(preferredTarget)
                    && rc.senseRubble(preferredTarget) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                rc.clearRubble(preferredDirection);
                return;
            }
			
			if (rc.isLocationOccupied(preferredTarget) && rc.senseRobotAtLocation(preferredTarget).team == Team.NEUTRAL) {
				if (rc.isWeaponReady()) {
					rc.attackLocation(preferredTarget);
					return;
				}
			}
			
        } catch (Exception e) {
            ErrorReporter.report(e, true);
        }
    }

    @Override
    public int getBytecodesUsed(InternalRobot robot) {
        // Zombies don't think.
        return 0;
    }

    @Override
    public boolean getTerminated(InternalRobot robot) {
        // Zombies never terminate due to computation errors.
        return false;
    }
}
