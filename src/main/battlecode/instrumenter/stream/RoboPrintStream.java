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

    private boolean writeToSystemOut;

    public RoboPrintStream(OutputStream robotOut, boolean writeToSystemOut) throws UnsupportedEncodingException {
        super(SilencedPrintStream.theInstance());
        this.real = new PrintStream(robotOut, true, "UTF-8");
        this.headerThisRound = false;
        this.writeToSystemOut = writeToSystemOut;
    }

    //************************
    //*** PRINT METHODS ***
    //************************

    public void print(boolean b) {
        maybePrintHeader();
        real.print(b);
        if (this.writeToSystemOut) java.lang.System.out.print(b);
    }

    public void print(char c) {
        maybePrintHeader();
        real.print(c);
        if (this.writeToSystemOut) java.lang.System.out.print(c);
    }

    public void print(char[] s) {
        maybePrintHeader();
        real.print(s);
        if (this.writeToSystemOut) java.lang.System.out.print(s);
    }

    public void print(double d) {
        maybePrintHeader();
        real.print(d);
        if (this.writeToSystemOut) java.lang.System.out.print(d);
    }

    public void print(float f) {
        maybePrintHeader();
        real.print(f);
        if (this.writeToSystemOut) java.lang.System.out.print(f);
    }

    public void print(int i) {
        maybePrintHeader();
        real.print(i);
        if (this.writeToSystemOut) java.lang.System.out.print(i);
    }

    public void print(long l) {
        maybePrintHeader();
        real.print(l);
        if (this.writeToSystemOut) java.lang.System.out.print(l);
    }

    public void print(Object obj) {
        maybePrintHeader();
        real.print(String.valueOf(obj));
        if (this.writeToSystemOut) java.lang.System.out.print(String.valueOf(obj));
    }

    public void print(String s) {
        maybePrintHeader();
        real.print(s);
        if (this.writeToSystemOut) java.lang.System.out.print(s);
    }

    //***************************
    //*** PRINTLN METHODS ***
    //***************************

    public void println(boolean b) {
        maybePrintHeader();
        real.println(b);
        if (this.writeToSystemOut) java.lang.System.out.println(b);
    }

    public void println(char c) {
        maybePrintHeader();
        real.println(c);
        if (this.writeToSystemOut) java.lang.System.out.println(c);
    }

    public void println(char[] s) {
        maybePrintHeader();
        real.println(s);
        if (this.writeToSystemOut) java.lang.System.out.println(s);
    }

    public void println(double d) {
        maybePrintHeader();
        real.println(d);
        if (this.writeToSystemOut) java.lang.System.out.println(d);
    }

    public void println(float f) {
        maybePrintHeader();
        real.println(f);
        if (this.writeToSystemOut) java.lang.System.out.println(f);
    }

    public void println(int i) {
        maybePrintHeader();
        real.println(i);
        if (this.writeToSystemOut) java.lang.System.out.println(i);
    }

    public void println(long l) {
        maybePrintHeader();
        real.println(l);
        if (this.writeToSystemOut) java.lang.System.out.println(l);
    }

    public void println(Object obj) {
        maybePrintHeader();
        real.println(obj);
        if (this.writeToSystemOut) java.lang.System.out.println(obj);
    }

    public void println(String s) {
        maybePrintHeader();
        real.println(s);
        if (this.writeToSystemOut) java.lang.System.out.println(s);
    }

    public void println() {
        maybePrintHeader();
        real.println();
        if (this.writeToSystemOut) java.lang.System.out.println();
    }

    //*************************
    //*** MISCELLANEOUS ***
    //*************************

    public PrintStream append(char c) {
        maybePrintHeader();
        real.print(c);
        if (this.writeToSystemOut) java.lang.System.out.print(c);
        return this;
    }

    public PrintStream append(CharSequence csq) {
        maybePrintHeader();
        real.print(String.valueOf(csq));
        if (this.writeToSystemOut) java.lang.System.out.print(String.valueOf(csq));
        return this;
    }

    public PrintStream append(CharSequence csq, int start, int end) {
        maybePrintHeader();
        real.print(csq.subSequence(start, end).toString());
        if (this.writeToSystemOut) java.lang.System.out.print(csq.subSequence(start, end).toString());
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
        if (this.writeToSystemOut) java.lang.System.out.print(String.format(format, args));
        return this;
    }

    public PrintStream printf(String format, Object... args) {
        maybePrintHeader();
        real.printf(format, args);
        if (this.writeToSystemOut) java.lang.System.out.printf(format, args);
        return this;
    }

    public void write(byte[] buf, int off, int len) {
        maybePrintHeader();
        real.write(buf, off, len);
        if (this.writeToSystemOut) java.lang.System.out.write(buf, off, len);
    }

    public void write(int b) {
        maybePrintHeader();
        real.write(b);
        if (this.writeToSystemOut) java.lang.System.out.write(b);
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
            real.print(team);
            real.print(':');
            real.print(type);
            real.print('#');
            real.print(id);
            real.print('@');
            real.print(round);
            real.print("] ");
            if (this.writeToSystemOut) {
                java.lang.System.out.print('[');
                java.lang.System.out.print(team);
                java.lang.System.out.print(':');
                java.lang.System.out.print(type);
                java.lang.System.out.print('#');
                java.lang.System.out.print(id);
                java.lang.System.out.print('@');
                java.lang.System.out.print(round);
                java.lang.System.out.print("] ");
            }
        }
    }
}
