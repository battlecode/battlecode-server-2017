package battlecode.engine.scheduler;

import battlecode.engine.ErrorReporter;
import battlecode.engine.instrumenter.RobotMonitor;

import java.util.concurrent.locks.LockSupport;


public class Scheduler {

    public static class ScheduledThread {
        volatile ScheduledThread next;
        volatile ScheduledThread prev;
        Thread thread;
        volatile boolean started;
        RobotMonitor.RobotData data;

        private ScheduledThread(Thread thread, RobotMonitor.RobotData data) {
            this.thread = thread;
            this.data = data;
            started = false;
        }
    }

    private volatile static ScheduledThread head, current;

    static {
        head = new ScheduledThread(null, new RobotMonitor.RobotData(-1));
        head.next = head;
        head.prev = head;
        current = head;
    }

    public static void start() {
        head.thread = Thread.currentThread();
    }

    public static void reset() {
        if (head.next != head) {
            ErrorReporter.report("Failed to clean up all threads");
        }
    }

    /**
     * Adds a new thread to the scheduler.
     */
    public static void add(Thread t, int ID) {
        ScheduledThread st = new ScheduledThread(t, new RobotMonitor.RobotData(ID));
        ScheduledThread last = head.prev;
        last.next = st;
        st.prev = last;
        head.prev = st;
        st.next = head;
    }

    /**
     * Removes the current thread from the scheduler.
     * Should be called right before the thread exits.
     * n
     */
    public static void die() {
        ScheduledThread last = current.prev;
        current = current.next;
        current.prev = last;
        last.next = current;
        wakeupNext();
    }

    /**
     * Wakes up the next thread.  Should be followed by a
     * call to endTurn().
     */
    private static void wakeupNext() {
        LockSupport.unpark(current.thread);
    }

    /**
     * Ends this thread's turn, but does not wake up the next thread.
     * New robot threads should call this immediately after starting.
     */
    public static void endTurn() {
        // we need do-while rather than while in case the current thread
        // is also the next thread
        do {
            LockSupport.park();
        } while (current.thread != Thread.currentThread());
        RobotMonitor.switchRunner(current.data);
    }

    //static private volatile long enterTime;
    //static public volatile long timeInScheduler;

    /**
     * Ends this thread's turn and wakes up the next thread.
     */
    public static void passToNextThread() {
        //enterTime = System.nanoTime();
        startNextThread();
        endTurn();
        //timeInScheduler+=System.nanoTime()-enterTime;
    }

    /**
     * Starts the next thread without parking this one.  Used
     * by the engine so it can write the match to disk while
     * robots are running.
     */
    public static void startNextThread() {
        current = current.next;
        wakeupNext();
    }

}
