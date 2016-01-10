package battlecode.instrumenter;

import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.instrumenter.stream.RoboPrintStream;
import battlecode.instrumenter.stream.SilencedPrintStream;
import battlecode.server.ErrorReporter;
import battlecode.server.Config;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Encapsulates an instrumented robot player, its personally-loaded classes,
 * and its main thread. Sort of like a mini-jvm.
 *
 * Closely bound to RobotMonitor; RobotMonitor exists inside the sandbox,
 * this exists as a container for the sandbox.
 *
 * This class is NOT THREAD SAFE, even though it uses threads internally.
 * Do NOT call its methods from multiple threads. Seriously. It will break
 * in confusing and insidious ways.
 *
 * However, it is okay if the thread SandboxedRobotPlayer is managing calls
 * into SandboxedRobotPlayer. This is still "single-threaded" use, since
 * there's only one thread operating at a time.
 *
 * @author james
 */
public class SandboxedRobotPlayer {

    /**
     * The controller for the robot we're controlling.
     */
    private final RobotController robotController;

    /**
     * The seed to use in all "random" operations.
     */
    private final int seed;

    /**
     * Whether the robot player is terminated.
     */
    private boolean terminated;

    /**
     * The classloader used for this player.
     */
    private final IndividualClassLoader individualLoader;

    /**
     * The main thread the player is running on.
     */
    private final Thread mainThread;

    /**
     * The cached 'killRobot' method of the monitor.
     */
    private final Method killMethod;

    /**
     * The cached 'setBytecodeLimit' method of the monitor.
     */
    private final Method setBytecodeLimitMethod;

    /**
     * The cached 'getBytecodeNum' method of the monitor.
     */
    private final Method getBytecodeNumMethod;

    /**
     * The cached 'setSystemOut' method of the monitor.
     */
    private final Method setSystemOutMethod;

    /**
     * The object used to trade of control between threads.
     */
    private final Object notifier;

    /**
     * Whether or not we are currently running, i.e. in "step".
     */
    private boolean running;

