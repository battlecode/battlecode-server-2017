package battlecode.server;

import battlecode.common.Team;
import org.junit.Test;

import static org.junit.Assert.*;

public class TeamMappingTest {

    @Test
    public void testGetters(){
        TeamMapping teamMapping = new TeamMapping(
                "teamA", "teamA.testbot",
                "teamB", "bananas");

        assertEquals(teamMapping.getTeamAName(), "teamA");
        assertEquals(teamMapping.getTeamAPackage(), "teamA.testbot");
        assertEquals(teamMapping.getTeamBName(), "teamB");
        assertEquals(teamMapping.getTeamBPackage(), "bananas");
        assertEquals(Team.A, teamMapping.getTeamFromID(teamMapping.getTeamAID()));
        assertEquals(Team.B, teamMapping.getTeamFromID(teamMapping.getTeamBID()));
        assertEquals(teamMapping.getTeamAID(), teamMapping.getIDFromTeam(Team.A));
        assertEquals(teamMapping.getTeamBID(), teamMapping.getIDFromTeam(Team.B));
    }

}
