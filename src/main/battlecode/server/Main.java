package battlecode.server;

import battlecode.server.controller.Controller;
import battlecode.server.controller.HeadlessController;
import battlecode.server.controller.InputStreamController;
import battlecode.server.proxy.FileProxy;
import battlecode.server.proxy.OutputStreamProxy;
import battlecode.server.proxy.Proxy;
import battlecode.server.serializer.JavaSerializerFactory;
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
