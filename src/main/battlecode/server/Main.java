package battlecode.server;

import java.io.File;
import java.net.URL;

public class Main {

    private static void runHeadless(Config options, String saveFile) {
        try {
            final Server server = new Server(
                    options,
                    false
            );

            final String teamA = options.get("bc.game.team-a");
            final URL teamAClasses;
            if (options.get("bc.game.team-a.classes") != null) {
                teamAClasses = new URL(options.get("bc.game.team-a.classes"));
            } else {
                teamAClasses = null;
            }
            final String teamAPackage;
            if (options.get("bc.game.team-a.package") != null) {
                teamAPackage = options.get("bc.game.team-a.package");
            } else {
                teamAPackage = teamA;
            }

            final String teamB = options.get("bc.game.team-b");
            final URL teamBClasses;
            if (options.get("bc.game.team-b.classes") != null) {
                teamBClasses = new URL(options.get("bc.game.team-b.classes"));
            } else {
                teamBClasses = null;
            }
            final String teamBPackage;
            if (options.get("bc.game.team-b.package") != null) {
                teamBPackage = options.get("bc.game.team-b.package");
            } else {
                teamBPackage = teamB;
            }

            final String[] maps = options.get("bc.game.maps").split(",");
            server.addGameNotification(new GameInfo(
                    teamA, teamAPackage, teamAClasses,
                    teamB, teamBPackage, teamBClasses,
                    maps,
                    new File(options.get("bc.server.save-file")),
                    options.getBoolean("bc.game.best-of-three") && maps.length == 3
            ));
            server.terminateNotification();

            server.run();
        } catch (Exception e) {
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
