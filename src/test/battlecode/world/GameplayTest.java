package battlecode.world;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.EnumMap;
import java.util.StringTokenizer;

import battlecode.common.*;

// Tests a bunch of gameplay scenarios and RobotController
public class GameplayTest {

    public final double EPSILON = 1.0e-9;

    public GameWorld getWorld(String map) {
        long[][] teamMemory = new long[2][GameConstants.TEAM_MEMORY_LENGTH];
        return getWorld(map, teamMemory);
    }

    public GameWorld getWorld(String mapstr, long[][] teamMemory) {
        String[] map = mapstr.split("\n");
        int height = map.length;
        int width = 1;
        for (int i = 0; i < map[0].length(); ++i) {
            if (map[0].charAt(i) == ' ') {
                width++;
            }
        } 

        EnumMap<GameMap.MapProperties, Integer> props = new EnumMap<GameMap.MapProperties, Integer>(GameMap.MapProperties.class);
        props.put(GameMap.MapProperties.MAX_ROUNDS, 2000);
        props.put(GameMap.MapProperties.SEED, 1337);

        String mapName = "map";
        int[][] ore = new int[height][width];
        TerrainTile[][] terrain = new TerrainTile[height][width];
        for (int i = 0; i < height; ++i) {
            StringTokenizer st = new StringTokenizer(map[i]);
            for (int j = 0; j < width; ++j) {
                String next = st.nextToken();
                terrain[i][j] = next.startsWith("v") ? TerrainTile.VOID : TerrainTile.NORMAL;
                ore[i][j] = Integer.parseInt(next.substring(1));
            }
        }

        GameMap m = new GameMap(props, terrain, ore, mapName);
        GameWorld world = new GameWorld(m, "", "", teamMemory);
        world.reserveRandomIDs(32000);
        return world;
    }

    public void start(GameWorld world) {
        world.processBeginningOfRound();
    }

    public void start(InternalRobot robot) {
        robot.processBeginningOfTurn();
    }

    public void end(InternalRobot robot) {
        robot.processEndOfTurn();
    }

    public void end(GameWorld world) {
        world.processEndOfRound();
    }

    @Test
    public void testBasic() {
        GameWorld world = getWorld("n0 n0 n0 n0 n0\nn0 n0 n0 n0 n0\nn0 n0 n0 n0 n0\nn0 n0 n0 n0 n0\nn0 n0 n0 n0 n0");
        MapLocation origin = world.getGameMap().getMapOrigin();

        // let's make an HQ
        InternalRobot hqa_bot = new InternalRobot(world, RobotType.HQ, origin, Team.A, false, 0);
        RobotControllerImpl hqa = new RobotControllerImpl(world, hqa_bot);
        world.setHQ(hqa_bot, Team.A);
        
        InternalRobot hqb_bot = new InternalRobot(world, RobotType.HQ, origin.add(1, 0), Team.B, false, 0);
        RobotControllerImpl hqb = new RobotControllerImpl(world, hqb_bot);
        world.setHQ(hqb_bot, Team.B);

        // start game
        start(world);

        // make sure HQ can sense itself
        start(hqa_bot);
        int id = hqa.getID();
        assertTrue(id >= 1 && id <= 32000);
        assertEquals(hqa.getTeam(), Team.A);
        assertEquals(hqa.getType(), RobotType.HQ);
        assertEquals(hqa.getLocation(), origin);
        assertEquals(hqa.getCoreDelay(), 0, EPSILON);
        assertEquals(hqa.getWeaponDelay(), 0, EPSILON);
        assertEquals(hqa.getHealth(), 2000, EPSILON);
        assertEquals(hqa.getSupplyLevel(), 0, EPSILON);
        assertEquals(hqa.senseHQLocation(), origin);
        assertEquals(hqa.senseEnemyHQLocation(), origin.add(1, 0));
        end(hqa_bot);

        start(hqb_bot);
        assertEquals(hqb.senseHQLocation(), origin.add(1, 0));
        assertEquals(hqb.senseEnemyHQLocation(), origin);
        end(hqb_bot);

        end(world);
    }

    public String duplicate(String line, int n) {
        String map = line;
        for (int i = 0; i < n - 1; ++i) {
            map = map + "\n" + line;
        }
        return map;
    }

