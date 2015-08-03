package battlecode.server.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implemented by things that can serialize and deserialize battlecode messages.
 *
 * Created by james on 7/24/15.
 */
public interface Serializer {
    /**
     * Write an object.
     *
     * @param output  the stream to write to.
     * @param message the object to write.
     * @throws IOException
     */
    void serialize(final OutputStream output, final Object message) throws IOException;

    /**
     * Read an object.
     * Note that this BLOCKS the calling thread until an object is read!
     *
     * @param input the stream to read from.
     */
    Object deserialize(final InputStream input) throws IOException;
}
