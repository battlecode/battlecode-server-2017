package battlecode.server;

import battlecode.server.http.HttpServer;
import battlecode.server.proxy.FileProxy;
import battlecode.server.proxy.Proxy;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Where the magic begins.
 *
 * Unless you're running the java client, in which case it begins in {@link battlecode.client.Main}.
 */
public class Main {

    /**
     * Runs the server such that it can connect to the browser.
     *
     * @param options options to run the server with
     */
    private static void runWebSocket(final Config options) {
        try {
            final HttpServer httpServer = new HttpServer(options.get("bc.html5client.client-directory"));

            final Proxy jsonFileProxy = new FileProxy("match.json", false) {
                final ObjectMapper mapper = new ObjectMapper();
                @Override
                public void writeObject(final Object o) throws IOException {
                    if (!mapper.canSerialize(o.getClass())) {
                        throw new IOException("Can't serialize object of class "+o.getClass().getCanonicalName());
                    }
                    mapper.writeValue(this.output, o);
                    mapper.writeValue(System.out, o);
                }
            };

            final Server server = new Server(options,
                    Server.Mode.HEADLESS,
                    httpServer.webSocketController,
                    httpServer.webSocketProxy,
                    jsonFileProxy
                    );
            server.run();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static void runHeadless(Config options, String saveFile) {
        try {
            Server server = ServerFactory.createHeadlessServer(options,
                    saveFile);
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runTCP(Config options, String saveFile) {

        int port = options.getInt("bc.server.port");

        try {
            Server server = ServerFactory.createRemoteServer(options, port,
                    saveFile);
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runPipe(Config options, String saveFile) {

        try {
            Server server = ServerFactory.createPipeServer(options,
                    saveFile);
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Config setupConfig(String[] args) {
        try {
            Config options = new Config(args);
            Config.setGlobalConfig(options);
            return options;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(64);
            return null;
        }
    }

    public static boolean run(Config options) {
        final Server.Mode mode = Server.Mode.valueOf(options.get("bc.server.mode").toUpperCase());

        String saveFile = options.get("bc.server.save-file");

        switch (mode) {
            case HEADLESS:
                runHeadless(options, saveFile);
                break;
            case TCP:
                runTCP(options, saveFile);
                break;
            case PIPE:
                runPipe(options, saveFile);
                break;
            case WEBSOCKET:
                runWebSocket(options);
                break;
            default:
                return false;
        }

        return true;
    }

    public static void main(String[] args) {

        final Config options = setupConfig(args);

        if (!run(options)) {
            System.err.println("invalid bc.server.mode");
            System.exit(64);
        }

    }
}
