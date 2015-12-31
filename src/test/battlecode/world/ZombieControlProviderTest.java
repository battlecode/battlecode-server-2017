package battlecode.world;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.world.control.NullControlProvider;
import battlecode.world.control.TeamControlProvider;
import battlecode.world.control.ZombieControlProvider;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test that zombies behave according to the spec.
 *
 * @author james
 */
public class ZombieControlProviderTest {
    private TeamControlProvider zombieControlProvider;

    @Before
    public void initZombieControlProvider() {
        zombieControlProvider = new TeamControlProvider();
        zombieControlProvider.registerControlProvider(Team.ZOMBIE, new ZombieControlProvider());
        zombieControlProvider.registerControlProvider(Team.A, new NullControlProvider());
        zombieControlProvider.registerControlProvider(Team.B, new NullControlProvider());
        zombieControlProvider.registerControlProvider(Team.NEUTRAL, new NullControlProvider());
    }

    @Test
    public void testZombieBehavior() {
        // . . . # .
        // Z . # # G
        // . . # # .
        final GameMap map = new TestMapGenerator(5, 3, 100)
                .withRobot(RobotType.STANDARDZOMBIE, Team.ZOMBIE, 0, 1)
                .withRobot(RobotType.GUARD, Team.A, 4, 1)
                .withRubble(2, 1, GameConstants.RUBBLE_OBSTRUCTION_THRESH)
                .withRubble(2, 2, GameConstants.RUBBLE_OBSTRUCTION_THRESH)
                .withRubble(3, 0, GameConstants.RUBBLE_OBSTRUCTION_THRESH)
                .withRubble(3, 1, GameConstants.RUBBLE_OBSTRUCTION_THRESH)
                .withRubble(3, 2, GameConstants.RUBBLE_OBSTRUCTION_THRESH)
                .getMap("map");

        final GameWorld world = new GameWorld(map, zombieControlProvider, "", "", new long[2][GameConstants.TEAM_MEMORY_LENGTH]);

        InternalRobot zombie = null;
        InternalRobot guard = null;

        for (InternalRobot robot : world.getAllGameObjects()) {
            if (robot.getTeam() == Team.ZOMBIE) {
                zombie = robot;
            }
            if (robot.getTeam() == Team.A) {
                guard = robot;
            }
        }

        final MapLocation origin = world.getGameMap().getOrigin();

        assertEquals(zombie.getLocation(), origin.add(0, 1));

        world.runRound();

        // . . . # .
        // . Z # # G
        // . . # # .
        assertEquals(zombie.getLocation(), origin.add(1, 1));

        // Let the zombie cool down
        while (zombie.getCoreDelay() - 1 >= 1) {
            world.runRound();
        }
        world.runRound();

        // . . Z # .
        // . . # # G
        // . . # # .
        assertEquals(zombie.getLocation(), origin.add(2, 0));

        while (zombie.getCoreDelay() - 1 >= 1) {
            world.runRound();
        }
        world.runRound();

        final MapLocation digLocation = zombie.getLocation()
                .add(zombie.getLocation().directionTo(guard.getLocation()));

        final double newRubble = GameConstants.RUBBLE_OBSTRUCTION_THRESH * (1 - GameConstants.RUBBLE_CLEAR_PERCENTAGE)
            - GameConstants.RUBBLE_CLEAR_FLAT_AMOUNT;

        // . . Z # .
        // . . # . G
        // . . # # .
        assertEquals(newRubble, world.getRubble(digLocation), 1e-9);

        while (zombie.getCoreDelay() - 1 >= 1) {
            world.runRound();
        }
        world.runRound();

        // . . . # .
        // . . # Z G
        // . . # # .
        assertEquals(zombie.getLocation(), digLocation);

        while (zombie.getWeaponDelay() - 1 >= 1) {
            world.runRound();
        }
        world.runRound();

        // Zombie should have attacked guard once
        assertEquals(RobotType.GUARD.maxHealth - RobotType.STANDARDZOMBIE.attackPower,
                guard.getHealthLevel(),
                1e-9);
    }

    @Test
    public void testBigZombieBehaviour() {
        // Z # G
        final GameMap map = new TestMapGenerator(3, 1, 100)
                .withRobot(RobotType.BIGZOMBIE, Team.ZOMBIE, 0, 0)
                .withRobot(RobotType.GUARD, Team.A, 2, 0)
                .withRubble(1, 0, GameConstants.RUBBLE_OBSTRUCTION_THRESH)
                .getMap("map");

        final GameWorld world = new GameWorld(map, zombieControlProvider, "", "", new long[2][GameConstants.TEAM_MEMORY_LENGTH]);

        InternalRobot zombie = null;
        InternalRobot guard = null;

        for (final InternalRobot robot : world.getAllGameObjects()) {
            if (robot.getTeam() == Team.ZOMBIE) {
                zombie = robot;
            }
            if (robot.getTeam() == Team.A) {
                guard = robot;
            }
        }

        final MapLocation origin = world.getGameMap().getOrigin();

        assertEquals(zombie.getLocation(), origin);

        world.runRound();

        // . Z G
        assertEquals(zombie.getLocation(), origin.add(1, 0));

        while (zombie.getWeaponDelay() - 1 >= 1) {
            world.runRound();
        }
        world.runRound();

        // Zombie should have attacked guard once
        assertEquals(RobotType.GUARD.maxHealth - RobotType.BIGZOMBIE.attackPower,
                guard.getHealthLevel(),
                1e-9);
    }

    @Test
    public void testDenBehaviour() {
        // . . . Z .
        // S . . D .
        // . . . G .
        final GameMap map = new TestMapGenerator(5, 3, 100)
                .withRobot(RobotType.ZOMBIEDEN, Team.ZOMBIE, 3, 1)
                .withRobot(RobotType.STANDARDZOMBIE, Team.ZOMBIE, 3, 0)
                .withRobot(RobotType.GUARD, Team.A, 3, 2)
                .withRobot(RobotType.SOLDIER, Team.A, 0, 1)
                .withZombieSpawn(0, RobotType.STANDARDZOMBIE, 7)
                .getMap("map");

        final GameWorld world = new GameWorld(map, zombieControlProvider, "", "", new long[2][GameConstants.TEAM_MEMORY_LENGTH]);

        final InternalRobot soldier = world.gameObjectsByID.values().stream()
                .filter(r -> r.getType() == RobotType.SOLDIER).findFirst().get();
        final InternalRobot guard = world.gameObjectsByID.values().stream()
                .filter(r -> r.getType() == RobotType.GUARD).findFirst().get();
        final InternalRobot zombie = world.gameObjectsByID.values().stream()
                .filter(r -> r.getType() == RobotType.STANDARDZOMBIE).findFirst().get();

        world.runRound();

        // There should now be 7 zombies
        assertEquals(7, world.getRobotTypeCount(Team.ZOMBIE, RobotType.STANDARDZOMBIE));

        // guard should take damage
        assertEquals(RobotType.GUARD.maxHealth - GameConstants.DEN_SPAWN_PROXIMITY_DAMAGE,
                guard.getHealthLevel(), 1e-9);

        // soldier and zombie should not
        assertEquals(RobotType.SOLDIER.maxHealth, soldier.getHealthLevel(), 1e-9);
        assertEquals(RobotType.STANDARDZOMBIE.maxHealth, zombie.getHealthLevel(), 1e-9);

    }
}
