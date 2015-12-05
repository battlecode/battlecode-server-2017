package battlecode.engine.instrumenter;

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

        player.setBytecodeLimit(1000);

        player.step();

        // Player should immediately return.

        assertTrue(player.getTerminated());
    }
}
