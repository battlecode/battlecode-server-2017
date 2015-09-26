package battlecode.server.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

/**
 * Serialize things with JSON.
 *
 * Created by james on 9/26/15.
 */
public final class JsonSerializerFactory implements SerializerFactory {

    @Override
    public Serializer createSerializer(OutputStream output, InputStream input) throws IOException {
        return new JsonSerializer(output, input);
    }

    public static final class JsonSerializer implements Serializer {

        private final JsonReader reader;
        private final JsonWriter writer;

        public JsonSerializer(final OutputStream output, final InputStream input) throws IOException {
            if (input != null) {
                reader = new JsonReader(input);
            } else {
                reader = null;
            }

            if (output != null) {
                writer = new JsonWriter(output);
            } else {
                writer = null;
            }
        }

        @Override
        public void serialize(Object message) throws IOException {
            if (writer == null) {
                throw new IOException("No OutputStream given");
            }

            writer.write(message);
        }

        @Override
        public Object deserialize() throws IOException {
            if (reader == null) {
                throw new IOException("No InputStream given");
            }

            return reader.readObject();
        }

        @Override
        public void close() throws IOException {
            if (writer != null) {
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
    }
}
