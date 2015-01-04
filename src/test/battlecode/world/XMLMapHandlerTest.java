package battlecode.world;

import static org.junit.Assert.*;
import org.junit.*;

import java.common.*;

public class XMLMapHandlerTest {

    GameMap getGameMap(String mapName) {
        XMLMapHandler handler = XMLMapHandler.loadMap(mapName, "src/test/battlecode/world/maps");
        long[][] teamMemory = new long[2][GameConstants.TEAM_MEMORY_LENGTH];
        GameWorld gw = handler.createGameWorld("team A", "team B", teamMemory);
    }

    @Test
    public void testBasic() {
        // load a valid map
        
    }
}
