package battlecode.server.proxy;

import battlecode.serial.ServerEvent;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Class that handles proxy I/O on a new thread.
 * Create with a list of proxies to write to, call enqueue()
 * to enqueue a new message to be written, call terminate()
 * to finish writing all enqueued messages and close the thread.
 * <p>
 * Messages are guaranteed to be written in the order enqueued.
 * <p>
 * No methods may be called after terminate() is called.
 */
public class ProxyWriter {
    /**
     * The number of spaces to have in the queue.
     *
     * Lots of space, most probably won't be used.
     */
    private static final int QUEUE_SIZE = 512;

    /**
     * The queue in which to store incoming messages.
     * Has operation take(), which blocks until it can
     * pull from the queue, and put(), which blocks until
     * there is space on the queue to store an object.
     */
    private final BlockingQueue<ServerEvent> writeQueue;

    /**
     * The worker thread.
     */
    private final Thread workThread;

    /**
     * The proxies to write to.
     */
    private final Proxy[] proxies;

    /**
     * The object enqueued to shut down the worker thread.
     */
    private final ServerEvent poisonPill;

    /**
     * Whether to write debug output to stdout.
     */
    private final boolean debug;

    // Worker thread pulls objects from the queue and
    // writes them to all proxies until it encounters
    // a "poison pill" object, and then shuts down.

    /**
     * Create a new proxy writer.
     *
     * @param proxies the proxies to write to.
     * @param debug   whether to print debug info to stdout.
     */
    public ProxyWriter(final Proxy[] proxies, final boolean debug) {
        this.proxies = proxies;
        this.debug = debug;
        this.writeQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
        this.poisonPill = new ServerEvent() {};
        this.workThread = new Thread(this::work);

        workThread.start();

        for (Proxy proxy : this.proxies) {
            debug("using proxy: "+proxy);
        }
    }

    /**
     * The main loop of the worker thread.
     */
    private void work() {
        debug("worker thread started");
        while (true) {
            final ServerEvent next;
            try {
                // Block until element is available
                next = writeQueue.take();
            } catch (final InterruptedException e) {
                debug("interrupted, shutting down");
                return;
            }

            if (next == poisonPill) {
                debug("terminated");
                // Kill the thread
                return;
            }

            for (final Proxy proxy : proxies) {
                try {
                    proxy.writeEvent(next);
                } catch (final IOException e) {
                    debug("couldn't write to proxy " + proxy + ": " + e.getMessage());
                    e.printStackTrace();
                } catch (final Exception e) {
                    // Keep this thread alive at all costs!

                    debug("encountered unexpected exception writing to proxy "
                            + proxy + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Enqueue a message to be written to proxies at some later time.
     * May block if there is no more space in the message queue.
     * May not be called after terminate() is called.
     *
     * @param message the message to send.
     */
    public synchronized void enqueue(final ServerEvent message) {
        assert workThread.isAlive();

        try {
            writeQueue.put(message);
        } catch (final InterruptedException e) {
            debug("interrupted while enqueueing object");
        }
    }

    /**
     * Shut down the worker thread, after sending all pending messages.
     * Blocks until messages are sent.
     * Closes all proxies.
     */
    public synchronized void terminate() {
        enqueue(poisonPill);

        try {
            workThread.join();
        } catch (final InterruptedException e) {
            // Shutting down, nothing to do
            debug("interrupted before worker thread could be joined");
        }

        for (Proxy proxy : proxies) {
            try {
                proxy.close();
            } catch (IOException e) {
                debug("couldn't close proxy: "+proxy+": "+e.getMessage());
            }
        }
    }

    private void debug(final String s) {
        if (debug) {
            System.err.println("[ProxyWriter:debug] "+s);
        }
    }
}
