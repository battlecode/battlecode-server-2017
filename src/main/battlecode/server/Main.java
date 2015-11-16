package battlecode.server;

import battlecode.server.controller.Controller;
import battlecode.server.controller.HeadlessController;
import battlecode.server.controller.InputStreamController;
import battlecode.server.proxy.FileProxy;
import battlecode.server.proxy.OutputStreamProxy;
import battlecode.server.proxy.Proxy;
import battlecode.server.serializer.JavaSerializerFactory;
import battlecode.server.serializer.Serializer;
import battlecode.server.serializer.SerializerFactory;
import battlecode.server.serializer.XStreamSerializerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class Main {

    private static void runHeadless(Config options, String saveFile) {
        try {
            final Controller controller = new HeadlessController(options);

            final SerializerFactory serializerFactory;
            if (options.getBoolean("bc.server.output-xml")) {
                serializerFactory = new XStreamSerializerFactory();
            } else {
                serializerFactory = new JavaSerializerFactory();
            }

            final Proxy proxy = new FileProxy(saveFile, serializerFactory);

            final Server server = new Server(options, Server.Mode.HEADLESS, controller, proxy);

            controller.addObserver(server);

            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runTCP(Config options, String saveFile) {

        int port = options.getInt("bc.server.port");

        try {
            RPCServer rpcServer;
            Thread rpcThread;

            final MatchInputFinder finder = new MatchInputFinder();

            // Start a new RPC server for handling match input requests.
            rpcServer = new RPCServer() {
                public Object handler(Object arg) {
                    if ("find-match-inputs".equals(arg))
                        return finder.findMatchInputsLocally();
                    return null;
                }
            };

            // Run it in a new thread.
            rpcThread = new Thread(rpcServer);
            rpcThread.setDaemon(true);
            rpcThread.start();

            // Start a server socket listening on the default port.
            ServerSocket serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();
            // serverSocket.close(); (?)

            final SerializerFactory serializerFactory;
            if (options.getBoolean("bc.server.output-xml")) {
                serializerFactory = new XStreamSerializerFactory();
            } else {
                serializerFactory = new JavaSerializerFactory();
            }

            Controller controller = new InputStreamController(clientSocket.getInputStream(), serializerFactory);

            List<Proxy> proxies = new LinkedList<Proxy>();

            if (saveFile != null)
                proxies.add(new FileProxy(saveFile, serializerFactory));

            proxies.add(new OutputStreamProxy(serializerFactory, clientSocket.getOutputStream()));

            Server server = new Server(options, Server.Mode.TCP, controller,
                    proxies.toArray(new Proxy[proxies.size()]));
            controller.addObserver(server);

            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runPipe(Config options, String saveFile) {
        try {
            final SerializerFactory serializerFactory;
            if (options.getBoolean("bc.server.output-xml")) {
                serializerFactory = new XStreamSerializerFactory();
            } else {
                serializerFactory = new JavaSerializerFactory();
            }

            Controller controller = new InputStreamController(System.in, serializerFactory);

            List<Proxy> proxies = new LinkedList<Proxy>();

            if (saveFile != null)
                proxies.add(new FileProxy(saveFile, serializerFactory));

            proxies.add(new OutputStreamProxy(serializerFactory, System.out));

            // since we're sending the match file to System.out, don't send log
            // messages there
            System.setOut(System.err);

            Server server = new Server(options, Server.Mode.TCP, controller,
                    proxies.toArray(new Proxy[proxies.size()]));
            controller.addObserver(server);

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
