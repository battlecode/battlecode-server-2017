package battlecode.server.proxy;

import java.io.IOException;

/**
 * This class represents a sink for match data, which could be a file, a
 * socket, or another object in the same JVM.
 *
 * Implementers can inspect passed objects to see when game state changes. For
 * instance, a proxy that writes an individual file for each match could check
 * if passed objects were MatchFooters to know when to start a new file.
 */
public interface Proxy {
    /**
     * Tries to write the given object to the recipient.
     *
     * @param message the object to write
     * @throws IOException if the object could not be written
     */
    void writeObject(final Object message) throws IOException;

    /**
     * Closes the connection.
     *
     * @throws IOException if the connection cannot be closed.
     */
    void close() throws IOException;
}
