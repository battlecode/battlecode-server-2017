package battlecode.server;

import battlecode.serial.serializer.JavaSerializerFactory;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by james on 11/15/15.
 */
public class JavaSerializerFactoryTest extends SerializerFactoryTestBase {
    @Test
    public void testJavaRoundTrip() throws IOException {
        testRoundTrip(new JavaSerializerFactory());
    }
}
