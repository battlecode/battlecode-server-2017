package battlecode.server;

import com.google.flatbuffers.FlatBufferBuilder;

/**
 * Used to make flatbuffer objects needed to serialize a Game.
 */
public class GameMaker {

    private FlatBufferBuilder builder;

    public GameMaker(){
        this.builder = new FlatBufferBuilder();
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

    public void makeGameHeader(){
        return;
    }

    // **********************************
    // ***** GAME FOOTER METHODS ********
    // **********************************

    public void makeGameFooter(){
        return;
    }

    // **********************************
    // ***** GAME WRAPPER METHODS *******
    // **********************************

    public void makeGameWrapper(){

    }

    // **********************************
    // ***** FILE I/0 METHODS ***********
    // **********************************

    public void writeGame(){
        return;
    }

}
