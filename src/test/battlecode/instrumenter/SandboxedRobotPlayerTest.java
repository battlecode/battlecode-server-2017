package battlecode.instrumenter;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.server.Config;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for SandboxedRobotPlayer; i.e.
 *
 * @author james
 */
public class SandboxedRobotPlayerTest {

    static String tempClassFolder;
    @BeforeClass
    public static void setupFolder() throws Exception {
        tempClassFolder = URLUtils.toTempFolder(
                "testplayeractions/RobotPlayer.class",
                "testplayerarraybytecode/RobotPlayer.class",
                "testplayerbytecode/RobotPlayer.class",
                "testplayerclock/RobotPlayer.class",
                "testplayerdebug/RobotPlayer.class",
                "testplayerempty/RobotPlayer.class",
                "testplayerloopforever/RobotPlayer.class",
                "testplayermultiarraybytecode/RobotPlayer.class",
                "testplayernodebug/RobotPlayer.class",
                "testplayerstatic/RobotPlayer.class",
                "testplayersuicide/RobotPlayer.class",
                "testplayersystem/RobotPlayer.class",
                "testplayeruseshared/RobotPlayer.class",
                "shared/SharedUtility.class"
        );
    }

    TeamClassLoaderFactory factory;
    TeamClassLoaderFactory.Loader loader;
    RobotController rc;

    public void setupController(String teamPackageName) throws Exception {
        // Uses the "mockito" library to create a mock RobotController object,
        // so that we don't have to create a GameWorld and all that
        rc = mock(RobotController.class);

        // SandboxedRobotPlayer uses rc.getTeam; tell it we're team A
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.getType()).thenReturn(RobotType.ARCHON);
        when(rc.getID()).thenReturn(0);
        when(rc.getLocation()).thenReturn(new MapLocation(0, 0));
        when(rc.getRoundNum()).thenReturn(0);

