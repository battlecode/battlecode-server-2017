package battlecode.world.control;

import battlecode.engine.ErrorReporter;
import battlecode.engine.RobotRunnable;
import battlecode.engine.instrumenter.IndividualClassLoader;
import battlecode.engine.instrumenter.InstrumentationException;
import battlecode.engine.instrumenter.RobotMonitor;
import battlecode.engine.instrumenter.lang.RoboRandom;
import battlecode.engine.scheduler.ScheduledRunnable;
import battlecode.engine.scheduler.Scheduler;
import battlecode.server.Config;
import battlecode.world.GameWorld;
import battlecode.world.InternalRobot;

/**
 * Controls robots with instrumented player code.
 * The point of contact between GameWorld and the instrumenter/scheduler
 * infrastructure.
 *
 * @author james
 */
public class PlayerControlProvider implements RobotControlProvider {

    /**
     * The gameWorld we're acting on.
     */
    private GameWorld world;

    /**
     * Create a new PlayerControlProvider.
     */
    public PlayerControlProvider() {}

    @Override
    public void matchStarted(GameWorld world) {
        this.world = world;

        IndividualClassLoader.reset();
        Scheduler.reset();
        RobotMonitor.reset();
        RobotMonitor.setGameWorld(world);
        RoboRandom.setMapSeed(world.getMapSeed());
        Scheduler.start();
    }

    @Override
    public void robotSpawned(InternalRobot robot) {
        final boolean debugMethodsEnabled = Config.getGlobalConfig().getBoolean("bc.engine.debug-methods");

        // now, we instantiate and instrument the player's class
        Class playerClass;
        try {
            String teamName = world.getTeamName(robot.getTeam());

            // The classloaders ignore silenced now - RobotMonitor takes care of it
            ClassLoader icl = new IndividualClassLoader(teamName, debugMethodsEnabled, false, true);
            playerClass = icl.loadClass(teamName + ".RobotPlayer");
            //~ System.out.println("PF done loading");
        } catch (InstrumentationException ie) {
            // if we get an InstrumentationException, then the error should have been reported, so we just kill the robot
            System.out.println("[Engine] Error during instrumentation of " + robot + ".\n[Engine] Robot will self-destruct in 3...2...1...");
            robot.suicide();
            return;
        } catch (Exception e) {
            ErrorReporter.report(e);
            robot.suicide();
            return;
        }

        // finally, create the player's thread, and let it loose
        new ScheduledRunnable(new RobotRunnable(playerClass, robot.getRobotController()), robot.getID());
    }

    @Override
    public void robotKilled(InternalRobot robot) {
        RobotMonitor.killRobot(robot.getID());
    }

    @Override
    public void runRound() {
        Scheduler.startNextThread();
        Scheduler.endTurn();
    }

    @Override
    public void matchEnded() {
        // Clean up threads
        Scheduler.passToNextThread();
    }
}
