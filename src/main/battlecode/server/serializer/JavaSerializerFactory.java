package battlecode.server.serializer;

import java.io.*;

/**
 * Serialize messages using the built-in Java serialization.
 *
 * Created by james on 7/24/15.
 */
public final class JavaSerializerFactory implements SerializerFactory {
    @Override
    public Serializer createSerializer(final OutputStream output, final InputStream input) throws IOException {
        final ObjectOutputStream wrappedOutput;
        if (output != null) {
            wrappedOutput = new ObjectOutputStream(output);
        } else {
            wrappedOutput = null;
        }

        final ObjectInputStream wrappedInput;
        if (input != null) {
            wrappedInput = new ObjectInputStream(input);
        } else {
            wrappedInput = null;
        }

        return new StandardSerializer(
                wrappedOutput,
                wrappedInput
        );
    }
}
