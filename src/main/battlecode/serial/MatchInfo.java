package battlecode.serial;

import java.io.Serializable;

public class MatchInfo implements Serializable {

    private static final long serialVersionUID = 638514128835207033L;

    private final String teamA;
    private final String teamB;
    private final String[] maps;

    public MatchInfo(String teamA, String teamB, String[] maps) {
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
    private MatchInfo() {
        this(null, null, null);
    }
}
