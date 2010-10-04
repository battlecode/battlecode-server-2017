package battlecode.engine.scheduler;

import battlecode.engine.ErrorReporter;
import battlecode.engine.instrumenter.RobotDeathException;
import battlecode.engine.instrumenter.RobotMonitor;

import java.util.concurrent.locks.*;	// for ReentrantLock and Condition


/**
 * The Scheduler is a singleton class that allows threads to execute in a round-robin fashion.  When the Scheduler is running, the active
 * thread runs until it calls Scheduler.passToNextThread(), at which point the active thread waits or blocks until its next turn, and the
 * next thread in the list is activated.  If a thread is bound to a Scheduler, this is the only way it can become active.
 * <p>
 * To start the scheduler, a thread calls start().  The current thread is then automatically added to the Scheduler, and can then
 * call passToNextThread() to start the round-robin thread execution.  The thread that calls start() is somewhat special -- it is the
 * first thread to execute every round, and it is not allowed to remove itself from the Scheduler.
 *  <p>
 * To add a thread to the Scheduler, a thread simply calls addCurrentThread.  Calling addCurrentThread will
 * immediately put the active thread in the Scheduler, i.e., the call will block or wait until it is the thread's turn to be active.  This method of
 * adding threads can occur before or after the Scheduler has started.  The newly created thread is added at the end of the rotation,
 * i.e., it executes immediately before the thread that called start().
 * <p>
 * Sometimes, it is necessary to ensure that a newly created thread adds itself to the Scheduler before any other thread hand-offs occur.
 * In this case, an existing thread should call notifyAddingNewThread before the new thread is created.  This will force calls to
 * start and passToNextThread to yield until a new thread adds itself to the Scheduler.  When using notifyAddingNewThread in this way,
 * it is necessary that at most one thread is spawning new threads that add themselves to the Scheduler; otherwise, synchronization errors
 * might occur.
 * <p>
 * A thread can remove itself from the Scheduler by calling removeCurrentThread().  Once a thread has removed itself, it should not
 * make any calls to the Scheduler, other than the above sequence of calls to re-add the thread to the Scheduler.  Also note that
 * the thread that called start() cannot remove itself from the Scheduler.
 * <p>
 * The Scheduler associates a RobotData object to each thread in its rotation.  The thread that calls start() is automatically assigned
 * a RobotData with ID == -1.
 * <p>
 * 
 * @author adamd
 */
public class Scheduler {
	/*
	Implementation Notes:
	Scheduler stores a circular, doubly-linked list of Conditions, all derived from the same ReentrantLock.  Each Condition is associated with a
	thread -- when the thread calls passToNextThread(), it awaits on its Condition, and to wake up the thread, the Scheduler signals
	its Condition.
	The thread that called start() has a SyncObject that is the "head" of the doubly-linked list.
	*/
	
	
	// the lock used to maintain synchronization for all Scheduler calls.
	// all Conditions used in this Scheduler are also derived from this Lock
	private static Lock myLock;
		
	// the Scheduler maintains a circular doubly-linked list of Conditions that threads are awaiting.
	// head is the first Condition (corresponding to the thread that executes first every round)
	private static SyncObject head;
		
	// current points to the condition that the currently active thread usually awaits
	private static volatile SyncObject current = null;
		
	// whether a new thread is about to be added to the scheduler
	// when this is true, certain calls (e.g., passToNextThread) are blocked, until the new thread is actually added
	private static volatile boolean addingNewThread = false;
		
	private static volatile boolean isResetting = false;
		
	// the instance of this singleton
	private static final Scheduler theInstance = new Scheduler();

	// SyncObject is the basic element of the doubly linked list
	private static class SyncObject {
		public volatile SyncObject next;
		public volatile SyncObject prev;
		public final Condition cond;
		public final battlecode.engine.instrumenter.RobotMonitor.RobotData data;
			
		public SyncObject(SyncObject next, SyncObject prev, Condition cond, int ID) {
			this.next = next;
			this.prev = prev;
			this.cond = cond;
			data = new battlecode.engine.instrumenter.RobotMonitor.RobotData(ID);
		}
	}
	
	static {
		init();
	}
	
	// sole constructor -- enforces Singleton pattern
	private Scheduler() {
	}
	
	private static void init() {
		myLock = new ReentrantLock();
		
		// hook up the doubly linked list with only one element -- the head
		head = new SyncObject(null, null, myLock.newCondition(), -1);
		head.next = head;
		head.prev = head;
		current = null;
	}

	private static synchronized void syncWithMainMemory() {
		// don't need to do anything, synchronized keyword does it for us
	}
	
