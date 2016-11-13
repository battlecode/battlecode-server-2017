package battlecode.world;

import battlecode.server.TeamMapping;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class GameMapIOTest {

    @Test
    public void testClearMap() throws IOException {
        TeamMapping teamMapping = new TeamMapping("teamA", "teamB");
        TestMapBuilder mapBuilder = new TestMapBuilder("clearMap", 0, 0, 50, 50, 128);
        battlecode.schema.GameMap actualMap = mapBuilder.build();
        mapBuilder.saveMap("test/battlecode/world/testmaps/clearMap.txt");

        GameMap readMap = GameMapIO.loadMap("test/battlecode/world/testmaps/clearMap.txt", teamMapping);
        assertEquals(readMap.getMapName(), "clearMap");
        assertEquals(readMap.getHeight(), 50.0, 0);
        assertEquals(readMap.getWidth(), 50.0, 0);
        assertEquals(readMap.getSeed(), 128);
        assertEquals(readMap.getOrigin().x, 0.0, 0);
        assertEquals(readMap.getOrigin().y, 0.0, 0);
    }

}
