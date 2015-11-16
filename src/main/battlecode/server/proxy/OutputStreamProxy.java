package battlecode.server.proxy;

import battlecode.server.serializer.Serializer;
import battlecode.server.serializer.SerializerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A proxy that writes to a given output stream, and closes the output stream
 * when finished.
 *
 * Created by james on 7/25/15.
 */
public class OutputStreamProxy implements Proxy {
    private final Serializer serializer;
    private final OutputStream output;

    public OutputStreamProxy(final SerializerFactory serializerFactory, final OutputStream output) throws IOException {
        this.serializer = serializerFactory.createSerializer(output, null);
        this.output = output;
    }

    @Override
    public void writeObject(final Object message) throws IOException {
        serializer.serialize(message);
        output.flush();
    }

    @Override
    public void close() throws IOException {
        serializer.close();
        output.close();
    }
}
