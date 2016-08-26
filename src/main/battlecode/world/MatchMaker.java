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
    private List<Integer> movedLocs; // Vecs

    private List<Integer> spawned; // SpawnedBody

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
        this.movedLocs = new ArrayList<>();
        this.spawned = new ArrayList<>();
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
    public void makeRound(){
        int[] movedIDs = ArrayUtils.toPrimitive(this.movedIDs.toArray(new Integer[this.movedIDs.size()]));
        int[] movedLocs = ArrayUtils.toPrimitive(this.movedLocs.toArray(new Integer[this.movedLocs.size()]));
        int[] spawned = ArrayUtils.toPrimitive(this.spawned.toArray(new Integer[this.spawned.size()]));
        int[] healthChangedIDs = ArrayUtils.toPrimitive(this.healthChangedIDs.toArray(new Integer[this.healthChangedIDs.size()]));
        float[] healthChangedLevels = ArrayUtils.toPrimitive(this.healthChangedLevels.toArray(new Float[this.healthChangedLevels.size()]));
        int[] diedIDs = ArrayUtils.toPrimitive(this.diedIDs.toArray(new Integer[this.diedIDs.size()]));
        int[] actionIDs = ArrayUtils.toPrimitive(this.actionIDs.toArray(new Integer[this.actionIDs.size()]));
        byte[] actions = ArrayUtils.toPrimitive(this.actions.toArray(new Byte[this.actions.size()]));
        int[] actionTargets = ArrayUtils.toPrimitive(this.actionTargets.toArray(new Integer[this.actionTargets.size()]));

        // Make the Round
        Round.startRound(builder);
        Round.addMovedIDs(builder, Round.createMovedIDsVector(builder,movedIDs));
        Round.addMovedLocs(builder, Round.createMovedLocsVector(builder,movedLocs));
        Round.addSpawned(builder, Round.createSpawnedVector(builder,spawned));
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
    public void writeAndClearRoundData(){
        makeRound();
        movedIDs.clear();
        movedLocs.clear();
        spawned.clear();
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
        int vec = Vec.createVec(builder, newLocaction.x, newLocaction.y);
        movedIDs.add(id);
        movedLocs.add(vec);
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
        int robotID     = robot.getID();
        float radius    = robot.getType().bodyRadius;
        int loc         = Vec.createVec(builder, robot.getLocation().x, robot.getLocation().y);
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

        SpawnedBody.startSpawnedBody(builder);
        SpawnedBody.addRobotID(builder, robotID);
        SpawnedBody.addTeamID(builder, teamID);
        SpawnedBody.addType(builder, type);
        SpawnedBody.addRadius(builder, radius);
        SpawnedBody.addLoc(builder, loc);
        spawned.add(SpawnedBody.endSpawnedBody(builder));
    }

    // Called in GameWorld in spawnTree
    public void addSpawnedTree(InternalTree tree){
        int robotID     = tree.getID();
        float radius    = tree.getRadius();
        int loc         = Vec.createVec(builder, tree.getLocation().x, tree.getLocation().y);
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

        SpawnedBody.startSpawnedBody(builder);
        SpawnedBody.addRobotID(builder, robotID);
        SpawnedBody.addTeamID(builder, teamID);
        SpawnedBody.addType(builder, type);
        SpawnedBody.addRadius(builder, radius);
        SpawnedBody.addLoc(builder, loc);
        spawned.add(SpawnedBody.endSpawnedBody(builder));
    }

    // Called in GameWorld in spawnBullet
    public void addSpawnedBullet(InternalBullet bullet){
        int robotID     = bullet.getID();
        float radius    = 0;
        int loc         = Vec.createVec(builder, bullet.getLocation().x, bullet.getLocation().y);
        int vel         = Vec.createVec(builder,
                bullet.getDirection().getDeltaX(bullet.getSpeed()),
                bullet.getDirection().getDeltaY(bullet.getSpeed()));
        byte type       = BodyType.BULLET;
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

        SpawnedBody.startSpawnedBody(builder);
        SpawnedBody.addRobotID(builder, robotID);
        SpawnedBody.addTeamID(builder, teamID);
        SpawnedBody.addType(builder, type);
        SpawnedBody.addRadius(builder, radius);
        SpawnedBody.addLoc(builder, loc);
        SpawnedBody.addVel(builder, vel);
        spawned.add(SpawnedBody.endSpawnedBody(builder));
    }

}
