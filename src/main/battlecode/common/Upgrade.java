package battlecode.common;

/**
 * Not used for Battlecode 2014
 */
public enum Upgrade {
    // Engineering Bay upgrades
    IMPROVEDBUILDING(50, 200, RobotType.ENGINEERINGBAY),
    IMPROVEDMINING(80, 400, RobotType.ENGINEERINGBAY),

    // Biomechatronic Research Lab upgrades
    REGENERATIVEMACHINERY(50, 300, RobotType.BIOMECHATRONICRESEARCHLAB),
    NEUROMORPHICS(50, 200, RobotType.BIOMECHATRONICRESEARCHLAB),
    CONTROLLEDECOPHAGY(50, 200, RobotType.BIOMECHATRONICRESEARCHLAB),
	;
	
	public final int numRounds;
    public final int oreCost;
    public final RobotType researcher;
	
	private Upgrade(int rounds, int oreCost, RobotType researcher) {
		this.numRounds = rounds;
        this.oreCost = oreCost;
        this.researcher = researcher;
	}
}
