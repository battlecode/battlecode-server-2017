package battlecode.serial.notification;

import battlecode.world.signal.InternalSignal;

/**
 * Signals the server to inject a internalSignal into the next round of the running
 * match.
 *
 * @author james
 */
public class InjectNotification implements Notification {
    private final InternalSignal internalSignal;

    /**
     * Create a new injection internalSignal.
     *
     * @param internalSignal the internalSignal to be injected
     */
    public InjectNotification(InternalSignal internalSignal) {
        this.internalSignal = internalSignal;
    }

    /**
     * @return the internalSignal to be injected
     */
    public InternalSignal getInternalSignal() {
        return internalSignal;
    }

    @Override
    public void accept(NotificationHandler handler) {
        handler.visitInjectNotification(this);
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private InjectNotification() {
        this(null);
    }
}
