package battlecode.server;

import battlecode.common.GameConstants;
import battlecode.common.Team;
import battlecode.serial.*;
import battlecode.serial.notification.*;
import battlecode.server.proxy.Proxy;
import battlecode.server.proxy.ProxyFactory;
import battlecode.server.proxy.ProxyWriter;
import battlecode.world.DominationFactor;
import battlecode.world.GameMap;
import battlecode.world.GameMapIO;
import battlecode.world.GameWorld;
import battlecode.world.control.*;
import battlecode.world.signal.InternalSignal;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Runs matches. Specifically, this class forms a pipeline connecting match and
 * configuration parameters to the game engine and engine output to an abstract
 * match data sink.
 */
public class Server implements Runnable, NotificationHandler {
    /**
     * The GameInfo that signals the server to terminate when it is encountered on the game queue.
     */
    private static final GameInfo POISON = new GameInfo(null, null, null, null, null, null, false) {};

    /**
     * The factories to use to create proxies for new games.
     */
    private final ProxyFactory[] proxyFactories;

    /**
     * The proxies to use for writing match data.
     */
    private ProxyWriter proxyWriter;

    /**
     * The queue of games to run.
     * When the server encounters the GameInfo POISON, it terminates.
     */
    private final BlockingQueue<GameInfo> gameQueue;

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
     * Whether to wait for notifications to control match run state, or to just
     * run all matches immediately.
     */
    private final boolean interactive;

    /**
     * The GameWorld the server is currently operating on.
     */
    private GameWorld currentWorld;


    /**
     * The server's mode affects how notifications are handled, whether or not
     * an RPC server is set up, and which controllers are chosen for server
     * operation.
     */
    public enum Mode {
        HEADLESS,
        LOCAL,
        SCRIMMAGE,
        TOURNAMENT
    }

    /**
     * Initializes a new server.
     *
     * @param options the configuration to use
     * @param interactive whether to wait for notifications to control the
     *                    match run state
     * @param proxyFactories the factories to use to create proxies for each
     *                       game (decoupled enough for you?)
     */
    public Server(Config options, boolean interactive, ProxyFactory... proxyFactories) {
        this.gameQueue = new LinkedBlockingQueue<>();
        this.proxyFactories = proxyFactories;

        this.interactive = interactive;

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
        assert isRunningMatch();

        InjectDelta result;

        try {
            result = new InjectDelta(true, currentWorld.inject(n.getInternalSignal()));
        } catch (final RuntimeException e) {
            warn("Injection failure: " + e.getMessage());
            e.printStackTrace();
            result = new InjectDelta(false, new InternalSignal[0]);
        }

        proxyWriter.enqueue(result);
    }

    @Override
    public void visitGameNotification(GameNotification n) {
        this.gameQueue.add(n.getInfo());
    }

    @Override
    public void visitTerminateNotification(TerminateNotification n) {
        this.gameQueue.add(POISON);

    }

