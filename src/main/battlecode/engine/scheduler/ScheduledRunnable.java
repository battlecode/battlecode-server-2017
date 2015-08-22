package battlecode.engine.scheduler;

import battlecode.engine.ErrorReporter;

/**
 * ScheduledRunnable is a wrapper for a Runnable, that runs the embedded
 * Runnable in a new thread that is locked into the Scheduler.
 *
 * @author adamd
 */
public class ScheduledRunnable implements Runnable {
    private final int myID; // the runnable's ID
    private final Runnable myRunnable;

    /**
     * Creates and runs a new thread that is locked into the Scheduler and runs
     * the given Runnable.
     *
     * @param r
     *            the Runnable to be run in the Scheduler
     * @param ID
     *            the integer ID that the Scheduler will associate with the
     *            given Runnable's thread
     */
    public ScheduledRunnable(Runnable r, int ID) {
        myID = ID;
        myRunnable = r;
        Thread t = new Thread(this, "robot " + ID);
        t.setDaemon(true);
        Scheduler.add(t, ID);
        t.start();
    }

    /**
     * This method is automatically called by the ScheduledRunnable constructor,
     * and should NOT be called by the client.
     */
    public void run() {

        try {

            myRunnable.run();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorReporter.report("Unexpected exception in ScheduledRunnable: " + e.getMessage());
        } finally {
            Scheduler.die();
        }
    }
}
