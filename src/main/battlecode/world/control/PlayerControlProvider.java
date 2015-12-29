package battlecode.world.control;

import battlecode.instrumenter.IndividualClassLoader;
import battlecode.instrumenter.SandboxedRobotPlayer;
import battlecode.server.Config;
import battlecode.world.GameWorld;
import battlecode.world.InternalRobot;

import java.util.HashMap;
import java.util.Map;

/**
 * Controls robots with instrumented player code.
 * The point of contact between GameWorld and the instrumenter/scheduler
 * infrastructure.
 *
 * @author james
 */
public class PlayerControlProvider implements RobotControlProvider {

    public static final String PLAYER_CLASS_NAME = "RobotPlayer";

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
     * The name of the team (package) we're processing.
     */
    private String teamName;

    /**
     * Create a new PlayerControlProvider.
     */
    public PlayerControlProvider(String teamName) {
        this.teamName = teamName;
    }

    @Override
    public void matchStarted(GameWorld gameWorld) {
        this.sandboxes = new HashMap<>(); // GameWorld maintains order for us
        this.gameWorld = gameWorld;

        IndividualClassLoader.reset();
    }

     @Override
    public void matchEnded() {
         // TODO clean up threads?
    }

    @Override
    public void robotSpawned(InternalRobot robot) {
        final SandboxedRobotPlayer player = new SandboxedRobotPlayer(
                teamName,
                PLAYER_CLASS_NAME,
                robot.getController(),
                gameWorld.getMapSeed()
        );

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
