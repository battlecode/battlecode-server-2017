package battlecode.engine.instrumenter.lang;

import java.io.PrintStream;

import battlecode.engine.Engine;
import battlecode.engine.ErrorReporter;
import battlecode.engine.scheduler.Scheduler;
import battlecode.world.GenericWorld;
import battlecode.world.InternalRobot;
import battlecode.common.Team;



/**
 * RoboPrintStream is a wrapper for java.lang.System.out that prepends a string identifying the current robot to
 * all outputted strings.  Also, RoboPrintStream will silence all output if the robot should be silenced.
 *
 * @author adamd
 */
class RoboPrintStream extends PrintStream {
	
	/*
	Implementation notes:
	
	Because the instrumenter sticks in SilencedSystem and SilencedPrintStream for teams that are silenced, one might think that 
	RoboPrintStream never needs to silence anything.  However, if the two teams are the same team (i.e., same classes), and one is silenced,
	but the other isn't, then the instrumenter can't stick in SilencedSystem, since that would silence both of them, so instead RoboPrintStream
	has to silence one of them.
	*/
	
	private GenericWorld gw = null;
	private boolean alreadyInLine = false;
	private int lastID = 0;
	private boolean silenceA, silenceB;
		
	private static RoboPrintStream theInstance = new RoboPrintStream();
		
	private RoboPrintStream() {
		super(java.lang.System.out);
	}
	
	/**
	 * Sets some properties about the current gameworld.  Should be called before each game.
	 * @param gw the GameWorld of the current game
	 * @param silenceA whether team A should be silenced
	 * @param silenceB whether team B should be silenced
	 * @param sameTeams whether the two teams are the same
	 */
	public static void setProperties(GenericWorld gw, boolean silenceA, boolean silenceB, boolean sameTeams) {
		theInstance.gw = gw;
		theInstance.alreadyInLine = false;
		theInstance.lastID = 0;
		if(sameTeams) {
			theInstance.silenceA = silenceA;
			theInstance.silenceB = silenceB;
		} else {
			// if they're not the same teams, SilencedPrintStream will be stuck in at compile time for teams that should be silenced,
			// so RoboPrintStream should not silence anyone
			theInstance.silenceA = theInstance.silenceB = false;
		}
	}
	
	static RoboPrintStream theInstance() {
		return theInstance;
	}
	
	//************************
	//*** PRINT METHODS ***
	//************************

	public void print(boolean b) {
		printHelper(String.valueOf(b));
	}

	public void print(char c) {
		printHelper(String.valueOf(c));
	}

	public void print(char[] s) {
		printHelper(String.valueOf(s));
	}

	public void print(double d) {
		printHelper(String.valueOf(d));
	}
	
	public void print(float f) {
		printHelper(String.valueOf(f));
	}

	public void print(int i) {
		printHelper(String.valueOf(i));
	}
	
	public void print(long l) {
		printHelper(String.valueOf(l));
	}

	public void print(Object obj) {
		printHelper(String.valueOf(obj));
	}

	public void print(String s) {
		printHelper(s);
	}
	
	//***************************
	//*** PRINTLN METHODS ***
	//***************************
	
	public void println(boolean b) {
		printlnHelper(String.valueOf(b));
	}

	public void println(char c) {
		printlnHelper(String.valueOf(c));
	}

	public void println(char[] s) {
		printlnHelper(String.valueOf(s));
	}

	public void println(double d) {
		printlnHelper(String.valueOf(d));
	}
	
	public void println(float f) {
		printlnHelper(String.valueOf(f));
	}

	public void println(int i) {
		printlnHelper(String.valueOf(i));
	}
	
	public void println(long l) {
		printlnHelper(String.valueOf(l));
	}

	public void println(Object obj) {
		printlnHelper(String.valueOf(obj));
	}

	public void println(String s) {
		printlnHelper(s);
	}
	
	//*************************
	//*** MISCELLANEOUS ***
	//*************************
	
	public PrintStream append(char c) {
		this.printHelper(String.valueOf(c));
		return this;
	}
	
	public PrintStream append(CharSequence csq) {
		this.printHelper(String.valueOf(csq));
		return this;
	}
	
	public PrintStream append(CharSequence csq, int start, int end) {
		this.printHelper(csq.subSequence(start,end).toString());
		return this;
	}
	
	public boolean checkError() {
		return false;
	}
	
	public void setError() {
	}
	
	public void close() {
		flush();
	}
	
	public PrintStream format(String format, Object... args) {
		this.printHelper(String.format(format,args));
		return this;
	}	
	
	public PrintStream printf(String format, Object... args) {
		this.printHelper(String.format(format,args));
		return this; 
	}
	
	public void write(byte[] buf, int off, int len) {
		this.printHelper(new String(buf, off, len));
	}
	
	public void write(int b) {
		this.printHelper(String.valueOf((char)b));
	}
	
	//**************************
	//*** HELPER METHODS ***
	//**************************

	private boolean isSilenced(int ID) {
		if(silenceA == silenceB)
			return silenceA;
		try{
			switch(((InternalRobot)gw.getObjectByID(ID)).getTeam()) {
				case A:
					return silenceA;
				case B:
					return silenceB;
				default:
					return true;
			}
		} catch(Exception e) {
			ErrorReporter.report(e);
			return false;
		}
	}
	
	private boolean isSilenced() {
		return isSilenced(Scheduler.getCurrentThreadID());
	}
	
	private void printHelper(String s) {
		int newID = Scheduler.getCurrentThreadID();
		if(isSilenced(newID))
			return;
		String header = getHeader(newID);
		if(newID != lastID || !alreadyInLine) {
			if(alreadyInLine)
				java.lang.System.out.print('\n');
			java.lang.System.out.print(header);
			lastID = newID;
		}
		java.lang.System.out.print(s);
		alreadyInLine = true;
	}
	
	private void printlnHelper(String s) {
		int newID = Scheduler.getCurrentThreadID();
		if(isSilenced(newID))
			return;
		String header = getHeader(newID);
		if(newID != lastID || !alreadyInLine) {
			java.lang.System.out.print(header);
			lastID = newID;
		}
		java.lang.System.out.println(s);
		alreadyInLine = false;
	}
	
	private String getHeader(int newID) {
		if(gw == null)
			ErrorReporter.report("RoboPrintStream.printHeader() has a null gameWorld", true);
		else {
			try{
				return "[" + gw.getObjectByID(newID).toString() + "@" + Engine.getRoundNum() + "]";
			} catch(Exception e) {
				ErrorReporter.report(e);
			}
		}
		return null;
	}
	
}
	
