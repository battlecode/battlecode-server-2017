package battlecode.world;

import battlecode.common.*;
import battlecode.schema.*;
import battlecode.server.Server;
import battlecode.util.FlatHelpers;
import battlecode.util.TeamMapping;
import com.google.flatbuffers.FlatBufferBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class contains the code for reading a flatbuffer map file and converting it
 * to a proper LiveMap.
 */
public final strictfp class GameMapIO {
    /**
     * The loader we use if we can't find a map in the correct path.
     */
    private static final ClassLoader BACKUP_LOADER = GameMapIO.class.getClassLoader();

    /**
     * The file extension for battlecode 2017 match files.
     */
    public static final String MAP_EXTENSION = ".map17";

    /**
     * The package we check for maps in if they can't be found in the file system.
     */
    public static final String DEFAULT_MAP_PACKAGE = "battlecode/world/resources/";

    /**
     * Returns a LiveMap for a specific map.
     * If the map can't be found in the given directory, the package
     * "battlecode.world.resources" is checked as a backup.
     *
     * @param mapName name of map.
     * @param mapDir directory to load the extra map from; may be null.
     * @return LiveMap for map
     * @throws IOException if the map fails to load or can't be found.
     */
    public static LiveMap loadMap(String mapName, File mapDir)
            throws IOException {

        final LiveMap result;

        final File mapFile = new File(mapDir, mapName + MAP_EXTENSION);
        if (mapFile.exists()) {
            result = loadMap(new FileInputStream(mapFile));
        } else {
            final InputStream backupStream = BACKUP_LOADER.getResourceAsStream(DEFAULT_MAP_PACKAGE + mapName + MAP_EXTENSION);
            if (backupStream == null) {
                throw new IOException("Can't load map: " + mapName + " from dir " + mapDir + " or default maps.");
            }
            result = loadMap(backupStream);
        }

        if (!result.getMapName().equals(mapName)) {
            throw new IOException("Invalid map: name (" + result.getMapName()
                    + ") does not match filename (" + mapName + MAP_EXTENSION + ")"
            );
        }

        return result;
    }

    public static LiveMap loadMapAsResource(final ClassLoader loader,
                                            final String mapPackage,
                                            final String map) throws IOException {
        final InputStream mapStream = loader.getResourceAsStream(
                mapPackage + (mapPackage.endsWith("/")? "" : "/") +
                map + MAP_EXTENSION
        );

        if (mapStream == null) {
            throw new IOException("Can't load map: " + map + " from package " + mapPackage);
        }

        final LiveMap result = loadMap(mapStream);

        if (!result.getMapName().equals(map)) {
            throw new IOException("Invalid map: name (" + result.getMapName()
                    + ") does not match filename (" + map + MAP_EXTENSION + ")"
            );
        }

        return result;
    }

    /**
     * Load a map from an input stream.
     *
     * @param stream the stream to read from; will be closed after the map is read.
     * @return a map read from the stream
     * @throws IOException if the read fails somehow
     */
    public static LiveMap loadMap(InputStream stream)
            throws IOException {

        return Serial.deserialize(IOUtils.toByteArray(stream));

    }

    /**
     * Write a map to a file.
     *
     * @param mapDir the directory to store the map in
     * @param map the map to write
     * @throws IOException if the write fails somehow
     */
    public static void writeMap(LiveMap map, File mapDir) throws IOException {
        final File target = new File(mapDir, map.getMapName() + MAP_EXTENSION);

        IOUtils.write(Serial.serialize(map), new FileOutputStream(target));
    }

    /**
     * @param mapDir the directory to check for extra maps. May be null.
     * @return a set of available map names, including those built-in to battlecode-server.
     */
    public static List<String> getAvailableMaps(File mapDir) {
        final List<String> result = new ArrayList<>();

        // Load maps from the extra directory
        if (mapDir != null) {
            if (mapDir.isDirectory()) {
                // Files in directory
                for (File file : mapDir.listFiles()) {
                    String name = file.getName();
                    if (name.endsWith(MAP_EXTENSION)) {
                        result.add(name.substring(0, name.length() - MAP_EXTENSION.length()));
                    }
                }
            }
        }

        // Load built-in maps
        URL serverURL = GameMapIO.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            if (GameMapIO.class.getResource("GameMapIO.class").getProtocol().equals("jar")) {
                // We're running from a jar file.
                final ZipInputStream serverJar = new ZipInputStream(serverURL.openStream());

                ZipEntry ze;
                while ((ze = serverJar.getNextEntry()) != null) {
                    final String name = ze.getName();
                    if (name.startsWith(DEFAULT_MAP_PACKAGE) && name.endsWith(MAP_EXTENSION)) {
                        result.add(
                                name.substring(DEFAULT_MAP_PACKAGE.length(), name.length() - MAP_EXTENSION.length())
                        );
                    }
                }
            } else {
                // We're running from class files.
                final String[] resourceFiles = new File(BACKUP_LOADER.getResource(DEFAULT_MAP_PACKAGE).toURI()).list();

                for (String file : resourceFiles) {
                    if (file.endsWith(MAP_EXTENSION)) {
                        result.add(file.substring(0, file.length() - MAP_EXTENSION.length()));
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            System.err.println("Can't load default maps: " + e.getMessage());
            e.printStackTrace();
        }

        Collections.sort(result);

        return result;
    }



    /**
     * Prevent instantiation.
     */
    private GameMapIO() {}

    /**
     * Conversion from / to flatbuffers.
     */
    public static class Serial {
        /**
         * Load a flatbuffer map into a LiveMap.
         *
         * @param mapBytes the raw bytes of the map
         * @return a new copy of the map as a LiveMap
         */
        public static LiveMap deserialize(byte[] mapBytes) {
            battlecode.schema.GameMap rawMap = battlecode.schema.GameMap.getRootAsGameMap(
                    ByteBuffer.wrap(mapBytes)
            );

            return Serial.deserialize(rawMap);
        }

        /**
         * Write a map to a byte[].
         *
         * @param gameMap the map to write
         * @return the map as a byte[]
         */
        public static byte[] serialize(LiveMap gameMap) {
            FlatBufferBuilder builder = new FlatBufferBuilder();

            int mapRef = Serial.serialize(builder, gameMap);

            builder.finish(mapRef);

            return builder.sizedByteArray();
        }

        /**
         * Load a flatbuffer map into a LiveMap.
         *
         * @param raw the flatbuffer map pointer
         * @return a new copy of the map as a LiveMap
         */
        public static LiveMap deserialize(battlecode.schema.GameMap raw) {
            final float width = raw.maxCorner().x() - raw.minCorner().x();
            final float height = raw.maxCorner().y() - raw.minCorner().y();
            final MapLocation origin = new MapLocation(raw.minCorner().x(), raw.minCorner().y());
            final int seed = raw.randomSeed();
            final int rounds = GameConstants.GAME_DEFAULT_ROUNDS;
            final String mapName = raw.name();

            ArrayList<BodyInfo> initBodies = new ArrayList<>();
            SpawnedBodyTable bodyTable = raw.bodies();
            initInitialBodiesFromSchemaBodyTable(bodyTable, initBodies);

            NeutralTreeTable treeTable = raw.trees();
            initInitialBodiesFromSchemaNeutralTreeTable(treeTable, initBodies);

            BodyInfo[] initialBodies = initBodies.toArray(new BodyInfo[initBodies.size()]);

            return new LiveMap(
                    width, height, origin, seed, rounds, mapName, initialBodies
            );
        }


        /**
         * Write a map to a builder.
         *
         * @param builder the target builder
         * @param gameMap the map to write
         * @return the object reference to the map in the builder
         */
        public static int serialize(FlatBufferBuilder builder, LiveMap gameMap) {
            int name = builder.createString(gameMap.getMapName());
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
            ArrayList<Float> treeHealths = new ArrayList<>();
            ArrayList<Float> treeMaxHealths = new ArrayList<>();
            for (BodyInfo initBody : gameMap.getInitialBodies()) {
                if (initBody.isRobot()) {
                    RobotInfo robot = (RobotInfo) initBody;
                    bodyIDs.add(robot.ID);
                    bodyTeamIDs.add(TeamMapping.id(robot.team));
                    bodyTypes.add(FlatHelpers.getBodyTypeFromRobotType(robot.type));
                    bodyLocsXs.add(robot.location.x);
                    bodyLocsYs.add(robot.location.y);
                } else if (initBody.isTree()) {
                    TreeInfo tree = (TreeInfo) initBody;
                    if (tree.team == Team.NEUTRAL) {
                        treeIDs.add(tree.ID);
                        treeRadii.add(tree.radius);
                        treeContainedBullets.add(tree.containedBullets);
                        treeContainedBodies.add(FlatHelpers.getBodyTypeFromRobotType(tree.containedRobot));
                        treeLocsXs.add(tree.location.x);
                        treeLocsYs.add(tree.location.y);
                        treeHealths.add(tree.health);
                        treeMaxHealths.add(tree.maxHealth);
                    } else {
                        bodyIDs.add(tree.ID);
                        bodyTeamIDs.add(TeamMapping.id(tree.team));
                        bodyTypes.add(BodyType.TREE_BULLET);
                        bodyLocsXs.add(tree.location.x);
                        bodyLocsYs.add(tree.location.y);
                    }
                } else {
                    // bullet; ignore?
                }
            }

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

            robotIDs = NeutralTreeTable.createRobotIDsVector(builder, ArrayUtils.toPrimitive(treeIDs.toArray(new Integer[treeIDs.size()])));
            int radii = NeutralTreeTable.createRadiiVector(builder, ArrayUtils.toPrimitive(treeRadii.toArray(new Float[treeRadii.size()])));
            int containedBullets = NeutralTreeTable.createContainedBulletsVector(builder, ArrayUtils.toPrimitive(treeContainedBullets.toArray(new Integer[treeContainedBullets.size()])));
            int containedBodies = NeutralTreeTable.createContainedBodiesVector(builder, ArrayUtils.toPrimitive(treeContainedBodies.toArray(new Byte[treeContainedBodies.size()])));
            int healths = NeutralTreeTable.createHealthsVector(builder, ArrayUtils.toPrimitive(treeHealths.toArray(new Float[treeHealths.size()])));
            int maxHealths = NeutralTreeTable.createMaxHealthsVector(builder, ArrayUtils.toPrimitive(treeMaxHealths.toArray(new Float[treeMaxHealths.size()])));
            locs = VecTable.createVecTable(builder,
                    VecTable.createXsVector(builder, ArrayUtils.toPrimitive(treeLocsXs.toArray(new Float[treeLocsXs.size()]))),
                    VecTable.createYsVector(builder, ArrayUtils.toPrimitive(treeLocsYs.toArray(new Float[treeLocsYs.size()]))));
            NeutralTreeTable.startNeutralTreeTable(builder);
            NeutralTreeTable.addRobotIDs(builder, robotIDs);
            NeutralTreeTable.addLocs(builder, locs);
            NeutralTreeTable.addRadii(builder, radii);
            NeutralTreeTable.addContainedBullets(builder, containedBullets);
            NeutralTreeTable.addContainedBodies(builder, containedBodies);
            NeutralTreeTable.addHealths(builder, healths);
            NeutralTreeTable.addMaxHealths(builder, maxHealths);
            NeutralTreeTable.addContainedBodies(builder, containedBodies);
            int trees = NeutralTreeTable.endNeutralTreeTable(builder);

            // Build LiveMap for flatbuffer
            battlecode.schema.GameMap.startGameMap(builder);
            battlecode.schema.GameMap.addName(builder, name);
            battlecode.schema.GameMap.addMinCorner(builder, Vec.createVec(builder, gameMap.getOrigin().x, gameMap.getOrigin().y));
            battlecode.schema.GameMap.addMaxCorner(builder, Vec.createVec(builder, gameMap.getOrigin().x + gameMap.getWidth(),
                    gameMap.getOrigin().y + gameMap.getHeight()));
            battlecode.schema.GameMap.addBodies(builder, bodies);
            battlecode.schema.GameMap.addTrees(builder, trees);
            battlecode.schema.GameMap.addRandomSeed(builder, randomSeed);

            return battlecode.schema.GameMap.endGameMap(builder);

        }

        // ****************************
        // *** HELPER METHODS *********
        // ****************************

        private static void initInitialBodiesFromSchemaBodyTable(SpawnedBodyTable bodyTable, ArrayList<BodyInfo> initialBodies) {
            // Assumes no neutral trees
            VecTable locs = bodyTable.locs();
            for (int i = 0; i < bodyTable.robotIDsLength(); i++) {
                RobotType bodyType = FlatHelpers.getRobotTypeFromBodyType(bodyTable.types(i));
                int bodyID = bodyTable.robotIDs(i);
                float bodyX = locs.xs(i);
                float bodyY = locs.ys(i);
                Team bodyTeam = TeamMapping.team(bodyTable.teamIDs(i));
                if (bodyType != null) {
                    initialBodies.add(new RobotInfo(bodyID, bodyTeam, bodyType, new MapLocation(bodyX, bodyY), bodyType.getStartingHealth(), 0, 0));
                } else {
                    initialBodies.add(new TreeInfo(bodyID, bodyTeam, new MapLocation(bodyX, bodyY), 0, 0, 0, null));
                }
            }
        }

        private static void initInitialBodiesFromSchemaNeutralTreeTable(NeutralTreeTable treeTable,
                                                                        ArrayList<BodyInfo> initialBodies) {
            VecTable locs = treeTable.locs();

            for (int i = 0; i < treeTable.robotIDsLength(); i++) {
                int bodyID = treeTable.robotIDs(i);
                float bodyX = locs.xs(i);
                float bodyY = locs.ys(i);
                float bodyRadius = treeTable.radii(i);
                int containedBullets = treeTable.containedBullets(i);
                RobotType containedType = FlatHelpers.getRobotTypeFromBodyType(treeTable.containedBodies(i));
                TreeInfo tree = new TreeInfo(
                        bodyID, Team.NEUTRAL, new MapLocation(bodyX, bodyY),
                        bodyRadius, bodyRadius * GameConstants.NEUTRAL_TREE_HEALTH_RATE,
                        containedBullets, containedType
                );
                initialBodies.add(tree);

                // we compute these ourselves so that we can enforce our invariants
                // but we will warn on mismatch
                float health = treeTable.healths(i);
                float maxHealth = treeTable.maxHealths(i);
            }
        }

    }
}
