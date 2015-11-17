package battlecode.server.serializer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Serialize things with JSON.
 * <p>
 * Created by james on 9/26/15.
 */
public final class JsonSerializerFactory implements SerializerFactory {

    @Override
    public Serializer createSerializer(OutputStream output, InputStream input) throws IOException {
        return new JsonSerializer(output, input);
    }

    /**
     * The mapper instance to use for all serializers.
     * ObjectMapper is thread-safe, so this is fine.
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * The reader factory to use to create all streaming parsers / generators.
     * Again, thread safe.
     */
    private static final JsonFactory streamingFactory = new JsonFactory();

    /**
     * Configure JSON serialization.
     */
    static {
        // Serialize *everything* with type annotations.
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        // Serialize empty classes (like ResumeNotification)
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // Allow us to use Optional<T> and stuff
        mapper.registerModule(new Jdk8Module());

        // Parser should own input, and close input when it is closed.
        // Generator should own output, and close output when it is closed.
        streamingFactory.enable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
    }

    public static final class JsonSerializer implements Serializer {
        private final JsonParser input;
        private final JsonGenerator output;

        public JsonSerializer(final OutputStream output, final InputStream input) throws IOException {
            // Note that we create parsers and generators instead of just calling mapper.readValue() /
            // mapper.writeValue(). This is because we read *multiple* values from the stream, with no
            // delimiters; mapper can't do that just being called on the stream?
            // Also, it might be faster - not recreating parsers each time?
            this.output = streamingFactory.createGenerator(output);
            this.input = streamingFactory.createParser(input);
        }

        @Override
        public void serialize(Object message) throws IOException {
            if (this.output == null) {
                throw new IOException("No OutputStream given");
            }

            mapper.writeValue(output, message);
        }

        @Override
        public Object deserialize() throws IOException {
            if (input == null) {
                throw new IOException("No InputStream given");
            }

            // Note: input has embedded type information, mapper will figure it out.
            return mapper.readValue(input, Object.class);
        }

        @Override
        public void close() throws IOException {
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
        }
    }
}
