package battlecode.common;

/**
 * Not used for Battlecode 2014
 */
public enum Upgrade {
    IMPROVEDMINING(80, 400),
    REGENERATIVEMACHINERY(50, 300),
    NEUROMORPHICS(50, 200),
    CONTROLLEDECOPHAGY(50, 200),
	;
	
	public final int numRounds;
    public final int oreCost;
	
	private Upgrade(int rounds, int oreCost) {
		this.numRounds = rounds;
        this.oreCost = oreCost;
	}
}
