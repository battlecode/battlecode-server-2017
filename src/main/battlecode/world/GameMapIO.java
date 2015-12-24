package battlecode.world;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.server.ErrorReporter;
import battlecode.server.Config;
import battlecode.server.serializer.Serializer;
import battlecode.server.serializer.XStreamSerializerFactory;
import battlecode.world.GameMap.MapProperties;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;

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
     * @param mapPath path of map.
     * @return GameMapIO for map.
     */
    public static GameMap loadMap(String mapName, String mapPath)
            throws IOException {

        final File mapDir = new File(mapPath);

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
    public static void writeMap(GameMap map, String mapDir)
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
        String mapPath = Config.getGlobalConfig().get("bc.game.map-path");
        for (String map : args) {
            try {
                if (!loadMap(mapPath, map).isTournamentLegal()) {
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
