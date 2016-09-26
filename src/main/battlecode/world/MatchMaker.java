package battlecode.world;

import battlecode.common.*;
import battlecode.schema.*;
import battlecode.schema.GameMap;
import battlecode.server.TeamMapping;
import com.google.flatbuffers.FlatBufferBuilder;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to make flatbuffer objects needed to serialize a Match.
 */
public class MatchMaker {

    private FlatBufferBuilder builder;
    private TeamMapping teamMapping;

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

    public MatchMaker(FlatBufferBuilder builder, TeamMapping teamMapping){
        this.builder = builder;
        this.teamMapping = teamMapping;
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
        int randomSeed = gameMap.getSeed();

        // Make body tables
        ArrayList<Integer> bodyIDs = new ArrayList<>();
        ArrayList<Byte> bodyTeamIDs = new ArrayList<>();
        ArrayList<Byte> bodyTypes = new ArrayList<>();
        ArrayList<Float> bodyLocsXs = new ArrayList<>();
        ArrayList<Float> bodyLocsYs = new ArrayList<>();

        ArrayList<Integer> treeIDs = new ArrayList<>();
        ArrayList<Float> treeRadii = new ArrayList<>();
        ArrayList<Integer> treeContainedBullets = new ArrayList<>();
        ArrayList<Byte> treeContainedBodies = new ArrayList<>();
        ArrayList<Float> treeLocsXs = new ArrayList<>();
        ArrayList<Float> treeLocsYs = new ArrayList<>();
        for(BodyInfo initBody : gameMap.getInitialBodies()){
            if(initBody.isRobot()){
                RobotInfo robot = (RobotInfo) initBody;
                bodyIDs.add(robot.ID);
                bodyTeamIDs.add(teamMapping.getIDFromTeam(robot.team));
                bodyTypes.add(getByteFromType(robot.type));
                bodyLocsXs.add(robot.location.x);
                bodyLocsXs.add(robot.location.y);
            }else{
                TreeInfo tree = (TreeInfo) initBody;
                if(tree.team == Team.NEUTRAL){
                    treeIDs.add(tree.ID);
                    treeRadii.add(tree.radius);
                    treeContainedBullets.add(tree.containedBullets);
                    treeContainedBodies.add(getByteFromType(tree.containedRobot));
                    treeLocsXs.add(tree.location.x);
                    treeLocsXs.add(tree.location.y);
                }else{
                    bodyIDs.add(tree.ID);
                    bodyTeamIDs.add(teamMapping.getIDFromTeam(tree.team));
                    bodyTypes.add(BodyType.TREE_BULLET);
                    bodyLocsXs.add(tree.location.x);
                    bodyLocsXs.add(tree.location.y);
                }
            }
        }

        SpawnedBodyTable.startSpawnedBodyTable(builder);
        int robotIDs = SpawnedBodyTable.createRobotIDsVector(builder, ArrayUtils.toPrimitive(bodyIDs.toArray(new Integer[bodyIDs.size()])));
        int teamIDs = SpawnedBodyTable.createTeamIDsVector(builder, ArrayUtils.toPrimitive(bodyTeamIDs.toArray(new Byte[bodyTeamIDs.size()])));
        int types = SpawnedBodyTable.createTypesVector(builder, ArrayUtils.toPrimitive(bodyTypes.toArray(new Byte[bodyTypes.size()])));
        int locs = VecTable.createVecTable(builder,
                VecTable.createXsVector(builder, ArrayUtils.toPrimitive(bodyLocsXs.toArray(new Float[bodyLocsXs.size()]))),
                VecTable.createYsVector(builder, ArrayUtils.toPrimitive(bodyLocsYs.toArray(new Float[bodyLocsYs.size()]))));
        SpawnedBodyTable.addRobotIDs(builder, robotIDs);
        SpawnedBodyTable.addTeamIDs(builder, teamIDs);
        SpawnedBodyTable.addTypes(builder, types);
        SpawnedBodyTable.addLocs(builder, locs);
        int bodies = SpawnedBodyTable.endSpawnedBodyTable(builder);


        NeutralTreeTable.startNeutralTreeTable(builder);
        robotIDs = NeutralTreeTable.createRobotIDsVector(builder, ArrayUtils.toPrimitive(treeIDs.toArray(new Integer[treeIDs.size()])));
        int radii = NeutralTreeTable.createRadiiVector(builder, ArrayUtils.toPrimitive(treeRadii.toArray(new Float[treeRadii.size()])));
        int containedBullets = NeutralTreeTable.createContainedBulletsVector(builder, ArrayUtils.toPrimitive(treeContainedBullets.toArray(new Integer[treeContainedBullets.size()])));
        int containedBodies = NeutralTreeTable.createContainedBodiesVector(builder, ArrayUtils.toPrimitive(treeContainedBodies.toArray(new Byte[treeContainedBodies.size()])));
        locs = VecTable.createVecTable(builder,
                VecTable.createXsVector(builder, ArrayUtils.toPrimitive(treeLocsXs.toArray(new Float[treeLocsXs.size()]))),
                VecTable.createYsVector(builder, ArrayUtils.toPrimitive(treeLocsYs.toArray(new Float[treeLocsYs.size()]))));
        NeutralTreeTable.addRobotIDs(builder, robotIDs);
        NeutralTreeTable.addLocs(builder, locs);
        NeutralTreeTable.addRadii(builder, radii);
        NeutralTreeTable.addContainedBullets(builder, containedBullets);
        NeutralTreeTable.addContainedBodies(builder, containedBodies);
        int trees = NeutralTreeTable.endNeutralTreeTable(builder);


        // Build GameMap for flatbuffer
        GameMap.startGameMap(builder);
        GameMap.addName(builder, name);
        GameMap.addMinCorner(builder, minCorner);
        GameMap.addMaxCorner(builder, maxCorner);
        GameMap.addBodies(builder, bodies);
        GameMap.addTrees(builder, trees);
        GameMap.addRandomSeed(builder, randomSeed);
        int map = GameMap.endGameMap(builder);

        events.add(EventWrapper.createEventWrapper(builder, Event.MatchHeader,
                MatchHeader.createMatchHeader(builder, map, gameMap.getRounds())));
        clearData();
    }

