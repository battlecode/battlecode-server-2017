package battlecode.server.serializer;

import java.io.IOException;

/**
 * Implemented by things that can serialize and deserialize battlecode messages.
 *
 * This has probably been implemented better by somebody else, but who cares.
 *
 * Created by james on 7/24/15.
 */
public interface Serializer {

    /**
     * Write an object. Safe to be called from multiple threads.
     *
     * @param message the object to write.
     * @throws IOException
     */
    void serialize(final Object message) throws IOException;

    /**
     * Read an object. Safe to be called from multiple threads; read order is
     * not guaranteed if multiple threads are calling. Otherwise,
     * objects will be returned in the order deserialized.
     *
     * Note that this BLOCKS the calling thread until an object is read!
     * @throws IOException
     */
    Object deserialize() throws IOException;

    /**
     * Release any resources used by the serializer.
     *
     * @throws IOException
     */
    void close() throws IOException;
}
