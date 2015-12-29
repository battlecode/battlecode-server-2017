package battlecode.instrumenter;

import battlecode.common.RobotController;
import battlecode.common.Team;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for SandboxedRobotPlayer; i.e.
 *
 * @author james
 */
public class SandboxedRobotPlayerTest {

    RobotController rc;

    @Before
    public void setupController() {
        // Uses the "mockito" library to create a mock RobotController object,
        // so that we don't have to create a GameWorld and all that
        rc = mock(RobotController.class);

        // SandboxedRobotPlayer uses rc.getTeam; tell it we're team A
        when(rc.getTeam()).thenReturn(Team.A);
    }

    @Test
    public void testLifecycleEmptyPlayer() throws Exception {
        SandboxedRobotPlayer player = new SandboxedRobotPlayer("testplayerempty", "RobotPlayer", rc, 0);

        player.setBytecodeLimit(10000);

        player.step();

        // Player should immediately return.

        assertTrue(player.getTerminated());
    }

    @Test
    public void testRobotControllerMethodsCalled() throws Exception {
        SandboxedRobotPlayer player = new SandboxedRobotPlayer("testplayeractions", "RobotPlayer", rc, 0);

        player.setBytecodeLimit(10000);

        player.step();

        assertTrue(player.getTerminated());

        // Make sure that the player called the correct methods

        verify(rc).addMatchObservation("text");
        verify(rc).readBroadcast(0);
        verify(rc).resign();
        verify(rc).senseNearbyRobots();
        verify(rc).setTeamMemory(0, 0);
    }

    @Test
    public void testYield() throws Exception {
        SandboxedRobotPlayer player = new SandboxedRobotPlayer("testplayerclock", "RobotPlayer", rc, 0);
        player.setBytecodeLimit(10000);

        player.step();

        assertFalse(player.getTerminated());
        verify(rc).broadcast(0, 0);

        player.step();

        assertFalse(player.getTerminated());
        verify(rc).broadcast(0, 1);

        player.step();

        assertTrue(player.getTerminated());
        verify(rc).broadcast(0, 2);
    }

    @Test
    public void testBytecodeCountingWorks() throws Exception {
        SandboxedRobotPlayer player = new SandboxedRobotPlayer("testplayerloopforever", "RobotPlayer", rc, 0);
        player.setBytecodeLimit(100);

        player.step();

        // The real test is whether step returns at all, since the player doesn't yield or terminate;
        // still, we should test that the player hasn't terminated

        assertFalse(player.getTerminated());

    }

    @Test
    public void testBytecodeCountsCorrect() throws Exception {
        SandboxedRobotPlayer player = new SandboxedRobotPlayer("testplayerclock", "RobotPlayer", rc, 0);
        player.setBytecodeLimit(10000);

        player.step();

        verify(rc).broadcast(0, 0);
        // broadcast() is 25 bytecodes, +3 extra
        assertEquals(player.getBytecodesUsed(), 28);
    }
}
