package battlecode.server.proxy;

import battlecode.serial.MatchFooter;
import battlecode.serial.MatchHeader;
import battlecode.serial.RoundDelta;
import battlecode.serial.RoundStats;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * This class represents a sink for match data. It provides a means for writing
 * various types of data to a match recipient (typically a file or a TCP socket)
 * and managing the connection to the recipient. Implementations typcially
 * need only to present an OutputStream; the default Proxy implementation is
 * sufficient for most cases.
 */
public abstract class Proxy {

    /**
     * The output stream used for writing objects.
     */
    protected ObjectOutputStream output;

    /**
     * Gets the OutputStream used for
     *
     * @return the OutputStream to use for writing to the recipient; this method
     *         should never return null
     * @throws IOException if the OutputStream instance could not be obtained
     */
    protected abstract OutputStream getOutputStream() throws IOException;

    public Proxy() {
        this.output = null;
    }

    /**
     * Prepares the connection for match data.
     *
     * @throws IOException if the connection cannot be opened
     */
    public void open() throws IOException {
        OutputStream out = getOutputStream();
        if (out != null) {
            if (out instanceof ObjectOutputStream)
                this.output = (ObjectOutputStream) out;
            else
                this.output = new ObjectOutputStream(out);
            this.output.flush();
        }
    }

    /**
     * Closes the connection.
     *
     * @throws IOException if the connection cannot be closed.
     */
    public void close() throws IOException {
        if (output != null) {
            this.output.flush();
            this.output.close();
        }
    }

    /**
     * Tries to write the given object to the recipient.
     *
     * @param o the object to write
     * @throws IOException if the object could not be written
     */
    public void writeObject(Object o) throws IOException {
        if (output != null) {
            output.reset();
            output.writeObject(o);
        }
    }

    /**
     * Writes header data to the recipient.
     *
     * @param data the header data bytes to write
     * @throws IOException if the recipient could not be written to
     */
    public void writeHeader(MatchHeader header) throws IOException {
        writeObject(header);
    }

    /**
     * Writes round data to the recipient.
     *
     * @param data the round data bytes to write
     * @throws IOException if the recipient could not be written to
     */
    public void writeRound(RoundDelta round) throws IOException {
        writeObject(round);
    }

    /**
     * Writes footer data to the recipient.
     *
     * @param data the footer data bytes to write
     * @throws IOException if the recipient could not be written to
     */
    public void writeFooter(MatchFooter footer) throws IOException {
        writeObject(footer);
    }

    /**
     * Writes stats data to the recipient.
     *
     * @param data the stats data bytes to write
     * @throws IOException if the recipient could not be written to
     */
    public void writeStats(RoundStats stats) throws IOException {
        writeObject(stats);
    }
}
