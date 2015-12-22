package battlecode.world;

import battlecode.common.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.*;
import java.util.List;

/**
 * The class represents the map in the game world on which
 * objects interact.
 *
 * This class is STATIC and immutable. It reflects the initial
 * condition of the map. All changes to the map are reflected in GameWorld.
 */
public class GameMap implements Serializable {

    private static final long serialVersionUID = -2068896916199851260L;

    /**
     * The width and height of the map.
     */
    private final int width, height;

    /**
     * The initial rubble on the map.
     */
    private final double[][] initialRubble;

    /**
     * The initial parts on the map.
     */
    private final double[][] initialParts;

    /**
     * The coordinates of the origin
     */
    private final MapLocation origin;

    /**
     * The random seed contained in the map file
     */
    private final int seed;
    /**
     * The maximum number of rounds in the game
     */
    private final int rounds;

    /**
     * The name of the map
     */
    private final String mapName;

    /**
     * The zombie spawn schedule for the map
     */
    private final ZombieSpawnSchedule zSchedule;

    /**
     * The robots to spawn on the map; MapLocations are in world space -
     * i.e. in game correct MapLocations that need to have the origin
     * subtracted from them to be used to index into the map arrays.
     */
    private final List<InitialRobotInfo> initialRobots;

    /**
     * Represents the various integer properties a GameMap
     * can have.
     */
    public enum MapProperties {
        WIDTH, HEIGHT, SEED, ROUNDS
    }

    /**
     * Creates a deep copy of the input GameMap.
     *
     * @param gm the GameMap to copy.
     */
    public GameMap(GameMap gm) {
        this.width = gm.width;
        this.height = gm.height;
        this.initialRubble = new double[this.width][this.height];
        this.initialParts = new double[this.width][this.height];
        for (int i = 0; i < this.width; i++) {
            System.arraycopy(gm.initialRubble[i], 0, this.initialRubble[i], 0, this.height);
            System.arraycopy(gm.initialParts[i], 0, this.initialParts[i], 0, this.height);
        }

        this.origin = gm.origin;
        this.seed = gm.seed;
        this.rounds = gm.rounds;
        this.mapName = gm.mapName;
        this.zSchedule = new ZombieSpawnSchedule(gm.zSchedule);
        this.initialRobots = gm.getInitialRobots();
    }

