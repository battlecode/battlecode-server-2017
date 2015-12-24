package battlecode.world;

import battlecode.common.RobotType;
import battlecode.common.Team;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertTrue;

public class GameMapIOTest {
    /**
     * Writes a map to a file and then reads it. A basic test to make sure
     * GameMapIO reads map files properly.
     *
     * @throws IOException shouldn't happen.
     */
    @Test(timeout=5000)
    public void testRoundTrip() throws IOException {
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

        final GameMap inputMap = gen.getMap("basicMap");

        final File tempDir = Files.createTempDirectory("battlecode-test").toFile();
        tempDir.deleteOnExit();

        GameMapIO.writeMap(inputMap, tempDir.toString());

        final GameMap outputMap = GameMapIO.loadMap("basicMap", tempDir.toString());

        assertTrue(inputMap.equals(outputMap));
    }
}
