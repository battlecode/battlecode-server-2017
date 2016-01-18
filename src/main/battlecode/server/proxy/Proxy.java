package battlecode.server.proxy;

import battlecode.serial.ServerEvent;

import java.io.Closeable;
import java.io.IOException;

/**
 * This class represents a sink for match data, which could be a file, a
 * socket, or another object in the same JVM.
 *
 * Implementers can inspect passed objects to see when game state changes. For
 * instance, a proxy that writes an individual file for each match could check
 * if passed objects were MatchFooters to know when to start a new file.
 */
public interface Proxy extends Closeable {
    /**
     * Tries to write the given object to the recipient.
     *
     * Safe to be called from multiple threads; write order is
     * not guaranteed of called from multiple threads.
     *
     * @param message the object to write
     * @throws IOException if the object could not be written
     */
    void writeEvent(final ServerEvent message) throws IOException;

    /**
     * Closes the connection. Called after a game is run.
     *
     * @throws IOException if the connection cannot be closed.
     */
    void close() throws IOException;
}
