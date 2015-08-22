package battlecode.server.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Created by james on 7/24/15.
 */
public final class JavaSerializer implements Serializer {

    public JavaSerializer() {
    }

    @Override
    public void serialize(final OutputStream output, final Object message) throws IOException {
        // TODO check how expensive this is
        final ObjectOutputStream wrappedOutput = new ObjectOutputStream(output);
        wrappedOutput.writeObject(message);
        wrappedOutput.flush();
    }

    @Override
    public Object deserialize(final InputStream input) throws IOException {
        // TODO check how expensive this is
        final ObjectInputStream wrappedInput = new ObjectInputStream(input);
        final Object result;
        try {
            result = wrappedInput.readObject();
        } catch (final ClassNotFoundException e) {
            throw new IOException(e);
        }
        return result;
    }
}
