package battlecode.engine.instrumenter.lang;

import java.io.PrintStream;

import battlecode.engine.Engine;
import battlecode.engine.ErrorReporter;
import battlecode.engine.instrumenter.RobotMonitor;
import battlecode.engine.scheduler.Scheduler;
import battlecode.world.InternalRobot;
import battlecode.common.Team;


/**
 * RoboPrintStream is a wrapper for java.lang.System.out that prepends a string identifying the current robot to
 * all outputted strings.  Also, RoboPrintStream will silence all output if the robot should be silenced.
 *
 * @author adamd
 */
public class RoboPrintStream extends PrintStream {
	
	private boolean alreadyInLine = false;
	
	private String header;

	private static RoboPrintStream theInstance = new RoboPrintStream();
		
	private RoboPrintStream() {
		super(java.lang.System.out);
	}
	
	static public RoboPrintStream theInstance() {
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

	public void changeRobot() {
		header = null;
	}
	
	private void checkHeader() {
		if(header==null) {
			header = String.format("%s@%d",RobotMonitor.getCurrentRobot().toString(),Engine.getRoundNum());
		}				
	}

	private void printHelper(String s) {
		checkHeader();
		if(!alreadyInLine)
			java.lang.System.out.print(header);
		java.lang.System.out.print(s);
		alreadyInLine = true;
	}
	
	private void printlnHelper(String s) {
		checkHeader();
		if(!alreadyInLine) {
			java.lang.System.out.print(header);
		}
		java.lang.System.out.println(s);
		alreadyInLine = false;
	}
	
}
