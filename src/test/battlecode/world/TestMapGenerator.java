package battlecode.world;

import battlecode.common.TerrainTile;

import java.io.File;
import java.util.EnumMap;
import java.util.StringTokenizer;

/**
 * A class that creates instances of GameMap, mostly to be used for testing purposes.
 */
public class TestMapGenerator {
    /** Width of the map. */
    private Integer width;
    /** Height of the map. */
    private Integer height;
    /** Random seed used for the map. Defaults to GameMap.GAME_DEFAULT_SEED. */
    private Integer seed;
    /** Maximum number of rounds for the game. Defaults to GameMap.GAME_DEFAULT_MAX_ROUNDS. */
    private Integer maxRounds;
    /** The terrain tiles of the map. Defaults to being normal everywhere. */
    private TerrainTile[][] tiles;
    /** The ore on the map. Defaults to no ore. */
    private int[][] ore;

    /**
     * Prepares an empty map of the given size. There will be no ore and all the tiles will be normal.
     *
     * @param width map width
     * @param height map height
     */
    public TestMapGenerator(int width, int height) {
        this(width, height, 0);
    }

    /**
     * Prepares an empty map of the given size. All the tiles will be normal. Each location will have the given amount
     * of ore.
     *
     * @param width map width
     * @param height map height
     * @param ore the ore for each location on the map
     */
    public TestMapGenerator(int width, int height, int ore) {
        this.width = width;
        this.height = height;
        this.seed = GameMap.GAME_DEFAULT_SEED;
        this.maxRounds = GameMap.GAME_DEFAULT_MAX_ROUNDS;
        this.tiles = new TerrainTile[this.width][this.height];
        for (int i = 0; i < this.width; ++i) {
            for (int j = 0; j < this.height; ++j) {
                this.tiles[i][j] = TerrainTile.NORMAL;
            }
        }
        this.ore = new int[this.width][this.height];
    }

    /**
     * Prepares a map based on the given input string. Format the string like you would in a map XML file.
     * Use 'n' to denote a normal tile and 'v' to denote a void tile. Separate rows with newlines.
     *
     * Example: "n0 n0 n0 n0 n0\nn0 n0 n0 n0 v10" will make a 2x5 map with no ore anywhere except for the bottom right.
     * All the tiles will be normal except for a void in the bottom right.
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
        this.tiles = new TerrainTile[height][width];
        this.ore = new int[height][width];
        for (int i = 0; i < height; ++i) {
            StringTokenizer st = new StringTokenizer(map[i]);
            for (int j = 0; j < width; ++j) {
                String next = st.nextToken();
                this.tiles[i][j] = next.startsWith("v") ? TerrainTile.VOID : TerrainTile.NORMAL;
                this.ore[i][j] = Integer.parseInt(next.substring(1));
            }
        }
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
     * Modifies a single ore value of this TestMapGenerator and returns itself.
     *
     * @param x the x coordinate of the ore value to change
     * @param y the y coordinate of the ore value to change
     * @param oreValue the new ore value
     * @return itself, after the ore value has been updated
     */
    public TestMapGenerator withOre(int x, int y, int oreValue) {
        this.ore[x][y] = oreValue;
        return this;
    }

    /**
     * Updates a single ore value.
     *
     * @param x the x coordinate of the ore value to change
     * @param y the y coordinate of the ore value to change
     * @param oreValue the new ore value
     */
    public void setOre(int x, int y, int oreValue) {
        this.ore[x][y] = oreValue;
    }

    /**
     * Modifies a single terrain tile of this TestMapGenerator and returns itself.
     *
     * @param x the x coordinate of the terrain tile to change
     * @param y the y coordinate of the terrain tile to change
     * @param terrain the new terrain tile
     * @return itself, after the terrain tile has been updated
     */
    public TestMapGenerator withTerrain(int x, int y, TerrainTile terrain) {
        this.tiles[x][y] = terrain;
        return this;
    }

    /**
     * Updates a single terrain tile.
     *
     * @param x the x coordinate of the terrain tile to change
     * @param y the y coordinate of the terrain tile to change
     * @param terrain the new terrain tile
     */
    public void setTerrain(int x, int y, TerrainTile terrain) {
        this.tiles[x][y] = terrain;
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
        return new GameMap(props, tiles, ore, "map");
    }
}
