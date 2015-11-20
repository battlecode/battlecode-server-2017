package battlecode.engine;

import battlecode.common.RobotController;
import battlecode.engine.instrumenter.RobotDeathException;
import battlecode.engine.scheduler.Scheduler;
import battlecode.world.RobotControllerImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/*
RobotRunnable is a wrapper for a player's main class.  It is basically a Runnable, whose run method both instantiates the player's
main class and runs the player's run method.

TODO:
- better commenting
- better error reporting
 */
public class RobotRunnable implements Runnable {

    private final Class<?> myPlayerClass;
    private final RobotControllerImpl myRobotController;

    public RobotRunnable(Class playerClass, RobotControllerImpl rc) {
        myPlayerClass = playerClass;
        myRobotController = rc;
    }

    public static void warnRunFunctionMissing(String specificMessage) {
        ErrorReporter.report(specificMessage + "\nYour player must have a function\npublic static void RobotPlayer.run(RobotController rc)", false);
    }

    public static boolean causedByRobotDeath(Throwable t) {
        while (t != null) {
            if (t instanceof RobotDeathException)
                return true;
            t = t.getCause();
        }
        return false;
    }

    // instantiates the class passed to the RobotRunnable constructor, and runs its run method
    public void run() {
        Method m;

        runbot:
        try {
            Scheduler.endTurn();
            try {
                m = myPlayerClass.getMethod("run", RobotController.class);
            } catch (NoSuchMethodException e) {
                warnRunFunctionMissing(myPlayerClass.getSimpleName() + ".run(RobotController) not found");
                break runbot;
            } catch (SecurityException e) {
                warnRunFunctionMissing(myPlayerClass.getSimpleName() + ".run(RobotController) is not public");
                break runbot;
            }

            if ((m.getModifiers() & Modifier.STATIC) == 0) {
                warnRunFunctionMissing(myPlayerClass.getSimpleName() + ".run(RobotController) is not static");
                break runbot;
            }

            m.invoke(null, myRobotController);

            System.out.println("[Engine] Robot " + myRobotController.getRobot() + " died on round "+
                    myRobotController.getRoundNum()+" because its run method returned");
        } catch (Throwable t) {
            while ((t instanceof InvocationTargetException) || (t instanceof ExceptionInInitializerError))
                t = t.getCause();
            if (!causedByRobotDeath(t)) {
                System.out.println("[Engine] Robot " + myRobotController.getRobot() + " died on round "+
                        myRobotController.getRoundNum()+" because of:");
                t.printStackTrace();
            }
        }
        myRobotController.getRobot().suicide();
    }
}
