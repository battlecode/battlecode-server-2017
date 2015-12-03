package battlecode.engine.instrumenter.lang;

import battlecode.engine.instrumenter.RobotMonitor;
import battlecode.server.Config;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * A Wrapper for java.lang.System that supports only arraycopy and System.out.  battlecode.engine.instrumenter.lang.System.out is
 * implemented as a RoboPrintStream, so only a subset of its methods may be implemented.
 * <p/>
 * The battlecode instrumenter should (sneakily) replace any references to java.lang.System with references to
 * battlecode.lang.System.
 *
 * Reloaded individually for every robot.
 * TODO move
 *
 * @author adamd
 */
@SuppressWarnings("unused")
public final class System {

    // singleton
    private System() {
    }

    /**
     * wrapper for java.lang.System.arraycopy(...)
     */
    public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {
        java.lang.System.arraycopy(src, srcPos, dest, destPos, length);
        if (length > 0)
            RobotMonitor.incrementBytecodes(length);
    }

    public static String getProperty(String key) {
        if (key.startsWith("bc.testing."))
            return Config.getGlobalConfig().get(key);
        else
            return null;
    }

    public static String getProperty(String key, String def) {
        String s = getProperty(key);
        return (s == null) ? def : s;
    }

    // set by RobotMonitor
    public static PrintStream out;
    public static PrintStream err;
    public static InputStream in = new InputStream() {
        public int read() throws java.io.IOException {
            throw new java.io.EOFException();
        }
    };

    /*
     public static long currentTimeMillis() {
         return battlecode.common.GameConstants.BYTECODE_LIMIT*battlecode.engine.Engine.getRoundNum()+RobotMonitor.getBytecodeNum();
     }

     public static long nanoTime() {
         return currentTimeMillis()*1000000;
     }
     */
}
