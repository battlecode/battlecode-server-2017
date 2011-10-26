package battlecode.common;

/*
TODO:

 */

public interface PowerNode extends Robot {

	public MapLocation getLocation();
	
	public MapLocation [] neighbors();

}
