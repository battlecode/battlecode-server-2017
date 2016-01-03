package battlecode.world;

import battlecode.common.RobotType;
import battlecode.common.Team;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author james
 */
public class GameMapTest {
    @Test
    public void testNotTournamentLegal() {
        final GameMap asymBots = new TestMapGenerator(2,1, 0)
                .withRobot(RobotType.ARCHON, Team.A, 0,0)
                .getMap("asymBots");

        assertFalse(asymBots.isTournamentLegal());

        final GameMap asymParts = new TestMapGenerator(2,2, 0)
                .withParts(0,0, 1.0)
                .withParts(0,1, 3.0)
                .getMap("asymParts");

        assertFalse(asymParts.isTournamentLegal());

        final GameMap asymRubble = new TestMapGenerator(2,2, 0)
                .withRubble(0,0, 1.0)
                .withRubble(0,1, 3.0)
                .getMap("asymRubble");

        assertFalse(asymRubble.isTournamentLegal());
    }

    @Test
    public void testTournamentLegal() {
        final GameMap horizSymmetricDen = new TestMapGenerator(3,1, 0)
        .withRobot(RobotType.ARCHON, Team.A, 0,0)
        .withRobot(RobotType.ARCHON, Team.B, 2,0)
        .withRobot(RobotType.ZOMBIEDEN, Team.ZOMBIE, 1, 0)
        .withParts(0,0, 50.0).withParts(1,0, 20.0).withParts(2,0, 50.0)
        .withRubble(0,0, 100.0).withRubble(2,0, 100).getMap("horizSymmetric");
        // Den lies on a line of symmetry, so fails
        assertFalse(horizSymmetricDen.isTournamentLegal());
        
        final GameMap horizSymmetric = new TestMapGenerator(3,1, 0)
                .withRobot(RobotType.ARCHON, Team.A, 0,0)
                .withRobot(RobotType.ARCHON, Team.B, 2,0)
                .withRobot(RobotType.STANDARDZOMBIE, Team.ZOMBIE, 1, 0)
                .withParts(0,0, 50.0).withParts(1,0, 20.0).withParts(2,0, 50.0)
                .withRubble(0,0, 100.0).withRubble(2,0, 100).getMap("horizSymmetric");

        assertTrue(horizSymmetric.isTournamentLegal());

        final GameMap vertSymmetric = new TestMapGenerator(1,3, 0)
                .withRobot(RobotType.ARCHON, Team.A, 0,0)
                .withRobot(RobotType.ARCHON, Team.B, 0,2)
                .withRobot(RobotType.STANDARDZOMBIE, Team.ZOMBIE, 0, 1)
                .withParts(0,0, 50.0).withParts(0,1, 20.0).withParts(0,2, 50.0)
                .withRubble(0,0, 100.0).withRubble(0,2, 100.0).getMap("vertSymmetric");

        assertTrue(vertSymmetric.isTournamentLegal());

        final GameMap rotSymmetric = new TestMapGenerator(3,3, 0)
                .withRobot(RobotType.ARCHON, Team.A, 0,0)
                .withRobot(RobotType.ARCHON, Team.B, 2,2)
                .withRobot(RobotType.ZOMBIEDEN, Team.ZOMBIE, 0,1)
                .withRobot(RobotType.ZOMBIEDEN, Team.ZOMBIE, 2,1)
                .withParts(1,0, 50.0)
                .withParts(1,2, 50.0)
                .withRubble(2,0, 25.0)
                .withRubble(0,2, 25.0)
                .getMap("rotSymmetric");

        assertTrue(rotSymmetric.isTournamentLegal());

        final GameMap posDiagSymmetric = new TestMapGenerator(3,3, 0)
                .withRobot(RobotType.ARCHON, Team.A, 2,0)
                .withRobot(RobotType.ARCHON, Team.B, 0,2)
                .withRobot(RobotType.ZOMBIEDEN, Team.ZOMBIE, 0,1)
                .withRobot(RobotType.ZOMBIEDEN, Team.ZOMBIE, 1,0)
                .withRobot(RobotType.SCOUT, Team.NEUTRAL, 1,1)
                .withParts(1,0, 50.0)
                .withParts(0,1, 50.0)
                .withRubble(2,0, 25.0)
                .withRubble(0,2, 25.0)
                .withRubble(0,0, 27.3)
                .getMap("posDiagSymmetric");

        assertTrue(posDiagSymmetric.isTournamentLegal());

        final GameMap negDiagSymmetric = new TestMapGenerator(3,3, 0)
                .withRobot(RobotType.ARCHON, Team.A, 0,0)
                .withRobot(RobotType.ARCHON, Team.B, 2,2)
                .withRobot(RobotType.ZOMBIEDEN, Team.ZOMBIE, 0,1)
                .withRobot(RobotType.ZOMBIEDEN, Team.ZOMBIE, 1,2)
                .withRobot(RobotType.SCOUT, Team.NEUTRAL, 1,1)
                .withParts(1,0, 50.0)
                .withParts(2,1, 50.0)
                .withRubble(2,0, 25.0)
                .withRubble(0,0, 27.3)
                .withRubble(2,2, 27.3)
                .getMap("negDiagSymmetric");

        assertTrue(negDiagSymmetric.isTournamentLegal());
    }
}