    /**
     * Runs the server. The server will wait for some game info (which
     * specifies the teams and set of maps to run) and then begin running
     * matches.
     */
    public void run() {
        // Note that this loop only runs once on the client.
        // Running it multiple times may break things.
        while (true) {
            final GameInfo currentGame;
            try {
                currentGame = gameQueue.take();
            } catch (InterruptedException e) {
                warn("Interrupted while waiting for next game!");
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return;
            }

            // Note: ==, not .equals()
            if (currentGame == POISON) {
                debug("Shutting down server");
                return;
            }

            debug("Running: "+currentGame);

            final Proxy[] gameProxies;
            try {
                gameProxies = Arrays.stream(proxyFactories)
                        .map(f -> {
                            try {
                                return f.createProxy(currentGame);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .filter(p -> p != null)
                        .toArray(Proxy[]::new);
            } catch (RuntimeException e) {
                warn("Unable to create proxies: ");
                e.printStackTrace();

                return;
            }

            // Set up our proxy writer
            this.proxyWriter = new ProxyWriter(
                    gameProxies,
                    options.getBoolean("bc.server.debug")
            );

            // Serialize engine metadata


            // Set up our control provider
            final RobotControlProvider prov = createControlProvider(currentGame);

            // We start with zeroed team memories.
            long[][] teamMemory = new long[2][GameConstants.TEAM_MEMORY_LENGTH];

            // Count wins
            int aWins = 0, bWins = 0;

            // Loop through the maps in the current game
            for (int matchIndex = 0; matchIndex < currentGame.getMaps().length; matchIndex++) {

                Team winner;
                try {
                    winner = runMatch(currentGame, matchIndex, prov, teamMemory, proxyWriter);
                } catch (Exception e) {
                    ErrorReporter.report(e);
                    this.state = State.ERROR;
                    return; // TODO
                }

                switch (winner) {
                    case A:
                        aWins++;
                        break;
                    case B:
                        bWins++;
                        break;
                    default:
                        warn("Team "+winner+" won???");
                }

                teamMemory = currentWorld.getTeamMemory();
                currentWorld = null;

                if (currentGame.isBestOfThree()) {
                    if (aWins == 2 || bWins == 2) {
                        break;
                    }
                }
            }

            // Terminate our proxy writer
            proxyWriter.terminate();
        }
    }


    /**
     * @return the winner of the match
     * @throws Exception if the match fails to run for some reason
     */
    private Team runMatch(GameInfo currentGame,
                          int matchIndex,
                          RobotControlProvider prov,
                          long[][] teamMemory,
                          ProxyWriter proxyWriter) throws Exception {

        final String mapName = currentGame.getMaps()[matchIndex];

        // Load the map for the match
        final GameMap loadedMap;
        try {
            loadedMap = GameMapIO.loadMap(mapName, new File(options.get("bc.game.map-path")));
            debug("running map " + loadedMap);
        } catch (IOException e) {
            warn("Couldn't load map " + mapName + ", skipping");
            throw e;
        }


        // Create the game world!
        currentWorld = new GameWorld(loadedMap, prov, currentGame.getTeamA(), currentGame.getTeamB(), teamMemory);


        // Get started
        if (interactive) {
            // TODO necessary?
            // Poll for RUNNING, if we're in interactive mode
            while (!State.RUNNING.equals(state)) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {}
            }
        } else {
            // Start the game immediately if we're not in interactive mode
            this.state = State.RUNNING;
            this.runUntil = Integer.MAX_VALUE;
        }


        // Print an
        long startTime = System.currentTimeMillis();
        say("-------------------- Match Starting --------------------");
        say(String.format("%s vs. %s on %s", currentGame.getTeamA(), currentGame.getTeamB(), mapName));


        // Compute the header and send it to all listeners.
        MatchHeader header = new MatchHeader(
                loadedMap,
                teamMemory,
                matchIndex,
                currentGame.getMaps().length // match count
        );
        proxyWriter.enqueue(header);

        // And the other part of the header
        // TODO: merge this into MatchHeader
        ExtensibleMetadata ex = new ExtensibleMetadata();
        ex.put("type", "header");
        ex.put("team-a", currentGame.getTeamA());
        ex.put("team-b", currentGame.getTeamB());
        ex.put("maps", currentGame.getMaps());
        proxyWriter.enqueue(ex);

        //this.state = State.RUNNING;

        // Used to count throttles
        int count = 0;

        final String throttle = options.get("bc.server.throttle");
        final int throttleCount = options.getInt("bc.server.throttle-count");
        final boolean doYield = "yield".equals(throttle);
        final boolean doSleep = "sleep".equals(throttle);

        // If there are more rounds to be run, run them and
        // and send the round (and optionally stats) bytes to
        // recipients.
        while (currentWorld.isRunning()) {

            // If not paused/stopped:
            switch (this.state) {

                case RUNNING:

                    if (currentWorld.getCurrentRound() + 1 == runUntil) {
                        Thread.sleep(25);
                        break;
                    }

                    GameState state = currentWorld.runRound();

                    if (GameState.BREAKPOINT.equals(state)) {
                        this.state = State.PAUSED;
                        proxyWriter.enqueue(new PauseEvent());
                    } else if (GameState.DONE.equals(state)) {
                        this.state = State.FINISHED;
                        break;
                    }

                    proxyWriter.enqueue(new RoundDelta(currentWorld.getAllSignals(true)));

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
        GameStats gameStats = currentWorld.getGameStats();
        proxyWriter.enqueue(gameStats);

        MatchFooter footer = new MatchFooter(currentWorld.getWinner(), currentWorld.getTeamMemory());
        proxyWriter.enqueue(footer);

        say(getWinnerString(currentGame, currentWorld.getWinner(), currentWorld.getCurrentRound()));
        say("-------------------- Match Finished --------------------");

        double timeDiff = (System.currentTimeMillis() - startTime) / 1000.0;
        debug(String.format("match completed in %.4g seconds", timeDiff));

        this.state = State.FINISHED;

        return currentWorld.getWinner();
    }

    /**
     * @return TODO
     */
    public State getState() {
        return this.state;
    }

    /**
     * Create a RobotControlProvider for a game.
     *
     * @param game the game to provide control for
     * @return a fresh control provider for the game
     */
    private RobotControlProvider createControlProvider(GameInfo game) {
        // Strictly speaking, this should probably be somewhere in battlecode.world
        // Whatever

        final TeamControlProvider teamProvider = new TeamControlProvider();

        teamProvider.registerControlProvider(
                Team.A,
                new PlayerControlProvider(game.getTeamA(), game.getTeamAClasses())
        );
        teamProvider.registerControlProvider(
                Team.B,
                new PlayerControlProvider(game.getTeamB(), game.getTeamBClasses())
        );
        teamProvider.registerControlProvider(
                Team.ZOMBIE,
                new ZombieControlProvider(options.getBoolean("bc.game.disable-zombies"))
        );
        teamProvider.registerControlProvider(
                Team.NEUTRAL,
                new NullControlProvider()
        );

        return teamProvider;
    }


    /**
     * Produces a string for the winner of the match.
     *
     * @return A string representing the match's winner.
     */
    public String getWinnerString(GameInfo game, Team winner, int roundNumber) {

        String teamName;

        switch (winner) {
            case A:
                teamName = game.getTeamA() + " (A)";
                break;

            case B:
                teamName = game.getTeamB() + " (B)";
                break;

            case ZOMBIE:
                teamName = "The Zombie Horde";
                break;

            default:
                teamName = "nobody";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < (50 - teamName.length()) / 2; i++)
            sb.append(' ');
        sb.append(teamName);
        sb.append(" wins (round ").append(roundNumber).append(")");

        sb.append("\nReason: ");
        GameStats stats = currentWorld.getGameStats();
        DominationFactor dom = stats.getDominationFactor();

        switch (dom) {
            case DESTROYED:
                sb.append("The winning team won by destruction.");
                break;
            case PWNED:
                sb.append("The winning team won on tiebreakers (more Archons remaining).");
                break;
            case OWNED:
                sb.append("The winning team won on tiebreakers (more Archon health).");
                break;
            case BARELY_BEAT:
                sb.append("The winning team won on tiebreakers (more Parts)");
                break;
            case WON_BY_DUBIOUS_REASONS:
                sb.append("The winning team won arbitrarily.");
                break;
            case ZOMBIFIED:
                sb.append("The Zombies have comsumed your team");
                break;
            case CLEANSED:
                sb.append("You have eradicated the Zombies");
                break;
        }

        return sb.toString();
    }

    /**
     * @return whether we are actively running a match
     */
    public boolean isRunningMatch() {
        return currentWorld != null && currentWorld.isRunning();
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
