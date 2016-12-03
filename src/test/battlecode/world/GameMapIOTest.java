package battlecode.world;

import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TreeInfo;
import battlecode.server.TeamMapping;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class GameMapIOTest {

    @Test
    public void testClearMap() throws IOException {
        TeamMapping teamMapping = new TeamMapping("teamA", "teamB");
//        TestMapBuilder mapBuilder = new TestMapBuilder("clearMap", 0, 0, 50, 50, 128);
//        battlecode.schema.GameMap actualMap = mapBuilder.build();
//        mapBuilder.saveMap("test/battlecode/world/testmaps/clearMap.txt");

        GameMap readMap = GameMapIO.loadMap("test/battlecode/world/testmaps/clearMap.txt", teamMapping);
        assertEquals(readMap.getMapName(), "clearMap");
        assertEquals(readMap.getHeight(), 50.0, 0);
        assertEquals(readMap.getWidth(), 50.0, 0);
        assertEquals(readMap.getSeed(), 128);
        assertEquals(readMap.getOrigin().x, 0.0, 0);
        assertEquals(readMap.getOrigin().y, 0.0, 0);
    }

    @Test
    public void testDefaultNeutralTreesMap() throws IOException {
        TeamMapping teamMapping = new TeamMapping("teamA", "teamB");
//        TestMapBuilder mapBuilder = new TestMapBuilder("neutralTreeMap", 0, 0, 50, 50, 128);
//        mapBuilder.addNeutralTree(1, 10, 10, 5, 100, BodyType.BULLET);
//        mapBuilder.addNeutralTree(2, 25, 25, 7, 0, BodyType.ARCHON);
//        battlecode.schema.GameMap actualMap = mapBuilder.build();
//        mapBuilder.saveMap("test/battlecode/world/testmaps/neutralTreeMap.txt");

        GameMap readMap = GameMapIO.loadMap("test/battlecode/world/testmaps/neutralTreeMap.txt", teamMapping);

        // Test general map stuff
        assertEquals(readMap.getMapName(), "neutralTreeMap");
        assertEquals(readMap.getHeight(), 50.0, 0);
        assertEquals(readMap.getWidth(), 50.0, 0);
        assertEquals(readMap.getSeed(), 128);
        assertEquals(readMap.getOrigin().x, 0.0, 0);
        assertEquals(readMap.getOrigin().y, 0.0, 0);

        // Test body one
        TreeInfo tree1 = (TreeInfo) readMap.getInitialBodies()[0];
        assertEquals(tree1.getLocation().x, 10.0, 0);
        assertEquals(tree1.getLocation().y, 10.0, 0);
        assertEquals(tree1.containedBullets, 100);
        assertEquals(tree1.getRadius(), 5.0, 0);
        assertEquals(tree1.containedRobot, null);
        assertEquals(tree1.team, Team.NEUTRAL);
        assertEquals(tree1.ID, 1);

        // Test body two
        TreeInfo tree2 = (TreeInfo) readMap.getInitialBodies()[1];
        assertEquals(tree2.getLocation().x, 25.0, 0);
        assertEquals(tree2.getLocation().y, 25.0, 0);
        assertEquals(tree2.containedBullets, 0);
        assertEquals(tree2.getRadius(), 7.0, 0);
        assertEquals(tree2.containedRobot, RobotType.ARCHON);
        assertEquals(tree2.team, Team.NEUTRAL);
        assertEquals(tree2.ID, 2);
    }

    @Test
    public void testDefaultBodiesMap() throws IOException {
        TeamMapping teamMapping = new TeamMapping("teamA", "teamB");
//        TestMapBuilder mapBuilder = new TestMapBuilder("neutralBodyMap", 0, 0, 100, 100, 128);
//        mapBuilder.addBody(1, teamMapping.getIDFromTeam(Team.A), BodyType.ARCHON, 10, 10);
//        mapBuilder.addBody(2, teamMapping.getIDFromTeam(Team.B), BodyType.GARDENER, 30, 30);
//        battlecode.schema.GameMap actualMap = mapBuilder.build();
//        mapBuilder.saveMap("test/battlecode/world/testmaps/neutralBodyMap.txt");

        GameMap readMap = GameMapIO.loadMap("test/battlecode/world/testmaps/neutralBodyMap.txt", teamMapping);

        // Test general map stuff
        assertEquals(readMap.getMapName(), "neutralBodyMap");
        assertEquals(readMap.getHeight(), 100.0, 0);
        assertEquals(readMap.getWidth(), 100.0, 0);
        assertEquals(readMap.getSeed(), 128);
        assertEquals(readMap.getOrigin().x, 0.0, 0);
        assertEquals(readMap.getOrigin().y, 0.0, 0);

        // Test body one
        RobotInfo robot1 = (RobotInfo) readMap.getInitialBodies()[0];
        assertEquals(robot1.getLocation().x, 10.0, 0);
        assertEquals(robot1.getLocation().y, 10.0, 0);
        assertEquals(robot1.type, RobotType.ARCHON);
        assertEquals(robot1.team, Team.A);
        assertEquals(robot1.ID, 1);

        // Test body two
        RobotInfo robot2 = (RobotInfo) readMap.getInitialBodies()[1];
        assertEquals(robot2.getLocation().x, 30.0, 0);
        assertEquals(robot2.getLocation().y, 30.0, 0);
        assertEquals(robot2.type, RobotType.GARDENER);
        assertEquals(robot2.team, Team.B);
        assertEquals(robot2.ID, 2);
    }

    @Test
    public void testDefaultDirectoryLoading() throws IOException {
        TeamMapping teamMapping = new TeamMapping("teamA", "teamB");
//        TestMapBuilder mapBuilder = new TestMapBuilder("clearMap", 0, 0, 50, 50, 128);
//        battlecode.schema.GameMap actualMap = mapBuilder.build();
//        mapBuilder.saveMap("main/battlecode/world/resources/clearMap.txt");

        GameMap readMap = GameMapIO.loadMap("clearMap.txt", null, teamMapping);
        assertEquals(readMap.getMapName(), "clearMap");
        assertEquals(readMap.getHeight(), 50.0, 0);
        assertEquals(readMap.getWidth(), 50.0, 0);
        assertEquals(readMap.getSeed(), 128);
        assertEquals(readMap.getOrigin().x, 0.0, 0);
        assertEquals(readMap.getOrigin().y, 0.0, 0);
    }

}
