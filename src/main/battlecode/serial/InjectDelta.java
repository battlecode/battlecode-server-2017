package battlecode.serial;

import battlecode.world.signal.Signal;

/**
 * Changes to a game world as the result of a signal injection.
 *
 * @author james
 */
public class InjectDelta implements ServerEvent {
    private final boolean success;
    private final Signal[] signals;

    /**
     * Create a new injection delta.
     *
     * @param success whether the injection was successful
     * @param signals the signals created by the injection, if it was successful.
     */
    public InjectDelta(boolean success, Signal[] signals) {
        this.success = success;
        this.signals = signals;
    }

    /**
     * @return whether the injection was successful.
     */
    public boolean getSuccess() {
        return success;
    }

    /**
     * @return an array containing the currentSignals representing changes
     *         to the game world; must not be modified.
     */
    public Signal[] getSignals() {
        return this.signals;
    }
}
