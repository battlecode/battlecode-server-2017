package battlecode.common;

public enum Upgrade {
	FUSION    (30),
	VISION    (50),
	DIFFUSION (50),
	PICKAXE   (80),
	NUKE      (400),
	;
	
	public final int numRounds;
	
	private Upgrade(int rounds) {
		this.numRounds = rounds;
	}
}
