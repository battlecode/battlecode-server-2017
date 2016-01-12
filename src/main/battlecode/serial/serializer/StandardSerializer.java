package battlecode.serial.serializer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A Serializer implementation that simply wraps standard Java
 * ObjectOutputStreams and ObjectInputStreams.
 *
 * Created by james on 9/26/15.
 */
public class StandardSerializer<T> implements Serializer<T> {
    final ObjectOutputStream output;
    final ObjectInputStream input;
    final Class<T> messageClass;

    /**
     * Create a serializer.
     *
     * @param output the output to use
     * @param input the input to use
     */
    public StandardSerializer(final ObjectOutputStream output,
                              final ObjectInputStream input,
                              final Class<T> messageClass) {
        this.output = output;
        this.input = input;
        this.messageClass = messageClass;
    }

    @Override
    public synchronized void serialize(final T message) throws IOException {
        if (output == null) {
            throw new IOException("No OutputStream given");
        }

        output.writeObject(message);
        output.flush();
    }

    @Override
    public synchronized T deserialize() throws IOException {
        if (input == null) {
            throw new IOException("No InputStream given");
        }

        final Object result;
        try {
            result = input.readObject();
        } catch (final ClassNotFoundException e) {
            throw new IOException(e);
        }

        return messageClass.cast(result);
    }

    @Override
    public synchronized void close() throws IOException {
        if (input != null) {
            input.close();
        }
        if (output != null) {
            output.close();
        }
    }
}
