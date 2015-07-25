package battlecode.server.controller;

import battlecode.server.Server;
import battlecode.server.serializer.Serializer;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is used to provide a means of controlling the server through a standard InputStream.
 * The input stream could be stdin, a file, a socket, whatever.
 */
public final class InputStreamController extends Controller {

    protected final InputStream input;
    protected final Serializer serializer;

    /**
     * The runnable that listens for input.
     */
    private final InputStreamListener listener;

    /**
     * The thread on which to listen for data.
     */
    private final Thread listenerThread;

    /**
     * Creates a new InputStreamController using the given properties.
     */
    public InputStreamController(final InputStream input, final Serializer serializer) {
        this.input = input;
        this.serializer = serializer;
        this.listener = new InputStreamListener();
        this.listenerThread = new Thread(listener);
        this.listenerThread.setDaemon(true);
    }

    /**
     * Sets up a server socket and a listener thread.
     * <p/>
     * {@inheritDoc}
     */
    public void start() throws IOException {
        // Start listening on a new thread.
        this.listenerThread.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Terminates the listener thread and closes the server socket.
     * <p/>
     * {@inheritDoc}
     */
    public void finish() throws IOException {
        listener.terminate();
    }

    /**
     * A Runnable that listens for data from the controller and calls the
     * appropriate methods to handle the data (namely Server.update()).
     */
    private class InputStreamListener implements Runnable {
        /**
         * Whether or not the controller is listening.
         */
        private boolean isRunning = true;

        /**
         * Closes the connection and stops listening for control data.
         *
         * @throws IOException if the connection couldn't be closed
         */
        public void terminate() throws IOException {
            isRunning = false;

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }

            input.close();
        }

        /**
         * Listens for objects on the socket in a loop, notifying this
         * controller's observers each time one is received.
         */
        public void run() {
            while (isRunning) {
                try {
                    final Object data = serializer.deserialize(input);

                    setChanged();
                    notifyObservers(data);
                    clearChanged();
                } catch (IOException e) {
                    if (isRunning)
                        Server.error("error getting data from client: " + e.getMessage());
                }
            }
        }
    }
}