	// should only be called by master thread; removes all but master thread
	public static void reset() {
		while(addingNewThread)
			Thread.yield();
		
		if(current == null)
			return;
		
		isResetting = true;
		passToNextThread();
		isResetting = false;
		init();
	}
	
		
	/**
	 * Notifies the Scheduler that a new thread will be adding itself to the Scheduler soon.  Calling notifyAddingNewThread forces all
	 * calls to start or passToNextThread to yield, until a new thread adds itself to the Scheduler.
	 */
	public static void notifyAddingNewThread() {
		while(addingNewThread)
			Thread.yield();
		
		addingNewThread = true;
	}
	
	/**
	 * Adds the current thread to the Scheduler, so that the current thread will run in a round-robin fashion.  This call
	 * will block until the current thread's next turn in the schedule.
	 *
	 * @param ID the integer ID to associate with this thread.  This ID is returned by getCurrentThreadID when the current
	 * thread is running.  IDs are not required to be unique.
	 */
	public static void addCurrentThread(int ID) {
		
		SyncObject objectToAwait;
		myLock.lock();
		try{
			addingNewThread = false;
			
			objectToAwait = new SyncObject(head, head.prev, myLock.newCondition(), ID);
			head.prev.next = objectToAwait;
			head.prev = objectToAwait;

			try{
				while(current != objectToAwait)
					objectToAwait.cond.await();
			}catch(InterruptedException ie) {
				ErrorReporter.report("InterruptedException in addCurrentThread");
			}
			
			wakeUp();
			
		} finally {
			myLock.unlock();
		}
	}
	
	/**
	 * Removes the current thread from the Scheduler, so that it no longer runs in a round-robin fashion.  Once a thread is removed
	 * from the Scheduler, it should not make calls to the Scheduler, other than to re-add itself via notifyAddingNewThread and
	 * addCurrentThread.  Note that the thread that called start cannot remove itself from the Scheduler.
	 *
	 */
	public static void removeCurrentThread() {
		if(current == head) {
		//	ErrorReporter.report("can't remove master thread!");
		//	Thread.dumpStack();
			return;
		}
		myLock.lock();
		try{
			current.next.prev = current.prev;
			current.prev.next = current.next;
			current = current.next;
			current.cond.signalAll();
		} finally {
			myLock.unlock();
		}
	}
	
	public static void removeCurrentThreadWithHack() {
		if(current == head) {
		//	ErrorReporter.report("can't remove master thread!");
		//	Thread.dumpStack();
			return;
		}
		myLock.lock();
		try{
			current.next.prev = current.prev;
			current.prev.next = current.next;
			//current = current;
			current.cond.signalAll();
		} finally {
			myLock.unlock();
		}
	}
	
	/**
	 * Starts round-robin execution of the threads.  The call to start does not block; instead, the current thread runs its first turn
	 * and must call passToNextThread as usual to activate the next thread in the schedule.  The thread that calls start is assigned an
	 * ID of -1.
	 */
	public static void start() {
		// if start has already been called, no need to do anything
		if(current != null)
			return;
		
		while(addingNewThread)
			Thread.yield();
		
		current = head;
		wakeUp();
	}
	
	/**
	 * Stops the current thread's turn, and passes control to the next thread in the schedule.  This call will block the calling thread until
	 * that thread's next turn to execute, and meanwhile the next thread in the schedule will be activated.
	 */
	public static void passToNextThread() {
		
		// don't pass to the next thread if we're waiting to add a new thread first
		while(addingNewThread)
			Thread.yield();

		myLock.lock();

		try{		
			if(isResetting) {
				//debug("throwing RDE in passToNextThread while resetting");
				//throw new RobotDeathException();
				if (current != head)
					removeCurrentThread();
			}	else {
				RobotMonitor.endRunner();
				final SyncObject objectToAwait = current;
				current = current.next;
				current.cond.signalAll();
				try{
					// make absolutely sure everything gets written to
					// main memory
					syncWithMainMemory();
					while(current != objectToAwait) {
						objectToAwait.cond.await();
					//	if(current != objectToAwait)
					//		ErrorReporter.report("SPURIOUS WAKE");
					}
					// make absolutely sure everything gets read from
					// main memory
					syncWithMainMemory();
				}catch(InterruptedException ie) {
					ErrorReporter.report("InterruptedException in addCurrentThread");
				}

				wakeUp();
			}
		} finally {
			myLock.unlock();
		}

	}
	
	// a method that is called whenever a thread "wakes up"
	private static void wakeUp() {

		RobotMonitor.switchRunner(current.data);
	}

	/**
	 * This method is used to identify the current object so that
	 * various components can distinguish which runnable the
	 * scheduler is currently running.
	 *
	 * @return the ID associated with the current thread
	 */
	public static int getCurrentThreadID() {

		if (current == null)
			return -1;

		return current.data.ID;

	}

	public static boolean isResetting() {
		return isResetting;
	}
	
}
