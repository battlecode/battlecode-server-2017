package battlecode.engine.instrumenter;

import battlecode.common.RobotController;
import battlecode.engine.ErrorReporter;
import battlecode.engine.instrumenter.lang.RoboPrintStream;
import battlecode.engine.instrumenter.lang.SilencedPrintStream;
import battlecode.server.Config;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Encapsulates an instrumented robot player, its personally-loaded classes,
 * and its main thread. Sort of like a mini-jvm.
 *
 * Closely bound to RobotMonitor; RobotMonitor exists inside the sandbox,
 * this exists as a container for the sandbox.
 *
 * @author james
 */
public class SandboxedRobotPlayer {

    // Note: The methods of this class should be called one-at-a-time.
    // To enforce this, they are synchronized.

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
     * Create a new sandboxed robot player.
     *
     * @param teamName        the name of the team to create a player for
     * @param playerClassName the name of the class to be loaded
     *                        (e.g. RobotPlayer or ZombiePlayer)
     * @param robotController           the robot we're loading a player for
     */
    public SandboxedRobotPlayer(String teamName, String playerClassName, RobotController robotController,
                                int seed)
            throws InstrumentationException {
        this.robotController = robotController;
        this.seed = seed;
        this.terminated = false;
        this.notifier = new Object();

        // Create classloader sandbox
        individualLoader = new IndividualClassLoader(teamName, false);

        // Load player in sandbox
        Class<?> robotPlayer;
        try {
            robotPlayer = individualLoader.loadClass(teamName + "." + playerClassName, true);
        } catch (Exception e) {
            throw new InstrumentationException("Couldn't sandbox player class", e);
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

        // Load monitor / monitor methods
        // Used to initialize the RobotMonitor for the player
        final Method initMethod;
        // Used to pause the player thread after loading
        final Method pauseMethod;
        try {
            // The loaded, uninstrumented-but-individual RobotMonitor for this player.
            Class<?> monitor = individualLoader.loadClass(RobotMonitor.class.getName());

            killMethod = monitor.getMethod("killRobot");
            setBytecodeLimitMethod = monitor.getMethod("setBytecodeLimit", int.class);
            setSystemOutMethod = monitor.getMethod("setSystemOut", PrintStream.class);
            getBytecodeNumMethod = monitor.getMethod("getBytecodeNum");
            pauseMethod = monitor.getMethod("pause");
            initMethod = monitor.getMethod("init", Pauser.class, Killer.class, int.class);


        } catch (Exception e) {
            throw new InstrumentationException("Couldn't load RobotMonitor", e);
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
                runMethod.invoke(null, robotController);
            } catch (final Exception e) {
                // Note: does not report RobotDeathExceptions - i.e. non-exceptional
                // robot deaths - since RobotDeathException does not actually subclass
                // Exception, and so won't be caught here.
                ErrorReporter.report(e, false);
            } finally {

                // Ensure that we know we're terminated.
                this.terminated = true;

                // Unpause the main thread, which is waiting on the player thread.
                synchronized (notifier) {
                    notifier.notifyAll();
                }
            }
        });


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
     * Set the bytecode limit of the sandboxed player.
     *
     * @param limit the new limit
     */
    public synchronized void setBytecodeLimit(int limit) {
        try {
            setBytecodeLimitMethod.invoke(null, limit);
        } catch (Exception e) {
            throw new InstrumentationException("Error invoking RobotMonitor methods", e);
        }
    }

    /**
     * Take a step on the RobotPlayer thread, blocking until it's completed.
     */
    public synchronized void step() throws InstrumentationException {
        // Is the RobotPlayer terminated?
        if (terminated) {
            throw new RuntimeException("step called after robot killed");
        }

        // Update the robot's information
        try {
            setSystemOutMethod.invoke(null, getOut());
        } catch (Exception e) {
            throw new InstrumentationException("Error invoking RobotMonitor methods", e);
        }


        try {
            synchronized (notifier) {
                // Unpause the robot's thread
                notifier.notifyAll();

                // Pause this thread until the robot ends turn or dies
                notifier.wait();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Unexpected interruption", e);
        }
    }

    /**
     * Kills a RobotPlayer control thread the next time it runs.
     * Does nothing if the player is already killed.
     */
    public synchronized void terminate() throws InstrumentationException {
        if (terminated) return;

        try {
            killMethod.invoke(null);
        } catch (Exception e) {
            throw new InstrumentationException("Error invoking RobotMonitor methods", e);
        }
    }

    /**
     * @return the bytecodes used by the player during the most recent step() call.
     */
    public synchronized int getBytecodesUsed() {
        try {
            return (Integer) getBytecodeNumMethod.invoke(null);
        } catch (Exception e) {
            throw new InstrumentationException("Error invoking RobotMonitor methods", e);
        }
    }

    /**
     * Whether the player controlling this robot is terminated.
     */
    public synchronized boolean getTerminated() {
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

        boolean[] teamSilences = new boolean[4];
        teamSilences[0] = options.getBoolean("bc.engine.silence-a");
        teamSilences[1] = options.getBoolean("bc.engine.silence-b");
        teamSilences[2] = options.getBoolean("bc.engine.silence-c");
        teamSilences[3] = options.getBoolean("bc.engine.silence-d");

        boolean silenced = teamSilences[robotController.getTeam().ordinal()];

        if (silenced) {
            if (!(cachedOut instanceof SilencedPrintStream)) {
                cachedOut = SilencedPrintStream.theInstance();
            }
            // Modifying Systems specific to this robot.
        } else {
            if (!(cachedOut instanceof RoboPrintStream)) {
                cachedOut = new RoboPrintStream();
            }

            ((RoboPrintStream) cachedOut).updateHeader(
                    String.format("[%s:%s#%d@%d]",
                            robotController.getTeam(),
                            robotController.getType(),
                            robotController.getID(),
                            robotController.getRoundNum()
                    )
            );

        }

        return cachedOut;
    }
}
