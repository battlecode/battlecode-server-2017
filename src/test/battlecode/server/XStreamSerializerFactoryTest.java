package battlecode.server;

import battlecode.serial.serializer.XStreamSerializerFactory;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by james on 11/15/15.
 */
public class XStreamSerializerFactoryTest extends SerializerFactoryTestBase {
    @Test
    public void testXStreamRoundTrip() throws IOException {
        testRoundTrip(new XStreamSerializerFactory());
    }
}
