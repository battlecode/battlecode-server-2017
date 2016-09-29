package battlecode.server;

import battlecode.schema.GameFooter;
import battlecode.schema.GameWrapper;
import com.google.flatbuffers.FlatBufferBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to make flatbuffer objects needed to serialize a Game.
 */
public class GameMaker {

    private FlatBufferBuilder builder;
    private int finalGameWrapper;
    private boolean isGameFinished = false;

    private List<Integer> events; // EventWrappers
    private List<Integer> matchHeaders;
    private List<Integer> matchFooters;

    public GameMaker(){
        this.builder = new FlatBufferBuilder();
        this.events = new ArrayList<>();
        this.matchHeaders = new ArrayList<>();
        this.matchFooters = new ArrayList<>();
    }

    // **********************************
    // ***** GETTER METHODS *************
    // **********************************

    public FlatBufferBuilder getBuilder(){
        return builder;
    }

    // **********************************
    // ***** GAME HEADER METHODS ********
    // **********************************

    public void makeGameHeader(String specVersion, TeamMapping teamMapping){
        return;
    }

    // **********************************
    // ***** GAME FOOTER METHODS ********
    // **********************************

    public void makeGameFooter(byte winner){
        events.add(GameFooter.createGameFooter(builder, winner));
    }

    // **********************************
    // ***** GAME WRAPPER METHODS *******
    // **********************************

    public void makeGameWrapper(){
        int events = GameWrapper.createEventsVector(builder, ArrayUtils.toPrimitive(this.events.toArray(new Integer[this.events.size()])));
        int matchHeaders = GameWrapper.createMatchHeadersVector(builder, ArrayUtils.toPrimitive(this.matchHeaders.toArray(new Integer[this.matchHeaders.size()])));
        int matchFooters = GameWrapper.createMatchFootersVector(builder, ArrayUtils.toPrimitive(this.matchFooters.toArray(new Integer[this.matchFooters.size()])));

        GameWrapper.startGameWrapper(builder);
        GameWrapper.addEvents(builder, events);
        GameWrapper.addMatchHeaders(builder, matchHeaders);
        GameWrapper.addMatchFooters(builder, matchFooters);
        this.finalGameWrapper = GameWrapper.endGameWrapper(builder);
        this.isGameFinished = true;
    }

    public void addMatchInfo(List<Integer> matchEvents){
        matchHeaders.add(events.size());
        events.addAll(matchEvents);
        matchFooters.add(events.size()-1);
    }

    // **********************************
    // ***** FILE I/0 METHODS ***********
    // **********************************

    public void writeGame(File saveFile){
        if(saveFile == null){
            return;
        }

        assert(isGameFinished);
        builder.finish(finalGameWrapper);
        ByteBuffer bb = builder.dataBuffer();
        byte[] byteData = bb.array();
        try {
            FileUtils.writeByteArrayToFile(saveFile, byteData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
