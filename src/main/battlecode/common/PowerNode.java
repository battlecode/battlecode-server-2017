package battlecode.common;

public interface PowerNode extends GameObject {

	/** Returns this power node's location. */
	public MapLocation getLocation();
	
	/** Returns the locations of all power nodes that are connected to this power node. */
	public MapLocation [] neighbors();

	/** Returns true if this power node is a team's power core. */
	public boolean isPowerCore();

}
