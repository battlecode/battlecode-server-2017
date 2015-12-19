package battlecode.world.signal;

/**
 * Implemented by objects that can absorb signals.
 */
public interface SignalHandler {
    void visitSignal(Signal s);
}
