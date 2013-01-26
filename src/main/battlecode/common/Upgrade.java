package battlecode.common;

public enum Upgrade {
	FUSION    (25),
	VISION    (25),
	DEFUSION  (25),
	PICKAXE   (25),
	NUKE      (404),
	;
	
	public final int numRounds;
	
	private Upgrade(int rounds) {
		this.numRounds = rounds;
	}
}
