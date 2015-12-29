package battlecode.instrumenter.stream;

import battlecode.common.RobotType;
import battlecode.common.Team;

import java.io.PrintStream;


/**
 * RoboPrintStream is a wrapper for java.lang.super that prepends a string identifying the current robot to
 * all outputted strings.  Also, RoboPrintStream will silence all output if the robot should be silenced.
 *
 * @author adamd
 */
@SuppressWarnings("unused")
public class RoboPrintStream extends PrintStream {

    private boolean alreadyInLine = false;

    private Team team;
    private RobotType type;
    private int id;
    private int round;

    public RoboPrintStream() {
        super(java.lang.System.out);
    }

    //************************
    //*** PRINT METHODS ***
    //************************

    public void print(boolean b) {
        if (!alreadyInLine) printHeader();
        super.print(b);
        alreadyInLine = true;
    }

    public void print(char c) {
        if (!alreadyInLine) printHeader();
        super.print(c);
        alreadyInLine = true;
    }

    public void print(char[] s) {
        if (!alreadyInLine) printHeader();
        super.print(s);
        alreadyInLine = true;
    }

    public void print(double d) {
        if (!alreadyInLine) printHeader();
        super.print(d);
        alreadyInLine = true;
    }

    public void print(float f) {
        if (!alreadyInLine) printHeader();
        super.print(f);
        alreadyInLine = true;
    }

    public void print(int i) {
        if (!alreadyInLine) printHeader();
        super.print(i);
        alreadyInLine = true;
    }

    public void print(long l) {
        if (!alreadyInLine) printHeader();
        super.print(l);
        alreadyInLine = true;
    }

    public void print(Object obj) {
        if (!alreadyInLine) printHeader();
        super.print(String.valueOf(obj));
        alreadyInLine = true;
    }

    public void print(String s) {
        if (!alreadyInLine) printHeader();
        super.print(s);
        alreadyInLine = true;
    }

    //***************************
    //*** PRINTLN METHODS ***
    //***************************

    public void println(boolean b) {
        if (!alreadyInLine) printHeader();
        super.println(b);
        alreadyInLine = false;
    }

    public void println(char c) {
        if (!alreadyInLine) printHeader();
        super.println(c);
        alreadyInLine = false;
    }

    public void println(char[] s) {
        if (!alreadyInLine) printHeader();
        super.println(s);
        alreadyInLine = false;
    }

    public void println(double d) {
        if (!alreadyInLine) printHeader();
        super.println(d);
        alreadyInLine = false;
    }

    public void println(float f) {
        if (!alreadyInLine) printHeader();
        super.println(f);
        alreadyInLine = false;
    }

    public void println(int i) {
        if (!alreadyInLine) printHeader();
        super.println(i);
        alreadyInLine = false;
    }

    public void println(long l) {
        if (!alreadyInLine) printHeader();
        super.println(l);
        alreadyInLine = false;
    }

    public void println(Object obj) {
        if (!alreadyInLine) printHeader();
        super.println(obj);
        alreadyInLine = false;
    }

    public void println(String s) {
        if (!alreadyInLine) printHeader();
        super.println(s);
        alreadyInLine = false;
    }

    public void println() {
        if (!alreadyInLine) printHeader();
        super.println();
        alreadyInLine = false;
    }

    //*************************
    //*** MISCELLANEOUS ***
    //*************************

    public PrintStream append(char c) {
        if (!alreadyInLine) printHeader();
        super.print(c);
        alreadyInLine = true;
        return this;
    }

    public PrintStream append(CharSequence csq) {
        if (!alreadyInLine) printHeader();
        super.print(String.valueOf(csq));
        alreadyInLine = true;
        return this;
    }

    public PrintStream append(CharSequence csq, int start, int end) {
        if (!alreadyInLine) printHeader();
        super.print(csq.subSequence(start, end).toString());
        alreadyInLine = true;
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
        if (!alreadyInLine) printHeader();
        super.print(String.format(format, args));
        alreadyInLine = true;
        return this;
    }

    public PrintStream printf(String format, Object... args) {
        if (!alreadyInLine) printHeader();
        super.printf(format, args);
        alreadyInLine = true;
        return this;
    }

    public void write(byte[] buf, int off, int len) {
        if (!alreadyInLine) printHeader();
        super.write(buf, off, len);
        alreadyInLine = true;
    }

    public void write(int b) {
        if (!alreadyInLine) printHeader();
        super.write(b);
        alreadyInLine = true;
    }

    //**************************
    //*** HELPER METHODS ***
    //**************************

    /**
     * Update the header prepended to messages printed with the stream.
     *
     * @param team
     * @param type
     * @param id
     * @param round
     */
    public void updateHeader(Team team, RobotType type, int id, int round) {
        this.team = team;
        this.type = type;
        this.id = id;
        this.round = round;
    }

    private void printHeader() {
        super.print('[');
        super.print(team);
        super.print(':');
        super.print(type);
        super.print('#');
        super.print(id);
        super.print('@');
        super.print(round);
        super.print("] ");
    }
}
