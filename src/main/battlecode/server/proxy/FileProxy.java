package battlecode.server.proxy;

import battlecode.serial.notification.Notification;
import battlecode.server.Server;

import java.io.*;
import java.util.zip.GZIPOutputStream;

import battlecode.server.serializer.Serializer;
import org.apache.commons.io.FileUtils;

/**
 * This class represents a "connection" to a file. It allows match data to be
 * written to disk so that it may be read later.
 *
 * Note that it writes to a temporary file, which it moves into place when finished.
 */
public class FileProxy implements Proxy {

    /**
     * The serializer used to turn objects into bytes.
     */
    protected final Serializer serializer;

    /**
     * The stream to use to write to the temporary file.
     */
    protected final OutputStream fileWriter;

    /**
     * The stream used to gzip the input before writing to the temporary.
     */
    protected final OutputStream gzipWriter;

    /**
     * The eventual, target file
     */
    protected final File file;

    /**
     * The temp file.
     */
    protected final File temp;

    /**
     * Creates a new FileProxy that utilizes the file given by the specified
     * filename.
     *
     * @param fileName The name of the file to write to.
     * @param serializer The serializer to use.
     * @throws IOException if the file cannot be opened or written to.
     */
    public FileProxy(String fileName, Serializer serializer) throws IOException {
        // Create directories if necessary.
        this.file = new File(fileName);
        if (!file.exists() && file.getParentFile() != null)
            file.getParentFile().mkdirs();

        this.temp = File.createTempFile("battlecode", ".tmp", new File(
                System.getProperty("java.io.tmpdir")));
        temp.deleteOnExit();

        this.fileWriter = new FileOutputStream(temp);
        this.gzipWriter = new GZIPOutputStream(fileWriter);

        this.serializer = serializer;
    }

    @Override
    public void close() throws IOException {
        gzipWriter.flush();
        fileWriter.flush();
        gzipWriter.close();
        fileWriter.close();

        // Move the file to its desired location.
        if (file.exists())
            file.delete();
        try {
            FileUtils.moveFile(temp, file);
        } catch(final IOError e) {
            Server.warn("unable to rename match file");
        }
    }

    @Override
    public void writeObject(final Object message) throws IOException {
        if (message instanceof Notification)
            return;
        serializer.serialize(gzipWriter, message);
    }
}
