package battlecode.engine.instrumenter;

import static battlecode.common.GameConstants.BYTECODES_PER_ROUND;
import static battlecode.common.GameConstants.STACK_SIZE_LIMIT;

import java.util.HashSet;
import java.util.Set;

import battlecode.engine.ErrorReporter;
import battlecode.engine.scheduler.Scheduler;
import battlecode.engine.GenericRobot;
import battlecode.engine.GenericWorld;
import battlecode.engine.instrumenter.lang.RoboPrintStream;
import battlecode.engine.instrumenter.lang.SilencedPrintStream;
import battlecode.server.Config;

/**
 * RobotMonitor is a singleton class for monitoring robots' bytecode execution and stack size, and killing robots' threads.  Player's classes should
 * be instrumented so that they make calls to RobotMonitor as appropriate.
 * 
 * @author adamd
 */
public class RobotMonitor {
	public static int bytecodeCtr = 0;
	
	public static int currentBytecodeLimit = 0;
	
	private static final int LARGE_NEG_NUMBER = -100000;
		
	private static final Set<Integer> robotsToKill = new HashSet<Integer>();
		
	// the RobotData for the currently running robot
	public volatile static RobotData currentRobotData;
	
	private static boolean [] silenced = new boolean [2];
	
	private static int currDebugLevel = 0;
	
	private static GenericWorld myGameWorld = null;


	/** A "struct" that holds data about a robot's execution, e.g., bytecodes, stack size, etc. */
	public static class RobotData {
		public int currentBytecodeLimit = BYTECODES_PER_ROUND;
		public int stackCheckCtr = 0;
		public int debugLevel = 0;
		public final int ID;
		public boolean thrownRobotDeathException = false;
		
		public RobotData(int ID) {
			this.ID = ID;
		}
	}
	
	// private constructor enforces the singleton pattern
	private RobotMonitor() {
	}

	private static void init() {
		bytecodeCtr = 0;
		robotsToKill.clear();
		Config options = Config.getGlobalConfig();
		silenced[0] = options.getBoolean("bc.engine.silence-a");
		silenced[1] = options.getBoolean("bc.engine.silence-b");
	}
	
	/** Resets the internal state of the RobotMonitor.  Should be called between games. */
	public static void reset() {
		init();
	}
	
	/**
	 * Ends the run of the currently active robot.
	 */
	public static void endRunner() {
		// first, update the bytecode limit for the current robot's next round
		if(currentRobotData != null) {
			currentRobotData.debugLevel = currDebugLevel;
			if(bytecodeCtr <= currentRobotData.currentBytecodeLimit)
				currentRobotData.currentBytecodeLimit = BYTECODES_PER_ROUND;
			else
				currentRobotData.currentBytecodeLimit += BYTECODES_PER_ROUND - bytecodeCtr;
		}
	}		
			
	/**
	 * Switches the currently active robot to the one referred to by the given RobotData.
	 */
	public static void switchRunner(RobotData newData) {
		
		// switch RobotData to the new robot
		currentRobotData = newData;

		if(robotsToKill.contains(newData.ID)) {
			//System.out.println("Killing "+newData.ID);
			throw new RobotDeathException();
		}
		
		currDebugLevel = currentRobotData.debugLevel;
		currentBytecodeLimit = currentRobotData.currentBytecodeLimit;
		if(currDebugLevel > 0)
			bytecodeCtr = LARGE_NEG_NUMBER;
		else
			bytecodeCtr = 0;

		if(newData.ID>=0) {
			GenericRobot robot = myGameWorld.getRobotByID(newData.ID);
			if(silenced[robot.getTeam().ordinal()]) {
				battlecode.engine.instrumenter.lang.System.out = SilencedPrintStream.theInstance();
			}
			else {
				RoboPrintStream stream = RoboPrintStream.theInstance();
				stream.changeRobot();
				battlecode.engine.instrumenter.lang.System.out = stream;
			}
		}
		myGameWorld.beginningOfExecution(newData.ID);
	}		
	
