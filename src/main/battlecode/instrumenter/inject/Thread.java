package battlecode.instrumenter.inject;

import battlecode.instrumenter.InstrumentationException;

@SuppressWarnings("unused")
public class Thread extends java.lang.Thread {

    private static Thread INSTANCE = new Thread(false);

    private Thread(boolean b) {
    }

    public Thread() {
        throw new InstrumentationException();
    }

    public Thread(Runnable r) {
        throw new InstrumentationException();
    }

    public static Thread currentThread() {
        return INSTANCE;
    }

}
