package battlecode.instrumenter;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertTrue;

/**
 * Utility to write a bunch of classes out to a URL.
 * Used because we need to test loading from jars / the filesystem.
 *
 * @author james
 */
@Ignore
public class URLUtils {

    /**
     * Take a list of resources, package them into a temp jar, and return
     * the URL of the jar.
     *
     * @param resources in the form {"instrumentertest/CallsIllegalMethods.class", ...}
     * @return the URL of the new jar file
     * @throws IOException
     */
    public static URL toTempJar(String... resources) throws IOException {
        File jar = Files.createTempFile("battlecode-test", ".jar").toFile();
        jar.deleteOnExit();

        ZipOutputStream jarOutput = new ZipOutputStream(new FileOutputStream(jar));

        for (String resource : resources) {
            jarOutput.putNextEntry(new ZipEntry(resource));
            IOUtils.copy(
                    URLUtils.class.getClassLoader().getResourceAsStream(resource),
                    jarOutput
            );
            jarOutput.closeEntry();
        }
        jarOutput.close();

        return jar.toURI().toURL();
    }

    /**
     * Take a list of resources, package them into a temp jar, and return
     * the URL of the jar.
     *
     * @param resources in the form {"instrumentertest/CallsIllegalMethods.class", ...}
     * @return the URL of the new folder.
     * @throws IOException
     */
    public static URL toTempFolder(String... resources) throws IOException {
        File folder = Files.createTempDirectory("battlecode-test").toFile();
        folder.deleteOnExit();

        for (String resource : resources) {
            File resFile = new File(folder, resource);
            resFile.getParentFile().mkdirs();
            try (FileOutputStream out = new FileOutputStream(resFile)) {
                IOUtils.copy(
                        URLUtils.class.getClassLoader().getResourceAsStream(resource),
                        out
                );
            }
        }

        return folder.toURI().toURL();
    }

    /**
     * Take a list of resources, package them into a temp jar, and return
     * the URL of the jar.
     *
     * @param resources in the form {"instrumentertest/CallsIllegalMethods.class", ...}
     * @return the URL of the new folder.
     * @throws IOException
     */
    public static URL toTempFolder(String[] paths, URL[] resources) throws IOException {
        File folder = Files.createTempDirectory("battlecode-test").toFile();
        folder.deleteOnExit();

        assert paths.length == resources.length;

        for (int i = 0; i < paths.length; i++) {
            File resFile = new File(folder, paths[i]);
            resFile.getParentFile().mkdirs();
            try (FileOutputStream out = new FileOutputStream(resFile)) {
                IOUtils.copy(
                        resources[i].openStream(),
                        out
                );
            }
        }

        return folder.toURI().toURL();
    }

    /**
     * Take a list of resources, package them into a temp jar, and return
     * the URL of the jar.
     *
     * @param resources in the form {"instrumentertest/CallsIllegalMethods.class", ...}
     * @return the URL of the new jar file
     * @throws IOException
     */
    public static URL toTempJar(String[] paths, URL[] resources) throws IOException {
        File jar = Files.createTempFile("battlecode-test", ".jar").toFile();
        jar.deleteOnExit();

        assert paths.length == resources.length;

        ZipOutputStream jarOutput = new ZipOutputStream(new FileOutputStream(jar));

        for (int i = 0; i < paths.length; i++) {
            jarOutput.putNextEntry(new ZipEntry(paths[i]));
            IOUtils.copy(
                    resources[i].openStream(),
                    jarOutput
            );
            jarOutput.closeEntry();
        }
        jarOutput.close();

        return jar.toURI().toURL();
    }

}
