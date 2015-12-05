package battlecode.server;

import battlecode.common.GameConstants;
import battlecode.common.Team;
import battlecode.engine.ErrorReporter;
import battlecode.engine.GameState;
import battlecode.engine.signal.Signal;
import battlecode.serial.*;
import battlecode.world.GameMap;
import battlecode.world.GameWorld;
import battlecode.world.XMLMapHandler;
import battlecode.world.control.PlayerControlProvider;
import battlecode.world.control.RobotControlProvider;
import battlecode.world.control.TeamControlProvider;

import java.util.Observable;

/**
 * Abstracts the game engine for the server. This class is responsible for
 * starting and managing matches and exporting match status and results to the
 * server.
 */
public class Match extends Observable {

    /**
     * The GenericWorld for getting signals.
     */
    private GameWorld gameWorld;

    /**
     * The MatchInfo from which this match was created.
     */
    private final MatchInfo info;

    /**
     * The map for this match (one of the maps in info).
     */
    private final String map;

    /**
     * The command-line/config file options to use.
     */
    private final Config options;

    private long[][] state = new long[2][GameConstants.TEAM_MEMORY_LENGTH];

    private int number;

    private int count;

    private boolean bytecodesUsedEnabled = true;

    private long[][] computedTeamMemory = null;

    /**
     * Creates a new match with the given parameters and options.
     *
     * @param info    the teams and map to use when running this match
     * @param options options relevant to match creation (i.e., default map path)
     */
    public Match(MatchInfo info, String map, Config options, int number,
                 int count) {

        this.info = info;
        this.map = map;
        this.options = options;

        this.number = number;
        this.count = count;

        this.gameWorld = null;
    }

    /**
     * Sets up the engine for this match. Because GameWorld's constructor
     * manipulates static state, engine object creation should not be done at
     * match creation time!
     */
    public void initialize() {
        String mapPath = options.get("bc.game.map-path");

        try {
            // Load the map for the match
            final XMLMapHandler handler = XMLMapHandler.loadMap(map, mapPath);
            final GameMap map = handler.getParsedMap();

            // Create the control provider for the match
            // TODO move this somewhere better-fitting
            final TeamControlProvider teamProvider = new TeamControlProvider();
            final RobotControlProvider playerProvider = new PlayerControlProvider();
            teamProvider.registerControlProvider(Team.A, playerProvider);
            teamProvider.registerControlProvider(Team.B, playerProvider);
            teamProvider.registerControlProvider(Team.ZOMBIE, playerProvider);

            // Create the game world!
            gameWorld = new GameWorld(map, teamProvider, info.getTeamA(), info.getTeamB(), state);
        } catch (IllegalArgumentException e) {
            System.out.println("[Engine] Error while loading map '" + map + "'");
            throw e;
        } catch (Exception e) {
            ErrorReporter.report(e);
            throw e;
        }

        assert this.gameWorld != null;
    }

    /**
     * Sends a signal directly to the game engine, possibly altering the match
     * state.
     *
     * @param signal the signal to send to the engine
     * @return the signals that represent the effect of the alteration, or an
     *         empty signal array if there was no effect
     */
    public Signal[] alter(Signal signal) {
        gameWorld.clearAllSignals();
        try {
            gameWorld.visitSignal(signal);
        } catch (RuntimeException e) {
            return new Signal[0];
        }

        return gameWorld.getAllSignals(false);
    }

    /**
     * Determines whether or not this match is ready to run.
     *
     * @return true if the match has been initialized, false otherwise
     */
    public boolean isInitialized() {
        return this.gameWorld != null;
    }

    /**
     * Runs the next round, returning a delta containing all the signals raised
     * during that round. Notifies observers of anything other than a successful
     * delta-producing run.
     *
     * @return the signals generated for the next round of the game, or null if
     *         the engine's result was a breakpoint or completion
     */
    public RoundDelta getRound() {

        if (gameWorld == null) {
            System.out.println("Match.getRound(): Null GameWorld, return null");
            return null;
        }

        // Run the next round.
        GameState result = gameWorld.runRound();

        // Notify the server of any other result.
        if (result == GameState.BREAKPOINT) {
            setChanged();
            notifyObservers(result);
            clearChanged();
        }

        if (result == GameState.DONE)
            return null;

        // Serialize the newly modified GameWorld.
        return new RoundDelta(
                gameWorld.getAllSignals(this.bytecodesUsedEnabled));
    }

