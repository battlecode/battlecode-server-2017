package battlecode.server;

import java.io.IOException;

public class Main {

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
