package battlecode.server;

import battlecode.common.Team;
import battlecode.world.signal.Signal;
import battlecode.serial.*;
import battlecode.serial.notification.*;
import battlecode.server.controller.Controller;
import battlecode.server.proxy.Proxy;
import battlecode.server.proxy.ProxyWriter;

import java.io.IOException;
import java.util.*;

/**
 * Runs matches. Specifically, this class forms a pipeline connecting match and
 * configuration parameters to the game engine and engine output to an abstract
 * match data sink.
 */
public class Server implements Observer, Runnable {

    /**
     * The controller to use for match data and notifications.
     */
    private Controller controller = null;

    /**
     * The proxies to use for writing match data.
     */
    private List<Proxy> proxies = null;

    /**
     * A queue of matches that this server has yet to run.
     */
    private final Deque<Match> matches;

    /**
     * A list of matches that this server has already run.
     */
    private final Deque<Match> finished;

    /**
     * The state of the match that the server is running (or about to run).
     */
    private State state;

    /**
     * The round number to run until.
     */
    private int runUntil;

    /**
     * The options provided to the server via config file and command line.
     */
    private final Config options;

    /**
     * The handler to use for processing notifications.
     */
    private final ServerNotificationHandler nHandler;

    /**
     * The server's mode.
     */
    private Mode mode;

    /**
     * The server's mode affects how notifications are handled, whether or not
     * an RPC server is set up, and which controllers are chosen for server
     * operation.
     */
    public enum Mode {
        HEADLESS, LOCAL, SCRIMMAGE, TOURNAMENT, TESTS, AUTOTEST, MATCH
    }

    /**
     * Initializes a new server.
     *
     * @param options the configuration to use
     * @param mode the mode to run the server in
     * @param controller the controller to use
     * @param proxies the proxies to use for this server
     */
    public Server(Config options, Mode mode, Controller controller,
                  Proxy... proxies) {
        this.matches = new ArrayDeque<>();
        this.finished = new ArrayDeque<>();

        this.mode = mode;
        this.controller = controller;
        this.proxies = new ArrayList<>();
        Collections.addAll(this.proxies, proxies);

        this.options = options;
        this.state = State.NOT_READY;
        this.nHandler = new ServerNotificationHandler();
    }

    /**
     * Handles events when notified by an observable.
     * <p/>
     * {@inheritDoc}
     */
    public void update(Observable o, Object arg) {
        if (arg instanceof MatchInfo) {
            synchronized (matches) {
                if (!matches.isEmpty())
                    return;
            }

            try {
                MatchInfo info = (MatchInfo) arg;

                int matchCount = info.getMaps().length;
                int matchNumber = 0;
                for (String map : info.getMaps()) {
                    if (map.endsWith(".xml"))
                        map = map.substring(0, map.indexOf('.'));
                    Match match = new Match(info, map, this.options,
                            matchNumber++, matchCount);
                    debug("queuing match " + match);
                    matches.add(match);
                }
            } catch (Exception e) {
                e.printStackTrace();
                fail("couldn't start the match: " + e.getMessage());
            }
        }

        // Some state-changing feedback from our controller; dispatch to
        // the notification handler.
        else if (arg instanceof Notification) {
            ((Notification) arg).accept(nHandler);
        }

        // Some match-altering signal from our controller. Send to the engine
        // and propagate the engine's repsonse.
        else if (arg instanceof Signal) {
            throw new RuntimeException("Can't observe currentSignals any more");
        }
    }

    /**
     * Handles notifications; dispatched to by update().
     */
    private class ServerNotificationHandler implements
            NotificationHandler<Void> {

        public Void visitPauseNotification(PauseNotification n) {
            state = State.PAUSED;
            for (Proxy p : proxies) {
                try {
                    p.writeObject(n);
                } catch (IOException e) {
                    warn("debug mode notification propagation failed");
                }
            }
            return null;
        }

        public Void visitStartNotification(StartNotification n) {
            state = State.READY;
            return null;
        }

        public Void visitRunNotification(RunNotification n) {
            if (state != State.PAUSED) {
                state = State.RUNNING;
                runUntil = n.getRounds();
            }
            return null;
        }

        public Void visitResumeNotification(ResumeNotification n) {
            if (state == State.PAUSED)
                state = State.RUNNING;
            return null;
        }

        public Void visitInjectNotification(InjectNotification n) {
            InjectDelta result = matches.peek().inject(n.getSignal());

            for (Proxy p : proxies) {
                try {
                    p.writeObject(result);
                } catch (IOException e) {
                    warn("debug mode signal handler failed");
                }
            }

            return null;
        }
    }

