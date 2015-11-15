package battlecode.world;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.ZombieCount;
import org.junit.Ignore;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.StringTokenizer;

/**
 * A class that creates instances of GameMap, mostly to be used for testing
 * purposes. Can also print a map to a file.
 */
@Ignore
public class TestMapGenerator {
    /**
     * A class to hold information about a specific robot that is on the map.
     */
    private class RobotInfo {
        /** The type of the robot. */
        public final RobotType type;
        /** The team of the robot. */
        public final Team team;
        /** The location of the robot. */
        public final MapLocation location;

        /**
         * Creates a new RobotInfo with the given information.
         *
         * @param type type of the robot.
         * @param team team of the robot.
         * @param location location of the robot.
         */
        public RobotInfo(RobotType type, Team team, MapLocation location) {
            this.type = type;
            this.team = team;
            this.location = location;
        }
    }

    public static String getSymbol(RobotType type) {
        switch (type) {
            case ZOMBIEDEN: return "z";
            case STANDARDZOMBIE: return "s";
            case RANGEDZOMBIE: return "r";
            case FASTZOMBIE: return "f";
            case BIGZOMBIE: return "b";
            case ARCHON: return "a";
            case SCOUT: return "c";
            case SOLDIER: return "o";
            case GUARD: return "g";
            case VIPER: return "v";
            case TURRET: return "t";
            case TTM: return "m";
            default: return "x";
        }
    }

    public static String getSymbol(Team team) {
        switch (team) {
            case A: return "a";
            case B: return "b";
            case NEUTRAL: return "n";
            case ZOMBIE: return "z";
            default: return "n";
        }
    }

    /**
     * For the purposes of this map generator, gives a unique String for
     * each (robot, team) combination.
     *
     * p will be parts.
     * r will be rubble.
     *
     * Lower case will be used for team A and upper case will be used for
     * team B.
     *
     * @param type type of the robot.
     * @param team team of the robot.
     * @return a unique String to represent the (robot, team) for map
     * writing purposes.
     */
    public static String getSymbol(RobotType type, Team team) {
        return getSymbol(type) + getSymbol(team);
    }

    /** Width of the map. */
    private Integer width;
    /** Height of the map. */
    private Integer height;
    /** Random seed used for the map. Defaults to GameMap.GAME_DEFAULT_SEED. */
    private Integer seed;
    /** Number of rounds for the game. */
    private Integer rounds;
    /** The rubble on the map. Defaults to no rubble. x is the first index. */
    private int[][] rubble;
    /** The parts on the map. Defaults to no parts. x is the first index. */
    private int[][] parts;
    /** The map's zombie spawn schedule. Defaults to no zombies. */
    private ZombieSpawnSchedule zSchedule;
    /** All the robots on the map. Defaults to none. */
    private ArrayList<RobotInfo> robots;

    /**
     * Prepares an empty map of the given size. There will be no parts or rubble.
     *
     * @param width map width
     * @param height map height
     * @param rounds the number of rounds
     */
    public TestMapGenerator(int width, int height, int rounds) {
        this.width = width;
        this.height = height;
        this.seed = GameMap.GAME_DEFAULT_SEED;
        this.rounds = rounds;
        this.rubble = new int[width][height];
        this.parts = new int[width][height];
        this.zSchedule = new ZombieSpawnSchedule();
        this.robots = new ArrayList<>();
    }

