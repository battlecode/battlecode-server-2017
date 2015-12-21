package battlecode.serial;

import java.io.Serializable;

/**
 * Represents a "Game": a series of Matches between two players,
 * on a set of maps.
 */
public class GameInfo implements Serializable {

    private static final long serialVersionUID = 638514128835207033L;

    private final String teamA;
    private final String teamB;
    private final String[] maps;

    /**
     * Create a GameInfo.
     *
     * @param teamA the A team
     * @param teamB the B team
     * @param maps the maps to play on
     */
    public GameInfo(String teamA, String teamB, String[] maps) {
        this.teamA = teamA;
        this.teamB = teamB;
        this.maps = maps;
    }

    public String[] getMaps() {
        return maps;
    }

    public String getTeamA() {
        return teamA;
    }

    public String getTeamB() {
        return teamB;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private GameInfo() {
        this(null, null, null);
    }
}
