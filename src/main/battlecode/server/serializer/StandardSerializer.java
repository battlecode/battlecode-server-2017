package battlecode.server.serializer;

import java.io.*;

/**
 * A Serializer implementation that simply wraps standard Java
 * ObjectOutputStreams and ObjectInputStreams.
 *
 * Created by james on 9/26/15.
 */
public class StandardSerializer implements Serializer {
    final ObjectOutputStream output;
    final ObjectInputStream input;

    /**
     * Create a serializer.
     *
     * @param output the output to use
     * @param input the input to use
     */
    public StandardSerializer(final ObjectOutputStream output, final ObjectInputStream input) {
        this.output = output;
        this.input = input;
    }

    @Override
    public void serialize(final Object message) throws IOException {
        if (output == null) {
            throw new IOException("No OutputStream given");
        }

        output.writeObject(message);
        output.flush();
    }

    @Override
    public Object deserialize() throws IOException {
        if (input == null) {
            throw new IOException("No InputStream given");
        }

        final Object result;
        try {
            result = input.readObject();
        } catch (final ClassNotFoundException e) {
            throw new IOException(e);
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        if (input != null) {
            input.close();
        }
        if (output != null) {
            output.close();
        }
    }
}