    /**
     * Runs the server. The server will wait for some match info (which
     * specifies the teams and set of maps to run) and then begin running
     * matches.
     */
    public void run() {

        debug("using controller " + this.controller.getClass().getSimpleName());
        for (Proxy p : this.proxies) {
            debug("using proxy " + p.getClass().getSimpleName());
        }

        try {
            controller.start();
        } catch (IOException e) {
            this.state = State.ERROR;
            e.printStackTrace();
            return;
        }

        final ProxyWriter proxyWriter = new ProxyWriter(proxies, options.getBoolean("bc.server.debug"));

        int aWins = 0, bWins = 0;

        while (!matches.isEmpty()) {
            Match match = matches.peek();
            if (!finished.isEmpty())
                match.setInitialTeamMemory(finished.getLast()
                        .getComputedTeamMemory());

            try {
                debug("running match " + match);
                match.initialize();
                runMatch(match, proxyWriter);
                finished.add(match);
                matches.remove(match);

                if (match.getWinner() == Team.A)
                    aWins++;
                else if (match.getWinner() == Team.B)
                    bWins++;

                match.finish();

                // Allow best of three scrimmages -- single game scrims should still work fine
                //TODO:This "win mode" should probably be something from the database
                if (mode == Mode.TOURNAMENT || mode == Mode.SCRIMMAGE || mode == Mode.AUTOTEST || mode == Mode.MATCH) {
                    if (aWins == 2 || bWins == 2)
                        break;
                }

            } catch (Exception e) {
                this.state = State.ERROR;
                e.printStackTrace();
                error("couldn't run match: " + e.getMessage());
            }
        }

        // Make sure to write out all messages before closing proxies.
        proxyWriter.terminate();

        for (Proxy p : proxies) {
            try {
                p.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Let the controller clean up.
        try {
            controller.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Runs a match; configures the controller and list of proxies, and starts
     * running the game in a separate thread.
     */
    private void runMatch(final Match match, final ProxyWriter proxyWriter) throws Exception {
        if (Mode.HEADLESS.equals(mode) || Mode.SCRIMMAGE.equals(mode)
                || Mode.TOURNAMENT.equals(mode) || Mode.TESTS.equals(mode)
                || Mode.AUTOTEST.equals(mode) || Mode.MATCH.equals(mode)) {
            this.state = State.RUNNING;
            this.runUntil = Integer.MAX_VALUE;
        }

        // Poll for RUNNING, if mode == Mode.LOCAL
        while (!State.RUNNING.equals(state)) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
            }
        }

        long startTime = System.currentTimeMillis();

        say("-------------------- Match Starting --------------------");
        say(match.toString());

        // Compute the header and send it to all listeners.
        MatchHeader header = match.getHeader();
        ExtensibleMetadata exHeader = match.getHeaderMetadata();
        for (Proxy p : proxies) {
            p.writeObject(header);
            p.writeObject(exHeader);
        }

        this.state = State.RUNNING;

        int count = 0;

        final String throttle = options.get("bc.server.throttle");
        final int throttleCount = options.getInt("bc.server.throttle-count");
        final boolean doYield = "yield".equals(throttle);
        final boolean doSleep = "sleep".equals(throttle);

        // If there are more rounds to be run, run them and
        // and send the round (and optionally stats) bytes to
        // recipients.
        while (match.hasMoreRounds()) {

            // If not paused/stopped:
            switch (this.state) {

                case RUNNING:

                    if (match.getRoundNumber() == runUntil) {
                        Thread.sleep(25);
                        break;
                    }

                    final RoundDelta round = match.getRound();
                    if (round == null)
                        break;

                    if (GameState.BREAKPOINT.equals(match.getGameState())) {
                        this.state = State.PAUSED;
                        for (Proxy p : proxies) {
                            try {
                                p.writeObject(PauseNotification.INSTANCE);
                            } catch (IOException e) {
                                error("Couldn't write pause notification to " +
                                        "proxy " + p + ": " + e);
                                e.printStackTrace();
                            }
                        }
                    } else if (GameState.DONE.equals(match.getGameState()))
                        this.state = State.FINISHED;

                    proxyWriter.enqueue(round);

                    final RoundStats stats = match.getStats();

                    proxyWriter.enqueue(stats);

                    if (count++ == throttleCount) {
                        if (doYield)
                            Thread.yield();
                        else if (doSleep)
                            Thread.sleep(1);
                        count = 0;
                    }

                    break;

                case PAUSED:
                    Thread.sleep(250);
                    break;
            }
        }

        // Compute footer data.
        GameStats gameStats = match.getGameStats();
        MatchFooter footer = match.getFooter();

        proxyWriter.enqueue(gameStats);
        proxyWriter.enqueue(footer);

        say(match.getWinnerString());
        say("-------------------- Match Finished --------------------");

        double timeDiff = (System.currentTimeMillis() - startTime) / 1000.0;
        debug(String.format("match completed in %.4g seconds", timeDiff));

        this.state = State.FINISHED;
    }

    public State getState() {
        return this.state;
    }

    /**
     * This method is used to display error messages. Invoking it terminates the
     * program.
     *
     * @param msg the error message to display
     */
    public static void fail(String msg) {
        System.err.printf("[server:FATAL] %s\n", msg);
        System.exit(-1);
    }

    /**
     * This method is used to display non-fatal error messages, issuing a
     * RuntimeException instead of terminating the Server.
     *
     * @param msg the error message to display
     */
    public static void error(String msg) {
        System.err.printf("[server:ERROR] %s\n", msg);
        throw new RuntimeException(msg);
    }

    /**
     * This method is used to display warning messages with formatted output.
     *
     * @param msg the warning message to display
     */
    public static void warn(String msg) {
        System.err.printf("[server:WARNING] %s\n", msg);
    }

    /**
     * This method is used to display "official" formatted messages from the
     * server.
     *
     * @param msg the message to display
     */
    public static void say(String msg) {
        System.out.printf("[server] %s\n", msg);
    }

    /**
     * This method is used to display debugging messages with formatted output.
     *
     * @param msg the debug message to display
     */
    public void debug(String msg) {
        if (options.getBoolean("bc.server.debug"))
            System.out.printf("[server:debug] %s\n", msg);
    }
}
