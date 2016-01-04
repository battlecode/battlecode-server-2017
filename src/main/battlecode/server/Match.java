package battlecode.server;

import battlecode.common.GameConstants;
import battlecode.common.Team;
import battlecode.world.DominationFactor;
import battlecode.world.control.NullControlProvider;
import battlecode.world.control.ZombieControlProvider;
import battlecode.world.signal.InternalSignal;
import battlecode.serial.*;
import battlecode.world.GameMap;
import battlecode.world.GameWorld;
import battlecode.world.GameMapIO;
import battlecode.world.control.PlayerControlProvider;
import battlecode.world.control.TeamControlProvider;

import java.io.File;

/**
 * Abstracts the game engine for the server. This class is responsible for
 * starting and managing matches and exporting match status and results to the
 * server.
 */
public class Match {

    /**
     * The GameWorld that runs the match.
     */
    private GameWorld gameWorld;

    /**
     * The GameInfo from which this match was created.
     */
    private final GameInfo info;

    /**
     * The map for this match (one of the maps in info).
     */
    private final String map;

    /**
     * The command-line/config file options to use.
     */
    private final Config options;

    /**
     * The state of the running game.
     */
    private GameState gameState;

    /**
     * The initial memory of the teams playing the map.
     */
    private long[][] state = new long[2][GameConstants.TEAM_MEMORY_LENGTH];

    /**
     * The number of this match.
     */
    private int number;

    /**
     * The count of this match.
     */
    private int count;

    /**
     * The team memory after this match has been run.
     */
    private long[][] computedTeamMemory = null;

    /**
     * Creates a new match with the given parameters and options.
     *
     * @param info    the teams and map to use when running this match
     * @param options options relevant to match creation (i.e., default map path)
     */
    public Match(GameInfo info, String map, Config options, int number,
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
    public void initialize() throws Exception {
        final File mapPath = new File(options.get("bc.game.map-path"));

        // Load the map for the match
        final GameMap loadedMap = GameMapIO.loadMap(map, mapPath);

        // Create the control provider for the match
        // TODO move this somewhere better-fitting
        final TeamControlProvider teamProvider = new TeamControlProvider();
        teamProvider.registerControlProvider(
                Team.A,
                new PlayerControlProvider(info.getTeamA())
        );
        teamProvider.registerControlProvider(
                Team.B,
                new PlayerControlProvider(info.getTeamB())
        );
        teamProvider.registerControlProvider(
                Team.ZOMBIE,
                new ZombieControlProvider(options.getBoolean("bc.game.disable-zombies"))
        );
        teamProvider.registerControlProvider(
                Team.NEUTRAL,
                new NullControlProvider()
        );

        // Create the game world!
        gameWorld = new GameWorld(loadedMap, teamProvider, info.getTeamA(), info.getTeamB(), state);
    }

    /**
     * Sends a internalSignal directly to the game engine, possibly altering the match
     * state.
     *
     * @param internalSignal the internalSignal to send to the engine
     * @return the currentInternalSignals that represent the effect of the alteration, or an
     *         empty internalSignal array if there was no effect
     */
    public InjectDelta inject(InternalSignal internalSignal) {
        assert isInitialized();

        try {
            return new InjectDelta(true, gameWorld.inject(internalSignal));
        } catch (final RuntimeException e) {
            System.err.println("Injection failure: "+e.getMessage());
            e.printStackTrace();
            return new InjectDelta(false, new InternalSignal[0]);
        }
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
     * Runs the next round, returning a delta containing all the currentInternalSignals raised
     * during that round. Notifies observers of anything other than a successful
     * delta-producing run.
     *
     * @return the currentInternalSignals generated for the next round of the game, or null if
     *         the engine's result was a breakpoint or completion
     */
    public RoundDelta getRound() {

        if (gameWorld == null) {
            System.out.println("Match.getRound(): Null GameWorld, return null");
            return null;
        }

        // Run the next round.
        gameState = gameWorld.runRound();

        if (gameState == GameState.DONE)
            return null;

        // Serialize the changes to the GameWorld.
        return new RoundDelta(gameWorld.getAllSignals(true));
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
     * Get the current game state.
     *
     * @return the current game state.
     */
    public GameState getGameState() {
        return gameState;
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