    /**
     * Queries the engine for stats for the most recent round and returns them.
     *
     * @return round stats from the engine
     */
    public RoundStats getStats() {
        return gameWorld.getRoundStats();
    }

    /**
     * Queries the engine for stats for the whole match.
     *
     * @return game stats from the engine
     */
    public GameStats getGameStats() {
        return gameWorld.getGameStats();
    }

    /**
     * Gets the header data for this match.
     *
     * @return this match's header
     */
    public MatchHeader getHeader() {
        return new MatchHeader(gameWorld.getGameMap(), state, number,
                count);
    }

    /**
     * Gets team and map metadata for this match.
     *
     * @return an ExtensibleMetadata with teams and maps
     */
    public ExtensibleMetadata getHeaderMetadata() {
        ExtensibleMetadata ex = new ExtensibleMetadata();
        ex.put("type", "header");
        ex.put("team-a", info.getTeamA());
        ex.put("team-b", info.getTeamB());
        ex.put("maps", info.getMaps());
        return ex;
    }

    /**
     * Gets the footer data for this match.
     *
     * @return this match's footer
     */
    public MatchFooter getFooter() {
        return new MatchFooter(gameWorld.getWinner(),
                getComputedTeamMemory());
    }

    /**
     * Gets the winner of this match.
     *
     * @return the Team that has won the match, or null if the match has not yet
     *         finished
     */
    public Team getWinner() {
        if (hasMoreRounds())
            return null;
        return gameWorld.getWinner();
    }

    /**
     * Determines whether or not there are more rounds to be run in this match.
     *
     * @return true if the match has finished running, false otherwise
     */
    public boolean hasMoreRounds() {
        return gameWorld != null && gameWorld.isRunning();
    }

    /**
     * Produces a string for the winner of the match.
     *
     * @return A string representing the match's winner.
     */
    public String getWinnerString() {

        String teamName;

        switch (getWinner()) {
            case A:
                teamName = info.getTeamA() + " (A)";
                break;

            case B:
                teamName = info.getTeamB() + " (B)";
                break;

            default:
                teamName = "nobody";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < (50 - teamName.length()) / 2; i++)
            sb.append(' ');
        sb.append(teamName);
        sb.append(" wins (round ").append(getRoundNumber()).append(")");

        sb.append("\nReason: ");
        GameStats stats = gameWorld.getGameStats();
        DominationFactor dom = stats.getDominationFactor();
        if (dom == DominationFactor.DESTROYED)
            sb.append("The winning team won by destruction.");
        else if (dom == DominationFactor.PWNED)
            sb.append("The winning team won on tiebreakers (more Archons remaining).");
        else if (dom == DominationFactor.OWNED)
            sb.append("The winning team won on tiebreakers (more Archon health).");
        else if (dom == DominationFactor.BARELY_BEAT)
            sb.append("The winning team won on tiebreakers (more Parts)");
        else if (dom == DominationFactor.WON_BY_DUBIOUS_REASONS)
            sb.append("Team ").append(getWinner()).append(" won arbitrarily.");

        return sb.toString();
    }

    public void setInitialTeamMemory(long[][] state) {
        this.state = state;
    }

    public long[][] getInitialTeamMemory() {
        return this.state;
    }

    public long[][] getComputedTeamMemory() {
        if (computedTeamMemory == null)
            return this.gameWorld.getTeamMemory();

        else return computedTeamMemory;
    }

    /**
     * @return the number of the most recently computed round, where the first
     *         round is 1 (0 if no rounds have been run yet)
     */
    public int getRoundNumber() {
        return gameWorld.getCurrentRound() + 1;
    }

    /**
     * Cleans up the match so that its resources can be garbage collected.
     */
    public void finish() {
        this.computedTeamMemory = this.gameWorld.getTeamMemory();
        this.gameWorld = null;
    }

    @Override
    public String toString() {
        return String.format("%s vs. %s on %s", info.getTeamA(), info
                .getTeamB(), map);
    }
}
