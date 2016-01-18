package battlecode.server;

import battlecode.serial.notification.GameNotification;
import battlecode.serial.notification.TerminateNotification;
import battlecode.server.proxy.FileProxy;
import battlecode.server.proxy.Proxy;
import battlecode.serial.serializer.JavaSerializerFactory;
import battlecode.serial.serializer.SerializerFactory;
import battlecode.serial.serializer.XStreamSerializerFactory;

import java.io.File;
import java.io.IOException;

public class Main {

    private static void runHeadless(Config options, String saveFile) {
        final SerializerFactory serializerFactory;
        if (options.getBoolean("bc.server.output-xml")) {
            serializerFactory = new XStreamSerializerFactory();
        } else {
            serializerFactory = new JavaSerializerFactory();
        }

        final Server server = new Server(
                options,
                false,
                new FileProxy.Factory(serializerFactory)
        );

        new GameNotification(new GameInfo(
                options.get("bc.game.team-a"),
                null,
                options.get("bc.game.team-b"),
                null,
                options.get("bc.game.maps").split(","),
                new File(saveFile),
                false
        )).accept(server);

        new TerminateNotification().accept(server);

        server.run();
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
