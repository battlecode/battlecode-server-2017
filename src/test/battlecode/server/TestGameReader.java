package battlecode.server;


import battlecode.schema.*;
import battlecode.schema.GameMap;
import battlecode.world.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TestGameReader {

    public GameHeader gameHeader;
    public GameFooter gameFooter;
    public List<MatchHeader> matchHeaders;
    public List<MatchFooter> matchFooters;

    public boolean runningGame;
    public boolean runningMatch;
    public int currentEventIndex;

    public GameWrapper gameWrapper;

    public TestGameReader(String path) throws IOException {
        Path gamePath = Paths.get(path);
        byte[] data = Files.readAllBytes(gamePath);
        ByteBuffer bb = ByteBuffer.wrap(data);
        this.gameWrapper = GameWrapper.getRootAsGameWrapper(bb);

        System.out.println(gameWrapper.matchHeadersLength());
        this.currentEventIndex = 1;
    }
}
