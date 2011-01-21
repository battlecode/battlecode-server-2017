package battlecode.engine;

import java.lang.Runnable;
import java.lang.reflect.*;

import battlecode.engine.instrumenter.RobotDeathException;
import battlecode.engine.instrumenter.RobotMonitor;
import battlecode.engine.scheduler.Scheduler;

/*
RobotRunnable is a wrapper for a player's main class.  It is basically a Runnable, whose run method both instantiates the player's
main class and runs the player's run method.

TODO:
- better commenting
- better error reporting
 */
class RobotRunnable implements Runnable {

    private final Class<?> myPlayerClass;
    private final GenericController myRobotController;

    public RobotRunnable(Class playerClass, GenericController rc) {
        myPlayerClass = playerClass;
        myRobotController = rc;
    }

    // instantiates the class passed to the RobotRunnable constructor, and runs its run method
    public void run() {
        Constructor ctor;
        Object o;
        Runnable r;
		

        try {
            try {
				Scheduler.endTurn();
                ctor = myPlayerClass.getConstructor(Class.forName("battlecode.common.RobotController"));
            } catch (Throwable t) {
                if ((t instanceof RobotDeathException) || (t.getCause() instanceof RobotDeathException))
                    return;
                ErrorReporter.report(t, "Check that the player class '" + myPlayerClass.getSimpleName() + "' has a constructor with one argument, of type RobotController.\n");
                return;
            }

            try {

                o = ctor.newInstance(myRobotController);

            } catch (Throwable t) {
                if ((t instanceof RobotDeathException) || (t.getCause() instanceof RobotDeathException))
                    return;
                ErrorReporter.report(t, "Check that the player does not throw an exception in its constructor.\n");
                return;
            }

            try {
                r = (Runnable) o;
            } catch (Exception e) {
                ErrorReporter.report("The player class '" + myPlayerClass.getSimpleName() + " does not implement Runnable.", "Check that the player class implements java.lang.Runnable.\n");
                return;
            }

            try {

                r.run();

            } catch (RobotDeathException rde) {
                return;
            } catch (Throwable t) {
                if (t.getCause() instanceof RobotDeathException)
                    return;
                System.out.println("[Engine] Robot " + myRobotController.getRobot() + " died because of:");
                t.printStackTrace();
                return;
            }

			if(!RobotMonitor.thrownRobotDeathException())
				System.out.println("[Engine] Robot " + myRobotController.getRobot() + " died because its run method returned");

        } finally {
            myRobotController.getRobot().suicide();
        }

    }
}