    /**
     * Prepares a map based on the given input string. Format the string like you would in a map XML file.
     * Use 'n' to denote a rubble tile and 'p' to denote a parts tile.
     * Separate rows with newlines.
     *
     * Example: "n0 n0 n0 n0 n0\nn0 n0 n0 n0 p10" will make a 2x5 map with no ore anywhere except for the bottom right.
     * All the tiles will have no rubble and the bottom right tile will have 10 parts.
     *
     * @param mapStr a string to describe the map
     * @param rounds the number of rounds for a game
     */
    public TestMapGenerator(String mapStr, int rounds) {
        String[] map = mapStr.split("\n");
        this.height = map.length;
        this.width = 1;
        for (int i = 0; i < map[0].length(); ++i) {
            if (map[0].charAt(i) == ' ') {
                this.width++;
            }
        }

        this.seed = GameMap.GAME_DEFAULT_SEED;
        this.rounds = rounds;
        this.parts = new int[width][height];
        this.rubble = new int[width][height];
        for (int i = 0; i < height; ++i) {
            StringTokenizer st = new StringTokenizer(map[i]);
            for (int j = 0; j < width; ++j) {
                String next = st.nextToken();
                int value = Integer.parseInt(next.substring(1));
                this.rubble[j][i] = next.startsWith("n") ? value : 0;
                this.parts[j][i] = next.startsWith("p") ? value : 0;
            }
        }

        this.zSchedule = new ZombieSpawnSchedule();
        this.robots = new ArrayList<>();
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
     * Adds a robot to the map and returns itself.
     *
     * @param type the type of the robot
     * @param team the team the robot belongs to
     * @param x x location for the robot
     * @param y y location for the robot
     * @return itself, after the robot has been added
     */
    public TestMapGenerator withRobot(RobotType type, Team team, int x, int y) {
        this.robots.add(new RobotInfo(type, team, new MapLocation(x, y)));
        return this;
    }

    /**
     * Adds a robot to the map.
     *
     * @param type the type of the robot
     * @param team the team the robot belongs to
     * @param x x location for the robot
     * @param y y location for the robot
     */
    public void addRobot(RobotType type, Team team, int x, int y) {
        this.robots.add(new RobotInfo(type, team, new MapLocation(x, y)));
    }

    /**
     * Generates the game map represented by this generator.
     *
     * @param mapName the name to give the map
     * @return the game map represented by this generator
     */
    public GameMap getMap(String mapName) {
        EnumMap<GameMap.MapProperties, Integer> props = new EnumMap<GameMap.MapProperties, Integer>(GameMap.MapProperties.class);
        props.put(GameMap.MapProperties.ROUNDS, this.rounds);
        props.put(GameMap.MapProperties.SEED, this.seed);
        return new GameMap(props, rubble, parts, zSchedule, mapName);
    }

    /**
     * All test maps will be placed in this folder.
     */
    public static final String MAP_PATH =
            "src/test/battlecode/world/maps/";

    public void writeMapToFile(String mapName) throws IOException {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter
                (MAP_PATH + mapName + ".xml")), true);
        out.printf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        out.printf("<map height=\"%d\" width=\"%d\">\n", height, width);
        out.printf("    <game seed=\"%d\" rounds=\"%d\"/>\n", seed, rounds);

        // Print zombie schedule
        for (int round : zSchedule.getRounds()) {
            for (ZombieCount zc : zSchedule.getScheduleForRound(round)) {
                out.printf("    <zombies round=\"%d\" type=\"%s\" " +
                        "count=\"%d\" />\n", round, zc.getType().toString(),
                        zc.getCount());
            }
        }

        out.printf("    <symbols>\n");
        out.printf("        <symbol terrain=\"RUBBLE\" type=\"TERRAIN\" " +
                "character=\"%s\"/>\n", "r");
        out.printf("        <symbol terrain=\"PARTS\" type=\"TERRAIN\" " +
                "character=\"%s\"/>\n", "p");

        for (RobotType type : RobotType.values()) {
            for (Team team : Team.values()) {
                out.printf("        <symbol team=\"%s\" type=\"%s\" " +
                        "character=\"%s\" />\n", team.toString(), type
                        .toString(), getSymbol(type, team));
            }
        }

        out.printf("    </symbols>\n");
        out.printf("    <data>\n");
        out.printf("<![CDATA[\n");

        String[][] characters = new String[width][height];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                if (parts[j][i] > 0) characters[j][i] = "p";
                else characters[j][i] = "r";
            }
        }
        for (RobotInfo robot : robots) {
            characters[robot.location.x][robot.location.y] = getSymbol(robot
                    .type, robot.team);
        }
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                out.printf("%s", characters[j][i]);
                if (parts[j][i] > 0) out.printf("%d", parts[j][i]);
                else out.printf("%d", rubble[j][i]);

                if (j == width - 1) out.printf("\n");
                else out.printf(" ");
            }
        }

        out.printf("]]>\n");
        out.printf("    </data>\n");
        out.printf("</map>\n");

        out.close();
    }
}
