package battlecode.world;

import battlecode.common.*;

import org.junit.Test;

import static org.junit.Assert.*;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
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
     * comments to serve as an example of how to use TestMapGenerator and
     * TestGame.
     *
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

        assertEquals(soldierABot.getLocation(), new MapLocation(oX, oY));

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
     * <p>
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
        final int soldierB = game.spawn(oX+3, oY+2, RobotType.SOLDIER, Team.B);
        final int stdZombie = game.spawn(oX+3, oY+3, RobotType.STANDARDZOMBIE, Team.ZOMBIE);
        InternalRobot soldierABot = game.getBot(soldierA);
        InternalRobot soldierBBot = game.getBot(soldierB);
        InternalRobot stdZombieBot = game.getBot(stdZombie);

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
            } else if (id == stdZombie) { // Attack soldierB to infect it
                rc.attackLocation(new MapLocation(oX+3,oY+2));
            }
        });

        game.waitRounds(5);

        // Die to make sure that the robot produces rubble.
        // The damage taken is to make sure the rubble is based on max
        // health, not previous health.
        soldierABot.takeDamage(47);
        soldierBBot.takeDamage(45);
        stdZombieBot.takeDamage(58);
        game.round((id, rc) -> {
            if (id == soldierA) { // All robots kill themselves
                rc.attackLocation(new MapLocation(oX, oY));
            } else if (id == soldierB) {
                rc.attackLocation(new MapLocation(oX + 3, oY + 2));
            } else if (id == stdZombie) {
                rc.attackLocation(new MapLocation(oX + 3, oY + 3));
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
        assertEquals(game.getWorld().getRubble(new MapLocation(oX + 3, oY + 2)),
                0, EPSILON); // soldierB turns into zombie and doesn't leave rubble
        assertEquals(game.getWorld().getRubble(new MapLocation(oX + 3, oY + 3)),
                60, EPSILON); // dead zombie leaves rubble
        assertEquals(game.getWorld().getRobot(new MapLocation(oX+3, oY+2)).getType(),
                RobotType.STANDARDZOMBIE); // soldierB actually did turn into a zombie
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
     * <p>
     * 0) You should not be able to sense values of parts and rubble out of
     * range.
     * 1) You should be able to sense values of parts and rubble in range.
     * 2) After moving out of range, your sensed value should reflect the
     * latest change.
     * 3) Sanity check that zombies work due to their infinite sight range.
     *
     * Note: this test hard-codes the soldier sight range of 24.
     */
    @Test
    public void testSenses() throws GameActionException {
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

        // Soldier moves away, should go back to -1
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
                assertEquals(rc.senseRubble(loc1), -1, EPSILON);
                assertEquals(rc.senseParts(loc2), -1, EPSILON);
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
                assertEquals(rc.senseRubble(loc1), -1, EPSILON);
                assertEquals(rc.senseParts(loc2), -1, EPSILON);
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

        // If the rubble value changes while you're able to sense it, you
        // should be able to sense the new value. (Former bug)
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

        // If you move away, you should lose the ability to sense.
        // Zombies can always sense every location; make sure their senses
        // update correctly.
        game.round((id, rc) -> {
            if (id == soldier) {
                rc.move(Direction.NORTH_WEST);
            } else if (id == zombie) {
                rc.move(Direction.NORTH_WEST);
            }
        });
        game.round((id, rc) -> {
            if (id == zombie) {
                assertEquals(rc.senseRubble(loc1), rubbleVal3, EPSILON);
            }
            else if (id == soldier) {
                assertEquals(rc.senseRubble(loc1), -1, EPSILON);
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

            assertEquals(RobotType.SOLDIER.maxHealth - RobotType.SOLDIER
                            .attackPower,
                    bInfoNew.health,
                    .00001);
        });
    }

    /**
     * Tests the archon repair() method.
     */
    @Test
    public void testRepair() throws GameActionException {
        TestMapGenerator mapGen = new TestMapGenerator(10, 10, 100);

        GameMap map = mapGen.getMap("test");

        TestGame game = new TestGame(map);

        int oX = game.getOriginX();
        int oY = game.getOriginY();
        final int archon = game.spawn(oX, oY, RobotType.ARCHON, Team.A);
        final int soldier = game.spawn(oX + 2, oY, RobotType.SOLDIER, Team.A);
        InternalRobot soldierBot = game.getBot(soldier);

        soldierBot.takeDamage(15);

        game.round((id, rc) -> {
            if (id == archon) {
                rc.repair(new MapLocation(oX + 2, oY));
            }
        });

        assertEquals(soldierBot.getHealthLevel(), RobotType.SOLDIER.maxHealth
                - 15 + GameConstants.ARCHON_REPAIR_AMOUNT, EPSILON);
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

    /**
     * Destroying a zombie den should reward parts to the attacker's team.
     */
    @Test
    public void testDenPartsReward() throws GameActionException {
        TestMapGenerator mapGen = new TestMapGenerator(10, 10, 100);

        GameMap map = mapGen.getMap("test");

        TestGame game = new TestGame(map);

        int oX = game.getOriginX();
        int oY = game.getOriginY();
        final int soldierA = game.spawn(oX, oY + 1, RobotType.SOLDIER, Team.A);
        final int soldierB = game.spawn(oX + 1, oY, RobotType.SOLDIER, Team.B);
        final int den = game.spawn(oX, oY, RobotType.ZOMBIEDEN, Team.ZOMBIE);
        InternalRobot denBot = game.getBot(den);

        assertEquals(game.getWorld().resources(Team.A), GameConstants
                .PARTS_INITIAL_AMOUNT, EPSILON);

        // The den should have enough health to survive 2 attacks.
        denBot.takeDamage(RobotType.ZOMBIEDEN.maxHealth - RobotType.SOLDIER
                .attackPower - 1);

        // Soldier A goes first
        game.round((id, rc) -> {
            if (id == soldierA) {
                rc.attackLocation(new MapLocation(oX, oY));
            } else if (id == soldierB) {
                rc.attackLocation(new MapLocation(oX, oY));
            }
        });

        assertEquals(game.getWorld().resources(Team.A), GameConstants
                .PARTS_INITIAL_AMOUNT, EPSILON);
        assertEquals(game.getWorld().resources(Team.B), GameConstants
                .PARTS_INITIAL_AMOUNT + GameConstants.DEN_PART_REWARD, EPSILON);
    }

    /**
     * Using more bytecode should incur delay penalties
     */
    @Test
    public void testDelayPenalty() throws GameActionException {
        TestMapGenerator mapGen = new TestMapGenerator(10, 10, 12);

        GameMap map = mapGen.getMap("test");

        TestGame game = new TestGame(map);

        int oX = game.getOriginX();
        int oY = game.getOriginY();
        final int soldierA = game.spawn(oX, oY, RobotType.SOLDIER, Team.A);
        final int soldierB = game.spawn(oX + 1, oY + 1, RobotType.SOLDIER, Team
                .B);
        InternalRobot soldierABot = game.getBot(soldierA);
        InternalRobot soldierBBot = game.getBot(soldierB);

        soldierABot.setBytecodesUsed(0); // Start out using no bytecode
        soldierBBot.setBytecodesUsed(0);

        //Soldier A moves, soldier B attacks
        game.round((id, rc) -> {
            if (id == soldierA) {
                rc.move(Direction.EAST);
            } else if (id == soldierB) {
                rc.attackLocation(new MapLocation(oX+2,oY+2));
            }
        });

        // Core delay = movement delay, weapon delay = attack delay
        assertEquals(soldierABot.getCoreDelay(),RobotType.SOLDIER.movementDelay,EPSILON);
        assertEquals(soldierBBot.getWeaponDelay(),RobotType.SOLDIER.attackDelay,EPSILON);

        game.waitRounds(1);
        // After one round with zero bytecode, should decrement by one
        assertEquals(soldierABot.getCoreDelay(),RobotType.SOLDIER.movementDelay-1,EPSILON);
        assertEquals(soldierBBot.getWeaponDelay(),RobotType.SOLDIER.attackDelay-1,EPSILON);

        game.waitRounds(3);
        // Should have gone back to zero
        assertEquals(soldierABot.getCoreDelay(),0,EPSILON);
        assertEquals(soldierBBot.getWeaponDelay(),0,EPSILON);

        // Now use intermediate amount of bytecode
        soldierABot.setBytecodesUsed(RobotType.SOLDIER.bytecodeLimit-4000);
        soldierBBot.setBytecodesUsed(RobotType.SOLDIER.bytecodeLimit-4000);

        //Soldier A moves, soldier B attacks
        game.round((id, rc) -> {
            if (id == soldierA) {
                rc.move(Direction.WEST);
            } else if (id == soldierB) {
                rc.attackLocation(new MapLocation(oX+2,oY+2));
            }
        });

        // Core delay = movement delay, weapon delay = attack delay
        assertEquals(soldierABot.getCoreDelay(),RobotType.SOLDIER.movementDelay,EPSILON);
        assertEquals(soldierBBot.getWeaponDelay(),RobotType.SOLDIER.attackDelay,EPSILON);

        game.waitRounds(1);
        // After one round with zero bytecode, should decrement by new value
        double decrement = 1.0 - (0.3 * Math.pow(0.5,1.5)); // Approx 0.894
        assertEquals(soldierABot.getCoreDelay(),RobotType.SOLDIER.movementDelay-decrement,EPSILON);
        assertEquals(soldierBBot.getWeaponDelay(),RobotType.SOLDIER.attackDelay-decrement,EPSILON);

        game.waitRounds(3);

        // Now use max amount of bytecode
        soldierABot.setBytecodesUsed(RobotType.SOLDIER.bytecodeLimit);
        soldierBBot.setBytecodesUsed(RobotType.SOLDIER.bytecodeLimit);

        //Soldier A moves, soldier B attacks
        game.round((id, rc) -> {
            if (id == soldierA) {
                rc.move(Direction.EAST);
            } else if (id == soldierB) {
                rc.attackLocation(new MapLocation(oX+2,oY+2));
            }
        });

        game.waitRounds(1);
        decrement = 0.7; // Should now only decrease by 0.7 in one turn
        assertEquals(soldierABot.getCoreDelay(),RobotType.SOLDIER.movementDelay-decrement,EPSILON);
        assertEquals(soldierBBot.getWeaponDelay(),RobotType.SOLDIER.attackDelay-decrement,EPSILON);
    }

    /**
     * Test outbreak mechanics.
     */
    @Test
    public void testZombieOutbreak() throws GameActionException {
        TestMapGenerator mapGen = new TestMapGenerator(10, 10, 1000);

        GameMap map = mapGen.getMap("test");

        TestGame game = new TestGame(map);

        int oX = game.getOriginX();
        int oY = game.getOriginY();
        final int archon = game.spawn(oX, oY + 1, RobotType.ARCHON, Team.A);
        InternalRobot archonBot = game.getBot(archon);
        final int zombie1 = game.spawn(oX, oY, RobotType.STANDARDZOMBIE, Team
                .ZOMBIE);

        // round 0
        game.round((id, rc) -> {
            if (id == zombie1) {
                assertEquals(rc.getHealth(), RobotType.STANDARDZOMBIE
                        .maxHealth, EPSILON);
                rc.attackLocation(new MapLocation(oX, oY + 1));
            }
        });

        assertEquals(archonBot.getHealthLevel(), RobotType.ARCHON.maxHealth -
                RobotType.STANDARDZOMBIE.attackPower, EPSILON);

        game.waitRounds(600);

        // round 601 (multiplier 1.2)
        final int zombie2 = game.spawn(oX + 1, oY, RobotType.RANGEDZOMBIE,
                Team.ZOMBIE);

        game.round((id, rc) -> {
            if (id == zombie2) {
                assertEquals(rc.getHealth(), RobotType.RANGEDZOMBIE
                        .maxHealth * 1.2, EPSILON);
                rc.attackLocation(new MapLocation(oX, oY + 1));
            }
        });

        assertEquals(archonBot.getHealthLevel(), RobotType.ARCHON.maxHealth -
                RobotType.STANDARDZOMBIE.attackPower - RobotType.RANGEDZOMBIE
                .attackPower * 1.2, EPSILON);

        // make sure that a zombie dying leaves the right amount of rubble
        InternalRobot zombie2Bot = game.getBot(zombie2);
        zombie2Bot.takeDamage(zombie2Bot.getHealthLevel());

        assertEquals(game.getWorld().getRubble(new MapLocation(oX + 1, oY)),
                RobotType.RANGEDZOMBIE.maxHealth * 1.2, EPSILON);
    }

    /**
     * Test getting the zombie spawn schedule, and makes sure that modifying
     * this schedule doesn't change the schedule for the actual game.
     */
    @Test
    public void testGetZombieSpawnSchedule() throws GameActionException {
        TestMapGenerator mapGen = new TestMapGenerator(10, 10, 1000)
                .withZombieSpawn(100, RobotType.FASTZOMBIE, 30)
                .withZombieSpawn(500, RobotType.RANGEDZOMBIE, 50)
                .withZombieSpawn(500, RobotType.BIGZOMBIE, 4)
                .withZombieSpawn(1000, RobotType.STANDARDZOMBIE, 10);

        GameMap map = mapGen.getMap("test");

        TestGame game = new TestGame(map);

        int oX = game.getOriginX();
        int oY = game.getOriginY();
        final int archon = game.spawn(oX, oY + 1, RobotType.ARCHON, Team.A);

        game.round((id, rc) -> {
            if (id == archon) {
                ZombieSpawnSchedule zombieSpawnSchedule = rc
                        .getZombieSpawnSchedule();
                assertThat(zombieSpawnSchedule.getRounds(), contains(100,
                        500, 1000));
                ArrayList<ZombieCount> count100 = zombieSpawnSchedule
                        .getScheduleForRound(100);
                assertThat(count100, contains(new ZombieCount(RobotType
                        .FASTZOMBIE, 30)));
                ArrayList<ZombieCount> count500 = zombieSpawnSchedule
                        .getScheduleForRound(500);
                assertThat(count500, contains(new ZombieCount(RobotType
                        .RANGEDZOMBIE, 50), new ZombieCount(RobotType
                        .BIGZOMBIE, 4)));
                ArrayList<ZombieCount> count1000 = zombieSpawnSchedule
                        .getScheduleForRound(1000);
                assertThat(count1000, contains(new ZombieCount(RobotType
                        .STANDARDZOMBIE, 10)));

                // now try to modify zombieSpawnSchedule
                zombieSpawnSchedule.add(1500, RobotType.STANDARDZOMBIE, 8);
            }
        });

        // Make sure things didn't change.
        ZombieSpawnSchedule zombieSpawnSchedule = game.getWorld()
                .getGameMap().getZombieSpawnSchedule();
        assertEquals(zombieSpawnSchedule.getRounds().size(), 3);
        assertThat(zombieSpawnSchedule.getRounds(), contains(100,
                500, 1000));
    }

    /**
     * Tests activation of neutral bots.
     */
    @Test
    public void testActivation() throws GameActionException {
        TestMapGenerator mapGen = new TestMapGenerator(10, 10, 1000);

        GameMap map = mapGen.getMap("test");

        TestGame game = new TestGame(map);

        int oX = game.getOriginX();
        int oY = game.getOriginY();
        final int archon = game.spawn(oX, oY + 1, RobotType.ARCHON, Team.A);
        final int neutral = game.spawn(oX, oY, RobotType.SOLDIER, Team.NEUTRAL);

        game.round((id, rc) -> {
            if (id == archon) {
                rc.activate(new MapLocation(oX, oY));
            }
        });

        // make sure that archon now has an ally
        game.round((id, rc) -> {
            if (id == archon) {
                RobotInfo[] nearby = rc.senseNearbyRobots();
                assertEquals(nearby.length, 1);
                assertEquals(nearby[0].location, new MapLocation(oX, oY));
                assertEquals(nearby[0].type, RobotType.SOLDIER);
                assertEquals(nearby[0].team, Team.A);
            }
        });
    }

    /**
     * Test signaling behavior
     */
    @Test
    public void testSignaling() throws GameActionException {
        TestMapGenerator mapGen = new TestMapGenerator(10, 10, 1000);

        GameMap map = mapGen.getMap("test");

        TestGame game = new TestGame(map);

        int oX = game.getOriginX();
        int oY = game.getOriginY();
        final int archon = game.spawn(oX, oY, RobotType.ARCHON, Team.A);
        final int soldier = game.spawn(oX, oY + 4, RobotType.SOLDIER, Team.B);
        final int guard = game.spawn(oX, oY + 5, RobotType.GUARD, Team.B);

        game.round((id, rc) -> {
            if (id == archon) {
                rc.broadcastMessageSignal(123, 456, 24);
                assertEquals(rc.getCoreDelay(), GameConstants
                        .BROADCAST_BASE_DELAY_INCREASE, EPSILON);
                assertEquals(rc.getWeaponDelay(), GameConstants
                        .BROADCAST_BASE_DELAY_INCREASE, EPSILON);
            } else if (id == soldier) {
                rc.broadcastSignal(2);
            } else if (id == guard) {
                rc.broadcastSignal(10000);
                double x = 10000.0 / RobotType.GUARD.sensorRadiusSquared - 2;
                assertEquals(rc.getCoreDelay(), GameConstants
                        .BROADCAST_BASE_DELAY_INCREASE + x * GameConstants
                        .BROADCAST_ADDITIONAL_DELAY_INCREASE, EPSILON);
                assertEquals(rc.getWeaponDelay(), GameConstants
                        .BROADCAST_BASE_DELAY_INCREASE + x * GameConstants
                        .BROADCAST_ADDITIONAL_DELAY_INCREASE, EPSILON);
            }
        });

        // verify messages
        game.round((id, rc) -> {
            if (id == archon) {
                Signal[] queue = rc.emptySignalQueue();
                assertEquals(queue.length, 1);
                assertEquals(queue[0].getMessage(), null);
                assertEquals(queue[0].getRobotID(), guard);
                assertEquals(queue[0].getLocation(), new MapLocation(oX, oY +
                        5));
                assertEquals(queue[0].getTeam(), Team.B);
            } else if (id == soldier) {
                Signal first = rc.readSignal();
                Signal second = rc.readSignal();
                Signal third = rc.readSignal();
                assertArrayEquals(first.getMessage(), new int[]{123, 456});
                assertEquals(first.getRobotID(), archon);
                assertEquals(first.getLocation(), new MapLocation(oX, oY));
                assertEquals(first.getTeam(), Team.A);
                assertArrayEquals(second.getMessage(), null);
                assertEquals(second.getRobotID(), guard);
                assertEquals(second.getLocation(), new MapLocation(oX, oY + 5));
                assertEquals(second.getTeam(), Team.B);
                assertEquals(third, null);
            } else if (id == guard) {
                Signal[] queue = rc.emptySignalQueue();
                assertEquals(queue.length, 1);
                assertEquals(queue[0].getMessage(), null);
                assertEquals(queue[0].getTeam(), Team.B);
                assertEquals(queue[0].getLocation(), new MapLocation(oX, oY +
                        4));
                assertEquals(queue[0].getRobotID(), soldier);
            }
        });
    }
}
