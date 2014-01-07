package battlecode.server.proxy;

import battlecode.serial.notification.Notification;
import battlecode.server.Server;

import java.io.*;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.FileUtils;

/**
 * This class represents a "connection" to a file. It provides a method for
 * saving binary match data to disk so that it may be read later.
 */
public class FileProxy extends Proxy {

    /**
     * The stream to use to write to the file.
     */
    protected OutputStream fileWriter;

    protected OutputStream stream;

    /**
     * The original file.
     */
    protected File file;

    /**
     * The temp file.
     */
    protected File temp;

    /**
     * Whether or not the file is buffered.
     */
    protected final boolean buffered;

    protected ByteArrayOutputStream buffer;

    /**
     * Creates a new FileProxy that utilizes the file given by the specified
     * filename.
     *
     * @param fileName The name of the file to write to.
     * @throws IOException if the file cannot be opened or written to.
     */
    FileProxy(String fileName) throws IOException {
        this(fileName, false);
    }

    protected FileProxy(String fileName, boolean buffered) throws IOException {
        super();

        this.buffered = buffered;

        if (buffered) {
            buffer = new ByteArrayOutputStream();
            fileWriter = buffer;
            // Create directories if necessary.
            file = new File(fileName);
            if (!file.exists() && file.getParentFile() != null)
                file.getParentFile().mkdirs();
        } else {
            buffer = null;

            // Create directories if necessary.
            file = new File(fileName);
            if (!file.exists() && file.getParentFile() != null)
                file.getParentFile().mkdirs();

            temp = File.createTempFile("battlecode", ".tmp", new File(System
                    .getProperty("java.io.tmpdir")));
            temp.deleteOnExit();

            fileWriter = new FileOutputStream(temp);
        }

        stream = new GZIPOutputStream(fileWriter);

    }

    public OutputStream getOutputStream() throws IOException {
        return stream;
    }

    public void close() throws IOException {
        super.close();

        fileWriter.close();
        stream.close();

        if (buffered) {
            if (file == null)
                throw new IOException("no file to write to");
            Server.say("writing to file " + file + " directly");
            FileOutputStream out = new FileOutputStream(file);
            out.write(buffer.toByteArray());
            out.close();
        } else {
            // Move the file to its desired location.
            if (file.exists())
                file.delete();
            try {
                FileUtils.moveFile(temp, file);
            } catch(IOError e) {
                Server.warn("unable to rename match file");
            }
        }
    }

    public void writeObject(Object o) throws IOException {
        if (o instanceof Notification)
            return;
        super.writeObject(o);
    }
}
