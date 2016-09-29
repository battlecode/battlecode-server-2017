package battlecode.server;

import battlecode.common.GameConstants;
import battlecode.common.RobotType;
import battlecode.schema.*;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sun.xml.internal.ws.client.sei.ResponseBuilder;
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

    // Called at beginning of run while loop
    public void makeGameHeader(String specVersion, TeamMapping teamMapping){
        int specVersionOffset = builder.createString(specVersion);

        TeamData.startTeamData(builder);
        TeamData.addName(builder, builder.createString(teamMapping.getTeamAName()));
        TeamData.addPackageName(builder, builder.createString(teamMapping.getTeamAName()));
        TeamData.addTeamID(builder, teamMapping.getTeamAID());
        int teamAOffset = TeamData.endTeamData(builder);

        TeamData.startTeamData(builder);
        TeamData.addName(builder, builder.createString(teamMapping.getTeamBName()));
        TeamData.addPackageName(builder, builder.createString(teamMapping.getTeamBName()));
        TeamData.addTeamID(builder, teamMapping.getTeamBID());
        int teamBOffset = TeamData.endTeamData(builder);
        int[] teamsVec = {teamAOffset, teamBOffset};

        int teamsOffset = GameHeader.createTeamsVector(builder, teamsVec);
        int bodyTypeMetadataOffset = makeBodyTypeMetadata();

        GameHeader.startGameHeader(builder);
        GameHeader.addSpecVersion(builder, specVersionOffset);
        GameHeader.addTeams(builder, teamsOffset);
        GameHeader.addBodyTypeMetadata(builder, bodyTypeMetadataOffset);
        int gameHeaderOffset = GameHeader.endGameHeader(builder);
        events.add(gameHeaderOffset);
    }

    public int makeBodyTypeMetadata(){
        List<Integer> bodyTypeMetadataOffsets = new ArrayList<>();

        // Add robot metadata
        for(RobotType type : RobotType.values()){
            BodyTypeMetadata.startBodyTypeMetadata(builder);
            BodyTypeMetadata.addType(builder, robotTypeToBodyType(type));
            BodyTypeMetadata.addRadius(builder, type.bodyRadius);
            BodyTypeMetadata.addCost(builder, type.bulletCost);
            BodyTypeMetadata.addMaxHealth(builder, type.maxHealth);
            BodyTypeMetadata.addStartHealth(builder, type.getStartingHealth());
            BodyTypeMetadata.addMoveDelay(builder, type.movementDelay);
            BodyTypeMetadata.addAttackDelay(builder, type.attackDelay);
            BodyTypeMetadata.addCooldownDelay(builder, type.cooldownDelay);
            BodyTypeMetadata.addBulletAttack(builder, type.attackPower);
            BodyTypeMetadata.addBulletSpeed(builder, type.bulletSpeed);
            bodyTypeMetadataOffsets.add(BodyTypeMetadata.endBodyTypeMetadata(builder));
        }

        // Add bullet tree metadata
        BodyTypeMetadata.startBodyTypeMetadata(builder);
        BodyTypeMetadata.addType(builder, BodyType.TREE_BULLET);
        BodyTypeMetadata.addRadius(builder, GameConstants.BULLET_TREE_RADIUS);
        BodyTypeMetadata.addCost(builder, GameConstants.BULLET_TREE_COST);
        BodyTypeMetadata.addMaxHealth(builder, GameConstants.BULLET_TREE_MAX_HEALTH);
        BodyTypeMetadata.addStartHealth(builder, GameConstants.BULLET_TREE_MAX_HEALTH * .20F);
        bodyTypeMetadataOffsets.add(BodyTypeMetadata.endBodyTypeMetadata(builder));

        // Make and return BodyTypeMetadata Vector offset
        return GameHeader.createBodyTypeMetadataVector(builder,
                ArrayUtils.toPrimitive(bodyTypeMetadataOffsets.toArray(new Integer[bodyTypeMetadataOffsets.size()])));
    }

    private byte robotTypeToBodyType(RobotType type){
        if (type == RobotType.ARCHON) return BodyType.ARCHON;
        if (type == RobotType.GARDENER) return BodyType.GARDENER;
        if (type == RobotType.SCOUT) return BodyType.SCOUT;
        if (type == RobotType.RECRUIT) return BodyType.RECRUIT;
        if (type == RobotType.SOLDIER) return BodyType.SOLDIER;
        if (type == RobotType.LUMBERJACK) return BodyType.LUMBERJACK;
        if (type == RobotType.TANK) return BodyType.TANK;
        return Byte.MIN_VALUE;
    }

    // **********************************
    // ***** GAME FOOTER METHODS ********
    // **********************************

    // Called at end of run while loop
    public void makeGameFooter(byte winner){
        events.add(GameFooter.createGameFooter(builder, winner));
    }

    // **********************************
    // ***** GAME WRAPPER METHODS *******
    // **********************************

    // Called at end of run while loop
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

    // Called at end of runMatch
    public void addMatchInfo(List<Integer> matchEvents){
        matchHeaders.add(events.size());
        events.addAll(matchEvents);
        matchFooters.add(events.size()-1);
    }

    // **********************************
    // ***** FILE I/0 METHODS ***********
    // **********************************

    // Called at end of run while loop
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
