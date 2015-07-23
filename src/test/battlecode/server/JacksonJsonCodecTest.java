package battlecode.server;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.engine.GameState;
import battlecode.server.http.JacksonJsonCodec;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Important note for these tests: for some reason Jackson can't serialize
 * java.lang.Objects. That might cause some confusion.
 *
 * Created by james on 7/22/15.
 */
@RunWith(MockitoJUnitRunner.class) // Make sure our mock context works.
public final class JacksonJsonCodecTest {
    final private ObjectMapper mapper = new ObjectMapper();
    final private JacksonJsonCodec codec = new JacksonJsonCodec();
    @Mock private ChannelHandlerContext mockCtx;
    @Mock private ChannelPromise mockPromise;

    @Test
    public void testCreate() {
        // Shouldn't throw an exception.
        new JacksonJsonCodec();
    }

    @Test
    public void testWritesObject() throws Exception {
        final MapLocation o = new MapLocation(0, 0);
        codec.write(mockCtx, o, mockPromise);
        final ArgumentCaptor<ByteBuf> args = ArgumentCaptor.forClass(ByteBuf.class);
        verify(mockCtx).write(args.capture());
        final ByteBuf comparison = Unpooled.buffer();
        mapper.writeValue(new ByteBufOutputStream(comparison), o);
        assertEquals(args.getValue(), comparison);
    }

    @Test
    public void testCanSerializeAll() throws Throwable {
        // Should be able to serialize anything we throw at it.
        final Class<?>[] classes = new Class<?>[] {
                MapLocation.class,
                Direction.class,
                GameState.class,
        };

        for (final Class<?> c : classes) {
            final AtomicReference<Throwable> error = new AtomicReference<>();
            if (!mapper.canSerialize(c, error)) {
                if (error.get() != null) {
                    throw error.get();
                }
                Assert.fail("Can't serialize object of class: " + c.getCanonicalName());
            }
        }
    }
}
