package battlecode.engine.instrumenter.lang;

import battlecode.engine.instrumenter.InstrumentationException;

public class Thread extends java.lang.Thread {

    private static Thread INSTANCE = new Thread(false);

    private Thread(boolean b) {
    }

    public Thread() {
        System.err.println("A new thread!");
        throw new InstrumentationException();
    }

    public Thread(Runnable r) {
        System.err.println("trying to create a thread with " + r);
        throw new InstrumentationException();
    }

    public static Thread currentThread() {
        return INSTANCE;
    }

}