    /**
     * Create a new sandboxed robot player.
     *
     * @param teamName        the name of the team to create a player for
     * @param playerClassName the name of the class to be loaded
     *                        (e.g. RobotPlayer)
     * @param robotController           the robot we're loading a player for
     * @throws InstrumentationException if the player doesn't work for some reason
     * @throws RuntimeException if our code fails for some reason
     */
    public SandboxedRobotPlayer(String teamName, String playerClassName, RobotController robotController,
                                int seed)
            throws InstrumentationException {
        this.robotController = robotController;
        this.seed = seed;
        this.terminated = false;
        this.notifier = new Object();

        // Create classloader sandbox
        individualLoader = new IndividualClassLoader(teamName);

        // Load monitor / monitor methods
        // Used to initialize the RobotMonitor for the player
        final Method initMethod;
        // Used to pause the player thread after loading
        final Method pauseMethod;
        try {
            // The loaded, uninstrumented-but-individual RobotMonitor for this player.
            Class<?> monitor = individualLoader
                    .loadClass("battlecode.instrumenter.inject.RobotMonitor");

            killMethod = monitor.getMethod("killRobot");
            setBytecodeLimitMethod = monitor.getMethod("setBytecodeLimit", int.class);
            getBytecodeNumMethod = monitor.getMethod("getBytecodeNum");
            pauseMethod = monitor.getMethod("pause");
            initMethod = monitor.getMethod("init", Pauser.class, Killer.class, int.class);

            Class<?> system = individualLoader
                    .loadClass("battlecode.instrumenter.inject.System");
            setSystemOutMethod = system.getMethod("setSystemOut", PrintStream.class);

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Couldn't load RobotMonitor", e);
        }

        // Used to pause the RobotPlayer main thread.
        final Pauser pauser = () -> {
            try {
                synchronized (notifier) {
                    // Unpause the main thread, which is waiting on the player thread
                    notifier.notifyAll();

                    // Wait for the main thread to restart us
                    notifier.wait();
                }
            } catch (InterruptedException e) {
                ErrorReporter.report("RobotPlayer thread interrupted while paused");
                throw new RobotDeathException();
                // What to do now?
            }
        };

        final Killer killer = () -> {
            // Will propagate up the robot's callchain,
            // eventually terminating the robot's thread.
            throw new RobotDeathException();
        };

        mainThread = new Thread(() -> {
            try {
                // Init RobotMonitor
                initMethod.invoke(null, pauser, killer, this.seed);
                // Pause immediately
                pauseMethod.invoke(null);
                // Run the robot!
                loadAndRunPlayer(teamName, playerClassName);
                // If we get here, we've returned from the 'run' method. Tell the user.
                System.out.println(robotController.getTeam().toString() + "'s "
                        + robotController.getType().toString() + " " +
                        robotController.getID() + " at location " + robotController.getLocation().toString()
                        + " died in round " +robotController.getRoundNum() +
                        " because it returned from its run() method!");
            } catch (final IllegalAccessException e) {
                ErrorReporter.report(e, true);
            } catch (final InvocationTargetException e) {
                if (e.getCause() instanceof RobotDeathException) {
                    return;
                }
                ErrorReporter.report(e.getCause(), false);
            } catch (final RobotDeathException e) {
                return;
            } finally {
                // Ensure that we know we're terminated.
                this.terminated = true;

                // Unpause the main thread, which is waiting on the player thread.
                synchronized (notifier) {
                    notifier.notifyAll();
                }
            }
        }, teamName + "." + playerClassName + " #"+ robotController.getID());


        // Wait for thread to tell us it's ready
        try {
            synchronized (notifier) {
                // Doesn't do anything besides initialize the sandbox, since thread pauses immediately
                mainThread.start();

                notifier.wait();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Unexpected interruption initializing sandbox", e);
        }
    }

    /**
     * Load the player class and invoke "run", counting bytecode as we go.
     * We do this after the rest of the player state is initialized, so that
     * static initialization will be counted as part of the bytecode used of
     * the first step.
     */
    private void loadAndRunPlayer(String teamName, String playerClassName)
            throws InvocationTargetException, IllegalAccessException, InstrumentationException {
        // Load player in sandbox
        Class<?> robotPlayer;
        try {
            robotPlayer = individualLoader.loadClass(teamName + "." + playerClassName, true);
        } catch (ClassNotFoundException e) {
            throw new InstrumentationException("Couldn't load player class: "+e.getMessage(), e);
        }

        // Load RobotPlayer.run()
        final Method runMethod;
        try {
            runMethod = robotPlayer.getMethod("run", RobotController.class);
        } catch (NoSuchMethodException e) {
            throw new InstrumentationException(robotPlayer.getSimpleName() + ".run(RobotController) not found",
                    e);
        } catch (SecurityException e) {
            throw new InstrumentationException(robotPlayer.getSimpleName() + ".run(RobotController) is not public",
                    e);
        }
        if ((runMethod.getModifiers() & Modifier.STATIC) == 0) {
            throw new InstrumentationException(robotPlayer.getSimpleName() + ".run(RobotController) is not static");
        }

        // Run!
        runMethod.invoke(null, robotController);
    }

    /**
     * Set the bytecode limit of the sandboxed player.
     *
     * @param limit the new limit
     */
    public void setBytecodeLimit(int limit) throws InstrumentationException {
        try {
            setBytecodeLimitMethod.invoke(null, limit);
        } catch (ReflectiveOperationException e) {
            ErrorReporter.report(e, true);
        }
    }

    /**
     * Take a step on the RobotPlayer thread, blocking until it's completed.
     */
    public void step() throws InstrumentationException {
        // Is the RobotPlayer terminated?
        if (terminated) {
            throw new RuntimeException("Step called after robot killed");
        }

        // Update the robot's information
        try {
            setSystemOutMethod.invoke(null, getOut());
        } catch (ReflectiveOperationException e) {
            ErrorReporter.report(e, true);
        }


        try {
            synchronized (notifier) {
                // We are now running the robot!
                running = true;

                // Unpause the robot's thread
                notifier.notifyAll();

                // Pause this thread until the robot ends turn or dies
                notifier.wait();

                // Main thread reactivated: no longer running.
                running = false;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Unexpected interruption", e);
        }
    }

    /**
     * Kills a RobotPlayer control thread immediately.
     * Does nothing if the player is already killed.
     */
    public void terminate() throws InstrumentationException {
        if (terminated) {
            System.err.println("[server:debug] Attempting to kill already-terminated robot?");
            return;
        }

        if (running) {
            // We're currently running, which means that terminate() has been
            // called from *inside* the running robot thread (because nobody
            // would violate the class contract and call SandboxedRobotPlayer
            // methods from multiple threads, right?)
            // We need to allow internal logic to finish executing, and then
            // kill the running thread. So, we set the "shouldDie" flag, and
            // the next time RobotMonitor.incrementBytecodes is called, it will
            // kill the robot.
            // This isn't ideal, since it means that players will be able to
            // execute zero-cost actions after "dying", like (maybe?) returning
            // from run(), which could issue nonsensical warnings. However, it's
            // the best solution I can think of.
            try {
                killMethod.invoke(null);
            } catch (ReflectiveOperationException e) {
                ErrorReporter.report(e, true);
            }
        } else {
            // We're not running.
            // Set the "shouldDie" flag and then step to finish the job.

            try {
                killMethod.invoke(null);
            } catch (ReflectiveOperationException e) {
                ErrorReporter.report(e, true);
            }

            // Step to make the robot die.
            step();
        }
    }

    /**
     * @return the bytecodes used by the player during the most recent step() call.
     */
    public int getBytecodesUsed() throws InstrumentationException {
        try {
            return (Integer) getBytecodeNumMethod.invoke(null);
        } catch (ReflectiveOperationException e) {
            ErrorReporter.report(e, true);
            return 0;
        }
    }

    /**
     * Whether the player controlling this robot is terminated.
     */
    public boolean getTerminated() {
        return terminated;
    }

    /**
     * Injected into RobotMonitor; used to synchronize it and the main thread.
     */
    public interface Pauser {
        /**
         * Called by RobotMonitor to pause its thread.
         */
        void pause();
    }

    /**
     * Injected into RobotMonitor; used to kill its thread.
     */
    public interface Killer {
        /**
         * Called by RobotMonitor to kill its thread immediately.
         */
        void kill();
    }

    /**
     * Used so we don't create a new PrintStream for every robot
     * every round.
     */
    private PrintStream cachedOut;

    /**
     * Create a new System.out for this robot and round.
     * @return a stream to use for System.out in the sandboxed player
     */
    private PrintStream getOut() {
        //TODO move this logic?

        Config options = Config.getGlobalConfig();

        if (robotController.getTeam() == Team.A
                && options.getBoolean("bc.engine.silence-a")
                || robotController.getTeam() == Team.B
                && options.getBoolean("bc.engine.silence-b")) {
            if (!(cachedOut instanceof SilencedPrintStream)) {
                cachedOut = SilencedPrintStream.theInstance();
            }
            // Modifying Systems specific to this robot.
        } else {
            if (!(cachedOut instanceof RoboPrintStream)) {
                cachedOut = new RoboPrintStream();
            }

            ((RoboPrintStream) cachedOut).updateHeader(
                        robotController.getTeam(),
                        robotController.getType(),
                        robotController.getID(),
                        robotController.getRoundNum()
            );

        }

        return cachedOut;
    }
}
