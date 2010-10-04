package battlecode.engine.instrumenter.lang;

import java.io.PrintStream;

import battlecode.engine.instrumenter.RobotMonitor;
import battlecode.world.GenericWorld;

/**
 * A Wrapper for java.lang.System that supports only arraycopy and System.out.  battlecode.engine.instrumenter.lang.System.out is
 * implemented as a RoboPrintStream, so only a subset of its methods may be implemented.
 * <p>
 * The battlecode instrumenter should (sneakily) replace any references to java.lang.System with references to
 * battlecode.lang.System.
 *
 * @author adamd
 */
public final class System {
	
	// singleton
	private System() {}
		
	/**
	 * wrapper for java.lang.System.arraycopy(...)
	 */
	public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {
		java.lang.System.arraycopy(src, srcPos, dest, destPos, length);
		if(length>0)
			RobotMonitor.incrementBytecodes(length * 8);
	}
	
	public static final PrintStream out = RoboPrintStream.theInstance();
		
	/**
	 * Sets certain properties about the gameworld.  Should be called at the beginning of each game.
	 * @param gw the GameWorld of the current game
	 * @param silenceA whether team A should be silenced
	 * @param silenceB whether team B should be silenced
	 * @param sameTeams whether the two teams are the same
	 */
	public static void setProperties(GenericWorld gw, boolean silenceA, boolean silenceB, boolean sameTeams) {
		RoboPrintStream.setProperties(gw, silenceA, silenceB, sameTeams);
	}
}