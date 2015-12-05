package battlecode.world.control;

import battlecode.engine.instrumenter.IndividualClassLoader;
import battlecode.engine.instrumenter.SandboxedRobotPlayer;
import battlecode.server.Config;
import battlecode.world.GameWorld;
import battlecode.world.InternalRobot;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controls robots with instrumented player code.
 * The point of contact between GameWorld and the instrumenter/scheduler
 * infrastructure.
 *
 * @author james
 */
public class PlayerControlProvider implements RobotControlProvider {

    /**
     * The sandboxed robot players we're using to control robots;
     * maps ids to sandboxes.
     */
    private Map<Integer, SandboxedRobotPlayer> sandboxes;

    /**
     * The GameWorld we're providing for.
     */
    private GameWorld gameWorld;

    /**
     * Create a new PlayerControlProvider.
     */
    public PlayerControlProvider() {}

    @Override
    public void matchStarted(GameWorld gameWorld) {
        this.sandboxes = new LinkedHashMap<>(); // maintain order!
        this.gameWorld = gameWorld;

        IndividualClassLoader.reset();
    }

     @Override
    public void matchEnded() {
         // TODO clean up threads?
    }

    @Override
    public void robotSpawned(InternalRobot robot) {
        final Config config = Config.getGlobalConfig();

        final SandboxedRobotPlayer player;

        switch (robot.getTeam()) {
            case A:
                player = new SandboxedRobotPlayer(config.get("bc.game.team-a"),
                        "RobotPlayer",
                        robot.getController(),
                        gameWorld.getMapSeed());
                break;
            case B:
                player = new SandboxedRobotPlayer(config.get("bc.game.team-b"),
                        "RobotPlayer",
                        robot.getController(),
                        gameWorld.getMapSeed());
                break;
            case ZOMBIE:
                player = new SandboxedRobotPlayer("ZombiePlayer",
                        "ZombiePlayer",
                        robot.getController(),
                        gameWorld.getMapSeed());
                break;
            default:
                throw new RuntimeException("Don't know how to create a neutral player??");
        }

        this.sandboxes.put(robot.getID(), player);
    }

    @Override
    public void robotKilled(InternalRobot robot) {
        // TODO check robot isn't running?

        assert this.sandboxes.containsKey(robot.getID());

        this.sandboxes.get(robot.getID()).terminate();
    }

    @Override
    public void roundStarted() {}

    @Override
    public void roundEnded() {}

    @Override
    public void runRobot(InternalRobot robot) {
        assert this.sandboxes.containsKey(robot.getID());

        final SandboxedRobotPlayer player = this.sandboxes.get(robot.getID());

        player.setBytecodeLimit(robot.getBytecodeLimit());

        player.step();
    }

    @Override
    public int getBytecodesUsed(InternalRobot robot) {
        assert this.sandboxes.containsKey(robot.getID());

        return this.sandboxes.get(robot.getID()).getBytecodesUsed();
    }

    @Override
    public boolean getTerminated(InternalRobot robot) {
        assert this.sandboxes.containsKey(robot.getID());

        return this.sandboxes.get(robot.getID()).getTerminated();
    }
}