        factory = new TeamClassLoaderFactory(tempClassFolder);
        loader = factory.createLoader();
    }

    @Test
    public void testLifecycleEmptyPlayer() throws Exception {
        setupController("testplayerempty");
        SandboxedRobotPlayer player = new SandboxedRobotPlayer("testplayerempty", rc, 0, loader);

        player.setBytecodeLimit(10000);

        player.step();

        // Player should immediately return.

        assertTrue(player.getTerminated());
    }

    @Test
    public void testRobotControllerMethodsCalled() throws Exception {
        setupController("testplayeractions");
        SandboxedRobotPlayer player = new SandboxedRobotPlayer("testplayeractions", rc, 0, loader);

        player.setBytecodeLimit(10000);

        player.step();

        assertTrue(player.getTerminated());

        // Make sure that the player called the correct methods

        verify(rc).resign();
        verify(rc).senseNearbyRobots();
        verify(rc).setTeamMemory(0, 0);
    }

    @Test
    public void testYield() throws Exception {
        setupController("testplayerclock");
        SandboxedRobotPlayer player = new SandboxedRobotPlayer("testplayerclock", rc, 0, loader);
        player.setBytecodeLimit(10000);

        player.step();

        assertFalse(player.getTerminated());
        verify(rc).broadcast(0, 1);

        player.step();

        assertFalse(player.getTerminated());
        verify(rc).broadcast(0, 2);

        player.step();

        assertTrue(player.getTerminated());
        verify(rc).broadcast(0, 3);
    }

    @Test
    public void testBytecodeCountingWorks() throws Exception {
        setupController("testplayerloopforever");
        SandboxedRobotPlayer player = new SandboxedRobotPlayer("testplayerloopforever", rc, 0, loader);
        player.setBytecodeLimit(100);

        player.step();

        // The real test is whether step returns at all, since the player doesn't yield or terminate;
        // still, we should test that the player hasn't terminated

        assertFalse(player.getTerminated());

    }

    @Test(timeout=300)
    public void testAvoidDeadlocks() throws Exception {
        setupController("testplayersuicide");
        SandboxedRobotPlayer player = new SandboxedRobotPlayer("testplayersuicide", rc, 0, loader);
        player.setBytecodeLimit(10);

        // Attempt to kill the player when it calls "disintegrate"
        // This used to deadlock because both step() and terminate() were synchronized.
        doAnswer(invocation -> {
            player.terminate();
            return null;
        }).when(rc).disintegrate();

        player.step();

        // And if the method returns, we know we have no deadlocks.
        assertTrue(player.getTerminated());
    }

    @Test
    public void testStaticInitialization() throws Exception {
        setupController("testplayerstatic");
        SandboxedRobotPlayer player = new SandboxedRobotPlayer("testplayerstatic", rc, 0, loader);
        player.setBytecodeLimit(10000);

        // Player calls "yield" in static initializer
        player.step();
        assertFalse(player.getTerminated());

        // Player terminates when actual "run" starts
        player.step();
        assertTrue(player.getTerminated());
    }

    @Test
    public void testBytecodeOveruse() throws Exception {
        setupController("testplayerbytecode");
        SandboxedRobotPlayer player = new SandboxedRobotPlayer("testplayerbytecode", rc, 0, loader);
        player.setBytecodeLimit(200);

        for (int i = 0; i < 10; i++) {
            player.step();
            assertFalse(player.getTerminated());
        }

        player.step();
        assertTrue(player.getTerminated());
    }

    @Test
    public void testArrayBytecode() throws Exception {
        setupController("testplayerarraybytecode");
        SandboxedRobotPlayer player = new SandboxedRobotPlayer("testplayerarraybytecode", rc, 0, loader);
        player.setBytecodeLimit(10000);

	int[] bytecodesUsed = new int[4];
	
        for (int i = 0; i < 4; i++) {
            player.step();
	    player.step();
            assertFalse(player.getTerminated());
	    bytecodesUsed[i] = player.getBytecodesUsed();
        }

	int baseBytecodes = 2*bytecodesUsed[0] - bytecodesUsed[1];
	int[] expectedBytecode = {2, 4, 8, 16};

	for (int i = 0; i < 4; i++)
	    assertTrue(bytecodesUsed[i] == baseBytecodes + expectedBytecode[i]);
	    
        player.step();
        assertTrue(player.getTerminated());
    }

    @Test
    public void testMultiArrayBytecode() throws Exception {
        setupController("testplayermultiarraybytecode");
        SandboxedRobotPlayer player = new SandboxedRobotPlayer("testplayermultiarraybytecode", rc, 0, loader);
        player.setBytecodeLimit(10000);

	int[] bytecodesUsed = new int[4];
	
        for (int i = 0; i < 4; i++) {
            player.step();
	    player.step();
            assertFalse(player.getTerminated());
	    bytecodesUsed[i] = player.getBytecodesUsed();
        }

	int baseBytecodes = (6*bytecodesUsed[0] - bytecodesUsed[1]) / 5;
	int[] expectedBytecode = {24, 144, 864, 5184};

	for (int i = 0; i < 4; i++)
	    assertTrue(bytecodesUsed[i] == baseBytecodes + expectedBytecode[i]);
	
        player.step();
        assertTrue(player.getTerminated());
    }

    @Test
    public void testBcTesting() throws Exception {
        Config.getGlobalConfig().set("bc.testing.should.terminate", "true");

        setupController("testplayersystem");
        SandboxedRobotPlayer player = new SandboxedRobotPlayer("testplayersystem", rc, 0, loader);
        player.setBytecodeLimit(200);

        player.step();
        assertTrue(player.getTerminated());
    }

    @Test
    public void testDebugMethodsEnabled() throws Exception {
        Config.getGlobalConfig().set("bc.engine.debug-methods", "true");

        setupController("testplayerdebug");
        SandboxedRobotPlayer player = new SandboxedRobotPlayer("testplayerdebug", rc, 0, loader);
        player.setBytecodeLimit(100);

        player.step();
        assertTrue(player.getTerminated());
    }

    @Test
    public void testDebugMethodsDisabled() throws Exception {
        Config.getGlobalConfig().set("bc.engine.debug-methods", "false");

        setupController("testplayernodebug");
        SandboxedRobotPlayer player = new SandboxedRobotPlayer("testplayernodebug", rc, 0, loader);
        player.setBytecodeLimit(200);

        player.step();
        assertTrue(player.getTerminated());
    }

    @Test
    public void testUseShared() throws Exception {
        setupController("testplayerusesshared");
        SandboxedRobotPlayer player = new SandboxedRobotPlayer("testplayerusesshared", rc, 0, loader);
        player.setBytecodeLimit(200);
        player.step();
        assertTrue(player.getTerminated());
        verify(rc).broadcast(0, 7);
    }
}
