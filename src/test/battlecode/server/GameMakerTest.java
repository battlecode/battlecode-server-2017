package battlecode.server;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.schema.Event;
import battlecode.schema.GameWrapper;
import battlecode.world.TestMapBuilder;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * @author james
 */
public class GameMakerTest {

    @Test(expected=RuntimeException.class)
    public void testStateExceptions() {
        TeamMapping tm = new TeamMapping("bananas", "yellow");
        GameMaker gm = new GameMaker(tm);

        gm.makeGameFooter(Team.A);
    }

    @Test(expected=RuntimeException.class)
    public void testMatchStateExceptions() {
        TeamMapping tm = new TeamMapping("bananas", "yellow");
        GameMaker gm = new GameMaker(tm);
        gm.makeGameHeader();
        gm.createMatchMaker().makeMatchFooter(Team.A, 23);
    }

    @Test
    public void fullReasonableGame() {
        TeamMapping tm = new TeamMapping("bananas", "yellow");
        GameMaker gm = new GameMaker(tm);
        gm.makeGameHeader();
        GameMaker.MatchMaker mm = gm.createMatchMaker();
        mm.makeMatchHeader(new TestMapBuilder("honolulu", 2, -3, 50, 50,1337, 50)
                .addRobot(0, Team.A, RobotType.ARCHON, new MapLocation(0, 0)).build());
        mm.addMoved(0, new MapLocation(1, 1));
        mm.makeRound(0);
        mm.addDied(0, false);
        mm.makeRound(1);
        mm.makeMatchFooter(Team.B, 2);

        GameMaker.MatchMaker mm2 = gm.createMatchMaker();
        mm.makeMatchHeader(new TestMapBuilder("argentina", 55.3f, -3, 58.76f, 50, 1337, 50)
                .build());
        mm.makeRound(0);
        mm.makeMatchFooter(Team.A, 1);
        gm.makeGameFooter(Team.A);

        byte[] gameBytes = gm.toBytes();

        GameWrapper output = GameWrapper.getRootAsGameWrapper(ByteBuffer.wrap(gameBytes));

        assertEquals(2, output.matchHeadersLength());
        assertEquals(2, output.matchFootersLength());
        assertEquals(Event.GameHeader, output.events(0).eType());
        assertEquals(Event.MatchHeader, output.events(1).eType());
        assertEquals(Event.Round, output.events(2).eType());
        assertEquals(Event.Round, output.events(3).eType());
        assertEquals(Event.MatchFooter, output.events(4).eType());
        assertEquals(Event.MatchHeader, output.events(5).eType());
        assertEquals(Event.Round, output.events(6).eType());
        assertEquals(Event.MatchFooter, output.events(7).eType());
        assertEquals(Event.GameFooter, output.events(8).eType());

    }

}
