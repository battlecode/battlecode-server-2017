package battlecode.world.signal;

/**
 * Implemented by objects that can absorb currentInternalSignals.
 */
public interface SignalHandler {
    void visitSignal(InternalSignal s);
}