    // Called in GameWorld in runRound
    public void makeMatchFooter(Team winTeam, int totalRounds){
        events.add(EventWrapper.createEventWrapper(builder, Event.MatchFooter,
                MatchFooter.createMatchFooter(builder, teamMapping.getIDFromTeam(winTeam), totalRounds)));
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
        clearData();
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
        spawnedTeamIDs.add(teamMapping.getIDFromTeam(robot.getTeam()));
        spawnedTeamIDs.add(getByteFromType(robot.getType()));
    }

    // Called in GameWorld in spawnTree
    public void addSpawnedTree(InternalTree tree){
        spawnedRobotIDs.add(tree.getID());
        spawnedRadii.add(tree.getRadius());
        spawnedLocsXs.add(tree.getLocation().x);
        spawnedLocsYs.add(tree.getLocation().y);
        spawnedTeamIDs.add(teamMapping.getIDFromTeam(tree.getTeam()));
        spawnedTypes.add(tree.getTeam() == Team.NEUTRAL ? BodyType.TREE_NEUTRAL : BodyType.TREE_BULLET);
    }

    // Called in GameWorld in spawnBullet
    public void addSpawnedBullet(InternalBullet bullet){
        spawnedRobotIDs.add(bullet.getID());
        spawnedRadii.add(0f);
        spawnedLocsXs.add(bullet.getLocation().x);
        spawnedLocsYs.add(bullet.getLocation().y);
        spawnedTypes.add(BodyType.BULLET);
        spawnedTeamIDs.add(teamMapping.getIDFromTeam(bullet.getTeam()));
    }

    // *********************************
    // ****** PRIVATE INFO *************
    // *********************************

    private void clearData(){
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
    
    private byte getByteFromType(RobotType type){
        if(type == null){
            return -1;
        }
        switch (type){
            case ARCHON:
                return BodyType.ARCHON;
            case GARDENER:
                return BodyType.GARDENER;
            case LUMBERJACK:
                return BodyType.LUMBERJACK;
            case RECRUIT:
                return BodyType.RECRUIT;
            case SOLDIER:
                return BodyType.SOLDIER;
            case TANK:
                return BodyType.TANK;
            default:
                return BodyType.SCOUT;
        }
    }

}
