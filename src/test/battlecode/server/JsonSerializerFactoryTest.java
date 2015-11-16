package battlecode.server;

import battlecode.server.serializer.JsonSerializerFactory;
import org.junit.Test;

import java.io.IOException;

/**
 * Test the JSON serializer.
 *
 * Created by james on 11/15/15.
 */
public class JsonSerializerFactoryTest extends SerializerFactoryTestBase {
    @Test
    public void testJsonRoundTrip() throws IOException {
        testRoundTrip(new JsonSerializerFactory());
    }
}
