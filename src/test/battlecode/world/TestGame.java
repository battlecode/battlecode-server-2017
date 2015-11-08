package battlecode.world;

import battlecode.common.*;

import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Ignore;

/**
 * TestGame holds a GameWorld and contains utility methods to spawn units and execute rounds. The main purpose is to
 * make it easier to write tests for RobotController.
 *
 * Using this and TestMapGenerator, it becomes easier to set up game scenarios and execute RobotController commands. A
 * basic test will have the following flow. First, we create a TestMapGenerator to create a GameMap. We create a
 * TestGame around this GameMap and spawn some units. Then, we use the turn method to execute RobotController commands.
 * During these turns, we have asserts to test that the behaviors are correct.
 */
@Ignore
public class TestGame {
    /** The game world that everything is based on. */
    private GameWorld world;
    /** Map from IDs to robots. */
    private TreeMap<Integer, InternalRobot> robots = new TreeMap<>();
    /** Map from IDs to robot controllers. */
    private TreeMap<Integer, RobotController> rcs = new TreeMap<>();

    /**
     * Creates a test game with the given map.
     *
     * @param map the game map
     */
    public TestGame(GameMap map) {
        long[][] teamMemory = new long[2][GameConstants.TEAM_MEMORY_LENGTH];
        world = new GameWorld(map, "A", "B", teamMemory);
    }

    /**
     * Creates a test game with the given map and team memory.
     *
     * @param map the game map
     * @param memory the previous round's team memory
     */
    public TestGame(GameMap map, long[][] memory) {
        world = new GameWorld(map, "A", "B", memory);
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
     * @return the ID of the robot spawned
     */
    public int spawn(int x, int y, RobotType type, Team team) {
        InternalRobot robot = new InternalRobot(this.world, type, new MapLocation(x, y), team, false, 0);

        RobotControllerImpl rc = new RobotControllerImpl(world, robot);

        int id = robot.getID();
        robots.put(id, robot);
        rcs.put(id, rc);

        return id;
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
        world.processBeginningOfRound();
        for (Integer id : rcs.keySet()) {
            robots.get(id).processBeginningOfTurn();
            f.accept(id, rcs.get(id));
            robots.get(id).processEndOfTurn();
        }
        world.processEndOfRound();
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
        this.round((fId, fRC) -> {
            if (fId == id) {
                f.accept(fRC);
            }
        });
    }
}
