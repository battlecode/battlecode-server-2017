package battlecode.world;

import battlecode.common.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

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
     *
     * IMPORTANT NOTE: it is accessed in [y][x] order,
     * in order to make serialization less wonky!
     */
    private final double[][] initialRubble;

    /**
     * The initial parts on the map.
     *
     * IMPORTANT NOTE: it is accessed in [y][x] order,
     * in order to make serialization less wonky!
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
    private final ZombieSpawnSchedule zombieSpawnSchedule;

    /**
     * Maps a den MapLocation to its ZombieSpawnSchedule
     */
    private transient HashMap<MapLocation, ZombieSpawnSchedule> zombieSpawnMap;
    
    /**
     * Boolean values representing the different types of symmetry the map has;
     * computed lazily.
     */
    private transient boolean symVert, symHoriz, symRot, symNegDiag, symPosDiag;
    
    /**
     * The robots to spawn on the map; MapLocations are in world space -
     * i.e. in game correct MapLocations that need to have the origin
     * subtracted from them to be used to index into the map arrays.
     */
    private final InitialRobotInfo[] initialRobots;

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
        this.initialRubble = new double[this.height][this.width];
        this.initialParts = new double[this.height][this.width];
        for (int i = 0; i < this.height; i++) {
            System.arraycopy(gm.initialRubble[i], 0, this.initialRubble[i], 0, this.width);
            System.arraycopy(gm.initialParts[i], 0, this.initialParts[i], 0, this.width);
        }

        this.origin = gm.origin;
        this.seed = gm.seed;
        this.rounds = gm.rounds;
        this.mapName = gm.mapName;
        this.zombieSpawnSchedule = new ZombieSpawnSchedule(gm.zombieSpawnSchedule);
        this.initialRobots = gm.getInitialRobots();
    }

    /**
     * Creates a GameMap with the given parameters. Note: the rubble and parts
     * arrays should be indexed such that rubble[y][x] gives you the rubble at
     * coordinate (x, y). The first index is the row, the second index is the
     * column.
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
     * @param zombieSpawnSchedule zombie spawn schedule.
     * @param initialRobots the robots initially on the map
     * @param mapName name of the map.
     */
    public GameMap(Map<MapProperties, Integer> mapProperties,
                   double[][] initialRubble,
                   double[][] initialParts,
                   ZombieSpawnSchedule zombieSpawnSchedule,
                   InitialRobotInfo[] initialRobots,
                   String mapName) {
        if (mapProperties.containsKey(MapProperties.WIDTH)) {
            this.width = mapProperties.get(MapProperties.WIDTH);
        } else {
            this.width = initialRubble[0].length;
        }

        if (mapProperties.containsKey(MapProperties.HEIGHT)) {
            this.height = mapProperties.get(MapProperties.HEIGHT);
        } else {
            this.height = initialRubble.length;
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
        this.zombieSpawnSchedule = zombieSpawnSchedule;
        this.mapName = mapName;
        this.initialRobots = initialRobots;
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
        if (!this.zombieSpawnSchedule.equals(other.zombieSpawnSchedule)) return false;

        return Arrays.equals(this.initialRobots, other.initialRobots);
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

        return initialRubble[y - origin.y][x - origin.x];
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

        return initialParts[y - origin.y][x - origin.x];
    }

    /**
     * Gives the inital robot at a location, if any.
     *
     * @param location the location to check
     * @return the RobotInfo for the robot at that
     */
    public Optional<InitialRobotInfo> getInitialRobotAtLocation(MapLocation location) {
        for (InitialRobotInfo robot : initialRobots) {
            if (robot.getLocation(origin).equals(location)) {
                return Optional.of(robot);
            }
        }
        return Optional.empty();
    }

    /**
     * Get a list of the initial robots on the map.
     *
     * @return the list of starting robots on the map.
     *         MUST NOT BE MODIFIED.
     */
    public InitialRobotInfo[] getInitialRobots() {
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

    /**
     * @return the zombie spawn schedule for the map.
     */
    public ZombieSpawnSchedule getZombieSpawnSchedule() {
        return zombieSpawnSchedule;
    }

    /**
     * @param round the round to look up
     * @return an array of ZombieCounts for that round
     */
    @JsonIgnore
    public ZombieCount[] getZombieSpawnSchedule(int round) {
        return zombieSpawnSchedule.getScheduleForRound(round);
    }

    /**
     * @param denLoc the location of the den
     * @return the ZombieSpawnSchedule for that den
     */
    @JsonIgnore
    public ZombieSpawnSchedule getZombieSpawnSchedule(MapLocation denLoc) {
        computeLazyValues();

        return this.zombieSpawnMap.get(denLoc);
    }
    
    /**
     * Gets the origin (i.e., upper left corner) of the map
     *
     * @return the origin of the map
     */
    public MapLocation getOrigin() {
        return origin;
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

        /**
         * For use by serializers.
         */
        @SuppressWarnings("unused")
        private InitialRobotInfo() {
            this(0, 0, null, null);
        }
    }


    /**
     * Updates the map's symmetry types
     */
    public void updateSymmetries() {
        // The different possible symmetries.
        symVert = true;
        symHoriz = true;
        symRot = true;
        symNegDiag = height == width; // across the line x=height-y
        symPosDiag = height == width; // across the line x=y

        // First, we check if rubble and parts are symmetric.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                symVert = symVert && sameTile(x, y, x, height - y - 1);
                symHoriz = symHoriz && sameTile(x, y, width - x - 1, y);
                symRot = symRot && sameTile(x, y, width - x - 1, height - y - 1);
                if (width == height) {
                    symNegDiag = symNegDiag && sameTile(x, y, height - y - 1, width - x - 1);
                    symPosDiag = symPosDiag && sameTile(x, y, y, x);
                }
            }
        }

        // Next, we check if robots are symmetric.

        // The byLoc map is just to speed up robot lookup by-location.
        final MapLocation origin = new MapLocation(0, 0);
        final Map<MapLocation, InitialRobotInfo> byLoc =
                Arrays.stream(initialRobots).collect(Collectors.toMap(
                        (robot) -> robot.getLocation(origin),
                        (robot) -> robot
                ));

        for (MapLocation loc : byLoc.keySet()) {
            final InitialRobotInfo r1 = byLoc.get(loc);
            final int x = loc.x, y = loc.y;

            symVert = symVert && oppositeRobots(
                    r1,
                    byLoc.get(new MapLocation(x, height - y - 1))
            );
            symHoriz = symHoriz && oppositeRobots(
                    r1,
                    byLoc.get(new MapLocation(width - x - 1, y))
            );
            symRot = symRot && oppositeRobots(
                    r1,
                    byLoc.get(new MapLocation(width - x - 1, height - y - 1))
            );
            if (width == height) {
                symNegDiag = symNegDiag && oppositeRobots(
                        r1,
                        byLoc.get(new MapLocation(height - y - 1, width - x - 1))
                );
                symPosDiag = symPosDiag && oppositeRobots(
                        r1,
                        byLoc.get(new MapLocation(y, x))
                );
            }
        }
    }
    
    /**
     * @return True if the map is symmetric in some way and ZOMBIEDENs are not
     * located on lines of symmetry.
     */
    @JsonIgnore
    public boolean isTournamentLegal() {
        computeLazyValues();

        // First, check to make sure there aren't any ZOMBIEDENs on lines of symmetry
        for(InitialRobotInfo robot : initialRobots) {
            final MapLocation origin = new MapLocation(0, 0);
            final MapLocation loc = robot.getLocation(origin);
            final int x = loc.x, y = loc.y;
            if(robot.type == RobotType.ZOMBIEDEN) {
                if(symVert) {
                    if(y == height - y - 1) return false;
                } else if (symHoriz) {
                    if(x == width - x - 1) return false;
                } else if (symNegDiag) {
                    if(x == height - y - 1 && y == width - x - 1) return false;
                } else if (symPosDiag) {
                    if(x == y) return false;
                } else if (symRot) {
                    if(x == width - x - 1 && y == height - y - 1) return false;
                } else {
                    return false;
                }
            }
        }
        // Make sure the map has some sort of symmetry
        return (symVert || symHoriz || symNegDiag || symPosDiag || symRot);
    }

    /**
     * Divides a ZombieSpawnSchedule symmetrically among all ZOMBIEDENs 
     * in a map.
     * 
     * @param schedule The ZombieSpawnSchedule to divide between ZOMBIEDENs
     * @return A Map mapping a ZOMBIEDEN's MapLocation to a ZombieSpawnSchedule
     */
    private HashMap<MapLocation,ZombieSpawnSchedule> buildZombieSpawnMap(ZombieSpawnSchedule schedule) {
        
        HashMap<MapLocation,ZombieSpawnSchedule> returnMap = new HashMap<MapLocation,ZombieSpawnSchedule>();
        
        // Used for robot lookups by location
        final MapLocation origin = new MapLocation(0, 0);
        final Map<MapLocation, InitialRobotInfo> byLoc =
                Arrays.stream(initialRobots).collect(Collectors.toMap(
                        (robot) -> robot.getLocation(origin),
                        (robot) -> robot
                ));
        
        // Build list of ZOMBIEDEN Locations (in symmetric order)
        ArrayList<MapLocation> denLocs = new ArrayList<MapLocation>();
        for (MapLocation loc : byLoc.keySet()) {

            InitialRobotInfo r1 = byLoc.get(loc);
            final int x = loc.x, y = loc.y;

            if(r1.type == RobotType.ZOMBIEDEN && denLocs.indexOf(loc) == -1) {
                // Add this location
                denLocs.add(r1.getLocation(origin));
                
                // Now find symmetric pair
                MapLocation newLocation;
                if(symVert) {
                    newLocation = new MapLocation(x, height - y - 1);
                } else if (symHoriz) {
                    newLocation = new MapLocation(width - x - 1, y);
                } else if (symNegDiag) {
                    newLocation = new MapLocation(height - y - 1, width - x - 1);
                } else if (symPosDiag) {
                    newLocation = new MapLocation(y, x);
                } else if (symRot) {
                    newLocation = new MapLocation(width - x - 1, height - y - 1);
                } else {
                    newLocation = null; // Map is not symmetric, so no pair
                }
                
                // Add the symmetric pair
                if (newLocation != null && oppositeRobots(r1,byLoc.get(newLocation)) && denLocs.indexOf(newLocation) == -1) {
                    denLocs.add(newLocation);
                } else {
                    // This should never happen in valid maps, but allows it to work with asymmetric maps (such as tests)
                }
            }
        }
        
        // Prevent future divide-by-zero errors
        if(denLocs.size() == 0) {
            return returnMap;
        }
        
        // Now we need to shift them to match the actual origin of the map
        ArrayList<MapLocation> shiftedDenLocs = new ArrayList<MapLocation>();
        for(MapLocation denLoc : denLocs) {
            shiftedDenLocs.add(denLoc.add(this.origin.x,this.origin.y));
        }
        denLocs = shiftedDenLocs;
        
        // Initialize a blank ZombieSpawnSchedule for each location in denLocs
        for(MapLocation location : denLocs) {
            returnMap.put(location, new ZombieSpawnSchedule());
        }
        
        int currentIndex = 0; // Index of the ZOMBIEDEN that receives the next zombie
        int numberDens = denLocs.size();
        
        // Divide ZombieSpawnSchedule between the dens
        for(int round : schedule.getRounds()) {
            for(ZombieCount zombieCount : schedule.getScheduleForRound(round)) {
                // First, divide as evenly as we can
                int evenlyDivided = zombieCount.getCount() / numberDens;
                int leftOver = zombieCount.getCount() % numberDens;
                for(MapLocation denLoc : denLocs) {
                    returnMap.get(denLoc).add(round, zombieCount.getType(), evenlyDivided);
                }
                
                // Now, iterate individually for all leftover
                for (int i=0; i<leftOver; i++) {
                    returnMap.get(denLocs.get(currentIndex)).add(round, zombieCount.getType(), 1);
                    currentIndex = (currentIndex+1)%numberDens;
                }
            }
        }
        return returnMap;
    }
    
    /**
     * Return whether two map tiles have the same properties.
     *
     * @param x1 the x coordinate of the first tile
     * @param y1 the y coordinate of the first tile
     * @param x2 the x coordinate of the second tile
     * @param y2 the y coordinate of the second tile
     * @return whether the tiles are the same
     */
    private boolean sameTile(int x1, int y1, int x2, int y2) {
        return initialRubble[y1][x1] == initialRubble[y2][x2] &&
                initialParts[y1][x1] == initialParts[y2][x2];
    }

    /**
     * Return whether two robots are considered "opposite",
     * for the purpose of tournament legality.
     *
     * @param r1 the first robot
     * @param r2 the second robot
     * @return whether the robots are legal
     */
    private boolean oppositeRobots(InitialRobotInfo r1, InitialRobotInfo r2) {
        if (r1 == null || r2 == null) {
            return false;
        }

        if (r1.type != r2.type) {
            return false;
        }

        if (r1.team == Team.ZOMBIE || r1.team == Team.NEUTRAL) {
            return r1.team == r2.team;
        } else {
            return r2.team != Team.ZOMBIE
                    && r2.team != Team.NEUTRAL
                    && r2.team != r1.team;
        }
    }

    /**
     * Computes symmetries and ZombieSpawnMap.
     */
    private synchronized void computeLazyValues() {
        if (this.zombieSpawnMap == null) {
            updateSymmetries();
            this.zombieSpawnMap = buildZombieSpawnMap(this.zombieSpawnSchedule);
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
        this.zombieSpawnSchedule = null;
        this.initialRobots = null;
        this.zombieSpawnMap = null;
    }
}
