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

    public MatchInfo(String teamA, String teamB, String mapName) {
        this(teamA, teamB, new String[]{mapName});
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
}
