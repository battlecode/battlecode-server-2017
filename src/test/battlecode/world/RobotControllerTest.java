package battlecode.world;

import battlecode.common.*;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for RobotController. These are where the gameplay tests are.
 *
 * Using TestGame and TestMapGenerator as helpers.
 */
public class RobotControllerTest {
    public final double EPSILON = 1.0e-9;

    /**
     * Tests the most basic methods of RobotController. This test has extra
     * comments to serve as an example of how to use TestMapGenerator and TestGame.
     * @throws GameActionException shouldn't happen
     */
    @Test
    public void testBasic() throws GameActionException {
        // Prepares a map with the following properties:
        // width = 10, height = 10, num rounds = 100
        // random seed = 1337
        // parts(1, 0) = parts(0, 1) = 10
        // The map doesn't have to meet specs.
        TestMapGenerator mapGen = new TestMapGenerator(10, 10, 100)
                .withSeed(1337)
                .withParts(1, 0, 30)
                .withParts(0, 1, 30)
                .withRubble(2, 2, 200);

        // This creates the actual GameMap.
        GameMap map = mapGen.getMap("test");

        // This creates the actual game.
        TestGame game = new TestGame(map);

        // Let's spawn a soldier for each team. The integers represent IDs.
        int oX = game.getOriginX();
        int oY = game.getOriginY();
        final int soldierA = game.spawn(oX, oY, RobotType.SOLDIER, Team.A);
        final int soldierB = game.spawn(oX + 1, oY + 1, RobotType.SOLDIER, Team
                .B);
        InternalRobot soldierABot = game.getBot(soldierA);
        InternalRobot soldierBBot = game.getBot(soldierB);

        assertEquals(soldierABot.getLocation(), new MapLocation(oX , oY));

        // The following specifies the code to be executed in the next round.
        // Bytecodes are not counted, and yields are automatic at the end.
        game.round((id, rc) -> {
            if (id == soldierA) {
                rc.move(Direction.EAST);
            } else if (id == soldierB) {
                // do nothing
            }
        });

        // Let's assert that things happened properly.
        assertEquals(soldierABot.getLocation(), new MapLocation(oX + 1, oY));
        assertEquals(game.getWorld().resources(Team.A), 30 + GameConstants
                        .PARTS_INITIAL_AMOUNT - RobotType.SOLDIER.partCost,
                EPSILON);

        // Lets 10 rounds go by.
        game.waitRounds(10);

        // Let's make sure that robots can attack each other.
        game.round((id, rc) -> {
            if (id == soldierA) {
                rc.attackLocation(new MapLocation(oX + 1, oY + 1));
            }
        });

        // Makes sure that the attack did damage.
        assertEquals(soldierBBot.getHealthLevel(), 46, EPSILON);
    }

    /**
     * This test verifies rubble behavior.
     *
     * 1) Clearing rubble doesn't go below 0, and follows the right formula
     * 2) You can't move onto tiles with >= 100 rubble
     * 3) Dying produces rubble equal to your max health.
     *
     * @throws GameActionException shouldn't happen
     */
    @Test
    public void testRubbleBasic() throws GameActionException {
        TestMapGenerator mapGen = new TestMapGenerator(10, 10, 100)
                .withRubble(0, 1, 2)
                .withRubble(1, 0, 100)
                .withRubble(1, 1, 99);
        GameMap map = mapGen.getMap("test");
        TestGame game = new TestGame(map);
        int oX = game.getOriginX();
        int oY = game.getOriginY();
        final int soldierA = game.spawn(oX, oY, RobotType.SOLDIER, Team.A);
        InternalRobot soldierABot = game.getBot(soldierA);

        game.round((id, rc) -> {
            if (id == soldierA) {
                assertFalse(rc.canMove(Direction.EAST));
                rc.clearRubble(Direction.EAST);
            }
        });

        game.waitRounds(10);

        game.round((id, rc) -> {
            if (id == soldierA) {
                assertTrue(rc.canMove(Direction.SOUTH));
                rc.clearRubble(Direction.SOUTH);
            }
        });

        game.waitRounds(10);

        game.round((id, rc) -> {
            if (id == soldierA) {
                assertTrue(rc.canMove(Direction.SOUTH_EAST));
                rc.clearRubble(Direction.SOUTH_EAST);
            }
        });

        game.waitRounds(10);

        // Die to make sure that the robot produces rubble.
        // The damage taken is to make sure the rubble is based on max
        // health, not previous health.
        soldierABot.takeDamage(47);
        game.round((id, rc) -> {
            if (id == soldierA) {
                rc.attackLocation(new MapLocation(oX, oY));
            }
        });

        // Make sure the rubble amounts are correct.
        assertEquals(game.getWorld().getRubble(new MapLocation(oX, oY)),
                RobotType.SOLDIER.maxHealth, EPSILON);
        assertEquals(game.getWorld().getRubble(new MapLocation(oX, oY + 1)),
                0, EPSILON);
        assertEquals(game.getWorld().getRubble(new MapLocation(oX + 1, oY)),
                100 * (1 - GameConstants.RUBBLE_CLEAR_PERCENTAGE) -
                        GameConstants
                                .RUBBLE_CLEAR_FLAT_AMOUNT, EPSILON);
        assertEquals(game.getWorld().getRubble(new MapLocation(oX + 1, oY + 1)),
                99 * (1 - GameConstants.RUBBLE_CLEAR_PERCENTAGE) - GameConstants
                        .RUBBLE_CLEAR_FLAT_AMOUNT, EPSILON);
    }

    /**
     * Verifies that moving onto tiles with rubble doubles your core and move
     * delay changes.
     */
    @Test
    public void testRubbleSlow() throws GameActionException {
        TestMapGenerator mapGen = new TestMapGenerator(10, 10, 100)
                .withRubble(0, 1, 49)
                .withRubble(0, 2, 51);
        GameMap map = mapGen.getMap("test");
        TestGame game = new TestGame(map);
        int oX = game.getOriginX();
        int oY = game.getOriginY();
        final int soldierA = game.spawn(oX, oY, RobotType.SOLDIER, Team.A);
        InternalRobot soldierABot = game.getBot(soldierA);

        game.round((id, rc) -> {
            if (id == soldierA) {
                rc.move(Direction.SOUTH);
            }
        });

        assertEquals(soldierABot.getCoreDelay(), RobotType.SOLDIER
                .movementDelay, EPSILON);
        assertEquals(soldierABot.getWeaponDelay(), RobotType.SOLDIER
                .cooldownDelay, EPSILON);

        game.waitRounds(10);

        game.round((id, rc) -> {
            if (id == soldierA) {
                rc.move(Direction.SOUTH);
            }
        });

        assertEquals(soldierABot.getCoreDelay(), RobotType.SOLDIER
                .movementDelay * 2, EPSILON);
        assertEquals(soldierABot.getWeaponDelay(), RobotType.SOLDIER
                .cooldownDelay * 2, EPSILON);
    }
}
