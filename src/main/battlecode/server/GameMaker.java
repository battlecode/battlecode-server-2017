package battlecode.server;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.schema.*;
import battlecode.util.FlatHelpers;
import battlecode.world.*;
import com.google.flatbuffers.FlatBufferBuilder;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

import static battlecode.util.FlatHelpers.*;

/**
 * Writes a game to a flatbuffer, hooray.
 */
public strictfp class GameMaker {

    /**
     * The protocol expects a series of valid state transitions;
     * we ensure that's true.
     */
    private enum State {
        /**
         * Waiting to write game header.
         */
        GAME_HEADER,
        /**
         * In a game, but not a match.
         */
        IN_GAME,
        /**
         * In a match.
         */
        IN_MATCH,
        /**
         * Complete.
         */
        DONE
    }
    private State state;

    // this un-separation-of-concerns makes me uncomfortable

    /**
     * We write the whole match to this builder, then write it to a file.
     */
    private final FlatBufferBuilder fileBuilder;

    /**
     * Null until the end of the match.
     */
    private byte[] finishedGame;

    /**
     * We have a separate byte[] for each packet sent to the client.
     * This is necessary because flatbuffers shares metadata between structures, so we
     * can't just cut out chunks of the larger buffer :/
     */
    private FlatBufferBuilder packetBuilder;

    /**
     * The server we're sending packets on.
     * May be null.
     */
    private final NetServer packetSink;

    /**
     * Eventually we might extend for more teams.
     */
    private TeamMapping teamMapping;

    /**
     * Only relevant to the file builder:
     * We add a table called a GameWrapper to the front of the saved files
     * that lets you quickly navigate to events by index, and tells you the
     * indices of headers and footers.
     */
    private TIntArrayList events;
    private TIntArrayList matchHeaders;
    private TIntArrayList matchFooters;

    /**
     * @param teamMapping the mapping of teams to bytes
     * @param packetSink the NetServer to send packets to
     */
    public GameMaker(final TeamMapping teamMapping, final NetServer packetSink){
        this.state = State.GAME_HEADER;

        this.teamMapping = teamMapping;

        this.packetSink = packetSink;
        if (packetSink != null) {
            this.packetBuilder = new FlatBufferBuilder();
        }

        this.fileBuilder = new FlatBufferBuilder();

        this.events = new TIntArrayList();
        this.matchHeaders = new TIntArrayList();
        this.matchFooters = new TIntArrayList();
    }

    /**
     * Assert we're in a particular state.
     *
     * @param state
     */
    private void assertState(State state) {
        if (this.state != state) {
            throw new RuntimeException("Incorrect GameMaker state: should be "+
                    state+", but is: "+this.state);
        }
    }

    /**
     * Make a state transition.
     */
    private void changeState(State start, State end) {
        assertState(start);
        this.state = end;
    }


    /**
     * Convert entire game to a byte array.
     *
     * @return game as a packed flatbuffer byte array.
     */
    public byte[] toBytes() {
        if (finishedGame == null) {
            assertState(State.DONE);

            int events = offsetVector(fileBuilder, this.events, GameWrapper::startEventsVector);
            int matchHeaders = offsetVector(fileBuilder, this.matchHeaders, GameWrapper::startMatchHeadersVector);
            int matchFooters = offsetVector(fileBuilder, this.matchFooters, GameWrapper::startMatchFootersVector);

            GameWrapper.startGameWrapper(fileBuilder);
            GameWrapper.addEvents(fileBuilder, events);
            GameWrapper.addMatchHeaders(fileBuilder, matchHeaders);
            GameWrapper.addMatchFooters(fileBuilder, matchFooters);

            fileBuilder.finish(GameWrapper.endGameWrapper(fileBuilder));
            finishedGame = fileBuilder.sizedByteArray();
        }
        return finishedGame;
    }

    /**
     * Write a match out to a file.
     *
     * @param saveFile the file to save to
     */
    public void writeGame(File saveFile) {
        if(saveFile == null) {
            throw new RuntimeException("Null file provided to writeGame");
        }

        try {
            FileUtils.writeByteArrayToFile(saveFile, toBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Run the same logic for both builders.
     *
     * @param perBuilder called with each builder; return event id. Should not mutate state.
     */
    private void createEvent(ToIntFunction<FlatBufferBuilder> perBuilder) {
        // make file event and add its offset to the list
        int eventAP = perBuilder.applyAsInt(fileBuilder);
        events.add(eventAP);

        if (packetSink != null) {
            // make packet event and package it up
            int eventBP = perBuilder.applyAsInt(packetBuilder);
            packetBuilder.finish(eventBP);
            packetSink.addEvent(packetBuilder.sizedByteArray());

            // reset packet builder
            packetBuilder = new FlatBufferBuilder(packetBuilder.dataBuffer());
        }
    }

    /**
     * Make a match maker for a particular match.
     *
     * ... you could actually reuse one, but eventually that might change?
     */
    public MatchMaker createMatchMaker() {
        return new MatchMaker();
    }

    public void makeGameHeader(){

        changeState(State.GAME_HEADER, State.IN_GAME);

        createEvent((builder) -> {
            int specVersionOffset = builder.createString(GameConstants.SPEC_VERSION);

            int name = builder.createString(teamMapping.getTeamAName());
            int packageName = builder.createString(teamMapping.getTeamAName());
            TeamData.startTeamData(builder);
            TeamData.addName(builder, name);
            TeamData.addPackageName(builder, packageName);
            TeamData.addTeamID(builder, teamMapping.getTeamAID());
            int teamAOffset = TeamData.endTeamData(builder);

            name = builder.createString(teamMapping.getTeamBName());
            packageName = builder.createString(teamMapping.getTeamBName());
            TeamData.startTeamData(builder);
            TeamData.addName(builder, name);
            TeamData.addPackageName(builder, packageName);
            TeamData.addTeamID(builder, teamMapping.getTeamBID());
            int teamBOffset = TeamData.endTeamData(builder);
            int[] teamsVec = {teamAOffset, teamBOffset};

            int teamsOffset = GameHeader.createTeamsVector(builder, teamsVec);
            int bodyTypeMetadataOffset = makeBodyTypeMetadata(builder);

            GameHeader.startGameHeader(builder);
            GameHeader.addSpecVersion(builder, specVersionOffset);
            GameHeader.addTeams(builder, teamsOffset);
            GameHeader.addBodyTypeMetadata(builder, bodyTypeMetadataOffset);
            int gameHeaderOffset = GameHeader.endGameHeader(builder);

            return EventWrapper.createEventWrapper(builder, Event.GameHeader, gameHeaderOffset);
        });
    }

    public int makeBodyTypeMetadata(FlatBufferBuilder builder){
        TIntArrayList bodyTypeMetadataOffsets = new TIntArrayList();

        // Add robot metadata
        for(RobotType type : RobotType.values()){
            BodyTypeMetadata.startBodyTypeMetadata(builder);
            BodyTypeMetadata.addType(builder, robotTypeToBodyType(type));
            BodyTypeMetadata.addRadius(builder, type.bodyRadius);
            BodyTypeMetadata.addCost(builder, type.bulletCost);
            BodyTypeMetadata.addMaxHealth(builder, type.maxHealth);
            BodyTypeMetadata.addStartHealth(builder, type.getStartingHealth());
            BodyTypeMetadata.addStrideRadius(builder, type.strideRadius);
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
        return offsetVector(builder, bodyTypeMetadataOffsets, GameHeader::startBodyTypeMetadataVector);
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

    public void makeGameFooter(Team winner){
        changeState(State.IN_GAME, State.DONE);

        createEvent((builder) -> EventWrapper.createEventWrapper(builder, Event.GameFooter,
                GameFooter.createGameFooter(builder, teamMapping.getIDFromTeam(winner))));
    }

    /**
     * Writes events from match to one or multiple flatbuffers.
     *
     * One of the rare cases where we want a non-static inner class in Java:
     * this basically just provides a restricted interface to GameMaker.
     */
    public class MatchMaker {
        private TIntArrayList movedIDs; // ints
        // VecTable for movedLocs in Round
        private TFloatArrayList movedLocsXs;
        private TFloatArrayList movedLocsYs;

        // SpawnedBodyTable for spawnedBodies
        private TIntArrayList spawnedBodiesRobotIDs;
        private TByteArrayList spawnedBodiesTeamIDs;
        private TByteArrayList spawnedBodiesTypes;
        private TFloatArrayList spawnedBodiesRadii;
        private TFloatArrayList spawnedBodiesLocsXs; //For locs
        private TFloatArrayList spawnedBodiesLocsYs; //For locs

        // SpawnedBulletTable for spawnedBullets
        private TIntArrayList spawnedBulletsRobotIDs;
        private TFloatArrayList spawnedBulletsDamages;
        private TFloatArrayList spawnedBulletsLocsXs; //For locs
        private TFloatArrayList spawnedBulletsLocsYs; //For locs
        private TFloatArrayList spawnedBulletsVelsXs; //For vels
        private TFloatArrayList spawnedBulletsVelsYs; //For vels

        private TIntArrayList healthChangedIDs; // ints
        private TFloatArrayList healthChangedLevels; // floats

        private TIntArrayList diedIDs; // ints
        private TIntArrayList diedBulletIDs; //ints

        private TIntArrayList actionIDs; // ints
        private TByteArrayList actions; // Actions
        private TIntArrayList actionTargets; // ints (IDs)

        public MatchMaker() {
            this.movedIDs = new TIntArrayList();
            this.movedLocsXs = new TFloatArrayList();
            this.movedLocsYs = new TFloatArrayList();
            this.spawnedBodiesRobotIDs = new TIntArrayList();
            this.spawnedBodiesTeamIDs = new TByteArrayList();
            this.spawnedBodiesTypes = new TByteArrayList();
            this.spawnedBodiesRadii = new TFloatArrayList();
            this.spawnedBodiesLocsXs = new TFloatArrayList();
            this.spawnedBodiesLocsYs = new TFloatArrayList();
            this.spawnedBulletsRobotIDs = new TIntArrayList();
            this.spawnedBulletsDamages = new TFloatArrayList();
            this.spawnedBulletsLocsXs = new TFloatArrayList();
            this.spawnedBulletsLocsYs = new TFloatArrayList();
            this.spawnedBulletsVelsXs = new TFloatArrayList();
            this.spawnedBulletsVelsYs = new TFloatArrayList();
            this.healthChangedIDs = new TIntArrayList();
            this.healthChangedLevels = new TFloatArrayList();
            this.diedIDs = new TIntArrayList();
            this.diedBulletIDs = new TIntArrayList();
            this.actionIDs = new TIntArrayList();
            this.actions = new TByteArrayList();
            this.actionTargets = new TIntArrayList();
        }

        public void makeMatchHeader(LiveMap gameMap) {
            changeState(State.IN_GAME, State.IN_MATCH);

            createEvent((builder) -> {
                int map = GameMapIO.Serial.serialize(builder, gameMap, teamMapping);

                return EventWrapper.createEventWrapper(builder, Event.MatchHeader,
                        MatchHeader.createMatchHeader(builder, map, gameMap.getRounds()));
            });

            matchHeaders.add(events.size() - 1);

            clearData();
        }

        public void makeMatchFooter(Team winTeam, int totalRounds) {
            changeState(State.IN_MATCH, State.IN_GAME);

            createEvent((builder) -> EventWrapper.createEventWrapper(builder, Event.MatchFooter,
                    MatchFooter.createMatchFooter(builder, teamMapping.getIDFromTeam(winTeam), totalRounds)));

            matchFooters.add(events.size() - 1);
        }

        public void makeRound(int roundNum) {
            assertState(State.IN_MATCH);

            createEvent((builder) -> {
                // The bodies that spawned
                int spawnedBodiesLocsP = createVecTable(builder, spawnedBodiesLocsXs, spawnedBodiesLocsYs);
                int spawnedBodiesRobotIDsP = intVector(builder, spawnedBodiesRobotIDs, SpawnedBodyTable::startRobotIDsVector);
                int spawnedBodiesTeamIDsP = byteVector(builder, spawnedBodiesTeamIDs, SpawnedBodyTable::startTeamIDsVector);
                int spawnedBodiesTypesP = byteVector(builder, spawnedBodiesTypes, SpawnedBodyTable::startTypesVector);
                SpawnedBodyTable.startSpawnedBodyTable(builder);
                SpawnedBodyTable.addLocs(builder, spawnedBodiesLocsP);
                SpawnedBodyTable.addRobotIDs(builder, spawnedBodiesRobotIDsP);
                SpawnedBodyTable.addTeamIDs(builder, spawnedBodiesTeamIDsP);
                SpawnedBodyTable.addTypes(builder, spawnedBodiesTypesP);
                int spawnedBodiesP = SpawnedBodyTable.endSpawnedBodyTable(builder);

                // The bullets that spawned
                int spawnedBulletsRobotIDsP = intVector(builder, spawnedBulletsRobotIDs, SpawnedBulletTable::startRobotIDsVector);
                int spawnedBulletsDamagesP = floatVector(builder, spawnedBulletsDamages, SpawnedBulletTable::startDamagesVector);
                int spawnedBulletsLocsP = createVecTable(builder, spawnedBulletsLocsXs, spawnedBulletsLocsYs);
                int spawnedBulletsVelsP = createVecTable(builder, spawnedBulletsVelsXs, spawnedBulletsVelsYs);
                SpawnedBulletTable.startSpawnedBulletTable(builder);
                SpawnedBulletTable.addRobotIDs(builder, spawnedBulletsRobotIDsP);
                SpawnedBulletTable.addDamages(builder, spawnedBulletsDamagesP);
                SpawnedBulletTable.addLocs(builder, spawnedBulletsLocsP);
                SpawnedBulletTable.addVels(builder, spawnedBulletsVelsP);
                int spawnedBulletsP = SpawnedBulletTable.endSpawnedBulletTable(builder);

                // The bodies that moved
                int movedIDsP = intVector(builder, movedIDs, Round::startMovedIDsVector);
                int movedLocsP = createVecTable(builder, movedLocsXs, movedLocsYs);

                // The bodies that changed health
                int healthChangedIDsP = intVector(builder, healthChangedIDs, Round::startHealthChangedIDsVector);
                int healthChangedLevelsP = floatVector(builder, healthChangedLevels, Round::startHealthChangeLevelsVector);

                // The bodies that died
                int diedIDsP = intVector(builder, diedIDs, Round::startDiedIDsVector);

                // The bullets that died
                int diedBulletIDsP = intVector(builder, diedBulletIDs, Round::startDiedBulletIDsVector);

                // The actions that happened
                int actionIDsP = intVector(builder, actionIDs, Round::startActionIDsVector);
                int actionsP = byteVector(builder, actions, Round::startActionsVector);
                int actionTargetsP = intVector(builder, actionTargets, Round::startActionTargetsVector);

                Round.startRound(builder);
                Round.addMovedIDs(builder, movedIDsP);
                Round.addMovedLocs(builder, movedLocsP);
                Round.addSpawnedBodies(builder, spawnedBodiesP);
                Round.addSpawnedBullets(builder, spawnedBulletsP);
                Round.addHealthChangedIDs(builder, healthChangedIDsP);
                Round.addHealthChangeLevels(builder, healthChangedLevelsP);
                Round.addDiedIDs(builder, diedIDsP);
                Round.addDiedBulletIDs(builder, diedBulletIDsP);
                Round.addActionIDs(builder, actionIDsP);
                Round.addActions(builder, actionsP);
                Round.addActionTargets(builder, actionTargetsP);
                Round.addRoundID(builder, roundNum);
                int round = Round.endRound(builder);

                return EventWrapper.createEventWrapper(builder, Event.Round, round);
            });

            clearData();
        }

        public void addMoved(int id, MapLocation newLocation) {
            movedIDs.add(id);
            movedLocsXs.add(newLocation.x);
            movedLocsYs.add(newLocation.y);
        }

        public void addHealthChanged(int id, float newHealthLevel) {
            healthChangedIDs.add(id);
            healthChangedLevels.add(newHealthLevel);
        }

        public void addDied(int id, boolean isBullet) {
            if (isBullet) {
                diedBulletIDs.add(id);
            } else {
                diedIDs.add(id);
            }
        }

        public void addAction(int userID, byte action, int targetID) {
            actionIDs.add(userID);
            actions.add(action);
            actionTargets.add(targetID);
        }

        public void addSpawnedRobot(InternalRobot robot) {
            spawnedBodiesRobotIDs.add(robot.getID());
            spawnedBodiesRadii.add(robot.getType().bodyRadius);
            spawnedBodiesLocsXs.add(robot.getLocation().x);
            spawnedBodiesLocsYs.add(robot.getLocation().y);
            spawnedBodiesTeamIDs.add(teamMapping.getIDFromTeam(robot.getTeam()));
            spawnedBodiesTypes.add(FlatHelpers.getBodyTypeFromRobotType(robot.getType()));
        }

        public void addSpawnedTree(InternalTree tree) {
            spawnedBodiesRobotIDs.add(tree.getID());
            spawnedBodiesRadii.add(tree.getRadius());
            spawnedBodiesLocsXs.add(tree.getLocation().x);
            spawnedBodiesLocsYs.add(tree.getLocation().y);
            spawnedBodiesTeamIDs.add(teamMapping.getIDFromTeam(tree.getTeam()));
            spawnedBodiesTypes.add(tree.getTeam() == Team.NEUTRAL ? BodyType.TREE_NEUTRAL : BodyType.TREE_BULLET);
        }

        public void addSpawnedBullet(InternalBullet bullet) {
            spawnedBulletsRobotIDs.add(bullet.getID());
            spawnedBulletsDamages.add(bullet.getDamage());
            spawnedBulletsLocsXs.add(bullet.getLocation().x);
            spawnedBulletsLocsYs.add(bullet.getLocation().y);
            spawnedBulletsVelsXs.add(bullet.getDirection().getDeltaX(bullet.getSpeed()));
            spawnedBulletsVelsYs.add(bullet.getDirection().getDeltaY(bullet.getSpeed()));
        }

        private void clearData() {
            movedIDs.clear();
            movedLocsXs.clear();
            movedLocsYs.clear();
            spawnedBodiesRobotIDs.clear();
            spawnedBodiesTeamIDs.clear();
            spawnedBodiesTypes.clear();
            spawnedBodiesRadii.clear();
            spawnedBodiesLocsXs.clear();
            spawnedBodiesLocsYs.clear();
            spawnedBulletsRobotIDs.clear();
            spawnedBulletsDamages.clear();
            spawnedBulletsLocsXs.clear();
            spawnedBulletsLocsYs.clear();
            spawnedBulletsVelsXs.clear();
            spawnedBulletsVelsYs.clear();
            healthChangedIDs.clear();
            healthChangedLevels.clear();
            diedIDs.clear();
            diedBulletIDs.clear();
            actionIDs.clear();
            actions.clear();
            actionTargets.clear();
        }
    }
}
