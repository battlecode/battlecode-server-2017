package battlecode.server;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.schema.*;
import battlecode.util.FlatHelpers;
import battlecode.world.*;
import com.google.flatbuffers.FlatBufferBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Used to make flatbuffer objects needed to serialize a Game.
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

    private FlatBufferBuilder builder;
    private TeamMapping teamMapping;

    private int finalGameWrapper;

    private List<Integer> events; // EventWrappers
    private List<Integer> matchHeaders;
    private List<Integer> matchFooters;

    /**
     * @param teamMapping
     */
    public GameMaker(final TeamMapping teamMapping){
        this.state = State.GAME_HEADER;
        this.builder = new FlatBufferBuilder();
        this.events = new ArrayList<>();
        this.matchHeaders = new ArrayList<>();
        this.matchFooters = new ArrayList<>();
        this.teamMapping = teamMapping;
    }

    /**
     * Write a match out to a file.
     *
     * @param saveFile
     */
    public void writeGame(File saveFile) {
        if(saveFile == null) {
            return;
        }

        builder.finish(finalGameWrapper);
        byte[] byteData = builder.sizedByteArray();
        try {
            FileUtils.writeByteArrayToFile(saveFile, byteData);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
     * Make a match maker for a particular match.
     */
    public MatchMaker createMatchMaker() {
        return new MatchMaker();
    }

    public void makeGameHeader(String specVersion, TeamMapping teamMapping){

        changeState(State.GAME_HEADER, State.IN_GAME);

        int specVersionOffset = builder.createString(specVersion);

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
        int bodyTypeMetadataOffset = makeBodyTypeMetadata();

        GameHeader.startGameHeader(builder);
        GameHeader.addSpecVersion(builder, specVersionOffset);
        GameHeader.addTeams(builder, teamsOffset);
        GameHeader.addBodyTypeMetadata(builder, bodyTypeMetadataOffset);
        int gameHeaderOffset = GameHeader.endGameHeader(builder);
        events.add(EventWrapper.createEventWrapper(builder, Event.GameHeader, gameHeaderOffset));
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

    public void makeGameFooter(byte winner){

        changeState(State.IN_GAME, State.DONE);

        events.add(EventWrapper.createEventWrapper(builder, Event.GameFooter, GameFooter.createGameFooter(builder, winner)));
    }

    public void makeGameWrapper(){

        assertState(State.DONE);

        int events = GameWrapper.createEventsVector(builder, ArrayUtils.toPrimitive(this.events.toArray(new Integer[this.events.size()])));
        int matchHeaders = GameWrapper.createMatchHeadersVector(builder, ArrayUtils.toPrimitive(this.matchHeaders.toArray(new Integer[this.matchHeaders.size()])));
        int matchFooters = GameWrapper.createMatchFootersVector(builder, ArrayUtils.toPrimitive(this.matchFooters.toArray(new Integer[this.matchFooters.size()])));

        GameWrapper.startGameWrapper(builder);
        GameWrapper.addEvents(builder, events);
        GameWrapper.addMatchHeaders(builder, matchHeaders);
        GameWrapper.addMatchFooters(builder, matchFooters);
        this.finalGameWrapper = GameWrapper.endGameWrapper(builder);
    }

    public void addMatchInfo(List<Integer> matchEvents) {
        matchHeaders.add(events.size());
        events.addAll(matchEvents);
        matchFooters.add(events.size()-1);
    }

    /**
     * Writes events from match to one or multiple flatbuffers.
     *
     * One of the rare cases where we want a non-static inner class in Java:
     * this basically just provides a restricted interface to GameMaker.
     */
    public class MatchMaker {
        private List<Integer> events; // EventWrappers

        private List<Integer> movedIDs; // ints
        // VecTable for movedLocs in Round
        private List<Float> movedLocsXs;
        private List<Float> movedLocsYs;

        // SpawnedBodyTable for spawnedBodies
        private List<Integer> spawnedBodiesRobotIDs;
        private List<Byte> spawnedBodiesTeamIDs;
        private List<Byte> spawnedBodiesTypes;
        private List<Float> spawnedBodiesRadii;
        private List<Float> spawnedBodiesLocsXs; //For locs
        private List<Float> spawnedBodiesLocsYs; //For locs

        // SpawnedBulletTable for spawnedBullets
        private List<Integer> spawnedBulletsRobotIDs;
        private List<Float> spawnedBulletsDamages;
        private List<Float> spawnedBulletsLocsXs; //For locs
        private List<Float> spawnedBulletsLocsYs; //For locs
        private List<Float> spawnedBulletsVelsXs; //For vels
        private List<Float> spawnedBulletsVelsYs; //For vels

        private List<Integer> healthChangedIDs; // ints
        private List<Float> healthChangedLevels; // floats

        private List<Integer> diedIDs; // ints
        private List<Integer> diedBulletIDs; //ints

        private List<Integer> actionIDs; // ints
        private List<Byte> actions; // Actions
        private List<Integer> actionTargets; // ints (IDs)

        public MatchMaker() {
            this.events = new ArrayList<>();
            this.movedIDs = new ArrayList<>();
            this.movedLocsXs = new ArrayList<>();
            this.movedLocsYs = new ArrayList<>();
            this.spawnedBodiesRobotIDs = new ArrayList<>();
            this.spawnedBodiesTeamIDs = new ArrayList<>();
            this.spawnedBodiesTypes = new ArrayList<>();
            this.spawnedBodiesRadii = new ArrayList<>();
            this.spawnedBodiesLocsXs = new ArrayList<>();
            this.spawnedBodiesLocsYs = new ArrayList<>();
            this.spawnedBulletsRobotIDs = new ArrayList<>();
            this.spawnedBulletsDamages = new ArrayList<>();
            this.spawnedBulletsLocsXs = new ArrayList<>();
            this.spawnedBulletsLocsYs = new ArrayList<>();
            this.spawnedBulletsVelsXs = new ArrayList<>();
            this.spawnedBulletsVelsYs = new ArrayList<>();
            this.healthChangedIDs = new ArrayList<>();
            this.healthChangedLevels = new ArrayList<>();
            this.diedIDs = new ArrayList<>();
            this.diedBulletIDs = new ArrayList<>();
            this.actionIDs = new ArrayList<>();
            this.actions = new ArrayList<>();
            this.actionTargets = new ArrayList<>();
        }

        public void makeMatchHeader(LiveMap gameMap) {
            changeState(State.IN_GAME, State.IN_MATCH);

            int map = GameMapIO.Serial.serialize(builder, gameMap, teamMapping);

            events.add(EventWrapper.createEventWrapper(builder, Event.MatchHeader,
                    MatchHeader.createMatchHeader(builder, map, gameMap.getRounds())));
            clearData();
        }

        public void makeMatchFooter(Team winTeam, int totalRounds) {
            changeState(State.IN_MATCH, State.IN_GAME);

            events.add(EventWrapper.createEventWrapper(builder, Event.MatchFooter,
                    MatchFooter.createMatchFooter(builder, teamMapping.getIDFromTeam(winTeam), totalRounds)));

            addMatchInfo(events);

            events.clear();
        }

        public void makeRound(int roundNum) {
            assertState(State.IN_MATCH);

            int[] movedIDs = ArrayUtils.toPrimitive(this.movedIDs.toArray(new Integer[this.movedIDs.size()]));
            int movedLocs = VecTable.createVecTable(builder,
                    VecTable.createXsVector(builder, ArrayUtils.toPrimitive(movedLocsXs.toArray(new Float[movedLocsXs.size()]))),
                    VecTable.createYsVector(builder, ArrayUtils.toPrimitive(movedLocsYs.toArray(new Float[movedLocsYs.size()]))));

            int robotIDs = SpawnedBodyTable.createRobotIDsVector(builder, ArrayUtils.toPrimitive(spawnedBodiesRobotIDs.toArray(new Integer[spawnedBodiesRobotIDs.size()])));
            int teamIDs = SpawnedBodyTable.createTeamIDsVector(builder, ArrayUtils.toPrimitive(spawnedBodiesTeamIDs.toArray(new Byte[spawnedBodiesTeamIDs.size()])));
            int types = SpawnedBodyTable.createTypesVector(builder, ArrayUtils.toPrimitive(spawnedBodiesTypes.toArray(new Byte[spawnedBodiesTypes.size()])));
            int locs = VecTable.createVecTable(builder,
                    VecTable.createXsVector(builder, ArrayUtils.toPrimitive(spawnedBodiesLocsXs.toArray(new Float[spawnedBodiesLocsXs.size()]))),
                    VecTable.createYsVector(builder, ArrayUtils.toPrimitive(spawnedBodiesLocsYs.toArray(new Float[spawnedBodiesLocsYs.size()]))));
            SpawnedBodyTable.startSpawnedBodyTable(builder);
            SpawnedBodyTable.addRobotIDs(builder, robotIDs);
            SpawnedBodyTable.addTeamIDs(builder, teamIDs);
            SpawnedBodyTable.addTypes(builder, types);
            SpawnedBodyTable.addLocs(builder, locs);
            int spawnedBodies = SpawnedBodyTable.endSpawnedBodyTable(builder);

            robotIDs = SpawnedBulletTable.createRobotIDsVector(builder, ArrayUtils.toPrimitive(spawnedBulletsRobotIDs.toArray(new Integer[spawnedBulletsRobotIDs.size()])));
            int damages = SpawnedBulletTable.createDamagesVector(builder, ArrayUtils.toPrimitive(spawnedBulletsDamages.toArray(new Float[spawnedBulletsDamages.size()])));
            locs = VecTable.createVecTable(builder,
                    VecTable.createXsVector(builder, ArrayUtils.toPrimitive(spawnedBulletsLocsXs.toArray(new Float[spawnedBulletsLocsXs.size()]))),
                    VecTable.createYsVector(builder, ArrayUtils.toPrimitive(spawnedBulletsLocsYs.toArray(new Float[spawnedBulletsLocsYs.size()]))));
            int vels = VecTable.createVecTable(builder,
                    VecTable.createXsVector(builder, ArrayUtils.toPrimitive(spawnedBulletsVelsXs.toArray(new Float[spawnedBulletsVelsXs.size()]))),
                    VecTable.createYsVector(builder, ArrayUtils.toPrimitive(spawnedBulletsVelsYs.toArray(new Float[spawnedBulletsVelsYs.size()]))));
            SpawnedBulletTable.startSpawnedBulletTable(builder);
            SpawnedBulletTable.addRobotIDs(builder, robotIDs);
            SpawnedBulletTable.addDamages(builder, damages);
            SpawnedBulletTable.addLocs(builder, locs);
            SpawnedBulletTable.addVels(builder, vels);
            int spawnedBullets = SpawnedBulletTable.endSpawnedBulletTable(builder);

            int[] healthChangedIDs = ArrayUtils.toPrimitive(this.healthChangedIDs.toArray(new Integer[this.healthChangedIDs.size()]));
            float[] healthChangedLevels = ArrayUtils.toPrimitive(this.healthChangedLevels.toArray(new Float[this.healthChangedLevels.size()]));
            int[] diedIDs = ArrayUtils.toPrimitive(this.diedIDs.toArray(new Integer[this.diedIDs.size()]));
            int[] diedBulletIDs = ArrayUtils.toPrimitive(this.diedBulletIDs.toArray(new Integer[this.diedBulletIDs.size()]));
            int[] actionIDs = ArrayUtils.toPrimitive(this.actionIDs.toArray(new Integer[this.actionIDs.size()]));
            byte[] actions = ArrayUtils.toPrimitive(this.actions.toArray(new Byte[this.actions.size()]));
            int[] actionTargets = ArrayUtils.toPrimitive(this.actionTargets.toArray(new Integer[this.actionTargets.size()]));

            // Make the Round
            int a = Round.createMovedIDsVector(builder, movedIDs);
            int b = Round.createHealthChangedIDsVector(builder, healthChangedIDs);
            int c = Round.createHealthChangeLevelsVector(builder, healthChangedLevels);
            int d = Round.createDiedIDsVector(builder, diedIDs);
            int e = Round.createDiedBulletIDsVector(builder, diedBulletIDs);
            int f = Round.createActionIDsVector(builder, actionIDs);
            int g = Round.createActionsVector(builder, actions);
            int h = Round.createActionTargetsVector(builder, actionTargets);
            Round.startRound(builder);
            Round.addMovedIDs(builder, a);
            Round.addMovedLocs(builder, movedLocs);
            Round.addSpawnedBodies(builder, spawnedBodies);
            Round.addSpawnedBullets(builder, spawnedBullets);
            Round.addHealthChangedIDs(builder, b);
            Round.addHealthChangeLevels(builder, c);
            Round.addDiedIDs(builder, d);
            Round.addDiedBulletIDs(builder, e);
            Round.addActionIDs(builder, f);
            Round.addActions(builder, g);
            Round.addActionTargets(builder, h);
            Round.addRoundID(builder, roundNum);
            int round = Round.endRound(builder);

            clearData();

            // Make and add the EventWrapper
            events.add(EventWrapper.createEventWrapper(builder, Event.Round, round));
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
