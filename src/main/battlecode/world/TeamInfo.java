package battlecode.world;

import battlecode.common.GameConstants;
import battlecode.common.Team;

/**
 * This class is used to hold information regarding team specific values such as
 * team names, bullet supply, and victory points.
 */
public class TeamInfo {

    private final String teamAName;
    private final String teamBName;
    private final long[][] teamMemory;
    private final long[][] oldTeamMemory;

    private int[] teamVictoryPoints = new int[3];
    private double[] teamBulletSupplies = new double[3];
    private int[][] teamSharedArrays = new int[3][GameConstants.BROADCAST_MAX_CHANNELS];

    public TeamInfo(String teamA, String teamB, long[][] oldTeamMemory){
        this.teamAName = teamA;
        this.teamBName = teamB;
        this.teamMemory = new long[2][oldTeamMemory[0].length];
        this.oldTeamMemory = oldTeamMemory;

        adjustBulletSupply(Team.A, GameConstants.BULLETS_INITIAL_AMOUNT);
        adjustBulletSupply(Team.B, GameConstants.BULLETS_INITIAL_AMOUNT);
    }

    // *********************************
    // ***** GETTER METHODS ************
    // *********************************

    public String getTeamName(Team t) {
        switch (t) {
            case A:
                return teamAName;
            case B:
                return teamBName;
            case NEUTRAL:
                return "neutralplayer";
            default:
                return null;
        }
    }

    public long[][] getTeamMemory() {
        return teamMemory;
    }

    public long[][] getOldTeamMemory() {
        return oldTeamMemory;
    }

    public int getVictoryPoints(Team t) {
        return teamVictoryPoints[t.ordinal()];
    }

    public double getBulletSupply(Team t) {
        return teamBulletSupplies[t.ordinal()];
    }

    // *********************************
    // ***** UPDATE METHODS ************
    // *********************************

    public void adjustBulletSupply(Team t, double amount) {
        teamBulletSupplies[t.ordinal()] += amount;
    }

    public void adjustVictoryPoints(Team t, int amount) {
        teamVictoryPoints[t.ordinal()] += amount;
    }

    public void setTeamMemory(Team t, int index, long state) {
        teamMemory[t.ordinal()][index] = state;
    }

    public void setTeamMemory(Team t, int index, long state, long mask) {
        long n = teamMemory[t.ordinal()][index];
        n &= ~mask;
        n |= (state & mask);
        teamMemory[t.ordinal()][index] = n;
    }

}
