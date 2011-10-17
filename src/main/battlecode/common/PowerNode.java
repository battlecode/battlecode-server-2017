package battlecode.common;

/*
TODO:

 */

public interface PowerNode extends GameObject {

	public MapLocation getLocation();
	
	public MapLocation [] neighbors();

}
