package battlecode.serial;

import battlecode.common.Team;

import java.io.Serializable;

/**
 * Used to keep track of various statistics in a given
 * battlecode match.  These should be stats that don't change from round
 * to round, but rather are given only at the end of the match.
 * <p/>
 * excitement factor currently isn't calculated in the engine
 */
public class GameStats implements Serializable {

    private static final long serialVersionUID = 4678980796113812229L;

    private int[] timeToFirstKill = new int[2];
    private int[] timeToFirstArchonKill = new int[2];
    private double[] totalPoints = new double[2];
    private int[] numArchons = new int[2];
    private double[] totalEnergon = new double[2];
    private DominationFactor dominationFactor = null;
    private double excitementFactor = 0.0;

    private int timeToTallestTower = 0;
    private int tallestTower = 0;

    public GameStats() {
    }

    public void setUnitKilled(Team t, int numRounds) {
        int index = t.opponent().ordinal();
        if (index < 2 && timeToFirstKill[index] == 0)
            timeToFirstKill[index] = numRounds;
    }

    public void setArchonKilled(Team t, int numRounds) {
        setUnitKilled(t, numRounds);
        int index = t.opponent().ordinal();
        if (timeToFirstArchonKill[index] == 0)
            timeToFirstArchonKill[index] = numRounds;
    }

    public void setDominationFactor(DominationFactor factor) {
        this.dominationFactor = factor;
    }

    public void setExcitementFactor(double factor) {
        this.excitementFactor = factor;
    }

    public void setPoints(Team t, double points) {
        int index = t.ordinal();
        if (totalPoints[index] == 0)
            totalPoints[index] = points;
    }

    public void setNumArchons(Team t, int archons) {
        int index = t.ordinal();
        if (numArchons[index] == 0)
            numArchons[index] = archons;
    }

    public void setTotalEnergon(Team t, double energon) {
        int index = t.ordinal();
        if (totalEnergon[index] == 0)
            totalEnergon[index] = energon;
    }

    public void setTimeToTallestTower(int numRounds) {
        timeToTallestTower = numRounds;
    }

    public void setTallestTower(int height) {
        tallestTower = height;
    }

    public int[] getTimeToFirstKill() {
        return timeToFirstKill;
    }

    public int[] getTimeToFirstArchonKill() {
        return timeToFirstArchonKill;
    }

    public DominationFactor getDominationFactor() {
        return this.dominationFactor;
    }

    public double getExcitementFactor() {
        return this.excitementFactor;
    }

    public double[] getTotalPoints() {
        return this.totalPoints;
    }

    public double[] getTotalEnergon() {
        return this.totalEnergon;
    }

    public int[] getNumArchons() {
        return this.numArchons;
    }

    public int getTimeToTallestTower() {
        return timeToTallestTower;
    }

    public int getTallestTower() {
        return tallestTower;
    }
}
