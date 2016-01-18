package battlecode.server.proxy;

import battlecode.serial.PauseEvent;
import battlecode.serial.ServerEvent;
import battlecode.server.GameInfo;
import battlecode.server.Server;
import battlecode.serial.serializer.Serializer;
import battlecode.serial.serializer.SerializerFactory;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.zip.GZIPOutputStream;

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
    protected final Serializer<ServerEvent> serializer;

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
    protected File file;

    /**
     * The temp file.
     */
    protected final File temp;

    /**
     * Creates a new FileProxy that utilizes the file given by the specified
     * filename.
     *
     * @param saveFile The name of the file to write to.
     * @param serializerFactory The serializerFactory to create a serializer with.
     * @throws IOException if the file cannot be opened or written to.
     */
    public FileProxy(File saveFile, SerializerFactory serializerFactory) throws IOException {
        // Create directories if necessary.
        this.file = saveFile;
        if (!file.exists() && file.getParentFile() != null)
            file.getParentFile().mkdirs();

        this.temp = File.createTempFile("battlecode", ".tmp", new File(
                System.getProperty("java.io.tmpdir")));
        temp.deleteOnExit();

        this.fileWriter = new FileOutputStream(temp);
        this.gzipWriter = new GZIPOutputStream(fileWriter);

        this.serializer = serializerFactory.createSerializer(
                gzipWriter,
                null,
                ServerEvent.class
        );
    }

    @Override
    public synchronized void close() throws IOException {
        serializer.close();

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
            Server.warn("unable to rename match file "+e.getMessage());
        }
    }

    @Override
    public synchronized void writeEvent(final ServerEvent message) throws IOException {
        if (message instanceof PauseEvent) {
            // We can ignore pauses, since people reading the file won't care.
            return;
        }
        serializer.serialize(message);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " { target: " + file + " }";
    }

    /**
     * A factory for file proxies.
     */
    public static class Factory implements ProxyFactory {
        private SerializerFactory serializerFactory;

        /**
         * Create a new serializer factory
         * @param serializerFactory the factory used to create serializers for
         *                          proxies created by this factory.
         */
        public Factory(SerializerFactory serializerFactory) {
            this.serializerFactory = serializerFactory;
        }

        @Override
        public Proxy createProxy(GameInfo info) throws IOException {
            return new FileProxy(info.getSaveFile(), serializerFactory);
        }
    }
}
