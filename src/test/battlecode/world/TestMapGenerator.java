package battlecode.world;

import battlecode.common.RobotType;

import java.io.File;
import java.util.EnumMap;
import java.util.StringTokenizer;

import org.junit.Ignore;

/**
 * A class that creates instances of GameMap, mostly to be used for testing purposes.
 */
@Ignore
public class TestMapGenerator {
    /** Width of the map. */
    private Integer width;
    /** Height of the map. */
    private Integer height;
    /** Random seed used for the map. Defaults to GameMap.GAME_DEFAULT_SEED. */
    private Integer seed;
    /** Maximum number of rounds for the game. Defaults to GameMap.GAME_DEFAULT_MAX_ROUNDS. */
    private Integer maxRounds;
    /** The rubble on the map. Defaults to no rubble. */
    private int[][] rubble;
    /** The parts on the map. Defaults to no parts. */
    private int[][] parts;
    /** The map's zombie spawn schedule. Defaults to no zombies. */
    private ZombieSpawnSchedule zSchedule;

    /**
     * Prepares an empty map of the given size. There will be no parts or rubble.
     *
     * @param width map width
     * @param height map height
     */
    public TestMapGenerator(int width, int height) {
        this.width = width;
        this.height = height;
        this.seed = GameMap.GAME_DEFAULT_SEED;
        this.maxRounds = GameMap.GAME_DEFAULT_MAX_ROUNDS;
        this.rubble = new int[width][height];
        this.parts = new int[width][height];
        this.zSchedule = new ZombieSpawnSchedule();
    }

    /**
     * Prepares a map based on the given input string. Format the string like you would in a map XML file.
     * Use 'n' to denote a rubble tile and 'p' to denote a parts tile. Separate rows with newlines.
     *
     * Example: "n0 n0 n0 n0 n0\nn0 n0 n0 n0 p10" will make a 2x5 map with no ore anywhere except for the bottom right.
     * All the tiles will have no rubble and the bottom right tile will have 10 parts.
     *
     * @param mapStr a string to describe the map
     */
    public TestMapGenerator(String mapStr) {
        String[] map = mapStr.split("\n");
        this.height = map.length;
        this.width = 1;
        for (int i = 0; i < map[0].length(); ++i) {
            if (map[0].charAt(i) == ' ') {
                this.width++;
            }
        }

        this.seed = GameMap.GAME_DEFAULT_SEED;
        this.maxRounds = GameMap.GAME_DEFAULT_MAX_ROUNDS;
        this.parts = new int[height][width];
        this.rubble = new int[height][width];
        for (int i = 0; i < height; ++i) {
            StringTokenizer st = new StringTokenizer(map[i]);
            for (int j = 0; j < width; ++j) {
                String next = st.nextToken();
                int value = Integer.parseInt(next.substring(1));
                this.rubble[i][j] = next.startsWith("n") ? value : 0;
                this.parts[i][j] = next.startsWith("p") ? value : 0;
            }
        }

        this.zSchedule = new ZombieSpawnSchedule();
    }

    /**
     * Modifies the random seed of this TestMapGenerator and returns itself.
     *
     * @param seed the new map seed
     * @return itself, after the map seed has been updated
     */
    public TestMapGenerator withSeed(int seed) {
        this.seed = seed;
        return this;
    }

    /**
     * Updates the seed.
     *
     * @param seed the new seed
     */
    public void setSeed(int seed) {
        this.seed = seed;
    }

    /**
     * Modifies a single parts value of this TestMapGenerator and returns itself.
     *
     * @param x the x coordinate of the parts value to change
     * @param y the y coordinate of the parts value to change
     * @param partsValue the new parts value
     * @return itself, after the parts value has been updated
     */
    public TestMapGenerator withParts(int x, int y, int partsValue) {
        this.parts[x][y] = partsValue;
        return this;
    }

    /**
     * Updates a single parts value.
     *
     * @param x the x coordinate of the parts value to change
     * @param y the y coordinate of the parts value to change
     * @param partsValue the new parts value
     */
    public void setParts(int x, int y, int partsValue) {
        this.parts[x][y] = partsValue;
    }

    /**
     * Modifies a single rubble value of this TestMapGenerator and returns itself.
     *
     * @param x the x coordinate of the rubble value to change
     * @param y the y coordinate of the rubble value to change
     * @param rubbleValue the new rubble value
     * @return itself, after the rubble value has been updated
     */
    public TestMapGenerator withRubble(int x, int y, int rubbleValue) {
        this.rubble[x][y] = rubbleValue;
        return this;
    }

    /**
     * Updates a single rubble value.
     *
     * @param x the x coordinate of the rubble value to change
     * @param y the y coordinate of the rubble value to change
     * @param rubbleValue the new rubble value
     */
    public void setRubble(int x, int y, int rubbleValue) {
        this.rubble[x][y] = rubbleValue;
    }

    /**
     * Adds to the zombie spawn schedule of this TestMapGenerator and returns itself.
     *
     * @param round the round to spawn the zombie at
     * @param type the type of zombie to spawn
     * @param count the number of zombies to spawn
     * @return itself, after the zombie spawn schedule has been updated
     */
    public TestMapGenerator withZombieSpawn(int round, RobotType type, int count) {
        this.zSchedule.add(round, type, count);
        return this;
    }

    /**
     * Adds to the zombie spawn schedule.
     *
     * @param round the round to spawn the zombie at
     * @param type the type of zombie to spawn
     * @param count the number of zombies to spawn
     */
    public void addZombieSpawn(int round, RobotType type, int count) {
        this.zSchedule.add(round, type, count);
    }

    /**
     * Generates the game map represented by this generator.
     *
     * @return the game map represented by this generator
     */
    public GameMap getMap() {
        EnumMap<GameMap.MapProperties, Integer> props = new EnumMap<GameMap.MapProperties, Integer>(GameMap.MapProperties.class);
        props.put(GameMap.MapProperties.MAX_ROUNDS, this.maxRounds);
        props.put(GameMap.MapProperties.SEED, this.seed);
        return new GameMap(props, rubble, parts, zSchedule, "map");
    }
}
