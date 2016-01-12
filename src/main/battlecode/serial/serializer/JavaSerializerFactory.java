package battlecode.serial.serializer;

import java.io.*;

/**
 * Serialize messages using the built-in Java serialization.
 *
 * Created by james on 7/24/15.
 */
public final class JavaSerializerFactory implements SerializerFactory {
    @Override
    public <T> Serializer<T> createSerializer(final OutputStream output,
                                              final InputStream input,
                                              final Class<T> messageClass)
            throws IOException {

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

        return new StandardSerializer<>(
                wrappedOutput,
                wrappedInput,
                messageClass
        );
    }
}
