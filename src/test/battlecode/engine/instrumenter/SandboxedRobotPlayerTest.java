package battlecode.engine.instrumenter;

import org.junit.Test;

/**
 * Tests for SandboxedRobotPlayer; i.e.
 *
 * @author james
 */
public class SandboxedRobotPlayerTest {
    @Test
    public void test() throws Exception {
        ClassLoader.getSystemClassLoader().loadClass("java.lang.Object");
    }
}
