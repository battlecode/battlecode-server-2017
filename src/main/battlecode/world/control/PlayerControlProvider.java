package battlecode.world.control;

import battlecode.instrumenter.IndividualClassLoader;
import battlecode.instrumenter.InstrumentationException;
import battlecode.instrumenter.SandboxedRobotPlayer;
import battlecode.server.Config;
import battlecode.server.ErrorReporter;
import battlecode.world.GameWorld;
import battlecode.world.InternalRobot;

import java.net.URL;
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

    /**
     * The cache shared between the classloaders of all players
     * loaded by this control provider.
     */
    private final IndividualClassLoader.Cache sharedCache;

    /**
     * The sandboxed robot players we're using to control robots;
     * maps ids to sandboxes.
     *
     * When a sandbox has been terminated, its entry in the map
     * will have a value of null, so that the classloader it uses
     * can be reclaimed.
     */
    private final Map<Integer, SandboxedRobotPlayer> sandboxes;

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
     *
     * @param teamName the name / package of the team we're loading
     * @param teamURL the url of the classes for the team;
     *                null to load from the system classpath
     */
    public PlayerControlProvider(String teamName, URL teamURL) {
        this.teamName = teamName;
        this.sandboxes = new HashMap<>(); // GameWorld maintains order for us

        if (teamURL == null) {
            this.sharedCache = new IndividualClassLoader.Cache();
        } else {
            this.sharedCache = new IndividualClassLoader.Cache(teamURL);
        }
    }

    @Override
    public void matchStarted(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
    }

    @Override
    public void matchEnded() {
        for (final SandboxedRobotPlayer player : this.sandboxes.values()) {
           if (player != null && !player.getTerminated()) {
               player.terminate();
           }
        }
        this.sandboxes.clear();
        this.gameWorld = null;
    }

    @Override
    public void robotSpawned(InternalRobot robot) {
        try {
            final SandboxedRobotPlayer player = new SandboxedRobotPlayer(
                    teamName,
                    robot.getController(),
                    gameWorld.getMapSeed(),
                    sharedCache
            );
            this.sandboxes.put(robot.getID(), player);
        } catch (InstrumentationException e) {
            ErrorReporter.report("Error while loading player "+teamName+": "+e.getMessage(), false);
            robot.suicide();
        } catch (RuntimeException e) {
            ErrorReporter.report(e, true);
            robot.suicide();
        }

    }

    @Override
    public void robotKilled(InternalRobot robot) {
        // Note that a robot may be killed even if it is not in Sandboxes, if
        // there was an error while loading it.

        final SandboxedRobotPlayer player = this.sandboxes.get(robot.getID());

        if (player != null) {
            this.sandboxes.get(robot.getID()).terminate();
        }

        this.sandboxes.put(robot.getID(), null);
    }

    @Override
    public void roundStarted() {}

    @Override
    public void roundEnded() {}

    @Override
    public void runRobot(InternalRobot robot) {

        assert this.sandboxes.get(robot.getID()) != null;

        final SandboxedRobotPlayer player = this.sandboxes.get(robot.getID());

        if (player != null) {
            player.setBytecodeLimit(robot.getBytecodeLimit());
            player.step();
        }

    }

    @Override
    public int getBytecodesUsed(InternalRobot robot) {
        assert this.sandboxes.containsKey(robot.getID());

        final SandboxedRobotPlayer player = this.sandboxes.get(robot.getID());

        if (player != null) {
            return player.getBytecodesUsed();
        } else {
            return 0;
        }
    }

    @Override
    public boolean getTerminated(InternalRobot robot) {
        assert this.sandboxes.containsKey(robot.getID());

        final SandboxedRobotPlayer player = this.sandboxes.get(robot.getID());

        if (player != null) {
            return player.getTerminated();
        } else {
            return true;
        }
    }
}
