package battlecode.serial;

import java.io.Serializable;

import battlecode.common.Team;

// Stub for compatibility with last year's Proxy interface.
public class RoundStats implements Serializable {

    private static final long serialVersionUID = -2422655921465613943L;
    private final double[] points;

    public RoundStats(double aPoints, double bPoints) {
        points = new double[] { aPoints, bPoints };
    }

    public double getPoints(Team team) {
        return points[team.ordinal()];
    }
}
