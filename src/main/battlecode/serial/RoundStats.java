package battlecode.serial;

import java.io.Serializable;
import java.util.ArrayList;

import battlecode.common.ComponentType;
import battlecode.common.Team;

// Stub for compatibility with last year's Proxy interface.
public class RoundStats implements Serializable {

    private static final long serialVersionUID = -2422655921465613943L;
    private final double[] gatheredPoints;
    private final double[] points;
    
    ArrayList<ComponentType> aTeamComponents;
    ArrayList<ComponentType> bTeamComponents;

    public RoundStats(double aPoints, double bPoints, double agp, double bgp, ArrayList<ComponentType> aComponents, ArrayList<ComponentType> bComponents) {
        points = new double[]{aPoints, bPoints};
        gatheredPoints = new double[]{agp, bgp};
        aTeamComponents = aComponents;
        bTeamComponents = bComponents;
    }
    
    public ArrayList<ComponentType> getATeamComponents(){
    	return aTeamComponents;
    }
    
    public ArrayList<ComponentType> getBTeamComponents(){
    	return bTeamComponents;
    	
    }
    
    public double getPoints(Team team) {
        return points[team.ordinal()];
    }

    public double getGatheredPoints(Team team) {
        return gatheredPoints[team.ordinal()];
    }
}
