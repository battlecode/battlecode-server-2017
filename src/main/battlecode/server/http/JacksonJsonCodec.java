package battlecode.server.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * Created by james on 7/19/15.
 */
public final class JacksonJsonCodec extends ChannelDuplexHandler {
    private final ObjectMapper mapper = new ObjectMapper();

    public JacksonJsonCodec() {
    }

    @Override
    public void write(final ChannelHandlerContext ctx, final Object message, final ChannelPromise promise) throws Exception {
        if (!mapper.canSerialize(message.getClass())) {
            throw new UnserializableException(message);
        }
        final ByteBuf output = Unpooled.buffer();
        mapper.writeValue(new ByteBufOutputStream(output), message);
        ctx.write(output);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object message) throws Exception {
        if (!(message instanceof ByteBuf)) {
            ctx.fireChannelRead(message);
        }
        final ByteBuf buf = (ByteBuf) message;
        final JsonNode tree = mapper.readTree(new ByteBufInputStream(buf));
        ctx.fireChannelRead(tree);
    }

    public static final class UnserializableException extends Exception {
        public final Object object;
        public UnserializableException(final Object object) {
            this.object = object;
        }
        @Override
        public String getMessage() {
            return "Can't serialize: " + object.toString();
        }
    }
}
