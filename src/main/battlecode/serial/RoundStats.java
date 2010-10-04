package battlecode.serial;

import java.io.Serializable;

import battlecode.common.Team;

// Stub for compatibility with last year's Proxy interface.
public class RoundStats implements Serializable {

	private static final long serialVersionUID = -2422655921465613943L;
	
	//~ private final int totalA, totalB;
	private final double[] productionA, productionB, points;
	//~ private final double energonA, energonB;
	//~ private final int archonsA, archonsB;
	
	public RoundStats(double[] productionA, double[] productionB, double aPoints, double bPoints) {
		//~ totalA = stats.getActiveTotal(Team.A);
		//~ totalB = stats.getActiveTotal(Team.B);
		this.productionA = productionA;
		this.productionB = productionB;
		//~ energonA = stats.getEnergon(Team.A);
		//~ energonB = stats.getEnergon(Team.B);
		//~ archonsA = stats.getNumArchons(Team.A);
		//~ archonsB = stats.getNumArchons(Team.A);
		points=new double[2]; // 0 is Team A, 1 is Team B
		points[0]=aPoints;
		points[1]=bPoints;
	}
	
	//~ public int getActiveTotal(Team team) {
		//~ return (team == Team.A ? totalA : totalB);
	//~ }
	
	public double[] getArchonProduction(Team team) {
		return (team == Team.A ? productionA : productionB);
	}
	
	public double getPoints(Team team) {
		return (team == Team.A ? points[0] : points[1]);
	}
	
	//~ public double getEnergon(Team team) {
		//~ return (team == Team.A ? energonA : energonB);
	//~ }
	
	//~ public int getNumArchons(Team team) {
		//~ return (team == Team.A ? archonsA : archonsB);
	//~ }

}
