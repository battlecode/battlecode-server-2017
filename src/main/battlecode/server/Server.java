package battlecode.server;

import battlecode.common.Team;
import battlecode.serial.*;
import battlecode.serial.notification.*;
import battlecode.server.proxy.Proxy;
import battlecode.server.proxy.ProxyWriter;

import java.io.IOException;
import java.util.*;

/**
 * Runs matches. Specifically, this class forms a pipeline connecting match and
 * configuration parameters to the game engine and engine output to an abstract
 * match data sink.
 */
public class Server implements Runnable, NotificationHandler {

    /**
     * The proxies to use for writing match data.
     */
    private final ProxyWriter proxyWriter;

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
     * The server's mode.
     */
    private Mode mode;

    /**
     * The server's mode affects how notifications are handled, whether or not
     * an RPC server is set up, and which controllers are chosen for server
     * operation.
     */
    public enum Mode {
        HEADLESS, LOCAL, SCRIMMAGE, TOURNAMENT
    }

    /**
     * Initializes a new server.
     *
     * @param options the configuration to use
     * @param mode the mode to run the server in
     * @param proxies the proxies to send messages to
     */
    public Server(Config options, Mode mode, Proxy... proxies) {
        this.matches = new ArrayDeque<>();
        this.finished = new ArrayDeque<>();

        this.mode = mode;

        this.proxyWriter = new ProxyWriter(proxies, options.getBoolean("bc.server.debug"));

        this.options = options;
        this.state = State.NOT_READY;
    }

    // Notification handling.

    @Override
    public void visitPauseNotification(PauseNotification n) {
        state = State.PAUSED;
        proxyWriter.enqueue(new PauseEvent());
    }

    @Override
    public void visitStartNotification(StartNotification n) {
                                                          state = State.READY;
                                                                              }

    @Override
    public void visitRunNotification(RunNotification n) {
        if (state != State.PAUSED) {
            state = State.RUNNING;
            runUntil = n.getRounds();
        }
    }

    @Override
    public void visitResumeNotification(ResumeNotification n) {
        if (state == State.PAUSED)
            state = State.RUNNING;
    }

    @Override
    public void visitInjectNotification(InjectNotification n) {
        InjectDelta result = matches.peek().inject(n.getInternalSignal());

        proxyWriter.enqueue(result);
    }

    @Override
    public void visitGameNotification(GameNotification n) {
        synchronized (matches) {
            if (!matches.isEmpty()) {
                return;
            }
        }

        try {
            GameInfo info = n.getInfo();

            int matchCount = info.getMaps().length;
            int matchNumber = 0;
            for (String map : info.getMaps()) {
                if (map.endsWith(".xml"))
                    map = map.substring(0, map.indexOf('.'));
                Match match = new Match(info, map, options,
                        matchNumber++, matchCount);
                debug("queuing match " + match);
                matches.add(match);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("couldn't start the match: " + e.getMessage());
        }
    }

    /**
     * Runs the server. The server will wait for some match info (which
     * specifies the teams and set of maps to run) and then begin running
     * matches.
     */
    public void run() {
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
                if (mode == Mode.TOURNAMENT || mode == Mode.SCRIMMAGE) {
                    if (aWins == 2 || bWins == 2)
                        break;
                }

            } catch (Exception e) {
                ErrorReporter.report(e);
                error("couldn't run match: ");

                this.state = State.ERROR;
            }
        }

        proxyWriter.terminate();
    }


    /**
     * Runs a match; configures the controller and list of proxies, and starts
     * running the game in a separate thread.
     */
    private void runMatch(final Match match, final ProxyWriter proxyWriter) throws Exception {
        if (Mode.HEADLESS.equals(mode) || Mode.SCRIMMAGE.equals(mode)
                || Mode.TOURNAMENT.equals(mode)) {
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
        proxyWriter.enqueue(header);
        ExtensibleMetadata exHeader = match.getHeaderMetadata();
        proxyWriter.enqueue(exHeader);

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
                        proxyWriter.enqueue(new PauseEvent());
                    } else if (GameState.DONE.equals(match.getGameState())) {
                        this.state = State.FINISHED;
                    }

                    proxyWriter.enqueue(round);

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
        System.out.printf("[server:fatal] %s\n", msg);
        System.exit(-1);
    }

    /**
     * This method is used to display non-fatal error messages, issuing a
     * RuntimeException instead of terminating the Server.
     *
     * @param msg the error message to display
     */
    public static void error(String msg) {
        for (String line : msg.split("\n")) {
            System.out.printf("[server:error] %s\n", line);
        }
        throw new RuntimeException(msg);
    }

    /**
     * This method is used to display warning messages with formatted output.
     *
     * @param msg the warning message to display
     */
    public static void warn(String msg) {
        for (String line : msg.split("\n")) {
            System.out.printf("[server:warning] %s\n", line);
        }
    }

    /**
     * This method is used to display "official" formatted messages from the
     * server.
     *
     * @param msg the message to display
     */
    public static void say(String msg) {
        for (String line : msg.split("\n")) {
            System.out.printf("[server] %s\n", line);
        }

    }

    /**
     * This method is used to display debugging messages with formatted output.
     *
     * @param msg the debug message to display
     */
    public void debug(String msg) {
        if (options.getBoolean("bc.server.debug")) {
            for (String line : msg.split("\n")) {
                System.out.printf("[server:debug] %s\n", line);
            }
        }
    }
}
