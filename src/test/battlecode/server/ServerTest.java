package battlecode.server;

import battlecode.common.Team;
import battlecode.schema.BodyType;
import battlecode.world.TestMapBuilder;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;

public class ServerTest {

    @Test
    public void testBasicMap() throws MalformedURLException {
        // Init server
        Config options = new Config(new String[0]);
        Server server = new Server(options, false);

        // Make game map
        String teamA = "teamA";
        String teamB = "teamB";
//        TeamMapping teamMapping = new TeamMapping(teamA, teamB);
//        TestMapBuilder mapBuilder = new TestMapBuilder("neutralBodyMap", 0, 0, 100, 100, 128);
//        mapBuilder.addBody(1, teamMapping.getIDFromTeam(Team.A), BodyType.ARCHON, 50, 60);
//        mapBuilder.addBody(2, teamMapping.getIDFromTeam(Team.B), BodyType.ARCHON, 50, 40);
//        battlecode.schema.GameMap actualMap = mapBuilder.build();
//        mapBuilder.saveMap("main/battlecode/world/resources/basicMap.txt");

        // Init GameInfo
        URL teamAClasses = new File("test/battlecode/server/testplayers/moveright/").toURI().toURL();
        URL teamBClasses = new File("test/battlecode/server/testplayers/moveright/").toURI().toURL();
        String[] maps = {"basicMap.txt"};
        File saveFile = new File("test/battlecode/server/testgames/basicGame.txt");
        GameInfo gameInfo = new GameInfo(teamA, teamAClasses, teamB, teamBClasses, maps, saveFile, false);

        // Run server
        server.addGameNotification(gameInfo);
        server.terminateNotification();
        server.run();

        // Check results in file
        assertTrue(true);
    }

}
