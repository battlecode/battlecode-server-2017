package battlecode.world;

import battlecode.common.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class GameMapIOTest {

    final static ClassLoader loader = GameMapIOTest.class.getClassLoader();

    @Test
    public void testFindsDefaultMap() throws IOException {
        // will throw exception if default map can't be loaded
        GameMapIO.loadMap("shrine", null);
    }

    @Test
    public void testFindsPackageMap() throws IOException {
        LiveMap readMap = GameMapIO.loadMapAsResource(loader,
                "battlecode/world/resources", "clearMap");
        assertEquals(readMap.getMapName(), "clearMap");
        assertEquals(readMap.getHeight(), 50.0, 0);
        assertEquals(readMap.getWidth(), 50.0, 0);
        assertEquals(readMap.getSeed(), 128);
        assertEquals(readMap.getOrigin().x, 0.0, 0);
        assertEquals(readMap.getOrigin().y, 0.0, 0);
    }

    @Test
    public void testFindsFsMap() throws IOException {
        final File tempDir = Files.createTempDirectory("battlecode-test").toFile();
        tempDir.deleteOnExit();

        LiveMap inputMap = new TestMapBuilder("simple", new MapLocation(1, 2), 30, 40, 222, 1000)
                .addNeutralTree(0, new MapLocation(24, 25), 2, 0, RobotType.TANK)
                .addRobot(1, Team.A, RobotType.SCOUT, new MapLocation(30, 40))
                .build();

        GameMapIO.writeMap(inputMap, tempDir);

        assertTrue("Map not in directory", GameMapIO.getAvailableMaps(tempDir).contains("simple"));

        final LiveMap outputMap = GameMapIO.loadMap("simple", tempDir);

        assertEquals("Maps don't match", inputMap, outputMap);

    }

    @Test
    public void testRoundTrip() throws IOException {
        LiveMap inputMap = new TestMapBuilder("simple", new MapLocation(1, 2), 30, 40, 222, 1000)
                .addNeutralTree(0, new MapLocation(24, 25), 2, 3, RobotType.TANK)
                .addRobot(1, Team.A, RobotType.SCOUT, new MapLocation(30, 40))
                .addRobot(3, Team.NEUTRAL, RobotType.ARCHON, new MapLocation(12, 13))
                .build();

        LiveMap outputMap = GameMapIO.Serial.deserialize(GameMapIO.Serial.serialize(inputMap));

        assertEquals("Round trip failed", inputMap, outputMap);
    }


}
