package battlecode.instrumenter;

/**
 * An exception used to indicate that there was a problem instrumenting a player (e.g., the player references a
 * disallowed class, or one if its classes can't be found).  This must be an unchecked Exception, because it
 * has to be thrown in overriden methods.
 *
 * @author adamd
 */
public class InstrumentationException extends RuntimeException {

    public InstrumentationException() {
        super();
    }

    public InstrumentationException(String message) {
        super(message);
    }

    public InstrumentationException(String message, Throwable cause) {
        super(message, cause);
    }
}
