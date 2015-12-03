package battlecode.world;

import battlecode.common.*;
import battlecode.world.control.NullControlProvider;
import battlecode.world.control.RobotControlProvider;
import org.junit.Ignore;

import java.util.function.BiConsumer;
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

    /** The function to run on robots. **/
    private Consumer<InternalRobot> runRobot;

    /**
     * Creates a test game with the given map.
     *
     * @param map the game map
     */
    public TestGame(GameMap map) {
        long[][] teamMemory = new long[2][GameConstants.TEAM_MEMORY_LENGTH];
        world = new GameWorld(map, new NullControlProvider(), "A", "B", teamMemory);
    }

    /**
     * Creates a test game with the given map and team memory.
     *
     * @param map the game map
     * @param memory the previous round's team memory
     */
    public TestGame(GameMap map, long[][] memory) {
        world = new GameWorld(map, new NullControlProvider(), "A", "B", memory);
    }

    /**
     * Returns the x coordinate of the map origin.
     *
     * @return the x coordinate of the map origin.
     */
    public int getOriginX() {
        return world.getGameMap().getMapOrigin().x;
    }

    /**
     * Returns the y coordinate of the map origin.
     *
     * @return the y coordinate of the map origin.
     */
    public int getOriginY() {
        return world.getGameMap().getMapOrigin().y;
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
    public void spawn(int x, int y, RobotType type, Team team) {
        world.spawnRobot(
                type,
                new MapLocation(x, y),
                team,
                0,
                null
        );
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
    public void round(BiConsumer<Integer, RobotController> f) {
        this.runRobot = (robot) -> {
            f.accept(robot.getID(), robot.getController());
        };

        world.runRound();
    }

    /**
     * Executes a round of gameplay, in which every robot does nothing except for the robot with the given ID. That
     * robot instead will use the input function f to execute actions. The function f must accept a RobotController
     * and use that RobotController to perform the actions.
     *
     * @param id the ID of the robot that performs actions
     * @param f a function that, given a RobotController, will perform the actions for the robot with the input ID
     */
    public void turn(int id, Consumer<RobotController> f) {
        this.runRobot = (robot) -> {
            if (id == robot.getID()) {
                f.accept(robot.getController());
            }
        };

        world.runRound();
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
            return 0;
        }

        @Override
        public boolean getTerminated(InternalRobot robot) {
            return false;
        }
    }
}
