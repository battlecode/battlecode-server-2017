package battlecode.engine.signal;

/**
 * Implemented by objects that can absorb signals.
 */
public interface SignalHandler {
    void visitSignal(Signal s);
}
