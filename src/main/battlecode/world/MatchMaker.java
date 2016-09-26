package battlecode.world;

import battlecode.common.MapLocation;
import battlecode.common.Team;
import battlecode.schema.*;
import battlecode.schema.GameMap;
import com.google.flatbuffers.FlatBufferBuilder;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to make flatbuffer objects needed to serialize a Match.
 */
public class MatchMaker {

    private FlatBufferBuilder builder;

    private List<Integer> events; // EventWrappers

    private List<Integer> movedIDs; // ints
    // VecTable for movedLocs in Round
    private List<Float> movedLocsXs;
    private List<Float> movedLocsYs;

    // SpawnedBodyTable for spawned
    private List<Integer> spawnedRobotIDs;
    private List<Byte> spawnedTeamIDs;
    private List<Byte> spawnedTypes;
    private List<Float> spawnedRadii;
    private List<Float> spawnedLocsXs; //For locs
    private List<Float> spawnedLocsYs; //For locs

    private List<Integer> healthChangedIDs; // ints
    private List<Float> healthChangedLevels; // floats

    private List<Integer> diedIDs; // ints

    private List<Integer> actionIDs; // ints
    private List<Byte> actions; // Actions
    private List<Integer> actionTargets; // ints (IDs)

    public MatchMaker(FlatBufferBuilder builder){
        this.builder = builder;
        this.events = new ArrayList<>();
        this.movedIDs = new ArrayList<>();
        this.movedLocsXs = new ArrayList<>();
        this.movedLocsYs = new ArrayList<>();
        this.spawnedRobotIDs = new ArrayList<>();
        this.spawnedTeamIDs = new ArrayList<>();
        this.spawnedTypes = new ArrayList<>();
        this.spawnedRadii = new ArrayList<>();
        this.spawnedLocsXs = new ArrayList<>();
        this.spawnedLocsYs = new ArrayList<>();
        this.healthChangedIDs = new ArrayList<>();
        this.healthChangedLevels = new ArrayList<>();
        this.diedIDs = new ArrayList<>();
        this.actionIDs = new ArrayList<>();
        this.actions = new ArrayList<>();
        this.actionTargets = new ArrayList<>();
    }

    // *********************************
    // ****** EVENT CREATORS ***********
    // *********************************

    // Called at end of GameWorld constructor
    public void makeMatchHeader(battlecode.world.GameMap gameMap){
        int name = builder.createString(gameMap.getMapName());
        int minCorner = Vec.createVec(builder, gameMap.getOrigin().x, gameMap.getOrigin().y);
        int maxCorner = Vec.createVec(builder, gameMap.getOrigin().x + gameMap.getWidth(),
                gameMap.getOrigin().y + gameMap.getHeight());
        int bodies = GameMap.createBodiesVector(builder,
                ArrayUtils.toPrimitive(this.spawned.toArray(new Integer[this.spawned.size()])));
        int randomSeed = gameMap.getSeed();

        GameMap.startGameMap(builder);
        GameMap.addName(builder, name);
        GameMap.addMinCorner(builder, minCorner);
        GameMap.addMaxCorner(builder, maxCorner);
        GameMap.addBodies(builder, bodies);
        GameMap.addRandomSeed(builder, randomSeed);
        int map = GameMap.endGameMap(builder);

        events.add(EventWrapper.createEventWrapper(builder, Event.MatchHeader,
                MatchHeader.createMatchHeader(builder, map, gameMap.getRounds())));
    }

    // Called in GameWorld in runRound
    public void makeMatchFooter(Team winTeam, int totalRounds){
        byte winner;
        switch (winTeam){
            case A:
                winner = 0;
                break;
            case B:
                winner = 1;
                break;
            default:
                winner = 2;
        }
        events.add(EventWrapper.createEventWrapper(builder, Event.MatchFooter,
                MatchFooter.createMatchFooter(builder, winner, totalRounds)));
    }

