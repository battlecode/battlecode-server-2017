package battlecode.server.proxy;

import battlecode.serial.MatchHeader;
import battlecode.server.http.HttpServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A proxy for writing to a set of websockets.
 *
 * Created by james on 7/18/15.
 */
public final class WebSocketProxy extends Proxy {

    /**
     * All objects sent in the most recent match.
     * Used to play catch-up if a user refreshes their browser.
     */
    private final List<Object> sentObjects = new ArrayList<>();

    /**
     * The JSON serializer.
     */
    private final ObjectMapper mapper = new ObjectMapper();

    public WebSocketProxy() {

    }

    @Override
    public void open() throws IOException {}

    @Override
    public void writeObject(final Object o) throws IOException {
        if (!mapper.canSerialize(o.getClass())) {
            throw new IOException("Can't serialize object of type " + o.getClass().getCanonicalName());
        }

        if (o instanceof MatchHeader) {
            sentObjects.clear();
        }

        sentObjects.add(o);
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        throw new IOException("WebSocketProxy does not expose an OutputStream");
    }

    public void registerChannel(){}

    public void forgetChannel(){}

    private void sendToAllChannels(final Object o) {

    }
}
