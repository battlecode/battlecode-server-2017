package battlecode.server.controller;

import battlecode.server.Server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

/**
 * This class is used to provide a means of controlling the server over TCP. New
 * matches are started using a request packet, and matches are stopped and start
 * using control bytes. See Protocol for definitions of these bytes.
 */
class TCPController extends Controller {

    protected ObjectInputStream input = null;

    /**
     * The runnable that listens for TCP data.
     */
    private TCPControllerListener listener;

    /**
     * The thread on which to listen for data from the client.
     */
    private Thread listenerThread;

    /**
     * Creates a new TCPController using the given properties.
     *
     * @param socket the client socket to listen on
     */
    TCPController(ObjectInputStream input) {
        this.input = input;
    }

    /**
     * Sets up a server socket and a listener thread.
     * <p/>
     * {@inheritDoc}
     */
    public void start() throws IOException {

        // Start listening on a new thread.
        this.listener = new TCPControllerListener(input);
        this.listenerThread = new Thread(listener);
        this.listenerThread.setDaemon(true);
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
    public class TCPControllerListener implements Runnable {

        /**
         * The input stream for deserializing objects from the network.
         */
        private ObjectInput input;

        /**
         * Whether or not the controller is listening.
         */
        private boolean isRunning = true;

        /**
         * Creates a listener on the given socket.
         *
         * @param socket the socket to listen for objects on
         * @throws IOException if the socket couldn't be used for listening
         */
        public TCPControllerListener(ObjectInputStream input) throws IOException {
            this.input = input;
        }

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
                    Object data = input.readObject();
                    //System.out.println(data);
                    //System.out.println(battlecode.server.proxy.XStreamProxy.getXStream().toXML(data));
                    setChanged();
                    notifyObservers(data);
                    clearChanged();
                } catch (IOException e) {
                    if (isRunning)
                        Server.error("error getting data from client: "
                                + e.getMessage());
                } catch (ClassNotFoundException e) {
                    Server.error("error getting data from client: "
                            + e.getMessage());
                }
            }
        }
    }
}
