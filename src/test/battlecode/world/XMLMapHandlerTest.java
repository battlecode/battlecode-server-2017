package battlecode.world;

import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import battlecode.common.*;

// Right now this only makes sure that the GOOD maps load properly.
// There are no tests for BAD maps.
public class XMLMapHandlerTest {
    public final String MAP_NAME = "map";
    public final String MAP_PATH = "src/test/battlecode/world/maps/";

    public void writeMapToFile(int width, int height, int rounds, int seed, char normal, char voidtile, char hqa, char hqb, char towera, char towerb, char[][] terrain, int[][] ore) throws IOException {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(MAP_PATH + MAP_NAME + ".xml")), true);
        out.printf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        out.printf("<map height=\"%d\" width=\"%d\">\n", height, width);
        out.printf("    <game seed=\"%d\" rounds=\"%d\"/>\n", seed, rounds);
        out.printf("    <symbols>\n");
        out.printf("        <symbol terrain=\"NORMAL\" type=\"TERRAIN\" character=\"%c\"/>\n", normal);
        out.printf("        <symbol terrain=\"VOID\" type=\"TERRAIN\" character=\"%c\"/>\n", voidtile);
        out.printf("        <symbol team=\"A\" type=\"HQ\" character=\"%c\"/>\n", hqa);
        out.printf("        <symbol team=\"B\" type=\"HQ\" character=\"%c\"/>\n", hqb);
        out.printf("        <symbol team=\"A\" type=\"TOWER\" character=\"%c\"/>\n", towera);
        out.printf("        <symbol team=\"B\" type=\"TOWER\" character=\"%c\"/>\n", towerb);
        out.printf("    </symbols>\n");
        out.printf("    <data>\n");
        out.printf("<![CDATA[\n");

        // MAP HERE
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                out.printf("%c%d", terrain[i][j], ore[i][j]);
                if (j == width - 1) {
                    out.printf("\n");
                } else {
                    out.printf(" ");
                }
            }
        }
        // END MAP

        out.printf("]]>\n");
        out.printf("    </data>\n");
        out.printf("</map>\n");

        out.close();
    }

    public GameWorld getMap(long[][] teamMemory) {
        XMLMapHandler handler = XMLMapHandler.loadMap(MAP_NAME, MAP_PATH);

        return handler.createGameWorld("", "", teamMemory);
    }

    @Test(timeout=1000)
    // This map is actually not always legal due to random void generation
    public void testBasic() throws IOException {
        System.out.println("begin test basic");
        Random random = new Random(123456);

        int width = 50;
        int height = 80;
        int rounds = 2000;
        int seed = 1337;
        char normal = 'n';
        char voidtile = 'v';
        char hqa = 'a';
        char hqb = 'b';
        char towera = 'A';
        char towerb = 'B';

        char[][] terrain = new char[height][width];
        int[][] ore = new int[height][width];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                terrain[i][j] = random.nextInt() % 2 == 0 ? normal : voidtile;
                ore[i][j] = random.nextInt(10000);
            }
        }

        terrain[0][0] = hqa;
        terrain[0][width - 1] = hqb;

        terrain[10][10] = towera;
        terrain[10][width - 10 - 1] = towerb;

        terrain[15][15] = towera;
        terrain[15][width - 15 - 1] = towerb;

        terrain[22][width - 7 - 1] = towera;
        terrain[22][7] = towerb;

        terrain[height - 1][width - 1] = towera;
        terrain[height - 1][0] = towerb;

        writeMapToFile(width, height, rounds, seed, normal, voidtile, hqa, hqb, towera, towerb, terrain, ore);

        long[][] teamMemory = new long[2][GameConstants.TEAM_MEMORY_LENGTH];
        for (int i = 0; i < GameConstants.TEAM_MEMORY_LENGTH; ++i) {
            teamMemory[0][i] = random.nextLong();
            teamMemory[1][i] = random.nextLong();
        }

        GameWorld world = getMap(teamMemory);
        GameMap map = world.getGameMap();

        assertEquals(map.getWidth(), width);
        assertEquals(map.getHeight(), height);
        assertEquals(map.getMapName(), "map");
        assertEquals(map.getSeed(), seed);
        assertEquals(map.getMaxRounds(), rounds);

        int[][] mapOre = map.getInitialOreMatrix();
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                assertEquals(mapOre[j][i], (terrain[i][j] == 'v' ? 0 : ore[i][j]));
            }
        }

        TerrainTile[][] mapTerrain = map.getTerrainMatrix();
        int orx = map.getMapOrigin().x;
        int ory = map.getMapOrigin().y;
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                if (terrain[i][j] == voidtile) {
                    assertEquals(mapTerrain[j][i], TerrainTile.VOID);
                } else {
                    assertEquals(mapTerrain[j][i], TerrainTile.NORMAL);
                }
                if (terrain[i][j] == hqa || terrain[i][j] == hqb) {
                    Team team = terrain[i][j] == hqa ? Team.A : Team.B;
                    InternalRobot ir = (InternalRobot) world.getBaseHQ(team);
                    InternalRobot ir2 = (InternalRobot) world.getObject(new MapLocation(j + orx, i + ory));
                    assertEquals(ir2.type, RobotType.HQ);
                    assertEquals(ir2.getTeam(), team);
                    assertEquals(ir.getID(), ir2.getID());
                } else if (terrain[i][j] == towera || terrain[i][j] == towerb) {
                    Team team = terrain[i][j] == towera ? Team.A : Team.B;
                    InternalRobot ir2 = (InternalRobot) world.getObject(new MapLocation(j + orx, i + ory));
                    assertEquals(ir2.type, RobotType.TOWER);
                    assertEquals(ir2.getTeam(), team);
                }
            }
        }

        MapLocation[] alocs = world.senseTowerLocations(Team.A);
        MapLocation[] blocs = world.senseTowerLocations(Team.B);
        assertEquals(alocs.length, 4);
        assertEquals(blocs.length, 4);
        for (int i = 0; i < 4; ++i) {
            assertEquals(towera, terrain[alocs[i].y - ory][alocs[i].x - orx]);
            assertEquals(towerb, terrain[blocs[i].y - ory][blocs[i].x - orx]);
            for (int j = 0; j < i; ++j) {
                assertTrue(!alocs[i].equals(alocs[j]));
                assertTrue(!blocs[i].equals(blocs[j]));
            }
        }

        assertArrayEquals(world.getOldTeamMemory(), teamMemory);
    }
}
