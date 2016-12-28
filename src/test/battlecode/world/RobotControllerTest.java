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
    public final double EPSILON = 1.0e-5;

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

        assertEquals(new MapLocation(oX + 3, oY + 3), archonABot.getLocation());

        // The following specifies the code to be executed in the next round.
        // Bytecodes are not counted, and yields are automatic at the end.
        game.round((id, rc) -> {
            if (id == archonA) {
                rc.move(Direction.getEast());
            } else if (id == soldierB) {
                // do nothing
            }
        });

        // Let's assert that things happened properly.
        assertEquals(new MapLocation(
                oX + 3 + RobotType.ARCHON.strideRadius,
                oY + 3
        ), archonABot.getLocation());

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
            assertEquals(new MapLocation(1, 1), start);

            rc.move(Direction.getEast());

            final MapLocation newLocation = rc.getLocation();
            assertEquals(new MapLocation(1 + RobotType.SOLDIER.strideRadius, 1), newLocation);
        });

        // Let delays go away
        game.waitRounds(10);
    }

    @Test
    public void testSpawns() throws GameActionException {
        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 10, 10, 1337, 100)
            .build();

        // This creates the actual game.
        TestGame game = new TestGame(map);

        // Let's spawn a robot for each team. The integers represent IDs.
        final int archonA = game.spawn(3, 3, RobotType.ARCHON, Team.A);

        // The following specifies the code to be executed in the next round.
        // Bytecodes are not counted, and yields are automatic at the end.
        game.round((id, rc) -> {
            assertTrue("Can't build robot", rc.canBuildRobot(RobotType.GARDENER, Direction.getEast()));
            rc.buildRobot(RobotType.GARDENER, Direction.getEast());
        });

        int[] ids = game.getWorld().getObjectInfo().getRobotIDs();

        for (int id : ids) {
            if (id != archonA) {
                InternalRobot gardener = game.getBot(id);
                assertEquals(RobotType.GARDENER, gardener.getType());
            }
        }

        // Lets wait for 10 rounds go by.
        game.waitRounds(10);

        // hooray!

    }
    
    /**
     * Checks attacks of bullets in various ways
     * 
     * @throws GameActionException
     */
    @Test
    public void testBulletAttack() throws GameActionException {
        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 10, 10, 1337, 100)
            .build();

        // This creates the actual game.
        TestGame game = new TestGame(map);
        
        // Create some units
        final int soldierA = game.spawn(5, 5, RobotType.SOLDIER, Team.A);
        final int soldierA2 = game.spawn(9, 5, RobotType.SOLDIER, Team.A);
        final int soldierB = game.spawn(1, 5, RobotType.SOLDIER, Team.B);
        
        //game.waitRounds(20);
        
        // soldierA fires a shot at soldierA2
        game.round((id, rc) -> {
            if (id != soldierA) return;
            RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, Team.A);
            assertEquals(nearbyRobots.length,1);
            assertTrue(rc.canSingleShot());
            rc.fireSingleShot(rc.getLocation().directionTo(nearbyRobots[0].location));
            assertFalse(rc.canSingleShot());
            
            // Ensure bullet exists and spawns at proper location
            int[] bulletIDs = game.getWorld().getObjectInfo().getBulletIDs();
            assertEquals(bulletIDs.length,1);
            InternalBullet bill = game.getBullet(bulletIDs[0]);
            assertEquals(bill.getLocation().distanceTo(rc.getLocation()),rc.getType().bodyRadius + GameConstants.BULLET_SPAWN_OFFSET, EPSILON);
        });
        
        // soldierA fires a shot at soldierB
        game.round((id, rc) -> {
            if (id != soldierA) return;
            
            RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, Team.B);
            assertEquals(nearbyRobots.length,1);
            assertTrue(rc.canSingleShot());
            rc.fireSingleShot(rc.getLocation().directionTo(nearbyRobots[0].location));
            assertFalse(rc.canSingleShot());
            
            // Ensure two bullets exist
            int[] bulletIDs = game.getWorld().getObjectInfo().getBulletIDs();
            assertEquals(bulletIDs.length,2);
        });
        
        // Let bullets propagate to targets
        game.waitRounds(1);
        
        // No more bullets in flight
        int[] bulletIDs = game.getWorld().getObjectInfo().getBulletIDs();
        assertEquals(bulletIDs.length,0);
        
        // Two targets are damaged
        assertEquals(game.getBot(soldierA2).getHealth(),RobotType.SOLDIER.maxHealth - RobotType.SOLDIER.attackPower,EPSILON);
        assertEquals(game.getBot(soldierB).getHealth(),RobotType.SOLDIER.maxHealth - RobotType.SOLDIER.attackPower,EPSILON);
    }
    
    /**
     * Ensures tank body attack performs according to spec
     * 
     * @throws GameActionException
     */
    @Test
    public void testBodyAttack() throws GameActionException {
        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 10, 10, 1337, 100)
        .build();

        // This creates the actual game.
        TestGame game = new TestGame(map);
        
        final int tankB = game.spawn(3, 5, RobotType.TANK, Team.B);
        final int lumberjackA = game.spawn(5, 3, RobotType.LUMBERJACK, Team.A);
    }
}
