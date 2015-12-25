package battlecode.world;

import battlecode.common.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

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
                        .PARTS_INITIAL_AMOUNT, EPSILON);

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


    /**
     * Test that zombies can see everything.
     */
    @Test
    public void testZombieSightRange() throws GameActionException {
        TestMapGenerator mapGen = new TestMapGenerator(100, 100, 100);
        GameMap map = mapGen.getMap("test");
        TestGame game = new TestGame(map);
        int oX = game.getOriginX();
        int oY = game.getOriginY();
        final int zombie = game.spawn(oX, oY, RobotType.FASTZOMBIE, Team
                .ZOMBIE);
        final int soldier = game.spawn(oX + 99, oY + 99, RobotType.SOLDIER,
                Team.B);

        game.round((id, rc) -> {
            if (id == zombie) {
                RobotInfo[] nearby = rc.senseNearbyRobots();
                assertEquals(nearby.length, 1);
            } else if (id == soldier) {
                RobotInfo[] nearby = rc.senseNearbyRobots();
                assertEquals(nearby.length, 0);
            }
        });
    }

    /**
     * Test Map Memory scenarios.
     *
     * 0) You should not be able to sense values of parts and rubble out of
     * range.
     * 1) You should be able to sense values of parts and rubble in range.
     * 2) If you move out of range, you should still be able to sense those
     * values.
     * 3) If values for an out-of-sensor-range tile change, you should still
     * be sensing the old value.
     * 4) Moving back into range of the tile should update your sensed value.
     * 5) A value changing while you're in sight range should properly update
     * map memory and your sensed value.
     * 6) After moving out of range, your sensed value should reflect the
     * latest change.
     * 7) Sanity check that zombies work due to their infinite sight range.
     *
     * Note: this test hard-codes the soldier sight range of 24.
     *
     * TODO: add a test that makes sure map memory works when there are many
     * robots on the same team, or if a robot dies.
     */
    @Test
    public void testMapMemory() throws GameActionException {
        final double rubbleVal = 100;
        final double partsVal = 30;
        TestMapGenerator mapGen = new TestMapGenerator(100, 100, 100)
                .withRubble(0, 5, rubbleVal)
                .withParts(5, 0, partsVal);
        GameMap map = mapGen.getMap("test");
        TestGame game = new TestGame(map);
        int oX = game.getOriginX();
        int oY = game.getOriginY();
        final int zombie = game.spawn(oX + 99, oY + 99, RobotType.FASTZOMBIE,
                Team.ZOMBIE);
        final int soldier = game.spawn(oX, oY, RobotType.SOLDIER, Team.B);
        // robots to clear rubble and take parts
        final int soldier2 = game.spawn(oX, oY + 6, RobotType.SOLDIER, Team.A);
        final int soldier3 = game.spawn(oX + 6, oY, RobotType.SOLDIER, Team.A);
        MapLocation loc1 = new MapLocation(oX, oY + 5);
        MapLocation loc2 = new MapLocation(oX + 5, oY);

        // Zombie can see everything. Soldier can only see values in range.
        game.round((id, rc) -> {
            if (id == zombie) {
                assertEquals(rc.senseRubble(loc1), rubbleVal, EPSILON);
                assertEquals(rc.senseParts(loc2), partsVal, EPSILON);
            } else if (id == soldier) {
                assertEquals(rc.senseRubble(loc1), -1, EPSILON);
                assertEquals(rc.senseParts(loc2), -1, EPSILON);
            }
        });

        // Soldier moving closer results in proper value being sensed.
        game.round((id, rc) -> {
            if (id == soldier) {
                rc.move(Direction.SOUTH_EAST);
            }
        });
        game.round((id, rc) -> {
            if (id == soldier) {
                assertEquals(rc.senseRubble(loc1), rubbleVal, EPSILON);
                assertEquals(rc.senseParts(loc2), partsVal, EPSILON);
            }
        });

        // Soldier moves away but should still be able to sense the old values.
        game.waitRounds(10);
        game.round((id, rc) -> {
            if (id == soldier) {
                rc.move(Direction.NORTH_WEST);
            }
        });
        game.round((id, rc) -> {
            if (id == soldier) {
                assertFalse(rc.canSenseLocation(loc1));
                assertFalse(rc.canSenseLocation(loc2));
                assertEquals(rc.senseRubble(loc1), rubbleVal, EPSILON);
                assertEquals(rc.senseParts(loc2), partsVal, EPSILON);
            }
        });

        // If parts or rubble values change while you're out of range, you
        // shouldn't be able to sense the changes.
        game.round((id, rc) -> {
            if (id == soldier2) {
                rc.clearRubble(Direction.NORTH);
            } else if (id == soldier3) {
                rc.move(Direction.WEST); // get parts
            }
        });
        game.round((id, rc) -> {
            if (id == soldier) {
                assertFalse(rc.canSenseLocation(loc1));
                assertFalse(rc.canSenseLocation(loc2));
                assertEquals(rc.senseRubble(loc1), rubbleVal, EPSILON);
                assertEquals(rc.senseParts(loc2), partsVal, EPSILON);
            }
        });

        // If you move back into the location, then you should be able to
        // sense the new values.
        game.waitRounds(10);
        game.round((id, rc) -> {
            if (id == soldier) {
                rc.move(Direction.SOUTH_EAST);
            }
        });
        final double rubbleVal2 = rubbleVal * (1 - GameConstants
                .RUBBLE_CLEAR_PERCENTAGE) -
                GameConstants.RUBBLE_CLEAR_FLAT_AMOUNT;
        game.round((id, rc) -> {
            if (id == soldier) {
                assertEquals(rc.senseRubble(loc1), rubbleVal2, EPSILON);
                assertEquals(rc.senseParts(loc2), 0, EPSILON);
            }
        });

        // If the rubble value changes while you're able to sense it, map
        // memory should update too and you should be able to sense the new
        // value. (Former bug)
        game.round((id, rc) -> {
            if (id == soldier2) {
                rc.clearRubble(Direction.NORTH);
            }
        });
        final double rubbleVal3 = rubbleVal2 * (1 - GameConstants
                .RUBBLE_CLEAR_PERCENTAGE) -
                GameConstants.RUBBLE_CLEAR_FLAT_AMOUNT;
        game.round((id, rc) -> {
            if (id == soldier) {
                assertEquals(rc.senseRubble(loc1), rubbleVal3, EPSILON);
            }
        });

        // If you move away, you should still be able to sense the old values.
        // Let's make sure zombie knows this too, and that zombie values
        // update properly on move.
        game.round((id, rc) -> {
            if (id == soldier) {
                rc.move(Direction.NORTH_WEST);
            } else if (id == zombie) {
                rc.move(Direction.NORTH_WEST);
            }
        });
        game.round((id, rc) -> {
            if (id == soldier || id == zombie) {
                assertEquals(rc.senseRubble(loc1), rubbleVal3, EPSILON);
            }
        });
    }

    /**
     * Ensure that actions take place immediately.
     */
    @Test
    public void testImmediateActions() throws GameActionException {
        TestMapGenerator mapGen = new TestMapGenerator(3, 1, 20);
        GameMap map = mapGen.getMap("test");
        TestGame game = new TestGame(map);
        int oX = game.getOriginX();
        int oY = game.getOriginY();

        final int a = game.spawn(oX, oY, RobotType.SOLDIER, Team.A);
        final int b = game.spawn(oX + 2, oY, RobotType.SOLDIER, Team.B);

        game.round((id, rc) -> {
            if (id != a) return;

            final MapLocation start = rc.getLocation();
            assertEquals(start, new MapLocation(oX, oY));

            rc.move(Direction.EAST);

            final MapLocation newLocation = rc.getLocation();
            assertEquals(newLocation, new MapLocation(oX + 1, oY));
        });

        // Let delays go away
        game.waitRounds(10);

        game.round((id, rc) -> {
            if (id != a) return;

            MapLocation bLoc = new MapLocation(oX + 2, oY);

            RobotInfo bInfo = rc.senseRobotAtLocation(bLoc);

            assertEquals(RobotType.SOLDIER.maxHealth, bInfo.health, .00001);

            rc.attackLocation(new MapLocation(oX + 2, oY));

            RobotInfo bInfoNew = rc.senseRobotAtLocation(bLoc);

            assertEquals(RobotType.SOLDIER.maxHealth - RobotType.SOLDIER.attackPower,
                    bInfoNew.health,
                    .00001);
        });
    }

    /**
     * Makes sure that parts costs are properly subtracted when building a unit.
     */
    @Test
    public void testPartsCost() throws GameActionException {
        TestMapGenerator mapGen = new TestMapGenerator(10, 10, 100);

        GameMap map = mapGen.getMap("test");

        TestGame game = new TestGame(map);

        int oX = game.getOriginX();
        int oY = game.getOriginY();
        final int archon = game.spawn(oX, oY, RobotType.ARCHON, Team.A);

        assertEquals(game.getWorld().resources(Team.A), GameConstants
                .PARTS_INITIAL_AMOUNT, EPSILON);

        game.round((id, rc) -> {
            if (id == archon) {
                rc.build(Direction.SOUTH_EAST, RobotType.SOLDIER);
            }
        });

        assertEquals(game.getWorld().resources(Team.A), GameConstants
                .PARTS_INITIAL_AMOUNT - RobotType.SOLDIER.partCost +
                        GameConstants.ARCHON_PART_INCOME, EPSILON);
    }
}
