package battlecode.world;

import battlecode.server.Config;
import battlecode.serial.serializer.Serializer;
import battlecode.serial.serializer.XStreamSerializerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class contains the code for reading a flatbuffer map file and converting it
 * to a proper GameMap.
 */
public final class GameMapIO {
    /**
     * The factory we use to create serializers.
     */
    private static final XStreamSerializerFactory FACTORY = new XStreamSerializerFactory();

    /**
     * The package we check for maps in if they can't be found in the file system.
     */
    public static final String DEFAULT_MAP_PACKAGE = "battlecode/world/resources/";

    /**
     * The loader we use if we can't find a map in the correct path.
     */
    private static final ClassLoader BACKUP_LOADER = GameMapIO.class.getClassLoader();

    /**
     * Returns a GameMap for a specific map.
     * If the map can't be found in the given directory, the package
     * "battlecode.world.resources" is checked as a backup.
     *
     * @param mapName name of map.
     * @param mapDir directory to load the extra map from; may be null.
     * @return GameMap for map
     * @throws IOException if the map fails to load or can't be found.
     */
    public static GameMap loadMap(String mapName, File mapDir)
            throws IOException {
        final GameMap result;

        if (mapName.endsWith(".xml")) {
            mapName = mapName.substring(0, mapName.lastIndexOf('.'));
        }

        final File mapFile = new File(mapDir, mapName + ".xml");
        if (mapFile.exists()) {
            result = loadMap(new FileInputStream(mapFile));
        } else {
            final InputStream backupStream = BACKUP_LOADER.getResourceAsStream(DEFAULT_MAP_PACKAGE + mapName + ".xml");
            if (backupStream == null) {
                throw new IOException("Can't load map: " + mapName + " from dir " + mapDir + " or default maps.");
            }
            result = loadMap(backupStream);
        }

        if (!result.getMapName().equals(mapName)) {
            throw new IOException("Invalid map: name (" + result.getMapName()
                    + ") does not match filename (" + mapName + ".xml)"
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
    public static GameMap loadMap(InputStream stream)
            throws IOException {

        try (final Serializer<GameMap> ser =
                     FACTORY.createSerializer(null, stream, GameMap.class)) {
            return ser.deserialize();
        }
    }

    /**
     * Write a map to an output directory.
     *
     * @param map the map to output
     * @param mapDir the directory to write the map to
     * @throws IOException if the map cannot be written
     */
    public static void writeMap(GameMap map, File mapDir)
            throws IOException {
        writeMap(
                map,
                new FileOutputStream(new File(mapDir, map.getMapName() + ".xml"))
        );
    }

    /**
     * Write a map to an output stream.
     *
     * @param map the map to output
     * @param stream the stream to write to
     * @throws IOException if the map cannot be written
     */
    public static void writeMap(GameMap map, OutputStream stream)
            throws IOException {

        try (final Serializer<GameMap> ser =
                     FACTORY.createSerializer(stream, null, GameMap.class)) {
            ser.serialize(map);
        }
    }

    /**
     * @param mapDir the directory to check for extra maps. May be null.
     * @return a set of available map names, including those built-in to battlecode-server.
     */
    public static List<String> getAvailableMaps(String mapDir) {
        final List<String> result = new ArrayList<>();

        // Load maps from the extra directory
        if (mapDir != null) {
            final File mapDirFile = new File(mapDir);

            if (mapDirFile.isDirectory()) {
                // Files in directory
                for (File file : mapDirFile.listFiles()) {
                    String name = file.getName();
                    if (name.endsWith(".xml")) {
                        result.add(name.substring(0, name.length() - 4));
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
                    if (name.startsWith(DEFAULT_MAP_PACKAGE) && name.endsWith(".xml")) {
                        result.add(name.substring(DEFAULT_MAP_PACKAGE.length(), name.length() - 4));
                    }
                }
            } else {
                // We're running from class files.
                final String[] resourceFiles = new File(BACKUP_LOADER.getResource(DEFAULT_MAP_PACKAGE).toURI()).list();

                for (String file : resourceFiles) {
                    if (file.endsWith(".xml")) {
                        result.add(file.substring(0, file.length() - 4));
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
     * Checks a set of maps for legality. It will search in bc.game.map-path
     * for the maps, and use the maps whose names are passed in as runtime
     * arguments.
     *
     * @param args names of maps to use.
     */
    public static void main(String[] args) {
        System.out.println("Checking maps for tournament legality...");
        File mapPath = new File(Config.getGlobalConfig().get("bc.game.map-path"));
        for (String map : args) {
            try {
                if (!loadMap(map, mapPath).isTournamentLegal()) {
                    System.err.println("Illegal map: " + map);
                }
            } catch (Exception e) {
                System.err.println("Couldn't load map: "+map);
                e.printStackTrace();
            }
        }
    }

    /**
     * Prevent instantiation.
     */
    private GameMapIO() {}
}
