package battlecode.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A utility class for receiving remote method calls.
 */
public abstract class RPCServer implements Runnable {

    /**
     * The socket to listen on.
     */
    private final ServerSocket socket;

    /**
     * The default port to listen on.
     */
    private static final int DEFAULT_PORT = 12370;

    /**
     * Creates an RPC server that listens on the default port.
     *
     * @throws IOException if there was a problem listening on the port
     */
    public RPCServer() throws IOException {
        this(DEFAULT_PORT);
    }

    /**
     * Creates an RPC server that listens on the given port.
     *
     * @param port the port to listen on
     * @throws IOException if there was a problem listening on the port
     */
    public RPCServer(int port) throws IOException {
        socket = new ServerSocket(port);
    }

    /**
     * Processes the argument from the RPC client.
     *
     * @param arg the argument sent by the client
     * @return the result of the method call to be passed back to the client
     */
    public abstract Object handler(Object arg);

    /**
     * Runs the server. This method listens for incoming connections
     * continually. When it gets one, it waits for an input argument, passes
     * it to handler(), and returns the result to the client.
     * <p/>
     * Note: in this RPC implementation, the client always terminates the
     * connection.
     */
    public void run() {
        Socket s;
        try {
            while ((s = socket.accept()) != null) {

                // Use Java serialization.
                ObjectInputStream input = new ObjectInputStream(s.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(s.getOutputStream());

                // Get the result.
                Object result = handler(input.readObject());

                // Write the result.
                output.writeObject(result);
                output.reset();
                output.flush();
            }
        } catch (IOException e) {
            Server.error("RPC server couldn't run: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            Server.error("RPC server couldn't run: " + e.getMessage());
        }
    }
}
