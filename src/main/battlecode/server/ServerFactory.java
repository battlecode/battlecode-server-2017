package battlecode.server;

import battlecode.server.controller.Controller;
import battlecode.server.controller.ControllerFactory;
import battlecode.server.proxy.Proxy;
import battlecode.server.proxy.ProxyFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class ServerFactory {

    public static Server createLocalServer(Config options, Proxy proxy,
                                           String saveFile) throws IOException {

        Controller controller = ControllerFactory.createLocalController(options, proxy);

        List<Proxy> proxies = new LinkedList<Proxy>();
        if (saveFile != null)
            proxies.add(ProxyFactory.createProxyFromFile(saveFile));

        proxies.add(proxy);

        Server server = new Server(options, Server.Mode.LOCAL, controller,
                proxies.toArray(new Proxy[0]));

        controller.addObserver(server);

        return server;
    }

    public static Server createHeadlessServer(Config options, String saveFile)
            throws IOException {

        Controller controller = ControllerFactory
                .createHeadlessController(options);

        Proxy[] proxies = new Proxy[]{ProxyFactory
                .createProxyFromFile(saveFile)};

        Server server = new Server(options, Server.Mode.HEADLESS, controller,
                proxies);
        controller.addObserver(server);

        return server;
    }

    public static Server createRemoteServer(Config options, int port,
                                            String saveFile) throws IOException {

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

        Controller controller = ControllerFactory
                .createTCPController(clientSocket.getInputStream(), options);

        List<Proxy> proxies = new LinkedList<Proxy>();

        if (saveFile != null)
            proxies.add(ProxyFactory.createProxyFromFile(saveFile));

        proxies.add(ProxyFactory.createProxy(clientSocket.getOutputStream()));

        Server server = new Server(options, Server.Mode.TCP, controller,
                proxies.toArray(new Proxy[proxies.size()]));
        controller.addObserver(server);
        return server;
    }

    public static Server createPipeServer(Config options,
                                          String saveFile) throws IOException {

        Controller controller = ControllerFactory
                .createTCPController(System.in, options);

        List<Proxy> proxies = new LinkedList<Proxy>();

        if (saveFile != null)
            proxies.add(ProxyFactory.createProxyFromFile(saveFile));

        proxies.add(ProxyFactory.createProxy(System.out));

        // since we're sending the match file to System.out, don't send log
        // messages there
        System.setOut(System.err);

        Server server = new Server(options, Server.Mode.TCP, controller,
                proxies.toArray(new Proxy[proxies.size()]));
        controller.addObserver(server);
        return server;
    }

}
