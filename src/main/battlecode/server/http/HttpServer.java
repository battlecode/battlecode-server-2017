package battlecode.server.http;

import battlecode.server.controller.Controller;
import battlecode.server.controller.PassthroughController;
import battlecode.server.proxy.WebSocketProxy;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;

/**
 * Handles serving local client files and multiplexing game events to client websockets.
 * Where the Java ends.
 *
 * Note: If this looks hella complicated, that's just Netty. The actual server structure is pretty simple.
 * The server accepts connections on ports 80 and 6370.
 * When something connects, Netty creates a new "Channel" - essentially a pipeline of things happening to incoming and
 * outgoing messages. On :80, this pipeline will be initialized with {@link HttpStaticFileServerInitializer}. On :6370,
 * {@link WebSocketServerInitializer}
 * Our http pipeline might look like:
 * -->raw bytes--->[HttpServerCodec]--->http message--->[HttpStaticFileServerHandler]
 * <--raw bytes<---[HttpServerCodec]<---http message<---[HttpStaticFileServerHandler]
 * Events on that HTTP channel will pass through the pipeline.
 *
 * (Our websocket pipeline is more complex.)
 *
 * Note: DON'T use file server in production!! (i.e. on servers; on users' computers it's fine.)
 * Websockets are probably okay, but the file server always gives the user whatever they want, without question.
 * Plus, we should already have static file serving infrastructure in production.
 *
 * Created by james on 7/18/15.
 */
public class HttpServer {
    final private NioEventLoopGroup boss;
    final private NioEventLoopGroup worker;
    //final private SslContext ssl;

    /**
     * A set of channels representing active websocket connections to the server.
     */
    final public ChannelGroup activeWebSockets;
    /**
     * The directory to serve client files from.
     */
    final public String staticFileDirectory;

    final public WebSocketProxy webSocketProxy;
    final public PassthroughController webSocketController;

    final private static int HTTP_PORT = 6370;
    final private static int WEB_SOCKET_PORT = 6371;

    /**
     * Spin up the Netty event loop and start serving things.
     *
     * @param staticFileDirectory the directory to serve files from;
     * @throws IOException, CertificateException
     */
    public HttpServer(final String staticFileDirectory) throws IOException, CertificateException {

        // The groups of threads that I/O runs on
        this.boss = new NioEventLoopGroup(1); // Dispatcher thread
        this.worker = new NioEventLoopGroup(); // Worker threads

        // A self-signed ssl certificate.
        // Mandatory; no reason not to have it.
        /*final SelfSignedCertificate ssc = new SelfSignedCertificate();
        this.ssl = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
                .sslProvider(SslProvider.JDK).build();*/

        this.activeWebSockets = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        this.webSocketProxy = new WebSocketProxy();
        this.webSocketController = new PassthroughController();

        if (staticFileDirectory == null) {
            // Don't serve files
            this.staticFileDirectory = null;
        } else {
            // *Do* serve files
            final File f = new File(staticFileDirectory);
            if (!f.isDirectory()) {
                throw new IOException("Invalid static file directory: "+f.getPath());
            }
            this.staticFileDirectory = f.getAbsolutePath();

            // A builder for a netty server
            final ServerBootstrap staticFileBootstrap = new ServerBootstrap()
                    // Use our event loops
                    .group(this.boss, this.worker)
                    // Use this type of channel, since the examples do
                    .channel(NioServerSocketChannel.class)
                    // Use logging
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // Initialize new channels a file-serving pipeline
                    .childHandler(new HttpStaticFileServerInitializer(null, this.staticFileDirectory));
            // Bind on :80
            final ChannelFuture serverSetupFuture = staticFileBootstrap.bind(HTTP_PORT);

        }

        // Always serve websockets
        final ServerBootstrap webSocketBootstrap = new ServerBootstrap()
                .group(this.boss, this.worker)
                .channel(NioServerSocketChannel.class)
                .handler((new LoggingHandler(LogLevel.INFO)))
                // New websocket connections will be added to our group;
                // They'll be automatically removed when closed.
                .childHandler(new WebSocketServerInitializer(null,
                        this.activeWebSockets,
                        this.webSocketProxy,
                        this.webSocketController));
        // Bind on :6370
        final ChannelFuture serverSetupFuture = webSocketBootstrap.bind(WEB_SOCKET_PORT);
    }
}
