package battlecode.server.proxy;

import java.io.IOException;
import java.io.OutputStream;

import battlecode.server.serializer.Serializer;

/**
 * A proxy that writes to a given output stream, and closes the output stream
 * when finished.
 *
 * Created by james on 7/25/15.
 */
public class OutputStreamProxy implements Proxy {
    private final Serializer serializer;
    private final OutputStream output;

    public OutputStreamProxy(final Serializer serializer, final OutputStream output) {
        this.serializer = serializer;
        this.output = output;
    }

    @Override
    public void writeObject(final Object message) throws IOException {
        serializer.serialize(output, message);
        output.flush();
    }

    @Override
    public void close() throws IOException {
        output.close();
    }
}
