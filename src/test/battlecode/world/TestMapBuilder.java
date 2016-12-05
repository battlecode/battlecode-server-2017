package battlecode.world;

import battlecode.schema.*;
import battlecode.schema.GameMap;
import com.google.flatbuffers.FlatBufferBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Ignore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

@Ignore
public class TestMapBuilder{

    private FlatBufferBuilder builder;
    private ByteBuffer bb = null;

    private int name;
    private float minCornerX;
    private float minCornerY;
    private float maxCornerX;
    private float maxCornerY;
    private int randomSeed;

    private ArrayList<Integer> bodyIDs;
    private ArrayList<Byte> bodyTeamIDs;
    private ArrayList<Byte> bodyTypes;
    private ArrayList<Float> bodyLocsXs;
    private ArrayList<Float> bodyLocsYs;

    private ArrayList<Integer> nIDs;
    private ArrayList<Float> nXLocs;
    private ArrayList<Float> nYLocs;
    private ArrayList<Float> nRadii;
    private ArrayList<Integer> nContainedBullets;
    private ArrayList<Byte> nContainedBodyTypes;

    public TestMapBuilder(String mapName, float minCornerX, float minCornerY,
                          float maxCornerX, float maxCornerY, int randomSeed){
        this.builder = new FlatBufferBuilder();

        this.name = builder.createString(mapName);
        this.minCornerX = minCornerX;
        this.minCornerY = minCornerY;
        this.maxCornerX = maxCornerX;
        this.maxCornerY = maxCornerY;
        this.randomSeed = randomSeed;

        this.bodyIDs = new ArrayList<>();
        this.bodyTeamIDs = new ArrayList<>();
        this.bodyTypes = new ArrayList<>();
        this.bodyLocsXs = new ArrayList<>();
        this.bodyLocsYs = new ArrayList<>();

        this.nIDs = new ArrayList<>();
        this.nXLocs = new ArrayList<>();
        this.nYLocs = new ArrayList<>();
        this.nRadii = new ArrayList<>();
        this.nContainedBullets = new ArrayList<>();
        this.nContainedBodyTypes = new ArrayList<>();
    }

    public void addBody(int id, byte teamID, byte bodyType, float xLoc, float yLoc){
        bodyIDs.add(id);
        bodyTeamIDs.add(teamID);
        bodyTypes.add(bodyType);
        bodyLocsXs.add(xLoc);
        bodyLocsYs.add(yLoc);
    }

    public void addNeutralTree(int id, float xLoc, float yLoc, float radius, int containedBullets, byte containedBody){
        nIDs.add(id);
        nXLocs.add(xLoc);
        nYLocs.add(yLoc);
        nRadii.add(radius);
        nContainedBullets.add(containedBullets);
        nContainedBodyTypes.add(containedBody);
    }

    public battlecode.schema.GameMap build(){
        int robotIDs = SpawnedBodyTable.createRobotIDsVector(builder, ArrayUtils.toPrimitive(bodyIDs.toArray(new Integer[bodyIDs.size()])));
        int teamIDs = SpawnedBodyTable.createTeamIDsVector(builder, ArrayUtils.toPrimitive(bodyTeamIDs.toArray(new Byte[bodyTeamIDs.size()])));
        int types = SpawnedBodyTable.createTypesVector(builder, ArrayUtils.toPrimitive(bodyTypes.toArray(new Byte[bodyTypes.size()])));
        int locs = VecTable.createVecTable(builder,
                VecTable.createXsVector(builder, ArrayUtils.toPrimitive(bodyLocsXs.toArray(new Float[bodyLocsXs.size()]))),
                VecTable.createYsVector(builder, ArrayUtils.toPrimitive(bodyLocsYs.toArray(new Float[bodyLocsYs.size()]))));

        SpawnedBodyTable.startSpawnedBodyTable(builder);
        SpawnedBodyTable.addRobotIDs(builder, robotIDs);
        SpawnedBodyTable.addTeamIDs(builder, teamIDs);
        SpawnedBodyTable.addTypes(builder, types);
        SpawnedBodyTable.addLocs(builder, locs);
        int bodies = SpawnedBodyTable.endSpawnedBodyTable(builder);

        robotIDs = NeutralTreeTable.createRobotIDsVector(builder, ArrayUtils.toPrimitive(nIDs.toArray(new Integer[nIDs.size()])));
        int radii = NeutralTreeTable.createRadiiVector(builder, ArrayUtils.toPrimitive(nRadii.toArray(new Float[nRadii.size()])));
        int containedBullets = NeutralTreeTable.createContainedBulletsVector(builder, ArrayUtils.toPrimitive(nContainedBullets.toArray(new Integer[nContainedBullets.size()])));
        int containedBodies = NeutralTreeTable.createContainedBodiesVector(builder, ArrayUtils.toPrimitive(nContainedBodyTypes.toArray(new Byte[nContainedBodyTypes.size()])));
        locs = VecTable.createVecTable(builder,
                VecTable.createXsVector(builder, ArrayUtils.toPrimitive(nXLocs.toArray(new Float[nXLocs.size()]))),
                VecTable.createYsVector(builder, ArrayUtils.toPrimitive(nYLocs.toArray(new Float[nYLocs.size()]))));

        NeutralTreeTable.startNeutralTreeTable(builder);
        NeutralTreeTable.addRobotIDs(builder, robotIDs);
        NeutralTreeTable.addLocs(builder, locs);
        NeutralTreeTable.addRadii(builder, radii);
        NeutralTreeTable.addContainedBullets(builder, containedBullets);
        NeutralTreeTable.addContainedBodies(builder, containedBodies);
        int trees = NeutralTreeTable.endNeutralTreeTable(builder);

        battlecode.schema.GameMap.startGameMap(builder);
        battlecode.schema.GameMap.addName(builder, name);
        battlecode.schema.GameMap.addMinCorner(builder, Vec.createVec(builder, minCornerX, minCornerY));
        battlecode.schema.GameMap.addMaxCorner(builder, Vec.createVec(builder, maxCornerX, maxCornerY));
        battlecode.schema.GameMap.addBodies(builder, bodies);
        battlecode.schema.GameMap.addTrees(builder, trees);
        battlecode.schema.GameMap.addRandomSeed(builder, randomSeed);
        int map = GameMap.endGameMap(builder);

        builder.finish(map);
        this.bb = builder.dataBuffer();
        return GameMap.getRootAsGameMap(this.bb);
    }

    public void saveMap(String path){
        try {
            File file = new File(path).getAbsoluteFile();
            boolean append = false;
            FileChannel wChannel = new FileOutputStream(file, append).getChannel();
            wChannel.write(bb);
            wChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
