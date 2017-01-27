package battlecode.world;

import battlecode.common.*;

import gnu.trove.list.array.TIntArrayList;
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

        for (InternalRobot robot : game.getWorld().getObjectInfo().robots()) {
            if (robot.getID() != archonA) {
                assertEquals(RobotType.GARDENER, robot.getType());
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
            assertTrue(rc.canFireSingleShot());
            rc.fireSingleShot(rc.getLocation().directionTo(nearbyRobots[0].location));
            assertFalse(rc.canFireSingleShot());
            
            // Ensure bullet exists and spawns at proper location
            InternalBullet[] bullets = game.getWorld().getObjectInfo().bulletsArray();
            assertEquals(bullets.length,1);
            assertEquals(
                    bullets[0].getLocation().distanceTo(rc.getLocation()),
                    rc.getType().bodyRadius + GameConstants.BULLET_SPAWN_OFFSET,
                    EPSILON
            );
        });
        
        // soldierA fires a shot at soldierB
        game.round((id, rc) -> {
            if (id != soldierA) return;

            // Original bullet should be gone
            InternalBullet[] bullets = game.getWorld().getObjectInfo().bulletsArray();
            assertEquals(bullets.length,0);
            
            RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, Team.B);
            assertEquals(nearbyRobots.length,1);
            assertTrue(rc.canFireSingleShot());
            rc.fireSingleShot(rc.getLocation().directionTo(nearbyRobots[0].location));
            assertFalse(rc.canFireSingleShot());
            
            // Ensure new bullet exists
            bullets = game.getWorld().getObjectInfo().bulletsArray();
            assertEquals(bullets.length,1);
        });
        
        // Let bullets propagate to targets
        game.waitRounds(1);
        
        // No more bullets in flight
        InternalBullet[] bulletIDs = game.getWorld().getObjectInfo().bulletsArray();
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
        
        final int tankB = game.spawn(3.25f, 5, RobotType.TANK, Team.B);
        final int neutralTree = game.spawnTree(7, 5, 1, Team.NEUTRAL, 0, null);
        game.waitRounds(20); // Wait for units to mature
        
        game.round((id, rc) -> {
            if (id != tankB) return;
            
            TreeInfo[] nearbyTrees = rc.senseNearbyTrees(-1);
            assertEquals(nearbyTrees.length,1);
            assertTrue(rc.canFireSingleShot());
            rc.fireSingleShot(rc.getLocation().directionTo(nearbyTrees[0].location));
            assertFalse(rc.canFireSingleShot());
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
            assertTrue(rc.canMove(rc.getLocation().directionTo(nearbyTrees[0].location)));
            rc.move(rc.getLocation().directionTo(nearbyTrees[0].location));
            assertTrue(rc.hasMoved());
            assertFalse(rc.getLocation().equals(originalLoc)); // Tank should have moved
        });
        // Move tank into tree
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
        
        final int lumberjackA = game.spawn(2.5f, 5, RobotType.LUMBERJACK, Team.A);
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
        expectedTreeHealth -= GameConstants.LUMBERJACK_CHOP_DAMAGE;
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
            assertFalse(rc.canPlantTree(Direction.getWest())); // tree in way
            assertTrue(rc.canPlantTree(Direction.getNorth())); // unobstructed
            assertTrue(rc.canPlantTree(Direction.getEast())); // unobstructed
            rc.plantTree(Direction.getEast());
            assertFalse(rc.canMove(Direction.getEast())); // tree now in the way
            assertFalse(rc.canPlantTree(Direction.getNorth())); // has already planted this turn
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
            teamAexpected += (GameConstants.BULLET_TREE_MAX_HEALTH-i*GameConstants.BULLET_TREE_DECAY_RATE)*GameConstants.BULLET_TREE_BULLET_PRODUCTION_RATE;
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
            TreeInfo[] neutralTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
            assertEquals(neutralTrees.length,1);

            // No watering neutral trees
            assertFalse(rc.canWater(neutralTrees[0].getID()));
            assertFalse(rc.canWater(neutralTrees[0].getLocation()));

            // Atempt to water a neutral tree
            boolean exception = false;
            try{
                rc.water(neutralTrees[0].ID);
            } catch (GameActionException e) {
                exception = true;
            }
            assertTrue(exception);
        });
    }

    @Test // Normal robots blocked by trees and other robots, drones fly over but blocked by other drones
    public void obstructionTest() throws GameActionException {
        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 10, 10, 1337, 100)
                .build();

        // This creates the actual game.
        TestGame game = new TestGame(map);

        final int archonA = game.spawn(4, 5, RobotType.ARCHON, Team.A);
        final int scoutA = game.spawn(7.5f, 5, RobotType.SCOUT, Team.A);
        final int scoutB = game.spawn(7.5f, 2, RobotType.SCOUT, Team.A);
        final int neutralTree = game.spawnTree(9f,5, 1, Team.NEUTRAL, 0, null);
        game.waitRounds(20);

        MapLocation originalArchonALoc = game.getWorld().getObjectInfo().getRobotByID(archonA).getLocation();
        MapLocation scoutBLoc = game.getWorld().getObjectInfo().getRobotByID(scoutB).getLocation();
        MapLocation neutralTreeLoc = game.getWorld().getObjectInfo().getTreeByID(neutralTree).getLocation();

        // Scout can move over trees, but not other robots
        game.round((id, rc) -> {
            if (id != scoutA) return;

            assertFalse(rc.canMove(originalArchonALoc));
            assertFalse(rc.canMove(scoutBLoc));
            assertTrue(rc.canMove(neutralTreeLoc)); // Scouts can go over trees
            rc.move(neutralTreeLoc);
        });

        game.round((id, rc) -> {
            if (id != scoutA) return;
            assertTrue(rc.canMove(neutralTreeLoc)); // Scouts can go over trees
            rc.move(neutralTreeLoc);
        });

        // Scout can't go off the map
        game.round((id, rc) -> {
            if (id != scoutA) return;

            assertFalse(rc.canMove(Direction.getEast(),0.01f)); // Off the map
            assertTrue(rc.canMove(Direction.getNorth()));
            rc.move(Direction.getNorth());  // Move away from tree
        });

        // Move Archon closer to tree
        int numMoves = 0;
        MapLocation currentArchonALoc = null;
        do {
            game.round((id, rc) -> {
                if (id != archonA) return;

                assertTrue(rc.canMove(neutralTreeLoc));
                rc.move(neutralTreeLoc);    // Move towards the tree

            });
            numMoves++;
            currentArchonALoc = game.getWorld().getObjectInfo().getRobotByID(archonA).getLocation();
            assertEquals(currentArchonALoc.distanceTo(neutralTreeLoc), originalArchonALoc.distanceTo(neutralTreeLoc) - RobotType.ARCHON.strideRadius*numMoves, EPSILON);
        } while (currentArchonALoc.distanceTo(neutralTreeLoc) > RobotType.ARCHON.bodyRadius+1+RobotType.ARCHON.strideRadius);

        // Move Archon to be just out of tree
        game.round((id, rc) -> {
            if (id != archonA) return;
            assertTrue(rc.canMove(rc.getLocation().directionTo(neutralTreeLoc),rc.getLocation().distanceTo(neutralTreeLoc)-(RobotType.ARCHON.bodyRadius+1+0.0001f)));
            rc.move(rc.getLocation().directionTo(neutralTreeLoc),rc.getLocation().distanceTo(neutralTreeLoc)-(RobotType.ARCHON.bodyRadius+1+0.0001f));    // Move towards the tree
            assertEquals(rc.getLocation().distanceTo(neutralTreeLoc),RobotType.ARCHON.bodyRadius+1+0.0001f,EPSILON);
        });

        // Archon can't go over tree
        game.round((id, rc) -> {
            if (id != archonA) return;
            assertFalse(rc.canMove(rc.getLocation().directionTo(neutralTreeLoc),0.001f));
        });
    }

    @Test // Bullet collision works continuously and not at discrete intervals
    public void continuousBulletCollisionTest() throws GameActionException {
        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 12, 10, 1337, 100)
                .build();

        // This creates the actual game.
        TestGame game = new TestGame(map);

        // Create some units
        final int soldierA = game.spawn(3, 5.01f +(RobotType.SOLDIER.bodyRadius-RobotType.SOLDIER.strideRadius), RobotType.SOLDIER, Team.A);
        final int soldierB = game.spawn(9, 5, RobotType.SOLDIER, Team.B);
        final int soldierB2 = game.spawn(10f,6.8f,RobotType.SOLDIER, Team.B);
        game.waitRounds(20);    // Wait for bots to mature to full health

        MapLocation soldierBLocation = game.getBot(soldierB).getLocation();
        // topOfSoldierB is a location just near the top edge of soldierB.
        // if discrete bullet position checking is used, the bullet will clip though some of the tests.
        MapLocation topOfSoldierB = soldierBLocation.add(Direction.getNorth(),RobotType.SOLDIER.bodyRadius - 0.01f);

        final float testInterval = 0.01f;
        for(float i=0; i<1; i+=testInterval){
            // soldierA fires a shot at soldierB, and moves a small amount closer.
            // Move before firing so it doesn't step into it's own bullet
            game.round((id, rc) -> {
                if (id != soldierA) return;
                rc.move(Direction.EAST,testInterval);
                rc.fireSingleShot(rc.getLocation().directionTo(topOfSoldierB));
            });
            game.waitRounds(5); // Bullet propagation

            // SoldierB should get hit every time (bullet never clips through)
            assertEquals(game.getBot(soldierB).getHealth(), RobotType.SOLDIER.maxHealth - RobotType.SOLDIER.attackPower, EPSILON);
            game.getBot(soldierB).repairRobot(10); // Repair back to full health so it doesn't die

            // SoldierB2 should never get hit
            assertEquals(game.getBot(soldierB).getHealth(), RobotType.SOLDIER.maxHealth, EPSILON);
        }

        // Now check cases where it shouldn't hit soldierB
        game.round((id, rc) -> {
            if (id != soldierA) return;
            rc.move(Direction.getNorth(),RobotType.SOLDIER.strideRadius);
            rc.fireSingleShot(Direction.getEast()); // Shoot a bullet parallel, slightly above soldierB
        });
        game.waitRounds(5); // Bullet propagation

        // Bullet goes over soldierB
        assertEquals(game.getBot(soldierB).getHealth(), RobotType.SOLDIER.maxHealth, EPSILON);
        // ...and hits soldier B2
        assertEquals(game.getBot(soldierB2).getHealth(), RobotType.SOLDIER.maxHealth - RobotType.SOLDIER.attackPower, EPSILON);

        // Test shooting off the map
        game.round((id, rc) -> {
            if (id == soldierA)
                rc.fireSingleShot(Direction.getEast()); // Shoot a bullet parallel, slightly above soldierB
            else if (id == soldierB2)
                rc.move(Direction.getNorth());  // Move out of way so soldierA can shoot off the map
        });

        float bulletDistanceToWall = 12-game.getWorld().getObjectInfo().bulletsArray()[0].getLocation().x;
        int turnsToApproachWall = (int)Math.floor(bulletDistanceToWall/RobotType.SOLDIER.bulletSpeed);
        game.waitRounds(turnsToApproachWall); // Bullet close to wall
        assertEquals(game.getBot(soldierB).getHealth(), RobotType.SOLDIER.maxHealth, EPSILON);
        // Bullet should still be in game
        assertEquals(game.getWorld().getObjectInfo().bullets().size(),1);
        game.waitRounds(1);
        // Bullet should hit wall and die
        assertEquals(game.getWorld().getObjectInfo().bullets().size(),0);
    }

    @Test // Buying victory points
    public void victoryPointTest() throws GameActionException {
        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 10, 10, 1337, 100)
                .build();

        // This creates the actual game.
        TestGame game = new TestGame(map);
        final int archonA = game.spawn(8, 5, RobotType.GARDENER, Team.A);
        final int archonB = game.spawn(2, 5, RobotType.GARDENER, Team.B);

        game.round((id, rc) -> {
            if (id != archonA) return;
            rc.donate(rc.getVictoryPointCost()*10);
            assertEquals(rc.getTeamBullets(),GameConstants.BULLETS_INITIAL_AMOUNT-rc.getVictoryPointCost()*10,EPSILON);
            assertEquals(rc.getTeamVictoryPoints(),10);
            rc.donate(rc.getVictoryPointCost()-0.1f);
            rc.donate(rc.getVictoryPointCost()-0.1f);
            assertEquals(rc.getTeamBullets(),GameConstants.BULLETS_INITIAL_AMOUNT-rc.getVictoryPointCost()*12+0.2f,1E-4);
            assertEquals(rc.getTeamVictoryPoints(),10);

            // Try to donate negative bullets, should fail.
            boolean exception = false;
            try {
                rc.donate(-1);
            } catch (GameActionException e) {
                exception = true;
            }
            assertTrue(exception);

            // Try to donate more than you have, should fail.
            exception = false;
            try {
                rc.donate(rc.getTeamBullets()+0.1f);
            } catch (GameActionException e) {
                exception = true;
            }
            assertTrue(exception);
        });

        // No winner yet
        assertEquals(game.getWorld().getWinner(),null);

        game.round((id, rc) -> {
            if(id != archonA) return;

            // Give TeamA lots of bullets
            game.getWorld().getTeamInfo().adjustBulletSupply(Team.A,GameConstants.VICTORY_POINTS_TO_WIN*rc.getVictoryPointCost());

            rc.donate(rc.getTeamBullets());
        });

        // Team A should win
        assertEquals(game.getWorld().getWinner(),Team.A);
        // ...by victory point threshold
        assertEquals(game.getWorld().getGameStats().getDominationFactor(), DominationFactor.PHILANTROPIED);

    }

    @Test // Test goodies inside trees
    public void testTreeGoodies() throws GameActionException {
        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 10, 10, 1337, 100)
                .build();

        // This creates the actual game.
        TestGame game = new TestGame(map);

        final int lumberjackA = game.spawn(5,5,RobotType.LUMBERJACK,Team.A);
        final int neutralTree1 = game.spawnTree(8,5,1,Team.NEUTRAL,123,null);
        final int neutralTree2 = game.spawnTree(2,5,1,Team.NEUTRAL, 0, RobotType.SOLDIER);
        final int neutralTree3 = game.spawnTree(5,8,1,Team.NEUTRAL,123,RobotType.SOLDIER);
        final int scoutA = game.spawn(2,5,RobotType.SCOUT,Team.A); // on top of tree to test if it dies
        final int scoutB = game.spawn(2,2,RobotType.SCOUT,Team.B); // needed so game doesn't declare a winner every time a robot dies (oops that took a while)

        game.waitRounds(20);    // Allow robots to mature

        game.round((id, rc) -> {
            if(id != lumberjackA) return;
            TreeInfo[] nearbyTrees = rc.senseNearbyTrees(-1,Team.NEUTRAL);
            assertEquals(nearbyTrees.length,3);
            int treesWithBullets=0;
            int treesWithBots=0;
            for(TreeInfo tree : nearbyTrees) {
                if(tree.containedBullets > 0)
                    treesWithBullets++;
                if(tree.containedRobot != null)
                    treesWithBots++;
            }
            assertEquals(treesWithBots,2);
            assertEquals(treesWithBullets,2);
            rc.chop(neutralTree1);
        });
        // While tree is not dead, continue hitting it
        while(game.getTree(neutralTree1).getHealth() > GameConstants.LUMBERJACK_CHOP_DAMAGE) {
            game.round((id, rc) -> {
                if(id != lumberjackA) return;
                rc.chop(neutralTree1);
            });
        }
        // Bullets before final blow
        assertEquals(game.getWorld().getTeamInfo().getBulletSupply(Team.A),GameConstants.BULLETS_INITIAL_AMOUNT,EPSILON);
        // Kill the tree
        game.round((id, rc) -> {
            if(id != lumberjackA) return;
            rc.chop(neutralTree1);
        });
        // Bullets rewarded after it dies
        assertEquals(game.getWorld().getTeamInfo().getBulletSupply(Team.A),GameConstants.BULLETS_INITIAL_AMOUNT+123,EPSILON);

        // While tree2 is not dead, continue hitting it
        while(game.getTree(neutralTree2).getHealth() > GameConstants.LUMBERJACK_CHOP_DAMAGE) {
            game.round((id, rc) -> {
                if(id != lumberjackA) return;
                rc.chop(neutralTree2);
            });
        }
        // Two active robots before killing the robot-containing tree
        assertEquals(game.getWorld().getObjectInfo().getRobotCount(Team.A),2);
        assertEquals(game.getWorld().getObjectInfo().getRobotCount(Team.B),1);
        assertEquals(game.getWorld().getObjectInfo().getRobotCount(Team.NEUTRAL),0);
        assertEquals(game.getWorld().getObjectInfo().getRobotAtLocation(new MapLocation(2,5)).getType(),RobotType.SCOUT);

        // Kill the tree
        game.round((id, rc) -> {
            if(id != lumberjackA) return;
            RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
            assertEquals(nearbyRobots.length,2);
            RobotInfo testRobot = rc.senseRobotAtLocation(new MapLocation(2,5));
            assertEquals(nearbyRobots[0].getType(),RobotType.SCOUT);

            rc.chop(neutralTree2);

            // New robot should exist immediately
            nearbyRobots = rc.senseNearbyRobots();
            assertEquals(nearbyRobots.length,2);
            testRobot = rc.senseRobotAtLocation(new MapLocation(2,5));
            assertEquals(testRobot.getType(),RobotType.SOLDIER);
            assertEquals(testRobot.getTeam(),Team.A);
        });
        // Two robots should exist after it dies
        assertEquals(game.getWorld().getObjectInfo().getRobotCount(Team.A),2);
        assertEquals(game.getWorld().getObjectInfo().getRobotCount(Team.B),1);
        assertEquals(game.getWorld().getObjectInfo().getRobotCount(Team.NEUTRAL),0);
        assertEquals(game.getWorld().getObjectInfo().getRobotAtLocation(new MapLocation(2,5)).getType(),RobotType.SOLDIER);

        TIntArrayList ids = new TIntArrayList();

        // Make sure the new robot runs player code
        game.round((id, rc) -> {
            if(id != lumberjackA && rc.getType() == RobotType.SOLDIER) {
                ids.add(id);
                assertTrue(rc.getType().equals(RobotType.SOLDIER));
                TreeInfo trees[] = rc.senseNearbyTrees(-1,Team.NEUTRAL);
                assertEquals(trees.length,1);
                rc.fireSingleShot(rc.getLocation().directionTo(trees[0].getLocation()));
            }
        });
        System.out.print(ids);
        assertEquals(ids.size(),1);

        assertEquals(game.getWorld().getObjectInfo().getAllBulletsWithinRadius(new MapLocation(2,5),10).length,1);

        // Last tree should get hit and lose health
        game.waitRounds(2);

        assertEquals(game.getWorld().getObjectInfo().getAllBulletsWithinRadius(new MapLocation(2,5),10).length,0);

        // Soldier should have damaged last tree
        assertEquals(game.getTree(neutralTree3).getHealth(),GameConstants.NEUTRAL_TREE_HEALTH_RATE-RobotType.SOLDIER.attackPower,EPSILON);

        // While tree3 is not dead, continue shooting it
        while(game.getTree(neutralTree3).getHealth() > RobotType.SOLDIER.attackPower) {
            game.round((id, rc) -> {
                if(id != lumberjackA) {
                    TreeInfo trees[] = rc.senseNearbyTrees(-1,Team.NEUTRAL);
                    assertEquals(trees.length,1);
                    rc.fireSingleShot(rc.getLocation().directionTo(trees[0].getLocation()));
                }
            });
        }
        // Tree alive before bullets propagate
        assertEquals(game.getWorld().getObjectInfo().getTreeCount(Team.NEUTRAL),1);
        float initialBullets = game.getWorld().getTeamInfo().getBulletSupply(Team.A);

        game.waitRounds(3); // Bullets propagate

        // Tree should be gone
        assertEquals(game.getWorld().getObjectInfo().getTreeCount(Team.NEUTRAL),0);

        // No additional robots added
        assertEquals(game.getWorld().getObjectInfo().getRobotCount(Team.A),2);
        assertEquals(game.getWorld().getObjectInfo().getRobotCount(Team.B),1);
        assertEquals(game.getWorld().getObjectInfo().getRobotCount(Team.NEUTRAL),0);

        // No additional bullets
        assertEquals(initialBullets,game.getWorld().getTeamInfo().getBulletSupply(Team.A),EPSILON);
    }

    @Test
    public void testNullSense() throws GameActionException {
        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 10, 10, 1337, 100)
                .build();

        // This creates the actual game.
        TestGame game = new TestGame(map);

        final int soldierA = game.spawn(3, 5, RobotType.SOLDIER, Team.A);
        final int soldierB = game.spawn(7, 5, RobotType.SOLDIER, Team.B);

        game.round((id, rc) -> {
            if(id != soldierA) return;

            RobotInfo actualBot = rc.senseRobotAtLocation(new MapLocation(3,5));
            RobotInfo nullBot = rc.senseRobotAtLocation(new MapLocation(5,7));

            assertNotEquals(actualBot,null);
            assertEquals(nullBot,null);
        });
    }

    @Test
    public void testDirections() throws GameActionException {
        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 10, 10, 1337, 100)
                .build();

        // This creates the actual game.
        TestGame game = new TestGame(map);

        final int soldierA = game.spawn(3, 5, RobotType.SCOUT, Team.A);
        final int neutralTree = game.spawnTree(5,5,1,Team.NEUTRAL,0,null);

        game.round((id, rc) -> {
            if (id != soldierA) return;

            // Silly Direction sanity checks
            for (int i = 0; i < 3; i++) {
                assertEquals(new Direction(0.1f).radians, new Direction(0.1f).rotateLeftRads((float) (2 * Math.PI * i)).radians, EPSILON);
                assertEquals(new Direction(-0.1f).radians, new Direction(-0.1f).rotateLeftRads((float) (2 * Math.PI * i)).radians, EPSILON);
                assertEquals(new Direction(0.1f).radians, new Direction(0.1f).rotateRightRads((float) (2 * Math.PI * i)).radians, EPSILON);
                assertEquals(new Direction(-0.1f).radians, new Direction(-0.1f).rotateRightRads((float) (2 * Math.PI * i)).radians, EPSILON);
            }

            // Ensure range (-Math.PI,Math.PI]
            Direction testDir = Direction.getNorth();
            float testRads = testDir.radians;
            Direction fromRads = new Direction(testRads);
            for (int i = 0; i < 200; i++) {
                testDir = testDir.rotateLeftDegrees(i);
                // Stays within range
                assertTrue(Math.abs(testDir.radians) <= Math.PI);

                // Direction.reduce() functionality works
                testRads += Math.toRadians(i);
                fromRads = new Direction(testRads);
                assertTrue(testDir.equals(fromRads,0.0001f)); // silly rounding errors can accumulate, so larger epsilon
            }
        });

        // Test from ndefilippis
        Direction d = new Direction((float) Math.PI);
        assertEquals(d.radians, Math.PI, 1E-7);

        // Equals override test
        assertTrue(Direction.getNorth().equals(Direction.getNorth()));
        assertFalse(Direction.getNorth().equals(Direction.getEast()));
        assertFalse(Direction.getNorth().equals(Direction.getEast(),0.01f));
        assertTrue(Direction.getNorth().equals(Direction.getEast(),(float)Math.PI/2+0.01f));
        assertTrue(Direction.NORTH.equals(Direction.getNorth()));
    }

    @Test
    public void overlappingScoutTest() throws GameActionException {
        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 10, 10, 1337, 100)
                .build();

        // This creates the actual game.
        TestGame game = new TestGame(map);

        final int scoutA = game.spawn(3, 5, RobotType.SCOUT, Team.A);
        final int neutralTree = game.spawnTree(5,5,1,Team.NEUTRAL,0,null);

        game.round((id, rc) -> {
            if (id != scoutA) return;

            TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
            rc.move(nearbyTrees[0].getLocation());

            boolean exception = false;
            try {
                nearbyTrees = rc.senseNearbyTrees();
            } catch (Exception e) {
                System.out.println("Scout threw an error when trying to sense tree at its location, this shouldn't happen");
                exception = true;
            }
            assertFalse(exception);

            MapLocation loc1 = new MapLocation(5, 5);
            MapLocation loc2 = loc1.add(null, 5);
            assertEquals(loc1, loc2);
        });
    }

    @Test
    public void testShakeInsideTree() throws GameActionException {
        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 10, 10, 1337, 100)
                .build();

        // This creates the actual game.
        TestGame game = new TestGame(map);

        final int scoutA = game.spawn(1, 5, RobotType.SCOUT, Team.A);
        final int neutralTree = game.spawnTree(5,5,5,Team.NEUTRAL,0,null);

        for(int i=0; i<8; i++) {
            game.round((id, rc) -> {
                if (id != scoutA) return;

                // I can shake the tree I am on
                assertTrue(rc.canShake(neutralTree));

                // I can see the tree I am on
                TreeInfo[] sensedTrees = rc.senseNearbyTrees(0.1f);
                assertEquals(sensedTrees.length, 1);

                // I can shake the tree I am on based on location
                assertTrue(rc.canShake(rc.getLocation()));

                // I can shake this tree in the same ways from another location
                assertTrue(rc.canMove(Direction.getEast(), 1));
                rc.move(Direction.getEast(), 1);
            });
        }
    }

    @Test
    public void testNullIsCircleOccupied() throws GameActionException {

        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 10, 10, 1337, 100)
                .build();

        // This creates the actual game.
        TestGame game = new TestGame(map);

        final int gardener = game.spawn(5, 5, RobotType.GARDENER, Team.A);


        game.round((id, rc) -> {
            if(id != gardener) return;

            boolean exception = false;
            try {
                assertFalse(rc.isCircleOccupiedExceptByThisRobot(rc.getLocation(), 3));
            } catch(Exception e) {
                exception = true;
            }
            assertFalse(exception);
        });
    }

    @Test
    public void hitScoutsBeforeTrees() throws GameActionException {
        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 10, 10, 1337, 100)
                .build();

        // This creates the actual game.
        TestGame game = new TestGame(map);

        // In case scouts are on top of trees with radius 1, hit scout first
        final int soldierA = game.spawn(5,5,RobotType.SOLDIER,Team.A);
        final int scoutB = game.spawn(8,5,RobotType.SCOUT,Team.B);
        final int neutralTree1 = game.spawnTree(8,5,1,Team.NEUTRAL,123,null);
        game.waitRounds(20); // Let them mature

        // Fire shot at tree/soldier combo
        game.round((id, rc) -> {
            if (id != soldierA) return;
            rc.fireSingleShot(rc.getLocation().directionTo(new MapLocation(8,5)));
        });
        game.waitRounds(1);
        // Scout gets hit, tree does not
        assertEquals(game.getBot(scoutB).getHealth(),RobotType.SCOUT.maxHealth-RobotType.SOLDIER.attackPower, EPSILON);
        assertEquals(game.getTree(neutralTree1).getHealth(),GameConstants.NEUTRAL_TREE_HEALTH_RATE, EPSILON);
    }

    // Check to ensure execution order is equal to spawn order
    @Test
    public void executionOrderTest() throws GameActionException {
        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 50, 10, 1337, 100)
                .build();

        // This creates the actual game.
        TestGame game = new TestGame(map);

        final int TEST_UNITS = 10;

        int[] testIDs = new int[TEST_UNITS];

        for(int i=0; i<TEST_UNITS; i++) {
            testIDs[i] = game.spawn(2+i*3,5,RobotType.SOLDIER,Team.A);
        }
        final int archonA = game.spawn(40,5,RobotType.ARCHON,Team.A);
        final int gardenerA = game.spawn(46,5,RobotType.GARDENER,Team.A);

        TIntArrayList executionOrder = new TIntArrayList();

        game.round((id, rc) -> {
            if(rc.getType() == RobotType.SOLDIER) {
                executionOrder.add(id);
            } else if (id == archonA) {
                assertTrue(rc.canHireGardener(Direction.getEast()));
                rc.hireGardener(Direction.getEast());
            } else if (id == gardenerA) {
                assertTrue(rc.canBuildRobot(RobotType.LUMBERJACK,Direction.getEast()));
            } else {
                // If either the spawned gardener or the lumberjack run code in the first round, this will fail.
                assertTrue(false);
            }
        });

        // Assert IDs aren't in order (random change, but very unlikely unless something is wrong)
        boolean sorted = true;
        for(int i=0; i<TEST_UNITS-1; i++) {
            if (testIDs[i] < testIDs[i+1])
                sorted = false;
        }
        assertFalse(sorted);


        // Assert execution IS in order
        for(int i=0; i<TEST_UNITS; i++) {
            assertEquals(testIDs[i],executionOrder.get(i));
        }
    }

    @Test
    public void noHealing() throws GameActionException {

        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 10, 10, 1337, 100)
                .build();

        // This creates the actual game.
        TestGame game = new TestGame(map);

        final int gardener = game.spawn(5,5,RobotType.GARDENER,Team.A);
        final int archon = game.spawn(5,2,RobotType.ARCHON,Team.A);
        final int soldier = game.spawn(5,8,RobotType.SOLDIER,Team.A);

        assertEquals(game.getBot(gardener).getHealth(),RobotType.GARDENER.maxHealth,EPSILON);
        assertEquals(game.getBot(archon).getHealth(),RobotType.ARCHON.maxHealth,EPSILON);
        assertEquals(game.getBot(soldier).getHealth(),RobotType.SOLDIER.getStartingHealth(),EPSILON);

        game.getBot(gardener).damageRobot(10);
        game.getBot(archon).damageRobot(10);

        game.waitRounds(20);

        // Gardener and Archon should not heal in first 20 turns
        assertEquals(game.getBot(gardener).getHealth(),RobotType.GARDENER.maxHealth-10,EPSILON);
        assertEquals(game.getBot(archon).getHealth(),RobotType.ARCHON.maxHealth-10,EPSILON);
        assertEquals(game.getBot(soldier).getHealth(),RobotType.SOLDIER.maxHealth,EPSILON);
    }

    @Test
    public void sensingEachOtherTest() throws GameActionException {
        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 50, 50, 1337, 100)
                .build();

        TestGame game = new TestGame(map);

        final int tankA = game.spawn(10, 10, RobotType.TANK, Team.A);
        final int soldierB = game.spawn(10, (float) 18.4534432, RobotType.SOLDIER, Team.B);

        game.waitRounds(50);


        // Soldier can see tank
        game.round((id, rc) -> {
            if (id == soldierB) {
                RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
                assertEquals(robots.length, 1);
                rc.fireSingleShot(rc.getLocation().directionTo(robots[0].getLocation()));
                assertEquals(rc.senseNearbyBullets(-1).length,1);
            }
        });

        // Tank can't see soldier, but can see its bullet
        game.round((id, rc) -> {
            if (id == tankA) {
                RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
                assertEquals(robots.length, 0);
                assertEquals(rc.senseNearbyBullets(-1).length,1);
                rc.fireSingleShot(Direction.EAST);
                assertEquals(rc.senseNearbyBullets(-1).length,2);
            }
        });
    }

    @Test
    public void turnOrderTest() throws GameActionException {
        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 50, 50, 1337, 100)
                .build();

        TestGame game = new TestGame(map);

        // Spawn two tanks close enough such that a bullet fired from one
        // at the other will hit after updating once.
        final int tankA = game.spawn(10, 10, RobotType.TANK, Team.A);
        final int tankB = game.spawn(15, 10, RobotType.TANK, Team.B);

        game.waitRounds(50);

        game.round((id, rc) -> {
            if (id == tankA) {
                // Fire a bullet.
                assertEquals(rc.senseNearbyBullets(-1).length,0);
                rc.fireSingleShot(Direction.EAST);
                assertEquals(rc.senseNearbyBullets(-1).length,1);
            } else if (id == tankB) {
                // The other bullet should have fired, but not yet moved.
                assertEquals(rc.senseNearbyBullets(-1).length,1);
                rc.fireSingleShot(Direction.WEST);
                assertEquals(rc.senseNearbyBullets(-1).length,2);
            }
        });

        game.round((id, rc) -> {
            if (id == tankA) {
                // The bullet fired by this tank last round should
                // now have hit the other tank.
                assertEquals(rc.senseNearbyBullets(-1).length,1);
                assertEquals(rc.senseRobot(tankB).health,
                             RobotType.TANK.maxHealth - RobotType.TANK.attackPower, 0.00001);
            } else if (id == tankB) {
                // Both bullets should now have updated.
                assertEquals(rc.senseNearbyBullets(-1).length,0);
                assertEquals(rc.senseRobot(tankA).health,
                             RobotType.TANK.maxHealth - RobotType.TANK.attackPower, 0.00001);
            }
        });
    }

    @Test
    public void testImmediateCollisionDetection() throws GameActionException {
        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 10, 10, 1337, 100)
                .build();

        // This creates the actual game.
        TestGame game = new TestGame(map);

        final int soldierA = game.spawn(2.99f,5,RobotType.SOLDIER,Team.A);
        final int soldierB = game.spawn(5,5,RobotType.SOLDIER,Team.B);

        game.waitRounds(20); // Let units mature

        game.round((id, rc) -> {
            if (id == soldierA) {
                rc.fireSingleShot(Direction.EAST);
                RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
                assertEquals(nearbyRobots.length,1);
                // Damage is done immediately
                assertEquals(nearbyRobots[0].getHealth(),RobotType.SOLDIER.maxHealth-RobotType.SOLDIER.attackPower,EPSILON);
            }
        });

        game.getBot(soldierB).damageRobot(RobotType.SOLDIER.maxHealth-RobotType.SOLDIER.attackPower-1);

        game.round((id, rc) -> {
            if (id == soldierA) {
                rc.fireSingleShot(Direction.EAST);
                RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
                assertEquals(nearbyRobots.length,0);
                // Damage is done immediately and robot is dead
                assertTrue(rc.canMove(Direction.EAST));
            }
        });
    }

    @Test
    public void testGetXAtLocation() throws GameActionException {
        LiveMap map = new TestMapBuilder("test", new MapLocation(0,0), 10, 10, 1337, 100)
                .build();

        // This creates the actual game.
        TestGame game = new TestGame(map);

        final int soldierA = game.spawn(2.9f,6,RobotType.SOLDIER,Team.A);
        final int tankB = game.spawn(6,6,RobotType.TANK,Team.B);
        final int tree1 = game.spawnTree(2.9f,2, 2, Team.NEUTRAL,0,null);
        final int tree2 = game.spawnTree(6,2, 1, Team.NEUTRAL,0,null);

        game.round((id, rc) -> {
            if (id == soldierA) {
                RobotInfo bot = rc.senseRobotAtLocation(new MapLocation(2.9f+0.9f,6));
                assertNotNull(bot);
                assertEquals(bot.getType(),RobotType.SOLDIER);
                bot = rc.senseRobotAtLocation(new MapLocation(6f-1.9f,6));
                assertNotNull(bot);
                assertEquals(bot.getType(),RobotType.TANK);

                TreeInfo tree = rc.senseTreeAtLocation(new MapLocation(2.9f+1.9f,2));
                assertNotNull(tree);
                assertEquals(tree.getID(),tree1);
                tree = rc.senseTreeAtLocation(new MapLocation(6f-0.9f,2));
                assertNotNull(tree);
                assertEquals(tree.getID(),tree2);

            }
        });
    }
}
