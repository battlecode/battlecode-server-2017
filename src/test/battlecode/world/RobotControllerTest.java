package battlecode.world;

import battlecode.common.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for RobotController.
 *
 * Using TestGame and TestMapGenerator as helpers.
 */
public class RobotControllerTest {
    public final double EPSILON = 1.0e-9;

    /**
     * Tests the most basic methods of RobotController. This test has extra comments to serve as an example of how to
     * use TestMapGenerator and TestGame.
     */
    @Test
    public void testBasic() {
        // Create a 10x10 map with 5 ore on every tile, and make a game using that map.
        TestMapGenerator gen = new TestMapGenerator(10, 10, 5);
        TestGame game = new TestGame(gen.getMap());

        // The map is randomly offset, so we'll need to get these offset values.
        int oX = game.getOriginX();
        int oY = game.getOriginY();

        // Now let's add two HQs on the map and save their IDs.
        final int hqA = game.spawn(oX, oY, RobotType.HQ, Team.A);
        final int hqB = game.spawn(9 + oX, 9 + oY, RobotType.HQ, Team.B);

        // This executes 1 round of the game.
        game.round((id, rc) -> {
            if (id == hqA) { // Team A HQ will execute this first.
                // Verify your ability to sense HQ locations.
                MapLocation myHQ = rc.senseHQLocation();
                assertEquals(myHQ, new MapLocation(oX, oY));
                MapLocation enemyHQ = rc.senseEnemyHQLocation();
                assertEquals(enemyHQ, new MapLocation(9 + oX, 9 + oY));

                // Make sure your team has starting ore.
                assertEquals(rc.getTeamOre(), GameConstants.ORE_INITIAL_AMOUNT, EPSILON);

                // Make sure you spawned with the right properties.
                assertEquals(rc.getTeam(), Team.A);
                assertEquals(rc.getHealth(), RobotType.HQ.maxHealth, EPSILON);
                assertEquals(rc.getLocation(), new MapLocation(oX, oY));
            } else if (id == hqB) { // Team B HQ will execute this second.
                // Verify your ability to sense HQ locations.
                MapLocation myHQ = rc.senseHQLocation();
                assertEquals(myHQ, new MapLocation(9 + oX, 9 + oY));
                MapLocation enemyHQ = rc.senseEnemyHQLocation();
                assertEquals(enemyHQ, new MapLocation(oX, oY));

                // Make sure your team has starting ore.
                assertEquals(rc.getTeamOre(), GameConstants.ORE_INITIAL_AMOUNT, EPSILON);

                // Make sure you spawned with the right properties.
                assertEquals(rc.getTeam(), Team.B);
                assertEquals(rc.getHealth(), RobotType.HQ.maxHealth, EPSILON);
                assertEquals(rc.getLocation(), new MapLocation(9 + oX, 9 + oY));
            }
        });

        // game.round will automatically process actions that happen at the beginning and end of turns.

        // This executes the next round of the game. game.turn will automatically have every robot except the
        // specified bot execute an empty turn (do nothing). Use game.turn when you only want one robot to perform
        // actions.
        game.turn(hqA, rc -> {
            // Verify that you gained some ore.
            assertEquals(rc.getTeamOre(), GameConstants.ORE_INITIAL_AMOUNT + GameConstants.HQ_ORE_INCOME, EPSILON);
        });
    }
}
