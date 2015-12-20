package battlecode.world.signal;

/**
 * Implemented by objects that can absorb currentSignals.
 */
public interface SignalHandler {
    void visitSignal(Signal s);
}