    // Called by writeAndClearRoundData
    public void makeRound(int roundNum){
        int[] movedIDs = ArrayUtils.toPrimitive(this.movedIDs.toArray(new Integer[this.movedIDs.size()]));

        int movedLocs = VecTable.createVecTable(builder,
                VecTable.createXsVector(builder, ArrayUtils.toPrimitive(movedLocsXs.toArray(new Float[movedLocsXs.size()]))),
                VecTable.createYsVector(builder, ArrayUtils.toPrimitive(movedLocsYs.toArray(new Float[movedLocsYs.size()]))));

        SpawnedBodyTable.startSpawnedBodyTable(builder);
        int robotIDs = SpawnedBodyTable.createRobotIDsVector(builder, ArrayUtils.toPrimitive(spawnedRobotIDs.toArray(new Integer[spawnedRobotIDs.size()])));
        int teamIDs = SpawnedBodyTable.createTeamIDsVector(builder, ArrayUtils.toPrimitive(spawnedTeamIDs.toArray(new Byte[spawnedTeamIDs.size()])));
        int types = SpawnedBodyTable.createTypesVector(builder, ArrayUtils.toPrimitive(spawnedTypes.toArray(new Byte[spawnedTypes.size()])));
        int radii = SpawnedBodyTable.createRadiiVector(builder, ArrayUtils.toPrimitive(spawnedRadii.toArray(new Float[spawnedRadii.size()])));
        int locs = VecTable.createVecTable(builder,
                VecTable.createXsVector(builder, ArrayUtils.toPrimitive(spawnedLocsXs.toArray(new Float[spawnedLocsXs.size()]))),
                VecTable.createYsVector(builder, ArrayUtils.toPrimitive(spawnedLocsYs.toArray(new Float[spawnedLocsYs.size()]))));
        SpawnedBodyTable.addRobotIDs(builder, robotIDs);
        SpawnedBodyTable.addTeamIDs(builder, teamIDs);
        SpawnedBodyTable.addTypes(builder, types);
        SpawnedBodyTable.addRadii(builder, radii);
        SpawnedBodyTable.addLocs(builder, locs);
        int spawned = SpawnedBodyTable.endSpawnedBodyTable(builder);

        int[] healthChangedIDs = ArrayUtils.toPrimitive(this.healthChangedIDs.toArray(new Integer[this.healthChangedIDs.size()]));
        float[] healthChangedLevels = ArrayUtils.toPrimitive(this.healthChangedLevels.toArray(new Float[this.healthChangedLevels.size()]));
        int[] diedIDs = ArrayUtils.toPrimitive(this.diedIDs.toArray(new Integer[this.diedIDs.size()]));
        int[] actionIDs = ArrayUtils.toPrimitive(this.actionIDs.toArray(new Integer[this.actionIDs.size()]));
        byte[] actions = ArrayUtils.toPrimitive(this.actions.toArray(new Byte[this.actions.size()]));
        int[] actionTargets = ArrayUtils.toPrimitive(this.actionTargets.toArray(new Integer[this.actionTargets.size()]));

        // Make the Round
        Round.startRound(builder);
        Round.addMovedIDs(builder, Round.createMovedIDsVector(builder,movedIDs));
        Round.addMovedLocs(builder, movedLocs);
        Round.addSpawned(builder, spawned);
        Round.addHealthChangedIDs(builder, Round.createHealthChangedIDsVector(builder,healthChangedIDs));
        Round.addHealthChangeLevels(builder, Round.createHealthChangeLevelsVector(builder,healthChangedLevels));
        Round.addDiedIDs(builder, Round.createDiedIDsVector(builder,diedIDs));
        Round.addActionIDs(builder, Round.createActionIDsVector(builder,actionIDs));
        Round.addActions(builder, Round.createActionsVector(builder,actions));
        Round.addActionTargets(builder, Round.createActionTargetsVector(builder,actionTargets));
        int round = Round.endRound(builder);

        // Make and add the EventWrapper
        events.add(EventWrapper.createEventWrapper(builder, Event.Round, round));
    }

    // *********************************
    // ****** UPDATE ROUND INFO ********
    // *********************************