    @Test
    public void testGetTeamOre() {
        GameWorld world = getWorld(duplicate("n0 n0 n0 n0 n0 n0 n0 n0 n0 n0 n0 n0 n0 n0 n0 n0 n0 n0 n0 n0", 20));
        MapLocation origin = world.getGameMap().getMapOrigin();

        InternalRobot hqa_bot = new InternalRobot(world, RobotType.HQ, origin, Team.A, false, 0);
        RobotControllerImpl hqa = new RobotControllerImpl(world, hqa_bot);
        world.setHQ(hqa_bot, Team.A);
        InternalRobot hqb_bot = new InternalRobot(world, RobotType.HQ, origin.add(19, 0), Team.B, false, 0);
        RobotControllerImpl hqb = new RobotControllerImpl(world, hqb_bot);
        world.setHQ(hqb_bot, Team.B);

        // start game
        start(world);

        start(hqa_bot);
        assertEquals(hqa.getTeamOre(), 500.0, EPSILON);
        end(hqa_bot);
        start(hqb_bot);
        assertEquals(hqb.getTeamOre(), 500.0, EPSILON);
        end(hqb_bot);
    
        end(world);

        start(world);

        start(hqa_bot);
        assertEquals(hqa.getTeamOre(), 505.0, EPSILON);
        end(hqa_bot);
        start(hqb_bot);
        assertEquals(hqb.getTeamOre(), 505.0, EPSILON);
        end(hqb_bot);
    
        end(world);

        start(world);

        start(hqa_bot);
        assertEquals(hqa.getTeamOre(), 510.0, EPSILON);
        end(hqa_bot);
        start(hqb_bot);
        assertEquals(hqb.getTeamOre(), 510.0, EPSILON);
        end(hqb_bot);
    
        end(world);
    }

    public void assertNoDuplicates(MapLocation[] locs) {
        for (int i = 0; i < locs.length; ++i) {
            for (int j = 0; j < i; ++j) {
                assertFalse(locs[i].equals(locs[j]));
            }
        }
    }

    @Test
    public void testSenseTowerLocations() {
        GameWorld world = getWorld(duplicate("n0 n0 n0 n0 n0 n0 n0 n0 n0 n0 n0 n0 n0 n0 n0 n0 n0 n0 n0 n0", 20));
        MapLocation origin = world.getGameMap().getMapOrigin();

        InternalRobot[] towera_bot = new InternalRobot[6];
        RobotControllerImpl[] towera = new RobotControllerImpl[6];
        for (int i = 0; i < 6; ++i) {
            towera_bot[i] = new InternalRobot(world, RobotType.HQ, origin.add(i + 1, 0), Team.A, false, 0);
            towera[i] = new RobotControllerImpl(world, towera_bot[i]);
            world.addTower(towera_bot[i], Team.A);
        }
        InternalRobot[] towerb_bot = new InternalRobot[5];
        RobotControllerImpl[] towerb = new RobotControllerImpl[5];
        for (int i = 0; i < 5; ++i) {
            towerb_bot[i] = new InternalRobot(world, RobotType.HQ, origin.add(i + 1, 1), Team.B, false, 0);
            towerb[i] = new RobotControllerImpl(world, towerb_bot[i]);
            world.addTower(towerb_bot[i], Team.B);
        }

        // start game
        start(world);

        for (int i = 0; i < 6; ++i) {
            start(towera_bot[i]);
            MapLocation[] towers = towera[i].senseTowerLocations();
            assertNoDuplicates(towers);
            assertEquals(towers.length, 6);
            for (int j = 0; j < 6; ++j) {
                assert(towers[j].x >= 1 && towers[j].x <= 6);
                assert(towers[j].y == 0);
            }
            MapLocation[] etowers = towera[i].senseEnemyTowerLocations();
            assertNoDuplicates(etowers);
            assertEquals(etowers.length, 5);
            for (int j = 0; j < 5; ++j) {
                assert(etowers[j].x >= 1 && etowers[j].x <= 5);
                assert(etowers[j].y == 1);
            }
            end(towera_bot[i]);
        }
        for (int i = 0; i < 5; ++i) {
            start(towerb_bot[i]);
            end(towerb_bot[i]);
        }
    
        end(world);
    }
}
