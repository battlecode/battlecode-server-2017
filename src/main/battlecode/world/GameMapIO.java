package battlecode.world;

import battlecode.server.TeamMapping;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class contains the code for reading a flatbuffer map file and converting it
 * to a proper GameMap.
 */
public final class GameMapIO {
    /**
     * The package we check for maps in if they can't be found in the file system.
     */
    public static final String DEFAULT_MAP_PACKAGE = "battlecode/world/resources/";

    /**
     * The loader we use if we can't find a map in the correct path.
     */
    private static final ClassLoader BACKUP_LOADER = GameMapIO.class.getClassLoader();

    public static GameMap loadMap(String mapName, File mapDir, TeamMapping teamMapping) throws IOException {
        // Checking in given directory
        String mapStringPath = mapDir.getPath() + mapName;
        File mapFile = new File(mapStringPath);
        if(mapFile.exists()){
            Path mapPath = Paths.get(mapDir.getPath() + mapName);
            byte[] data  = Files.readAllBytes(mapPath);
            ByteBuffer bb = ByteBuffer.wrap(data);
            battlecode.schema.GameMap schemaMap = battlecode.schema.GameMap.getRootAsGameMap(bb);
            return new GameMap(schemaMap, teamMapping);
        }

        // Check in local directory
        mapStringPath = BACKUP_LOADER.getResource("resources/" + mapName).getPath();
        mapFile = new File(mapStringPath);
        if(mapFile.exists()){
            Path mapPath = Paths.get(mapStringPath);
            byte[] data = Files.readAllBytes(mapPath);
            ByteBuffer bb = ByteBuffer.wrap(data);
            battlecode.schema.GameMap schemaMap = battlecode.schema.GameMap.getRootAsGameMap(bb);
            return new GameMap(schemaMap, teamMapping);
        }
        return null;
    }

    public static GameMap loadMap(String path, TeamMapping teamMapping) throws IOException {
        File mapFile = new File(path);
        if(mapFile.exists()){
            FileInputStream fIn = new FileInputStream(path);
            FileChannel fChan = fIn.getChannel();
            long fSize = fChan.size();
            ByteBuffer bb = ByteBuffer.allocate((int) fSize);
            fChan.read(bb);
            bb.rewind();
            fChan.close();
            battlecode.schema.GameMap schemaMap = battlecode.schema.GameMap.getRootAsGameMap(bb);
            return new GameMap(schemaMap, teamMapping);
        }
        return null;
    }

    /**
     * Prevent instantiation.
     */
    private GameMapIO() {}
}
