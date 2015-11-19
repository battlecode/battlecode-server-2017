package battlecode.engine;

import battlecode.engine.instrumenter.IndividualClassLoader;
import battlecode.engine.instrumenter.InstrumentationException;
import battlecode.engine.scheduler.ScheduledRunnable;
import battlecode.server.Config;
import battlecode.world.RobotControllerImpl;

/*
TODO:
 - better error reporting
 */
public class PlayerFactory {

    private PlayerFactory() {}

    public static void loadPlayer(RobotControllerImpl rc, String teamName) {
        final boolean debugMethodsEnabled = Config.getGlobalConfig().getBoolean("bc.engine.debug-methods");

        // now, we instantiate and instrument the player's class
        Class playerClass;
        try {
            // The classloaders ignore silenced now - RobotMonitor takes care of it
            ClassLoader icl = new IndividualClassLoader(teamName, debugMethodsEnabled, false, true);
            playerClass = icl.loadClass(teamName + ".RobotPlayer");
            //~ System.out.println("PF done loading");
        } catch (InstrumentationException ie) {
            // if we get an InstrumentationException, then the error should have been reported, so we just kill the robot
            System.out.println("[Engine] Error during instrumentation of " + rc.getRobot().toString() + ".\n[Engine] Robot will self-destruct in 3...2...1...");
            rc.getRobot().suicide();
            return;
        } catch (Exception e) {
            ErrorReporter.report(e);
            rc.getRobot().suicide();
            return;
        }

        // finally, create the player's thread, and let it loose
        new ScheduledRunnable(new RobotRunnable(playerClass, rc), rc.getRobot().getID());

    }
    
    public static void loadZombiePlayer(RobotControllerImpl rc) {
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
            System.out.println("[Engine] Error during instrumentation of " + rc.getRobot().toString() + ".\n[Engine] Robot will self-destruct in 3...2...1...");
            rc.getRobot().suicide();
            return;
        } catch (Exception e) {
            ErrorReporter.report(e);
            rc.getRobot().suicide();
            return;
        }

        // finally, create the ZombiePlayer's thread, and let it loose
        new ScheduledRunnable(new RobotRunnable(playerClass, rc), rc.getRobot().getID());

    }
}
