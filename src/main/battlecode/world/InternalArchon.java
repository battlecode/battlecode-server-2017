package battlecode.world;

import battlecode.common.*;
import static battlecode.common.GameConstants.*;

public class InternalArchon extends InternalRobot {

	private volatile double myProduction;
	
	// TODO: volatile?
	private int roundsLeft = 0;
	
	private final int myArchonID;
		
	private static int[] nextArchonID;
		
	static {
		reset();
	}
	
	public static void reset() {
		nextArchonID = new int[2];
		nextArchonID[0] = 0;
		nextArchonID[1] = 0;
	}		
			
	public InternalArchon(GameWorld gw, MapLocation loc, Team t, boolean wakeDelay, double production) {
		super(gw, RobotType.ARCHON, loc, t, wakeDelay);
		
		myProduction = ARCHON_PRODUCTION;
		myArchonID = nextArchonID[t.ordinal()]++;
	}
		
	public void processEndOfRound() {
		double production = myProduction;
		changeEnergonLevel(production);
		
		super.processEndOfRound();
	}
	
	public double getProduction() {
		return myProduction;
	}
	
	public int getRoundsLeft() {
		return roundsLeft;
	}

	public void addFlux(int amount) {
		System.out.println("InternalArchon.addFlux is deprecated");
	}
	
	// TODO get rid of constants
	public void burnFlux() {
		System.out.println("InternalArchon.burnFlux is deprecated");
	}
	
	public boolean canBurnFlux() {
		System.out.println("InternalArchon.canBurnFlux is deprecated");
		return false;
	}
	
	public void setArchonMemory(long memory) {
		myGameWorld.setArchonMemory(getTeam(), myArchonID, memory);
	}
	
}