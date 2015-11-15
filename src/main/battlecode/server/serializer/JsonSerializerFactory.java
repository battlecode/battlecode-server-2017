package battlecode.server.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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

        /**
         * The mapper instance to use for all serializers.
         * ObjectMapper is thread-safe, so this is fine.
         */
        private static final ObjectMapper mapper = new ObjectMapper();

        /**
         * The reader factory to use to create all streaming parsers.
         * Again, thread safe.
         */
        private static final JsonFactory streamingParserFactory = new JsonFactory();

        /**
         * Configure JSON serialization.
         */
        static {
            // Serialize *everything* with type annotations.
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
            // Serialize empty classes (like ResumeNotification)
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

            // Parser should own input, and close input when it is closed.
            // Generator should own output, and close output when it is closed.
            streamingParserFactory.enable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        }


        private final JsonParser input;
        private final JsonGenerator output;

        public JsonSerializer(final OutputStream output, final InputStream input) throws IOException {
            this.output = streamingParserFactory.createGenerator(output);
            this.input = streamingParserFactory.createParser(input);
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
