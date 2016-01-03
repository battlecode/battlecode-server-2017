package battlecode.serial;

import battlecode.world.signal.InternalSignal;

/**
 * Changes to a game world as the result of a signal injection.
 *
 * @author james
 */
public class InjectDelta implements ServerEvent {
    private final boolean success;
    private final InternalSignal[] internalSignals;

    /**
     * Create a new injection delta.
     *
     * @param success whether the injection was successful
     * @param internalSignals the internalSignals created by the injection, if it was successful.
     */
    public InjectDelta(boolean success, InternalSignal[] internalSignals) {
        this.success = success;
        this.internalSignals = internalSignals;
    }

    /**
     * @return whether the injection was successful.
     */
    public boolean getSuccess() {
        return success;
    }

    /**
     * @return an array containing the currentInternalSignals representing changes
     *         to the game world; must not be modified.
     */
    public InternalSignal[] getInternalSignals() {
        return this.internalSignals;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private InjectDelta() {
        this(false, null);
    }
}
