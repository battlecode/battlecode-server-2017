package battlecode.serial;

import battlecode.world.GameMap;

public class MatchHeader implements ServerEvent {

    private static final long serialVersionUID = -4228142649046586925L;

    /**
     * The map the match is being played on.
     */
    private final GameMap map;

    /**
     * The initial memory of the teams.
     */
    private final long[][] state;

    /**
     * The number of the current match.
     */
    private final int matchNumber;

    /**
     * The number of matches being run in this game.
     */
    private final int matchCount;

    public MatchHeader(GameMap map, long[][] state, int matchNumber, int matchCount) {
        this.map = map;
        this.state = state;
        this.matchNumber = matchNumber;
        this.matchCount = matchCount;
    }

    public int getMatchCount() {
        return matchCount;
    }

    public int getMatchNumber() {
        return matchNumber;
    }

    public GameMap getMap() {
        return map;
    }

    public long[][] getState() {
        return state;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private MatchHeader() {
        this(null, null, 0, 0);
    }
}
