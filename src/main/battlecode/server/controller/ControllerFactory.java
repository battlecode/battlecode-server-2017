package battlecode.server.controller;

import battlecode.server.Config;
import battlecode.server.proxy.Proxy;
import battlecode.server.proxy.XStreamProxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * Produces controller instances from different control data sources.
 */
public class ControllerFactory {

    /**
     * Creates a controller that operates over a socket.
     *
     * @return a Controller instance for handling data from the socket source
     */
    public static Controller createTCPController(InputStream input, Config options) throws IOException {
        if (Boolean.parseBoolean(options.get("bc.server.output-xml")))
            return new TCPController(XStreamProxy.getXStream().createObjectInputStream(input));
        else
            return new TCPController(new ObjectInputStream(input));
    }

    /**
     * Creates a controller that feeds match parameters from a configuration
     * file or the command line.
     *
     * @param options command-line/configuration file options
     * @return a Controller instance for handling data from a config file
     */
    public static Controller createHeadlessController(Config options) {
        return new HeadlessController(options);
    }

    public static Controller createLocalController(Config options, Proxy proxy) {
        return new LocalController(options, proxy);
    }
}