    // Called in GameWorld in runRound
    public void writeAndClearRoundData(int roundNum){
        makeRound(roundNum);
        movedIDs.clear();
        movedLocsXs.clear();
        movedLocsYs.clear();
        spawnedRobotIDs.clear();
        spawnedTeamIDs.clear();
        spawnedTypes.clear();
        spawnedRadii.clear();
        spawnedLocsXs.clear();
        spawnedLocsYs.clear();
        healthChangedIDs.clear();
        healthChangedLevels.clear();
        diedIDs.clear();
        actionIDs.clear();
        actions.clear();
        actionTargets.clear();
    }

    // *********************************
    // ****** UPDATE ATTRIBUTE INFO ****
    // *********************************

    // Called in RobotControllerImpl in move
    public void addMoved(int id, MapLocation newLocaction){
        movedIDs.add(id);
        movedLocsXs.add(newLocaction.x);
        movedLocsYs.add(newLocaction.y);
    }

    // Called in InternalRobot/InternalTree in processEndOfRound
    public void addHealthChanged(int id, float newHealthLevel){
        healthChangedIDs.add(id);
        healthChangedLevels.add(newHealthLevel);
    }

    // Called in GameWorld in destroy methods
    public void addDied(int id){
        diedIDs.add(id);
    }

    // Called in RobotControllerImpl except DIE_SUICIDE
    public void addAction(int userID, byte action, int targetID){
        actionIDs.add(userID);
        actions.add(action);
        actionTargets.add(targetID);
    }

    // Called in GameWorld in spawnRobot
    public void addSpawnedRobot(InternalRobot robot){
        spawnedRobotIDs.add(robot.getID());
        spawnedRadii.add(robot.getType().bodyRadius);
        spawnedLocsXs.add(robot.getLocation().x);
        spawnedLocsYs.add(robot.getLocation().y);
        byte teamID;
        switch (robot.getTeam()){
            case A:
                teamID = 0;
                break;
            case B:
                teamID = 1;
                break;
            default:
                teamID = 2;
        }
        spawnedTeamIDs.add(teamID);
        byte type;
        switch (robot.getType()){
            case ARCHON:
                type = BodyType.ARCHON;
                break;
            case GARDENER:
                type = BodyType.GARDENER;
                break;
            case LUMBERJACK:
                type = BodyType.LUMBERJACK;
                break;
            case RECRUIT:
                type = BodyType.RECRUIT;
                break;
            case SOLDIER:
                type = BodyType.SOLDIER;
                break;
            case TANK:
                type = BodyType.TANK;
                break;
            default:
                type = BodyType.SCOUT;
        }
        spawnedTeamIDs.add(type);
    }

    // Called in GameWorld in spawnTree
    public void addSpawnedTree(InternalTree tree){
        spawnedRobotIDs.add(tree.getID());
        spawnedRadii.add(tree.getRadius());
        spawnedLocsXs.add(tree.getLocation().x);
        spawnedLocsYs.add(tree.getLocation().y);
        byte type;
        byte teamID;
        switch (tree.getTeam()){
            case A:
                type = BodyType.TREE_BULLET;
                teamID = 0;
                break;
            case B:
                type = BodyType.TREE_BULLET;
                teamID = 1;
                break;
            default:
                type = BodyType.TREE_NEUTRAL;
                teamID = 2;
        }
        spawnedTeamIDs.add(teamID);
        spawnedTypes.add(type);
    }

    // Called in GameWorld in spawnBullet
    public void addSpawnedBullet(InternalBullet bullet){
        spawnedRobotIDs.add(bullet.getID());
        spawnedRadii.add(0f);
        spawnedLocsXs.add(bullet.getLocation().x);
        spawnedLocsYs.add(bullet.getLocation().y);
        spawnedTypes.add(BodyType.BULLET);
        byte teamID;
        switch (bullet.getTeam()){
            case A:
                teamID = 0;
                break;
            case B:
                teamID = 1;
                break;
            default:
                teamID = 2;
        }
        spawnedTeamIDs.add(teamID);
    }

}
