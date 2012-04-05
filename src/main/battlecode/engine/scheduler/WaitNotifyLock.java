package battlecode.engine.scheduler;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * WaitNotifyLock is faster than ReentrantLock; it wastes about 3 percent of game time as
 * opposed to about 5 percent for ReentrantLock.  It is not well tested though.
 * <p/>
 * It is also useful for profiling in Netbeans.  (Netbeans profiler has an option for not
 * counting time spent in Object.wait(), but no such option for LockSupport.park().)
 */
class WaitNotifyLock implements Lock {

    private class WaitNotifyCondition implements Condition {

        public synchronized void await() throws InterruptedException {
            InterruptedException ex = null;
            unlock();
            try {
                wait();
            } catch (InterruptedException e) {
                ex = e;
            }
            lock();
            if (ex != null)
                throw ex;
        }

        public synchronized void signal() {
            notify();
        }

        public synchronized void signalAll() {
            notifyAll();
        }

        // we never use these functions
        public boolean await(long time, java.util.concurrent.TimeUnit unit) {
            return false;
        }

        public long awaitNanos(long nanosTimeout) {
            return 0;
        }

        public void awaitUninterruptibly() {
        }

        public boolean awaitUntil(java.util.Date deadline) {
            return false;
        }

    }

    Thread owner;
    int lockCount;

    public synchronized void lock() {
        while (owner != null && owner != Thread.currentThread()) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        owner = Thread.currentThread();
        lockCount++;
    }

    public synchronized void unlock() {
        lockCount--;
        if (lockCount <= 0) {
            owner = null;
            notify();
        }
    }

    public Condition newCondition() {
        return new WaitNotifyCondition();
    }

    // we never use these functions
    public void lockInterruptibly() {
    }

    public boolean tryLock() {
        return false;
    }

    public boolean tryLock(long time, java.util.concurrent.TimeUnit unit) {
        return false;
    }

}

