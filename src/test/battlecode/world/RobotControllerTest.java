package battlecode.world;

import battlecode.common.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for RobotController. These are where the gameplay tests are.
 *
 * Using TestGame and TestMapBuilder as helpers.
 */
public class RobotControllerTest {
    public final double EPSILON = 1.0e-9;

    /**
     * Tests the most basic methods of RobotController. This test has extra
     * comments to serve as an example of how to use TestMapBuilder and
     * TestGame.
     *
     * @throws GameActionException shouldn't happen
     */
    @Test
    public void testBasic() throws GameActionException {
        // Prepares a map with the following properties:
        // origin = [0,0], width = 10, height = 10, num rounds = 100
        // random seed = 1337
        // The map doesn't have to meet specs.
        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 10, 10, 1337, 100)
            .build();

        // This creates the actual game.
        TestGame game = new TestGame(map);

        // Let's spawn a robot for each team. The integers represent IDs.
        float oX = game.getOriginX();
        float oY = game.getOriginY();
        final int archonA = game.spawn(oX + 3, oY + 3, RobotType.ARCHON, Team.A);
        final int soldierB = game.spawn(oX + 1, oY + 1, RobotType.SOLDIER, Team
                .B);
        InternalRobot archonABot = game.getBot(archonA);

        assertEquals(archonABot.getLocation(), new MapLocation(oX + 3, oY + 3));

        // The following specifies the code to be executed in the next round.
        // Bytecodes are not counted, and yields are automatic at the end.
        game.round((id, rc) -> {
            if (id == archonA) {
                rc.move(Direction.getEast(), 0.5F);
            } else if (id == soldierB) {
                // do nothing
            }
        });

        // Let's assert that things happened properly.
        assertEquals(archonABot.getLocation(), new MapLocation(oX + 5, oY + 3));

        // Lets wait for 10 rounds go by.
        game.waitRounds(10);

        // hooray!
    }

    /**
     * Ensure that actions take place immediately.
     */
    @Test
    public void testImmediateActions() throws GameActionException {
        LiveMap map= new TestMapBuilder("test", 0, 0, 100, 100, 1337, 1000).build();
        TestGame game = new TestGame(map);

        final int a = game.spawn(1, 1, RobotType.SOLDIER, Team.A);

        game.round((id, rc) -> {
            if (id != a) return;

            final MapLocation start = rc.getLocation();
            assertEquals(start, new MapLocation(1, 1));

            rc.move(Direction.getEast());

            final MapLocation newLocation = rc.getLocation();
            assertEquals(newLocation, new MapLocation(3, 1));
        });

        // Let delays go away
        game.waitRounds(10);
    }
}
