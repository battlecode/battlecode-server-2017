package battlecode.world;

import battlecode.server.Config;
import battlecode.server.serializer.Serializer;
import battlecode.server.serializer.XStreamSerializerFactory;

import java.io.*;

/**
 * This class contains the code for reading an XML map file and converting it
 * to a proper GameMap.
 */
public final class GameMapIO {
    private static final XStreamSerializerFactory factory = new XStreamSerializerFactory();

    /**
     * Returns an GameMapIO for a specific map.
     *
     * @param mapName name of map.
     * @param mapDir directory to store the map file in.
     * @return GameMapIO for map.
     */
    public static GameMap loadMap(String mapName, File mapDir)
            throws IOException {

        assert mapDir.isDirectory();

        final File mapFile = new File(mapDir, mapName + ".xml");

        final GameMap result = loadMap(new FileInputStream(mapFile));

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
                     factory.createSerializer(null, stream, GameMap.class)) {
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
                     factory.createSerializer(stream, null, GameMap.class)) {
            ser.serialize(map);
        }
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
