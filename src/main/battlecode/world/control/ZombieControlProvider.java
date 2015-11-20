package battlecode.world.control;

import battlecode.engine.ErrorReporter;
import battlecode.engine.RobotRunnable;
import battlecode.engine.instrumenter.IndividualClassLoader;
import battlecode.engine.instrumenter.InstrumentationException;
import battlecode.engine.scheduler.ScheduledRunnable;
import battlecode.server.Config;
import battlecode.world.GameWorld;
import battlecode.world.InternalRobot;

/**
 * Loads zombie players using the PlayerControlProvider infrastructure.
 *
 * Currently in the awkward position of having to be used in a TeamControlProvider
 *
 * Note that it doesn't override robotKilled because we need PlayerControlProvider
 * to handle that.
 *
 * @author james
 */
public class ZombieControlProvider extends PlayerControlProvider {
    @Override
    public void matchStarted(GameWorld world) {}

    @Override
    public void matchEnded() {}

    @Override
    public void robotSpawned(InternalRobot robot) {
        final boolean debugMethodsEnabled = Config.getGlobalConfig().getBoolean("bc.engine.debug-methods");

        // instantiate and instrument the ZombiePlayer class
        Class playerClass;
        try {
            // The classloaders ignore silenced now - RobotMonitor takes care of it
            ClassLoader icl = new IndividualClassLoader("ZombiePlayer", debugMethodsEnabled, false, true);
            playerClass = icl.loadClass("ZombiePlayer.ZombiePlayer");
            //~ System.out.println("PF done loading");
        } catch (InstrumentationException ie) {
            // if we get an InstrumentationException, then the error should have been reported, so we just kill the robot
            System.out.println("[Engine] Error during instrumentation of " + robot
                    + ".\n[Engine] Robot will self-destruct in 3...2...1...");
            robot.suicide();
            return;
        } catch (Exception e) {
            ErrorReporter.report(e);
            robot.suicide();
            return;
        }

        // finally, create the ZombiePlayer's thread, and let it loose
        new ScheduledRunnable(new RobotRunnable(playerClass, robot.getRobotController()), robot.getID());
    }

    @Override
    public void runRound() {}
}
