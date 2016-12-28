package battlecode.world;

import battlecode.common.*;
import battlecode.schema.*;
import battlecode.server.TeamMapping;
import battlecode.util.FlatHelpers;
import com.google.flatbuffers.FlatBufferBuilder;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to make flatbuffer objects needed to serialize a Match.
 */
public strictfp class MatchMaker {

    private FlatBufferBuilder builder;
    private TeamMapping teamMapping;

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

    public MatchMaker(FlatBufferBuilder builder, TeamMapping teamMapping) {
        this.builder = builder;
        this.teamMapping = teamMapping;
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

    // *********************************
    // ****** GETTERS ******************
    // *********************************

    public List<Integer> getEvents() {
        return events;
    }

    // *********************************
    // ****** EVENT CREATORS ***********
    // *********************************

    // Called at end of GameWorld constructor
    public void makeMatchHeader(LiveMap gameMap) {
        int map = GameMapIO.Serial.serialize(builder, gameMap, teamMapping);

        events.add(EventWrapper.createEventWrapper(builder, Event.MatchHeader,
                MatchHeader.createMatchHeader(builder, map, gameMap.getRounds())));
        clearData();
    }

    // Called in GameWorld in runRound
    public void makeMatchFooter(Team winTeam, int totalRounds) {
        events.add(EventWrapper.createEventWrapper(builder, Event.MatchFooter,
                MatchFooter.createMatchFooter(builder, teamMapping.getIDFromTeam(winTeam), totalRounds)));
    }

    // Called by writeAndClearRoundData
    public void makeRound(int roundNum) {
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

        // Make and add the EventWrapper
        events.add(EventWrapper.createEventWrapper(builder, Event.Round, round));
    }

    // *********************************
    // ****** UPDATE ROUND INFO ********
    // *********************************

    // Called in GameWorld in runRound
    public void writeAndClearRoundData(int roundNum) {
        makeRound(roundNum);
        clearData();
    }

    // *********************************
    // ****** UPDATE ATTRIBUTE INFO ****
    // *********************************

    // Called in RobotControllerImpl in move
    public void addMoved(int id, MapLocation newLocation) {
        movedIDs.add(id);
        movedLocsXs.add(newLocation.x);
        movedLocsYs.add(newLocation.y);
    }

    // Called in InternalRobot/InternalTree in processEndOfRound
    public void addHealthChanged(int id, float newHealthLevel) {
        healthChangedIDs.add(id);
        healthChangedLevels.add(newHealthLevel);
    }

    // Called in GameWorld in destroy methods
    public void addDied(int id, boolean isBullet) {
        if (isBullet) {
            diedBulletIDs.add(id);
        } else {
            diedIDs.add(id);
        }
    }

    // Called in RobotControllerImpl except DIE_SUICIDE
    public void addAction(int userID, byte action, int targetID) {
        actionIDs.add(userID);
        actions.add(action);
        actionTargets.add(targetID);
    }

    // Called in GameWorld in spawnRobot
    public void addSpawnedRobot(InternalRobot robot) {
        spawnedBodiesRobotIDs.add(robot.getID());
        spawnedBodiesRadii.add(robot.getType().bodyRadius);
        spawnedBodiesLocsXs.add(robot.getLocation().x);
        spawnedBodiesLocsYs.add(robot.getLocation().y);
        spawnedBodiesTeamIDs.add(teamMapping.getIDFromTeam(robot.getTeam()));
        spawnedBodiesTypes.add(FlatHelpers.getBodyTypeFromRobotType(robot.getType()));
    }

    // Called in GameWorld in spawnTree
    public void addSpawnedTree(InternalTree tree) {
        spawnedBodiesRobotIDs.add(tree.getID());
        spawnedBodiesRadii.add(tree.getRadius());
        spawnedBodiesLocsXs.add(tree.getLocation().x);
        spawnedBodiesLocsYs.add(tree.getLocation().y);
        spawnedBodiesTeamIDs.add(teamMapping.getIDFromTeam(tree.getTeam()));
        spawnedBodiesTypes.add(tree.getTeam() == Team.NEUTRAL ? BodyType.TREE_NEUTRAL : BodyType.TREE_BULLET);
    }

    // Called in GameWorld in spawnBullet
    public void addSpawnedBullet(InternalBullet bullet) {
        spawnedBulletsRobotIDs.add(bullet.getID());
        spawnedBulletsDamages.add(bullet.getDamage());
        spawnedBulletsLocsXs.add(bullet.getLocation().x);
        spawnedBulletsLocsYs.add(bullet.getLocation().y);
        spawnedBulletsVelsXs.add(bullet.getDirection().getDeltaX(bullet.getSpeed()));
        spawnedBulletsVelsYs.add(bullet.getDirection().getDeltaY(bullet.getSpeed()));
    }

    // *********************************
    // ****** PRIVATE INFO *************
    // *********************************

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
