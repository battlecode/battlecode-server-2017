package battlecode.serial;

import battlecode.common.Team;

import java.io.Serializable;

// Stub for compatibility with last year's Proxy interface.
public class RoundStats implements Serializable {

    private static final long serialVersionUID = -2422655921465613943L;
    private final double[] gatheredPoints;
    private final double[] points;

    public RoundStats(double aPoints, double bPoints, double agp, double bgp) {
        points = new double[]{aPoints, bPoints};
        gatheredPoints = new double[]{agp, bgp};
    }

    public double getPoints(Team team) {
        return points[team.ordinal()];
    }

    public double getGatheredPoints(Team team) {
        return gatheredPoints[team.ordinal()];
    }
}