    /**
     * Creates a GameMap with the given parameters. Note: the rubble and
     * parts arrays should be indexed such that rubble[x][y] gives you the
     * rubble at coordinate (x, y). This is weird if you're used to imagining
     * the first index as the row and the second index as the column, because
     * here it's the other way around.
     *
     * The map will be initialized with a pseudorandom origin between (0, 0) and
     * (500, 500), based on the seed.
     *
     * YOU MUST NOT MODIFY ANY OF THESE OBJECTS AFTER CREATING THE MAP.
     *
     * @param mapProperties used to specify integer properties of the map
     *                      (width, height, seed, and number of rounds).
     * @param initialRubble initial rubble array for the map.
     * @param initialParts initial parts array for the map.
     * @param zSchedule zombie spawn schedule.
     * @param initialRobots the robots initially on the map
     * @param mapName name of the map.
     */
    public GameMap(Map<MapProperties, Integer> mapProperties,
                   double[][] initialRubble,
                   double[][] initialParts,
                   ZombieSpawnSchedule zSchedule,
                   List<InitialRobotInfo> initialRobots,
                   String mapName) {
        if (mapProperties.containsKey(MapProperties.WIDTH)) {
            this.width = mapProperties.get(MapProperties.WIDTH);
        } else {
            this.width = initialRubble.length;
        }

        if (mapProperties.containsKey(MapProperties.HEIGHT)) {
            this.height = mapProperties.get(MapProperties.HEIGHT);
        } else {
            this.height = initialRubble[0].length;
        }

        if (mapProperties.containsKey(MapProperties.SEED)) {
            this.seed = mapProperties.get(MapProperties.SEED);
        } else {
            this.seed = GameConstants.GAME_DEFAULT_SEED;
        }

        if (mapProperties.containsKey(MapProperties.ROUNDS)) {
            this.rounds = mapProperties.get(MapProperties.ROUNDS);
        } else {
            this.rounds = GameConstants.GAME_DEFAULT_ROUNDS;
        }

        final Random rand = new Random(this.seed);

        this.origin = new MapLocation(rand.nextInt(500), rand.nextInt(500));
        this.initialRubble = initialRubble;
        this.initialParts = initialParts;
        this.zSchedule = zSchedule;
        this.mapName = mapName;
        this.initialRobots = Collections.unmodifiableList(initialRobots);
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GameMap)) return false;

        return this.equals((GameMap) o);
    }

    /**
     * Returns whether two GameMaps are equal. Two equal game maps
     * have the same information, but their zombie schedules might be in
     * different orders and their origins do not have to be the same.
     *
     * @param other the other map to compare to.
     * @return whether the two maps are equivalent.
     */
    public boolean equals(GameMap other) {
        if (this.rounds != other.rounds) return false;
        if (this.width != other.width) return false;
        if (this.height != other.height) return false;
        if (this.seed != other.seed) return false;
        if (!Arrays.deepEquals(this.initialRubble, other.initialRubble))
            return false;
        if (!Arrays.deepEquals(this.initialParts, other.initialParts))
            return false;
        if (!this.mapName.equals(other.mapName)) return false;
        if (!this.origin.equals(other.origin)) return false;
        if (!this.zSchedule.equivalentTo(other.zSchedule)) return false;

        return this.initialRobots.equals(other.initialRobots);
    }

    /**
     * Returns the width of this map.
     *
     * @return the width of this map.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of this map.
     *
     * @return the height of this map.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the name of the map.
     *
     * @return the name o the map.
     */
    public String getMapName() {
        return mapName;
    }

    /**
     * Determines whether or not the location at the specified
     * coordinates is on the map. The coordinate should be a shifted one
     * (takes into account the origin).
     *
     * @param x the (shifted) x-coordinate of the location
     * @param y the (shifted) y-coordinate of the location
     * @return true if the given coordinates are on the map,
     *         false if they're not
     */
    private boolean onTheMap(int x, int y) {
        return (x >= origin.x && y >= origin.y && x < origin.x + width && y < origin.y + height);
    }

    /**
     * Determines whether or not the specified location is on the map.
     *
     * @param location the MapLocation to test
     * @return true if the given location is on the map,
     *         false if it's not
     */
    public boolean onTheMap(MapLocation location) {
        return onTheMap(location.x, location.y);
    }

    /**
     * Determines the amount of rubble on the map at
     * the given location.
     *
     * @param x the x value of the MapLocation to get rubble for.
     * @param y the y value of the MapLocation to get rubble for.
     * @return the amount of rubble in the given location, or 0 if off map
     */
    public double initialRubbleAtLocation(int x, int y) {
        if (!onTheMap(x, y)) {
            return 0;
        }

        return initialRubble[x - origin.x][y - origin.y];
    }

    /**
     * Returns a two-dimensional array of rubble data for this map.
     *
     * @return the map's rubble in a 2D array
     */
    public double[][] getInitialRubble() {
        return initialRubble;
    }
    
    /**
     * Determines the amount of parts on the map at the
     * given location.
     *
     * @param x the x value of the MapLocation to get parts for.
     * @param y the y value of the MapLocation to get parts for.
     * @return the amount of parts in the given location, or 0 if off the map
     */
    public double initialPartsAtLocation(int x, int y) {
        if (!onTheMap(x, y))
            return 0.0;

        return initialParts[x - origin.x][y - origin.y];
    }

    /**
     * Returns a two-dimensional array of ore data for this map.
     *
     * @return the map's ore in a 2D array
     */
    public double[][] getInitialParts() {
        return initialParts;
    }

    /**
     * Gives the inital robot at a location, if any.
     *
     * @param location the location to check
     * @return the RobotInfo for the robot at that
     */
    public Optional<InitialRobotInfo> getInitialRobotAtLocation(MapLocation location) {
        return initialRobots.stream()
                .filter(robot -> robot.getLocation(origin).equals(location))
                .findFirst();
    }

    /**
     * Get a list of the initial robots on the map.
     *
     * @return the list of starting robots on the map.
     */
    public List<InitialRobotInfo> getInitialRobots() {
        return initialRobots;
    }

    /**
     * Gets the maximum number of rounds for this game.
     *
     * @return the maximum number of rounds for this game
     */
    public int getRounds() {
        return rounds;
    }

    public int getSeed() {
        return seed;
    }

    @JsonIgnore
    public ZombieCount[] getZombieSpawnSchedule(int round) {
        final List<ZombieCount> sched = zSchedule.getScheduleForRound(round);
        return sched.toArray(new ZombieCount[sched.size()]);
    }

    /**
     * Gets the origin (i.e., upper left corner) of the map
     *
     * @return the origin of the map
     */
    @JsonIgnore
    public MapLocation getOrigin() {
        return new MapLocation(origin.x, origin.y);
    }

    /**
     * Stores the current "field of view" / "fog of war" for a robot team.
     */
    public static class MapMemory {
        private final GameMap map;

        /** Represents the number of units that can see all squares. */
        private int globalCount;

        /** Represents how many units are currently able to sense a given location. */
        private final int[][] currentCount;

        /** Represents whether a certain location was ever in sensor range. */
        private final boolean[][] seen;

        /** Represents the amount of rubble on location when the location was last in sensor range. */
        private final double[][] rubbleOnSquare;
        
        /** Represents the amount of parts on location when the location was last in sensor range. */
        private final double[][] partsOnSquare;

        /** It's important to keep track of OFF_MAP squares so we have this buffer around the map. */
        private final int OFFSET = 50;

        /**
         * Creates a MapMemory using a given GameMap.
         *
         * @param map map to base the map memory on.
         */
        public MapMemory(GameMap map) {
            this.map = map;
            this.currentCount = new int[map.getWidth() + 2 * OFFSET][map.getHeight() + 2 * OFFSET];
            this.seen = new boolean[map.getWidth() + 2 * OFFSET][map.getHeight() + 2 * OFFSET];
            this.rubbleOnSquare = new double[map.getWidth() + 2 * OFFSET][map.getHeight() + 2 * OFFSET];
            this.partsOnSquare = new double[map.getWidth() + 2 * OFFSET][map.getHeight() + 2 * OFFSET];
        }

        /**
         * Returns whether (x, y) is a location tracked by the MapMemory. The
         * MapMemory tracks all locations within OFFSET of the map boundaries.
         *
         * @param x x-coordinate relative to origin.
         * @param y y-coordinate relative to origin.
         * @return whether (x, y) is a location tracked by the MapMemory.
         */
        private boolean validLoc(int x, int y) {
            return x >= -OFFSET && x < map.getWidth() + OFFSET && y >= -OFFSET && y < map.getHeight() + OFFSET;
        }

        /**
         * Returns whether (x, y) is a location on the map.
         *
         * @param x x-coordinate relative to origin.
         * @param y y-coordinate relative to origin.
         * @return whether (x, y) is on the map.
         */
        private boolean onTheMap(int x, int y) {
            return x >= 0 && x < map.getWidth() && y >= 0 && y
                    < map.getHeight();
        }

        /**
         * Marks that a unit on a certain location is no longer there, and
         * will update currentCount accordingly.
         *
         * @param loc the location that no longer contains a unit.
         * @param radiusSquared the sight range of that unit.
         */
        public void removeLocation(MapLocation loc, int radiusSquared) {
            if (radiusSquared < 0) {
                globalCount--;
            } else {
                MapLocation[] locs = MapLocation.getAllMapLocationsWithinRadiusSq(loc, radiusSquared);

                for (MapLocation target : locs) {
                    int x = target.x - map.origin.x;
                    int y = target.y - map.origin.y;
                    if (validLoc(x, y)) {
                        currentCount[x + OFFSET][y + OFFSET]--;
                    }
                }
            }
        }

        /**
         * Marks that a new unit is now on a certain location, and updates
         * rubble and parts values based on what that unit can currently see.
         *
         * @param loc the location that the unit enters.
         * @param radiusSquared sight range of that unit.
         * @param rubble all the map's rubble.
         * @param parts all the map's parts.
         */
        public void rememberLocation(MapLocation loc, int radiusSquared,
                                     double[][] rubble, double[][] parts) {
            if (radiusSquared < 0) {
                globalCount++;
                if (globalCount == 1) {
                    for (int x = 0; x < rubble.length; ++x) {
                        for (int y = 0; y < rubble[0].length; ++y) {
                            seen[x + OFFSET][y + OFFSET] = true;
                            rubbleOnSquare[x + OFFSET][y + OFFSET] =
                                    rubble[x][y];
                            partsOnSquare[x + OFFSET][y + OFFSET] = parts[x][y];
                        }
                    }
                }
            } else {
                MapLocation[] locs = MapLocation.getAllMapLocationsWithinRadiusSq(loc, radiusSquared);

                for (MapLocation target : locs) {
                    int x = target.x - map.origin.x;
                    int y = target.y - map.origin.y;
                    if (validLoc(x, y)) {
                        seen[x + OFFSET][y + OFFSET] = true;
                        currentCount[x + OFFSET][y + OFFSET]++;
                        if (currentCount[x + OFFSET][y + OFFSET] == 1 && onTheMap
                                (x, y)) {
                            partsOnSquare[x + OFFSET][y + OFFSET] = parts[x][y];
                        }
                        if (currentCount[x + OFFSET][y + OFFSET] == 1 && onTheMap
                                (x, y)) {
                            rubbleOnSquare[x + OFFSET][y + OFFSET] = rubble[x][y];
                        }
                    }
                }
            }
        }

        /**
         * Tells the map memory that a specific location has had its rubble
         * value changed.
         *
         * @param loc the location.
         * @param rubble the new rubble value.
         */
        public void updateLocationRubble(MapLocation loc, double rubble) {
            if (canSense(loc)) {
                int x = loc.x - map.origin.x;
                int y = loc.y - map.origin.y;
                this.rubbleOnSquare[x + OFFSET][y + OFFSET] = rubble;
            }
        }

        /**
         * Tells the map memory that a specific location has had its parts
         * value changed.
         *
         * @param loc the location.
         * @param parts the new parts value.
         */
        public void updateLocationParts(MapLocation loc, double parts) {
            if (canSense(loc)) {
                int x = loc.x - map.origin.x;
                int y = loc.y - map.origin.y;
                this.partsOnSquare[x + OFFSET][y + OFFSET] = parts;
            }
        }

        /**
         * Returns whether any unit has seen that location before.
         *
         * @param loc the location.
         * @return whether any unit has seen that location before.
         */
        public boolean seenBefore(MapLocation loc) {
            int X = loc.x - map.origin.x;
            int Y = loc.y - map.origin.y;

            if (validLoc(X, Y)) {
                return seen[X + OFFSET][Y + OFFSET];
            } else {
                return false;
            }
        }

        /**
         * Returns whether the location is in sight range currently.
         *
         * @param loc the location to check.
         * @return whether the location is currently in sight range.
         */
        public boolean canSense(MapLocation loc) {
            int X = loc.x - map.origin.x;
            int Y = loc.y - map.origin.y;

            if (validLoc(X, Y)) {
                return currentCount[X + OFFSET][Y + OFFSET] > 0 ||
                        globalCount > 0;
            } else {
                return false;
            }
        }

        /**
         * Returns the last seen rubble value from that location, or -1 if
         * that location has never been seen before.
         *
         * @param loc the location.
         * @return last seen rubble value.
         */
        public double recallRubble(MapLocation loc) {
            int X = loc.x - map.origin.x;
            int Y = loc.y - map.origin.y;

            if (seenBefore(loc)) {
                return rubbleOnSquare[X + OFFSET][Y + OFFSET];
            } else {
                return -1;
            }
        }

        /**
         * Returns the last seen parts value from that location, or -1 if
         * that location has never been seen before.
         *
         * @param loc the location.
         * @return last seen parts value.
         */
        public double recallParts(MapLocation loc) {
            int X = loc.x - map.origin.x;
            int Y = loc.y - map.origin.y;

            if (seenBefore(loc)) {
                return partsOnSquare[X + OFFSET][Y + OFFSET];
            } else {
                return -1;
            } 
        }
    }

    /**
     * Information about a robot that starts on the map.
     */
    public static final class InitialRobotInfo implements Serializable {

        private static final long serialVersionUID = -2012039299851260L;

        /**
         * The offset from the origin of this robot.
         */
        public final int originOffsetX, originOffsetY;

        /**
         * The type of the robot.
         */
        public final RobotType type;

        /**
         * The team of the robot.
         */
        public final Team team;

        public InitialRobotInfo(int originOffsetX, int originOffsetY, RobotType type, Team team) {
            this.originOffsetX = originOffsetX;
            this.originOffsetY = originOffsetY;
            this.type = type;
            this.team = team;
        }

        /**
         * This is a somewhat awkward workaround to deal with the fact
         * that InitialRobotInfos are often created before a game map,
         * and therefore don't know their gamemap's origin.
         *
         * @param origin the origin of a GameMap
         * @return the location of this initial robot (not relative to origin)
         */
        public MapLocation getLocation(MapLocation origin) {
            return new MapLocation(
                    origin.x + originOffsetX,
                    origin.y + originOffsetY
            );
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof InitialRobotInfo)) {
                return false;
            }

            final InitialRobotInfo that = (InitialRobotInfo) o;

            return this.originOffsetX == that.originOffsetX &&
                    this.originOffsetY == that.originOffsetY &&
                    this.type.equals(that.type) &&
                    this.team.equals(that.team);
        }

        @Override
        public int hashCode() {
            return Objects.hash(originOffsetX, originOffsetY, type, team);
        }

        @Override
        public String toString() {
            return "InitialRobotInfo{" +
                    "originOffsetX=" + originOffsetX +
                    ", originOffsetY=" + originOffsetY +
                    ", type=" + type +
                    ", team=" + team +
                    '}';
        }

        /**
         * For use by serializers.
         */
        @SuppressWarnings("unused")
        private InitialRobotInfo() {
            this(0, 0, null, null);
        }
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private GameMap() {
        this.width = 0;
        this.height = 0;
        this.origin = null;
        this.initialRubble = null;
        this.initialParts = null;
        this.seed = 0;
        this.rounds = 0;
        this.mapName = null;
        this.zSchedule = null;
        this.initialRobots = null;
    }
}
