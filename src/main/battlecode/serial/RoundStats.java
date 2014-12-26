package battlecode.serial;

import battlecode.common.Team;

import java.io.Serializable;

// Stub for compatibility with last year's Proxy interface.
public class RoundStats implements Serializable {

    private static final long serialVersionUID = -2422655921465613943L;
    private final double[] points;

    public RoundStats(double aPoints, double bPoints) {
        points = new double[]{aPoints, bPoints};
    }

    public double getPoints(Team team) {
        return points[team.ordinal()];
    }
}
