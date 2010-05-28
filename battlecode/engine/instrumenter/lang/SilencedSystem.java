package battlecode.engine.instrumenter.lang;

import java.io.PrintStream;

import battlecode.engine.instrumenter.RobotMonitor;
import battlecode.world.GameWorld;

/**
 * A Wrapper for java.lang.System that supports only arraycopy and System.out.  SilencedSystem.out is
 * implemented as a SilencedPrintStream, it doesn't actually print anything.
 * <p>
 * The battlecode instrumenter should (sneakily) replace any references to java.lang.System with references to
 * SilencedSystem, if the robot should be silenced.
 *
 * @author adamd
 */
public final class SilencedSystem {
	
	// singleton
	private SilencedSystem() {}
		
	/**
	 * wrapper for java.lang.System.arraycopy(...)
	 */
	public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {
		java.lang.System.arraycopy(src, srcPos, dest, destPos, length);
		if(length>0)
			RobotMonitor.incrementBytecodes(length * 8);
	}
	
	public static final PrintStream out = SilencedPrintStream.theInstance();
}