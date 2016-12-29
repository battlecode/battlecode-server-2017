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
    public final double EPSILON = 1.0e-5; // Smaller epsilon requred, possibly due to strictfp? Used to be 1.0e-9

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
        game.waitRounds(20); // Let soldiers mature
        
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
     * Ensures tank body attack (and bullet attack) perform according to spec
     * 
     * @throws GameActionException
     */
    @Test
    public void testTankAttack() throws GameActionException {
        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 10, 10, 1337, 100)
        .build();

        // This creates the actual game.
        TestGame game = new TestGame(map);
        
        final int tankB = game.spawn(2, 5, RobotType.TANK, Team.B);
        final int neutralTree = game.spawnTree(6, 5, 1, Team.NEUTRAL, 0, null);
        game.waitRounds(20); // Wait for units to mature
        
        game.round((id, rc) -> {
            if (id != tankB) return;
            
            TreeInfo[] nearbyTrees = rc.senseNearbyTrees(-1);
            assertEquals(nearbyTrees.length,1);
            assertTrue(rc.canSingleShot());
            rc.fireSingleShot(rc.getLocation().directionTo(nearbyTrees[0].location));
            assertFalse(rc.canSingleShot());
        });
        
        // Let bullets propagate to targets
        game.waitRounds(1);
        
        // Tree took bullet damage
        assertEquals(game.getTree(neutralTree).getHealth(),GameConstants.NEUTRAL_TREE_HEALTH_RATE*1 - RobotType.TANK.attackPower,EPSILON);
        
        // Move tank toward tree
        game.round((id, rc) -> {
            if (id != tankB) return;
            
            TreeInfo[] nearbyTrees = rc.senseNearbyTrees(-1);
            assertEquals(nearbyTrees.length,1);
            MapLocation originalLoc = rc.getLocation();
            assertFalse(rc.hasMoved());
            rc.move(rc.getLocation().directionTo(nearbyTrees[0].location));
            assertTrue(rc.hasMoved());
            assertFalse(rc.getLocation().equals(originalLoc)); // Tank should have moved
        });
        // Mpve tank into tree
        game.round((id, rc) -> {
            if (id != tankB) return;
            
            TreeInfo[] nearbyTrees = rc.senseNearbyTrees(-1);
            assertEquals(nearbyTrees.length,1);
            MapLocation originalLoc = rc.getLocation();
            assertFalse(rc.hasMoved());
            rc.move(rc.getLocation().directionTo(nearbyTrees[0].location));
            assertTrue(rc.hasMoved());
            assertTrue(rc.getLocation().equals(originalLoc)); // Tank doesn't move due to tree in the way
        });
        // Body Damage
        assertEquals(game.getTree(neutralTree).getHealth(),GameConstants.NEUTRAL_TREE_HEALTH_RATE*1 - RobotType.TANK.attackPower - GameConstants.TANK_BODY_DAMAGE,EPSILON);
        
        // Hit exactly enough times to kill tree
        for(int i=0; i<(GameConstants.NEUTRAL_TREE_HEALTH_RATE*1-RobotType.TANK.attackPower)/GameConstants.TANK_BODY_DAMAGE - 1; i++) {
            game.round((id, rc) -> {
                if (id != tankB) return;
                
                TreeInfo[] nearbyTrees = rc.senseNearbyTrees(-1);
                assertEquals(nearbyTrees.length,1);
                rc.move(rc.getLocation().directionTo(nearbyTrees[0].location));
            });
        }
        
        // Should be able to move now
        game.round((id, rc) -> {
            if (id != tankB) return;
            
            TreeInfo[] nearbyTrees = rc.senseNearbyTrees(-1);
            assertEquals(nearbyTrees.length,0);
            MapLocation originalLoc = rc.getLocation();
            rc.move(Direction.getEast());
            assertFalse(rc.getLocation().equals(originalLoc)); 
        });
    }
    
    /**
     * Ensures lumberjacks can cut down trees and perform basher-like attacks
     * 
     * @throws GameActionException
     */
    @Test
    public void testLumberjacks() throws GameActionException {
        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 10, 10, 1337, 100)
        .build();

        // This creates the actual game.
        TestGame game = new TestGame(map);
        
        final int lumberjackA = game.spawn(2, 5, RobotType.LUMBERJACK, Team.A);
        final int soldierA = game.spawn(3, (float)7.1, RobotType.SOLDIER, Team.A);
        final int soldierB = game.spawn(3, (float)2.9, RobotType.SOLDIER, Team.B);
        final int neutralTree = game.spawnTree(6, 5, 1, Team.NEUTRAL, 0, null);
        float expectedTreeHealth = GameConstants.NEUTRAL_TREE_HEALTH_RATE*1;
        game.waitRounds(20); // Let bots mature to full health
        
        // Trying to chop a tree
        game.round((id, rc) -> {
            if (id != lumberjackA) return;
            
            TreeInfo[] nearbyTrees = rc.senseNearbyTrees(-1);
            assertEquals(nearbyTrees.length,1);
            
            boolean exception = false;
            try {
                rc.chop(nearbyTrees[0].ID); // Try to chop neutralTree
            } catch (GameActionException e) {
                exception = true;
            }
            assertTrue(exception); // fails, tree is out of range
            assertFalse(rc.hasAttacked()); // attack attempt is not counted
            
            // Move toward neutralTree
            rc.move(rc.getLocation().directionTo(nearbyTrees[0].location));
            
            exception = false;
            try {
                rc.chop(nearbyTrees[0].ID);  // Try to chop again
            } catch (GameActionException e) {
                exception = true;
            }
            assertFalse(exception); // succeeds, tree now in range
            assertTrue(rc.hasAttacked());
        });
        expectedTreeHealth -= RobotType.LUMBERJACK.attackPower*GameConstants.LUMBERJACK_CHOP_DAMAGE_MULTIPLIER;
        assertEquals(game.getTree(neutralTree).getHealth(),expectedTreeHealth,EPSILON);
        
        // Striking surrounding units
        game.round((id, rc) -> {
            if (id != lumberjackA) return;
            
            rc.strike();
            assertTrue(rc.hasAttacked());
        });
        
        expectedTreeHealth -= RobotType.LUMBERJACK.attackPower;
        assertEquals(game.getTree(neutralTree).getHealth(),expectedTreeHealth,EPSILON);
        assertEquals(game.getBot(soldierA).getHealth(),RobotType.SOLDIER.maxHealth-RobotType.LUMBERJACK.attackPower,EPSILON);
        assertEquals(game.getBot(soldierB).getHealth(),RobotType.SOLDIER.maxHealth-RobotType.LUMBERJACK.attackPower,EPSILON);
    }
    
    /**
     * Planting, withering, watering, bullet income, gardener's can't water neutral, etc
     * 
     * @throws GameActionException
     */
    @Test
    public void treesBulletsTest() throws GameActionException {
        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 10, 10, 1337, 100)
        .build();

        // This creates the actual game.
        TestGame game = new TestGame(map);
        
        final int gardenerA = game.spawn(5, 5, RobotType.GARDENER, Team.A);
        final int neutralTree = game.spawnTree(2, 5, 1, Team.NEUTRAL, 0, null);
        
        // Check initial bullet amounts
        assertEquals(game.getWorld().getTeamInfo().getBulletSupply(Team.A),GameConstants.BULLETS_INITIAL_AMOUNT,EPSILON);
        assertEquals(game.getWorld().getTeamInfo().getBulletSupply(Team.B),GameConstants.BULLETS_INITIAL_AMOUNT,EPSILON);
        game.getWorld().getTeamInfo().adjustBulletSupply(Team.B, -GameConstants.BULLETS_INITIAL_AMOUNT);
        
        // Keep track of expected bullet amounts throughout match
        float teamBexpected = 0;
        float teamAexpected = GameConstants.BULLETS_INITIAL_AMOUNT;
        
        game.waitRounds(20); // Let bots mature to full health
        
        game.round((id, rc) -> {
            if (id != gardenerA) return;
            assertFalse(rc.canPlantBulletTree(Direction.getWest())); // tree in way
            assertTrue(rc.canPlantBulletTree(Direction.getNorth())); // unobstructed
            assertTrue(rc.canPlantBulletTree(Direction.getEast())); // unobstructed
            rc.plantBulletTree(Direction.getEast());
            assertFalse(rc.canMove(Direction.getEast())); // tree now in the way
            assertFalse(rc.canPlantBulletTree(Direction.getNorth())); // has already planted this turn
            TreeInfo[] trees = rc.senseNearbyTrees();
            assertEquals(trees.length,2);
            TreeInfo[] bulletTrees = rc.senseNearbyTrees(-1, rc.getTeam());
            assertEquals(bulletTrees.length,1);
            assertEquals(bulletTrees[0].health,GameConstants.BULLET_TREE_MAX_HEALTH*GameConstants.PLANTED_UNIT_STARTING_HEALTH_FRACTION,EPSILON);
        });
        game.waitRounds(80);
        
        teamAexpected -= GameConstants.BULLET_TREE_COST;
        assertEquals(game.getWorld().getTeamInfo().getBulletSupply(Team.A),teamAexpected,EPSILON);
        
        game.round((id, rc) -> {    // Reaches flull health
            if (id != gardenerA) return;
            TreeInfo[] trees = rc.senseNearbyTrees();
            assertEquals(trees.length,2);
            TreeInfo[] bulletTrees = rc.senseNearbyTrees(-1, rc.getTeam());
            assertEquals(bulletTrees.length,1);
            assertEquals(bulletTrees[0].health,GameConstants.BULLET_TREE_MAX_HEALTH,EPSILON);
        });
        game.waitRounds(10);
        
        // Check income from trees
        for(int i=0; i<11; i++){
            teamAexpected += (GameConstants.BULLET_TREE_MAX_HEALTH-i)*GameConstants.BULLET_TREE_BULLET_PRODUCTION_RATE;
        }
        assertEquals(game.getWorld().getTeamInfo().getBulletSupply(Team.A),teamAexpected,EPSILON);
        
        game.round((id, rc) -> {    // Decays for eleven (wait ten plus this one) turns
            if (id != gardenerA) return;
            TreeInfo[] bulletTrees = rc.senseNearbyTrees(-1, rc.getTeam());
            assertEquals(bulletTrees.length,1);
            assertEquals(bulletTrees[0].health,GameConstants.BULLET_TREE_MAX_HEALTH-GameConstants.BULLET_TREE_DECAY_RATE*11,EPSILON);
            assertTrue(rc.canWater());
            rc.water(bulletTrees[0].ID);
            // Health has not increased from watering yet, wait until following turn
            assertEquals(bulletTrees[0].health,GameConstants.BULLET_TREE_MAX_HEALTH-GameConstants.BULLET_TREE_DECAY_RATE*11,EPSILON);
            assertFalse(rc.canWater());
        });
        game.round((id, rc) -> {    // Decays for eleven (wait ten plus this one) turns
            if (id != gardenerA) return;
            TreeInfo[] bulletTrees = rc.senseNearbyTrees(-1, rc.getTeam());
            assertEquals(bulletTrees.length,1);
            // Tree was watered last turn, so will now increase in health by 10.
            assertEquals(bulletTrees[0].health,GameConstants.BULLET_TREE_MAX_HEALTH-GameConstants.BULLET_TREE_DECAY_RATE*12+GameConstants.WATER_HEALTH_REGEN_RATE,EPSILON);
            assertTrue(rc.canWater());
            rc.water(bulletTrees[0].ID);
        });
        game.round((id, rc) -> {    // Decays for eleven (wait ten plus this one) turns
            if (id != gardenerA) return;
            TreeInfo[] bulletTrees = rc.senseNearbyTrees(-1, rc.getTeam());
            assertEquals(bulletTrees.length,1);
            // Tree was watered last turn, so will now increase in health by 10.
            assertEquals(bulletTrees[0].health,GameConstants.BULLET_TREE_MAX_HEALTH-GameConstants.BULLET_TREE_DECAY_RATE,EPSILON);
        });
        //assertEquals(game.getWorld().getTeamInfo().getBulletSupply(Team.A),0.0,EPSILON);
        int numRounds = game.getWorld().getCurrentRound();
        
        // Team B's supply was reset to zero at beginning, this will make sure income works as expected.
        for(int i=0; i<numRounds; i++) {
            teamBexpected += GameConstants.ARCHON_BULLET_INCOME-teamBexpected*GameConstants.BULLET_INCOME_UNIT_PENALTY;
        }
        assertEquals(game.getWorld().getTeamInfo().getBulletSupply(Team.B),teamBexpected,EPSILON);
        
        // Wait for tree to decay completely
        game.waitRounds(100);
        
        // Tree should be dead
        game.round((id, rc) -> {    // Decays for eleven (wait ten plus this one) turns
            if (id != gardenerA) return;
            TreeInfo[] bulletTrees = rc.senseNearbyTrees(-1, rc.getTeam());
            assertEquals(bulletTrees.length,0); // no more tree
        });
    }
    
    @Test // Normal robots blocked by trees and other robots, drones fly over but blocked by other drones
    public void obstructionTest() throws GameActionException {
        
    }
    
    @Test // Bullet collision works continuously and not at discrete intervals
    public void continuousBulletCollisionTest() throws GameActionException {
        
    }
    
    // test goodies inside trees
}
