package battlecode.world.control;

import battlecode.common.*;
import battlecode.server.ErrorReporter;
import battlecode.server.Server;
import battlecode.world.GameMap;
import battlecode.world.GameWorld;
import battlecode.world.InternalRobot;

import java.util.*;

/**
 * The control provider for zombies. Doesn't use instrumentation or anything,
 * just plain-old logic.
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

    private boolean disableSpawning;

    /**
     * Create a ZombieControlProvider.
     */
    public ZombieControlProvider() {
        this.disableSpawning = false;
        this.denQueues = new HashMap<>();
    }

    public ZombieControlProvider(boolean disableSpawning) {
        this.disableSpawning = disableSpawning;
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
            if (!disableSpawning) {
                processZombieDen(robot);
            }
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
        spawnAllPossible(rc, spawnQueue);

        // Now we've tried every direction. If we still have things in queue, damage surrounding robots
        RobotType next = null;
        for (RobotType type : ZOMBIE_TYPES) {
            if (spawnQueue.get(type) != 0) {
                next = type;
            }
        }
        if (next != null) {
            // There are still things in queue, so attack all locations
            for (int dirOffset = 0; dirOffset < DIRECTIONS.length; dirOffset++) {
                final InternalRobot block = world.getObject(rc.getLocation().add(DIRECTIONS[dirOffset]));
                if (block != null && block.getTeam() != Team.ZOMBIE) {
                    block.takeDamage(GameConstants.DEN_SPAWN_PROXIMITY_DAMAGE);
                }
            }

            // Now spawn in remaining locations
            spawnAllPossible(rc, spawnQueue);
        }
    }

    /**
     * Spawn as of the queued robots as space allows
     *
     * @param rc a robotcontroller
     * @param spawnQueue the queue of robots to be spawned
     */
    private void spawnAllPossible(RobotController rc, Map<RobotType, Integer> spawnQueue) {
        // Walk around the den, attempting to spawn zombies.
        // We choose a random direction to start spawning so that we don't prefer to spawn zombies
        // to the north.
        final int startingDirection = getSpawnDirection(rc.getLocation());
        final int chirality = getSpawnChirality(rc.getLocation());

        for (int dirOffset = 0; dirOffset < DIRECTIONS.length; dirOffset++) {
            final Direction dir = DIRECTIONS[
                    Math.floorMod(startingDirection + dirOffset*chirality, DIRECTIONS.length)
            ];

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
            if (!rc.isCoreReady() || (world.getGameMap().isArmageddon() && world.isArmageddonDaytime())) {
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

    // just some memoization
    private final Map<MapLocation, Integer> directionCache = new HashMap<>();
    private final Map<MapLocation, Integer> chiralityCache = new HashMap<>();

    /**
     * Get the direction to spawn the first available zombie in.
     *
     * @param loc the location of the zombie den
     * @return an index into DIRECTIONS that represents the first
     *         direction to spawn in
     */
    private int getSpawnDirection(MapLocation loc) {
        if (directionCache.containsKey(loc)) {
            return directionCache.get(loc);
        }

        final GameMap map = world.getGameMap();

        final MapLocation closest = Arrays.stream(map.getInitialRobots())
                .filter(r -> r.type == RobotType.ARCHON)
                .map(r -> r.getLocation(map.getOrigin()))
                .min((l1, l2) -> Integer.compare(
                        l1.distanceSquaredTo(loc),
                        l2.distanceSquaredTo(loc)
                ))
                .orElseGet(() -> {
                    Server.warn("Zombie den: No initial archons on map, spawning towards middle; may not be fair");
                    return map.getOrigin().add(map.getWidth() / 2, map.getHeight() / 2);
                });

        final int direction = Arrays.asList(DIRECTIONS).indexOf(loc.directionTo(closest));

        if (direction == -1) {
            throw new RuntimeException("Zombie den: can't find direction towards "+closest+
                    " from "+loc+", this is a bug.");
        }

        directionCache.put(loc, direction);

        return direction;
    }

    /**
     * Get the clock direction to spawn zombies in, after the first zombie.
     *
     * @param loc the location of the zombie den
     * @return 1 for clockwise, -1 for counterclockwise
     */
    private int getSpawnChirality(MapLocation loc) {
        if (chiralityCache.containsKey(loc)) {
            return chiralityCache.get(loc);
        }

        final GameMap map = world.getGameMap();

        int chir;

        if (map.getSymmetry() == GameMap.Symmetry.ROTATIONAL
                || map.getSymmetry() == GameMap.Symmetry.NONE) {
            // always spawn zombies clockwise
            chir = 1;
        } else {
            // this is somewhat obtuse
            // basically, chirality should be opposite on opposite sides of the line of symmetry
            // so we get the opposite location of this location over the line of symmetry
            // and compare them
            // and have the guarantee that loc.compareTo(opp) == -opp.compareTo(loc),
            // since MapLocations have a total order, and loc != opp
            // and take the sign of that to get 1 and -1;
            // so getSpawnChirality(opp) == -getSpawnChirality(loc)
            chir = Integer.signum(loc.compareTo(map.getSymmetry()
                    .getOpposite(loc, map.getWidth(), map.getHeight(), map.getOrigin())));

            // or 0, if loc is on line of symmetry
            if (chir == 0) {
                Server.warn("Zombie den on line of symmetry, spawning clockwise");
                chir = 1;
            }
        }

        chiralityCache.put(loc, chir);

        return chir;
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
