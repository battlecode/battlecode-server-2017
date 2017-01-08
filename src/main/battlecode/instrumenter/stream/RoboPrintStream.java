package battlecode.instrumenter.stream;

import battlecode.common.RobotType;
import battlecode.common.Team;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * RoboPrintStream is a wrapper for System.out that prepends a string identifying the current robot to
 * all outputted strings.  Also, RoboPrintStream will silence all output if the robot should be silenced.
 *
 * @author adamd
 */
@SuppressWarnings("unused")
public class RoboPrintStream extends PrintStream {

    private final PrintStream real;

    private boolean headerThisRound;
    private Team team;
    private RobotType type;
    private int id;
    private int round;

    public RoboPrintStream(OutputStream robotOut) throws UnsupportedEncodingException {
        super(SilencedPrintStream.theInstance());
        real = new PrintStream(robotOut, true, "UTF-8");
        headerThisRound = false;
    }

    //************************
    //*** PRINT METHODS ***
    //************************

    public void print(boolean b) {
        maybePrintHeader();
        real.print(b);
        java.lang.System.out.print(b);
    }

    public void print(char c) {
        maybePrintHeader();
        real.print(c);
        java.lang.System.out.print(c);
    }

    public void print(char[] s) {
        maybePrintHeader();
        real.print(s);
        java.lang.System.out.print(s);
    }

    public void print(double d) {
        maybePrintHeader();
        real.print(d);
        java.lang.System.out.print(d);
    }

    public void print(float f) {
        maybePrintHeader();
        real.print(f);
        java.lang.System.out.print(f);
    }

    public void print(int i) {
        maybePrintHeader();
        real.print(i);
        java.lang.System.out.print(i);
    }

    public void print(long l) {
        maybePrintHeader();
        real.print(l);
        java.lang.System.out.print(l);
    }

    public void print(Object obj) {
        maybePrintHeader();
        real.print(String.valueOf(obj));
        java.lang.System.out.print(String.valueOf(obj));
    }

    public void print(String s) {
        maybePrintHeader();
        real.print(s);
        java.lang.System.out.print(s);
    }

    //***************************
    //*** PRINTLN METHODS ***
    //***************************

    public void println(boolean b) {
        maybePrintHeader();
        real.println(b);
        java.lang.System.out.println(b);
    }

    public void println(char c) {
        maybePrintHeader();
        real.println(c);
        java.lang.System.out.println(c);
    }

    public void println(char[] s) {
        maybePrintHeader();
        real.println(s);
        java.lang.System.out.println(s);
    }

    public void println(double d) {
        maybePrintHeader();
        real.println(d);
        java.lang.System.out.println(d);
    }

    public void println(float f) {
        maybePrintHeader();
        real.println(f);
        java.lang.System.out.println(f);
    }

    public void println(int i) {
        maybePrintHeader();
        real.println(i);
        java.lang.System.out.println(i);
    }

    public void println(long l) {
        maybePrintHeader();
        real.println(l);
        java.lang.System.out.println(l);
    }

    public void println(Object obj) {
        maybePrintHeader();
        real.println(obj);
        java.lang.System.out.println(obj);
    }

    public void println(String s) {
        maybePrintHeader();
        real.println(s);
        java.lang.System.out.println(s);
    }

    public void println() {
        maybePrintHeader();
        real.println();
        java.lang.System.out.println();
    }

    //*************************
    //*** MISCELLANEOUS ***
    //*************************

    public PrintStream append(char c) {
        maybePrintHeader();
        real.print(c);
        java.lang.System.out.print(c);
        return this;
    }

    public PrintStream append(CharSequence csq) {
        maybePrintHeader();
        real.print(String.valueOf(csq));
        java.lang.System.out.print(String.valueOf(csq));
        return this;
    }

    public PrintStream append(CharSequence csq, int start, int end) {
        maybePrintHeader();
        real.print(csq.subSequence(start, end).toString());
        java.lang.System.out.print(csq.subSequence(start, end).toString());
        return this;
    }

    public boolean checkError() {
        return false;
    }

    public void setError() {}

    public void close() {
        flush();
    }

    public PrintStream format(String format, Object... args) {
        maybePrintHeader();
        real.print(String.format(format, args));
        java.lang.System.out.print(String.format(format, args));
        return this;
    }

    public PrintStream printf(String format, Object... args) {
        maybePrintHeader();
        real.printf(format, args);
        java.lang.System.out.printf(format, args);
        return this;
    }

    public void write(byte[] buf, int off, int len) {
        maybePrintHeader();
        real.write(buf, off, len);
        java.lang.System.out.write(buf, off, len);
    }

    public void write(int b) {
        maybePrintHeader();
        real.write(b);
        java.lang.System.out.write(b);
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
        this.headerThisRound = false;
    }

    private void maybePrintHeader() {
        if (!this.headerThisRound) {
            this.headerThisRound = true;
            real.print('[');
            java.lang.System.out.print('[');
            real.print(team);
            java.lang.System.out.print(team);
            real.print(':');
            java.lang.System.out.print(':');
            real.print(type);
            java.lang.System.out.print(type);
            real.print('#');
            java.lang.System.out.print('#');
            real.print(id);
            java.lang.System.out.print(id);
            real.print('@');
            java.lang.System.out.print('@');
            real.print(round);
            java.lang.System.out.print(round);
            real.print("] ");
            java.lang.System.out.print("] ");
        }
    }
}
