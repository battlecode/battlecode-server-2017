package battlecode.common;

public enum Upgrade {
	FUSION    (100),
	VISION    (100),
	DIFFUSION (200),
	PICKAXE   (200),
	NUKE      (1000),
	;
	
	public final int numRounds;
	
	private Upgrade(int rounds) {
		this.numRounds = rounds;
	}
}
