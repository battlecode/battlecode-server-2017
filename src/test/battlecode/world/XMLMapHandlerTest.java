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
     * @param mapName name of the map.
     * @return a GameWorld with this map.
     */
    public GameMap getMap(String mapName) {
        XMLMapHandler handler = XMLMapHandler.loadMap(mapName,
                TestMapGenerator.MAP_PATH);

        return handler.getParsedMap();
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

        GameMap outputMap = getMap("basicMap");

        assertTrue(inputMap.equals(outputMap));
    }
}
