package battlecode.world;

import battlecode.common.*;
import battlecode.server.GameMaker;
import battlecode.world.control.RobotControlProvider;
import org.junit.Ignore;
import org.mockito.Mockito;

import java.util.function.Consumer;

/**
 * TestGame holds a GameWorld and contains utility methods to spawn units and execute rounds. The main purpose is to
 * make it easier to write tests for RobotController.
 *
 * Using this and TestMapGenerator, it becomes easier to set up game scenarios and execute RobotController commands. A
 * basic test will have the following flow. First, we create a TestMapGenerator to create a GameMap. We create a
 * TestGame and GameWorld around this GameMap and spawn some units. Then, we use the turn method to execute
 * RobotController commands. During these turns, we have asserts to test that the behaviors are correct.
 */
@Ignore
public class TestGame {
    /** The game world that everything is based on. */
    private final GameWorld world;

    /** The function to run on robots. */
    private Consumer<InternalRobot> runRobot;

    /** Any exception thrown while running the game. */
    private GameActionException exception;

    /**
     * Creates a test game with the given map.
     *
     * @param map the game map
     */
    public TestGame(LiveMap map) {
        this(map, new long[2][GameConstants.TEAM_MEMORY_LENGTH]);
    }

    /**
     * Creates a test game with the given map and team memory.
     *
     * @param map the game map
     * @param memory the previous round's team memory
     */
    public TestGame(LiveMap map, long[][] memory) {
        world = new GameWorld(map,
                new TestControlProvider(),
                memory,
                // this is a hack.
                // there should be a cleaner way to do this?
                Mockito.mock(GameMaker.MatchMaker.class));
    }

    /**
     * Returns the x coordinate of the map origin.
     *
     * @return the x coordinate of the map origin.
     */
    public float getOriginX() {
        return world.getGameMap().getOrigin().x;
    }

    /**
     * Returns the y coordinate of the map origin.
     *
     * @return the y coordinate of the map origin.
     */
    public float getOriginY() {
        return world.getGameMap().getOrigin().y;
    }

    /**
     * Spawns a robot of the given type and team on the given location, and returns its ID. Does not spend resources,
     * and all units are created with no delays.
     *
     * @param x x coordinate for the spawn
     * @param y y coordinate for the spawn
     * @param type type of the robot to spawn
     * @param team team of the robot to spawn
     */
    public int spawn(float x, float y, RobotType type, Team team) {
        return world.spawnRobot(type, new MapLocation(x, y), team);
    }
    
    /**
     * Spawns a tree of the given parameters, and returns its ID.
     * 
     * @param x x coordinate for the spawn
     * @param y y coordinate for the spawn
     * @param radius radius of the tree
     * @param team team of the tree for bullet trees, Team.NEUTRAL for neutral trees
     * @param containedBullets any bullets contained as reward
     * @param containedRobot any robot contained as reward
     * @return
     */
    public int spawnTree(float x, float y, float radius, Team team, int containedBullets, RobotType containedRobot) {
        return world.spawnTree(team, radius, new MapLocation(x,y), containedBullets, containedRobot);
    }


    /**
     * A helper class to deal with the fact that it's tricky for Java lambdas
     * to handle checked exceptions. This is equivalent to a BiConsumer,
     * except that "accept" throws GameActionException.
     */
    public interface BiConsumerWithException {
        void accept(int id, RobotController rc) throws GameActionException;
    }

    /**
     * Executes a round of gameplay. Each robot will receive a turn, with the robots spawned earlier going first.
     *
     * To perform the actions of the round, the input function f will be called on each robot in spawn order, with the
     * arguments being the robot's ID and the robot's RobotController. Thus, the function f that you supply must
     * use the ID to determine the corresponding action that the robot takes on this turn, and perform the actions
     * using the RobotController.
     *
     * @param f a function that, given an integer ID and a RobotController, will perform the actions for the robot with
     *          that ID for the current turn
     */
    public void round(BiConsumerWithException f) throws
            GameActionException {
        this.runRobot = (robot) -> {
            try {
                f.accept(robot.getID(), robot.getController());
            } catch (GameActionException e) {
                exception = e;
            }
        };

        world.runRound();
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Skips a number of rounds. The game state will change, but no robots
     * will perform actions.
     *
     * @param n the number of rounds to skip.
     */
    public void waitRounds(int n) {
        this.runRobot = (robot) -> {
        };

        for (int i = 0; i < n; ++i) {
            world.runRound();
        }
    }

    /**
     * Returns the InternalRobot associated with a given ID.
     *
     * @param id the ID of the robot to query
     * @return the InternalRobot for the given ID
     */
    public InternalRobot getBot(int id) {
        return world.getObjectInfo().getRobotByID(id);
    }
    
    /**
     * Returns the InternalBullet associated with a given ID.
     * 
     * @param id the ID of the bullet to query
     * @return the InternalBullet for the given ID
     */
    public InternalBullet getBullet(int id) {
        return world.getObjectInfo().getBulletByID(id);
    }
    
    /**
     * Returns the InternalTree associated with a given ID
     * 
     * @param id the ID of the tree to query
     * @return the InteralTree for the given ID
     */
    public InternalTree getTree(int id) {
        return world.getObjectInfo().getTreeByID(id);
    }

    /**
     * Returns the GameWorld.
     *
     * @return the GameWorld
     */
    public GameWorld getWorld() {
        return world;
    }

    private class TestControlProvider implements RobotControlProvider {

        @Override
        public void matchStarted(GameWorld world) {}

        @Override
        public void matchEnded() {}

        @Override
        public void roundStarted() {}

        @Override
        public void roundEnded() {}

        @Override
        public void robotSpawned(InternalRobot robot) {}

        @Override
        public void robotKilled(InternalRobot robot) {}

        @Override
        public void runRobot(InternalRobot robot) {
            runRobot.accept(robot);
        }

        @Override
        public int getBytecodesUsed(InternalRobot robot) {
            // BytecodesUsed defaults to zero in InternalRobot, so this gives us
            // the ability to change bytecodesUsed in a test case without it being
            // written back to zero
            return robot.getBytecodesUsed();
        }

        @Override
        public boolean getTerminated(InternalRobot robot) {
            return false;
        }
    }
}
