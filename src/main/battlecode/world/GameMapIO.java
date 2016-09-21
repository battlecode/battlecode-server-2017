package battlecode.world;

import battlecode.server.Config;
import battlecode.serial.serializer.Serializer;
import battlecode.serial.serializer.XStreamSerializerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

    public static GameMap myLoadMap(String mapName, File mapDir) throws IOException {
        // Checking in given directory
        String mapStringPath = mapDir.getPath() + mapName;
        File mapFile = new File(mapStringPath);
        if(mapFile.exists()){
            Path mapPath = Paths.get(mapDir.getPath() + mapName);
            byte[] data  = Files.readAllBytes(mapPath);
        }

        // Check in local directory
        mapStringPath = BACKUP_LOADER.getResource("resources/" + mapName).getPath();
        mapFile = new File(mapStringPath);
        if(mapFile.exists()){
            Path mapPath = Paths.get(mapStringPath);
            byte[] data = Files.readAllBytes(mapPath);
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
     * Prevent instantiation.
     */
    private GameMapIO() {}
}