	public static GenericRobot getCurrentRobot() {
		return myGameWorld.getRobotByID(currentRobotData.ID);
	}

	/**
	 * Increments the active robot's debug level.  Should be called at the beginning of any debug method.
	 */
	public static void incrementDebugLevel() {
		if(currDebugLevel == 0) {
			currentRobotData.currentBytecodeLimit -= bytecodeCtr;
			currentBytecodeLimit = currentRobotData.currentBytecodeLimit;
			bytecodeCtr = LARGE_NEG_NUMBER;
		}
		currDebugLevel++;
	}
	
	/**
	 * Decrements the active robot's debug level.  Should be called at the end of any debug method.
	 */
	public static void decrementDebugLevel() {
		currDebugLevel--;
		if(currDebugLevel == 0) {
			bytecodeCtr = 0;
		}
	}
	
	/**
	 * Increments the currently active robot's bytecode count by the given amount.  If the robot exceeds its bytecode limit for the round,
	 * this method will block until the robot's next round.  Should be called at the end of every basic block.
	 *
	 * @param numBytecodes the number of bytecodes the robot just executed
	 */
	public static final void incrementBytecodes(int numBytecodes) {
		bytecodeCtr += numBytecodes;

		while(bytecodeCtr >= currentBytecodeLimit) {
			myGameWorld.endOfExecution(currentRobotData.ID);
			Scheduler.passToNextThread();
		}
	}
	
	/**
	 * Monitors the stack size of the currently active robot.  If the robot is exceeding the stack size limit, it dies.  Should
	 * be called at the beginning of every method.
	 */
	public static void monitorStackSize() {
		if(++currentRobotData.stackCheckCtr == 50) {
			currentRobotData.stackCheckCtr = 0;
			if(Thread.currentThread().getStackTrace().length > STACK_SIZE_LIMIT) {
				ErrorReporter.report("[Engine] Robot " + Scheduler.getCurrentThreadID() + " exceeding stack size limit.  See stack trace below.", false);
				Thread.currentThread().dumpStack();
				throw new RobotDeathException();
			}
		}
	}
			
	/**
	 * Kills the robot thread of the robot with the given ID.  More specifically, the next time the thread is activated,
	 * it will throw a RobotDeathException.
	 * @param robotID the integer ID of the robot to kill
	 */
	public static void killRobot(int robotID) {
		robotsToKill.add(robotID);
	}
	
	/**
	 * Returns the bytecode number that the active robot is currently on.  Note that this can be above BYTECODES_PER_ROUND in some cases.
	 */
	public static int getBytecodeNum() {
		if(currDebugLevel == 0)
			return BYTECODES_PER_ROUND - currentBytecodeLimit + bytecodeCtr;
		else
			return BYTECODES_PER_ROUND - currentBytecodeLimit;
	}
	
	/**
	 * Notifies the RobotMonitor of what the current GameWorld is.  Should be called before the start of each game.
	 */
	public static void setGameWorld(GenericWorld gw) {
		myGameWorld = gw;
	}
	
	public static boolean thrownRobotDeathException() {
		return currentRobotData.thrownRobotDeathException;
	}

	public static void checkForRobotDeath() {
		if(currentRobotData.thrownRobotDeathException)
			throw new RobotDeathException();
	}
	
	public static void roboFinallyClause() {
		if (currentRobotData.thrownRobotDeathException) {
			// if (Clock.getRoundNum() != 1616)
			//	System.out.println("Detected a thrownRobotDeathException state in a finally clause, throwing a new one");
			//if (Clock.getRoundNum() == 1616)
			//	Thread.dumpStack(); 
		    //    System.out.println("hitting roboFinallyClause with RDE thrown");
			//System.exit(-1);
			Scheduler.removeCurrentThreadWithHack();
			Scheduler.passToNextThread();
			// RobotMonitor.switchRunner(Scheduler.getCurrent().data);
			// throw new RobotDeathException();
		}
	}
	
}
