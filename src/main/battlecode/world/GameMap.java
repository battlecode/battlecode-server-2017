package battlecode.world;

import battlecode.common.*;

import battlecode.server.Server;
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

    public enum Symmetry {
        VERTICAL,
        HORIZONTAL,
        ROTATIONAL,
        NEGATIVE_DIAGONAL,
        POSITIVE_DIAGONAL,
        NONE;

        /**
         * Get an opposite MapLocation, based on a map size and a zero origin.
         *
         * @param loc a MapLocation based on a ZERO ORIGIN
         * @param width the width of the map
         * @param height the height of the map
         * @return the opposite map location based on a ZERO ORIGIN,
         *         or null if there is no opposite
         */
        public MapLocation getOpposite(MapLocation loc, int width, int height) {
            final float x, y;

            switch (this) {
                case VERTICAL:
                    x = loc.x;
                    y = height - loc.y ;
                    break;
                case HORIZONTAL:
                    x = width - loc.x ;
                    y = loc.y;
                    break;
                case ROTATIONAL:
                    x = width - loc.x ;
                    y = height - loc.y ;
                    break;
                case NEGATIVE_DIAGONAL:
                    if (width != height) {
                        throw new RuntimeException("Can't have negative diagonal map" +
                                " with different width ("+width+") and height ("+height+")");
                    }

                    x = height - loc.y ;
                    y = width - loc.x ;
                    break;
                case POSITIVE_DIAGONAL:
                    if (width != height) {
                        throw new RuntimeException("Can't have negative diagonal map" +
                                " with different width ("+width+") and height ("+height+")");
                    }

                    x = loc.y;
                    y = loc.x;
                    break;
                default:
                    return null;
            }

            return new MapLocation(x, y);
        }

        /**
         * Get an opposite MapLocation, based on a map size and origin
         *
         * @param loc the initial map location
         * @param width the width of the map
         * @param height the height of the map
         * @param origin the origin of the map
         * @return the opposite map location, or null if there is none
         */
        public MapLocation getOpposite(MapLocation loc, int width, int height, MapLocation origin) {
            MapLocation zeroBasedResult = getOpposite(loc.translate(-origin.x, -origin.y), width, height);

            return zeroBasedResult == null ?
                    null :
                    origin.translate(zeroBasedResult.x, zeroBasedResult.y);
        }
    }

    /**
     * The type of symmetry the map has; computed lazily.
     */
    private transient Symmetry symmetry;

    /**
     * The robots to spawn on the map; MapLocations are in world space -
     * i.e. in game correct MapLocations that need to have the origin
     * subtracted from them to be used to index into the map arrays.
     */
    private final InitialRobotInfo[] initialRobots;

    /**
     * The trees to spawn on the map; MapLocations are in world space -
     * i.e. in game correct MapLocations that need to have the origin
     * subtracted from them to be used to index into the map arrays.
     */
    private final InitialTreeInfo[] initialTrees;

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

        this.origin = gm.origin;
        this.seed = gm.seed;
        this.rounds = gm.rounds;
        this.mapName = gm.mapName;
        this.initialRobots = gm.getInitialRobots();
        this.initialTrees = gm.getInitialTrees();
    }

    /**
     * Creates a GameMap with the given parameters.
     *
     * The map will be initialized with a pseudorandom origin between (0, 0) and
     * (500, 500), based on the seed.
     *
     * YOU MUST NOT MODIFY ANY OF THESE OBJECTS AFTER CREATING THE MAP.
     *
     * @param mapProperties used to specify integer properties of the map
     *                      (width, height, seed, and number of rounds).
     * @param initialRobots the robots initially on the map     
     * @param initialTrees the trees initially on the map
     * @param mapName name of the map.
     */
    public GameMap(Map<MapProperties, Integer> mapProperties,
                   InitialRobotInfo[] initialRobots,
                   InitialTreeInfo[] initialTrees,
                   String mapName) {
        if (mapProperties.containsKey(MapProperties.WIDTH)) {
            this.width = mapProperties.get(MapProperties.WIDTH);
        } else {
            this.width = 50;
        }

        if (mapProperties.containsKey(MapProperties.HEIGHT)) {
            this.height = mapProperties.get(MapProperties.HEIGHT);
        } else {
            this.height = 50;
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
        this.mapName = mapName;
        this.initialRobots = initialRobots;
        this.initialTrees = initialTrees;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GameMap)) return false;

        return this.equals((GameMap) o);
    }

    /**
     * Returns whether two GameMaps are equal. Two equal game maps
     * have the same information, but their origins do not have to be the same.
     *
     * @param other the other map to compare to.
     * @return whether the two maps are equivalent.
     */
    public boolean equals(GameMap other) {
        if (this.rounds != other.rounds) return false;
        if (this.width != other.width) return false;
        if (this.height != other.height) return false;
        if (this.seed != other.seed) return false;
        if (!this.mapName.equals(other.mapName)) return false;
        if (!this.origin.equals(other.origin)) return false;
        if (!Arrays.equals(this.initialRobots, other.initialRobots)) return false;

        return Arrays.equals(this.initialTrees, other.initialTrees);
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
    private boolean onTheMap(float x, float y) {
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
     * Determines whether or not the specified circle is completely on the map.
     *
     * @param loc the center of the circle
     * @param radius the radius of the circle
     * @return true if the given circle is on the map,
     *         false if it's not
     */
    public boolean onTheMap(MapLocation loc, float radius){
        return (onTheMap(loc.translate(-radius, 0)) &&
                onTheMap(loc.translate(radius, 0)) &&
                onTheMap(loc.translate(0, -radius)) &&
                onTheMap(loc.translate(0, radius)));
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
     * Get a list of the initial trees on the map.
     *
     * @return the list of starting trees on the map.
     *         MUST NOT BE MODIFIED.
     */
    public InitialTreeInfo[] getInitialTrees() {
        return initialTrees;
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
     * Gets the origin (i.e., upper left corner) of the map
     *
     * @return the origin of the map
     */
    public MapLocation getOrigin() {
        return origin;
    }

    /**
     * @return the symmetry of the map
     */
    @JsonIgnore
    public Symmetry getSymmetry() {
        computeLazyValues();

        return symmetry;
    }

    /**
     * Information about a robot that starts on the map.
     */
    public static final class InitialRobotInfo implements Serializable {

        private static final long serialVersionUID = -2012039299851260L;

        /**
         * The offset from the origin of this robot.
         */
        public final float originOffsetX, originOffsetY;

        /**
         * The type of the robot.
         */
        public final RobotType type;

        /**
         * The team of the robot.
         */
        public final Team team;

        public InitialRobotInfo(float originOffsetX, float originOffsetY, RobotType type, Team team) {
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
     * Information about a tree that starts on the map.
     */
    public static final class InitialTreeInfo implements Serializable {

        private static final long serialVersionUID = -2012005687925650L;

        /**
         * The offset from the origin of this tree.
         */
        public final float originOffsetX, originOffsetY;

        /**
         * The radius of the tree.
         */
        public final double radius;

        /**
         * The team of the robot.
         */
        public final Team team;

        /**
         * The current amount of bullets contained within the tree.
         * Note: only NEUTRAL trees can contain bullets
//         */
        public final double containedBullets;

        /**
         * The robot contained within the tree that is obtained upon
         * destroying the tree using the cut() method.  Has a value
         * of null if no robot is contained within the tree
         * Note: only NEUTRAL trees can contain robots
         */
        public final RobotType containedRobot;

        public InitialTreeInfo(float originOffsetX, float originOffsetY, double radius, Team team,
                               double containedBullets, RobotType containedRobot) {
            this.originOffsetX = originOffsetX;
            this.originOffsetY = originOffsetY;
            this.team = team;
            if(team == Team.NEUTRAL){
                this.radius = radius;
                this.containedRobot = containedRobot;
                this.containedBullets = containedBullets;
            }else{
                this.radius = GameConstants.BULLET_TREE_RADIUS;
                this.containedRobot = null;
                this.containedBullets = 0;
            }
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
            if (!(o instanceof InitialTreeInfo)) {
                return false;
            }

            final InitialTreeInfo that = (InitialTreeInfo) o;

            return this.originOffsetX == that.originOffsetX &&
                    this.originOffsetY == that.originOffsetY &&
                    this.radius == that.radius &&
                    this.team.equals(that.team);
        }

        @Override
        public int hashCode() {
            return Objects.hash(originOffsetX, originOffsetY, radius, team);
        }

        /**
         * For use by serializers.
         */
        @SuppressWarnings("unused")
        private InitialTreeInfo() {
            this(0, 0, 0, null, 0, null);
        }
    }


    /**
     * Updates the map's symmetry types
     * TODO: fix probable floating point errors
     */
    private void updateSymmetries() {
        // The different possible symmetries.

        boolean symVert, symHoriz, symRot, symNegDiag, symPosDiag;

        symVert = true;
        symHoriz = true;
        symRot = true;
        symNegDiag = height == width; // across the line x=height-y
        symPosDiag = height == width; // across the line x=y

        // Next, we check if robots are symmetric.

        // The byLoc map is just to speed up robot lookup by-location.
        final MapLocation origin = new MapLocation(0, 0);
        final Map<MapLocation, InitialRobotInfo> byLocRobot =
                Arrays.stream(initialRobots).collect(Collectors.toMap(
                        (robot) -> robot.getLocation(origin),
                        (robot) -> robot
                ));

        for (MapLocation loc : byLocRobot.keySet()) {
            final InitialRobotInfo r1 = byLocRobot.get(loc);
            final float x = loc.x, y = loc.y;

            symVert = symVert && oppositeRobots(
                    r1,
                    byLocRobot.get(new MapLocation(x, height - y ))
            );
            symHoriz = symHoriz && oppositeRobots(
                    r1,
                    byLocRobot.get(new MapLocation(width - x , y))
            );
            symRot = symRot && oppositeRobots(
                    r1,
                    byLocRobot.get(new MapLocation(width - x , height - y ))
            );
            if (width == height) {
                symNegDiag = symNegDiag && oppositeRobots(
                        r1,
                        byLocRobot.get(new MapLocation(height - y , width - x ))
                );
                symPosDiag = symPosDiag && oppositeRobots(
                        r1,
                        byLocRobot.get(new MapLocation(y, x))
                );
            }
        }

        // Next, we check if trees are symmetric.

        // The byLoc map is just to speed up robot lookup by-location.
        final Map<MapLocation, InitialTreeInfo> byLocTree = 
                Arrays.stream(initialTrees).collect(Collectors.toMap(
                        (tree) -> tree.getLocation(origin),
                        (tree) -> tree
                ));

        for (MapLocation loc : byLocTree.keySet()) {
            final InitialTreeInfo r1 = byLocTree.get(loc);
            final float x = loc.x, y = loc.y;

            symVert = symVert && oppositeTrees(
                    r1,
                    byLocTree.get(new MapLocation(x, height - y ))
            );
            symHoriz = symHoriz && oppositeTrees(
                    r1,
                    byLocTree.get(new MapLocation(width - x , y))
            );
            symRot = symRot && oppositeTrees(
                    r1,
                    byLocTree.get(new MapLocation(width - x , height - y ))
            );
            if (width == height) {
                symNegDiag = symNegDiag && oppositeTrees(
                        r1,
                        byLocTree.get(new MapLocation(height - y , width - x ))
                );
                symPosDiag = symPosDiag && oppositeTrees(
                        r1,
                        byLocTree.get(new MapLocation(y, x))
                );
            }
        }


        this.symmetry = null;

        if (symVert) {
            this.symmetry = Symmetry.VERTICAL;
        }
        if (symHoriz) {
            if (this.symmetry != null) {
                Server.warn("Multiple map symmetries on "+mapName+", using "+this.symmetry);
                return;
            }

            this.symmetry = Symmetry.HORIZONTAL;
        }

        if (symRot) {
            if (this.symmetry != null) {
                Server.warn("Multiple map symmetries on "+mapName+", using "+this.symmetry);
                return;
            }

            this.symmetry = Symmetry.ROTATIONAL;
        }

        if (symNegDiag) {
            if (this.symmetry != null) {
                Server.warn("Multiple map symmetries on "+mapName+", using "+this.symmetry);
                return;
            }

            this.symmetry = Symmetry.NEGATIVE_DIAGONAL;
        }

        if (symPosDiag) {
            if (this.symmetry != null) {
                Server.warn("Multiple map symmetries on "+mapName+", using "+this.symmetry);
                return;
            }

            this.symmetry = Symmetry.POSITIVE_DIAGONAL;
        }

        if (this.symmetry == null) {
            this.symmetry = Symmetry.NONE;
        }
    }
    
    /**
     * @return True if the map is symmetric in some way.
     */
    @JsonIgnore
    public boolean isTournamentLegal() {
        computeLazyValues();

        // Make sure the map has some sort of symmetry
        return symmetry != Symmetry.NONE;
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

        return r1.team.opponent() == r2.team;
    }

    /**
     * Return whether two trees are considered "opposite",
     * for the purpose of tournament legality.
     *
     * @param t1 the first tree
     * @param t2 the second tree
     * @return whether the trees are legal
     */
    private boolean oppositeTrees(InitialTreeInfo t1, InitialTreeInfo t2) {
        if (t1 == null || t2 == null) {
            return false;
        }

        if (t1.radius != t2.radius) {
            return false;
        }

        return t1.team.opponent() == t2.team;
    }

    /**
     * Computes symmetries.
     */
    private synchronized void computeLazyValues() {
        if (this.symmetry == null) {
            updateSymmetries();
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
        this.seed = 0;
        this.rounds = 0;
        this.mapName = null;
        this.initialRobots = null;
        this.initialTrees = null;
    }
}
