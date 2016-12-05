package battlecode.server;

import java.io.File;

public class Main {

    private static void runHeadless(Config options, String saveFile) {
        final Server server = new Server(
                options,
                false
        );
        final String teamA = options.get("bc.game.team-a");
        final String teamB = options.get("bc.game.team-b");
        final String[] maps = options.get("bc.game.maps").split(",");
        server.addGameNotification(new GameInfo(
                teamA, null,
                teamB, null,
                maps,
                new File(options.get("bc.server.save-file")),
                false
        ));
        server.terminateNotification();

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
