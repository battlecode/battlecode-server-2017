package battlecode.world;

import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.server.Config;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class XMLMapHandlerTest {

    /**
     * Reads a game map from file given the team memory and the name of the
     * map. Will use the map math specified in TestMapGenerator.
     *
     * @param teamMemory team memory to pass in with the given map.
     * @param mapName name of the map.
     * @return a GameWorld with this map.
     */
    public GameWorld getMap(long[][] teamMemory, String mapName) {
        XMLMapHandler handler = XMLMapHandler.loadMap(mapName,
                TestMapGenerator.MAP_PATH);

        return handler.createGameWorld("", "", teamMemory);
    }

    /**
     * Sets unit-test-mode so that the engine will not try to instrument any
     * player code. It's not necessary for these tests.
     */
    @Before
    public void prepare() {
        Config.getGlobalConfig().setBoolean("bc.engine.unit-test-mode", true);
    }

    /**
     * Writes a map to a file and then reads it. A basic test to make sure
     * XMLMapHandler reads map files properly.
     *
     * @throws IOException shouldn't happen.
     */
    @Test(timeout=5000)
    public void testBasic() throws IOException {
        int width = 50;
        int height = 80;
        int rounds = 2123;
        int seed = 1234;

        TestMapGenerator gen = new TestMapGenerator(width, height, rounds)
                .withSeed(seed)
                .withRubble(1, 1, 100)
                .withRubble(48, 78, 100)
                .withParts(0, 1, 111)
                .withParts(1, 0, 111)
                .withParts(49, 78, 111)
                .withParts(48, 79, 111)
                .withZombieSpawn(100, RobotType.FASTZOMBIE, 12)
                .withZombieSpawn(500, RobotType.RANGEDZOMBIE, 15)
                .withRobot(RobotType.ARCHON, Team.A, 0, 0)
                .withRobot(RobotType.ARCHON, Team.B, 49, 79)
                .withRobot(RobotType.ZOMBIEDEN, Team.ZOMBIE, 0, 79)
                .withRobot(RobotType.ZOMBIEDEN, Team.ZOMBIE, 49, 0);

        gen.writeMapToFile("basicMap");

        GameMap inputMap = gen.getMap("basicMap");

        long[][] teamMemory = new long[2][32];
        GameWorld world = getMap(teamMemory, "basicMap");
        GameMap outputMap = world.getGameMap();

        assertTrue(inputMap.equivalentTo(outputMap));
    }
}
