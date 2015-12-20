package battlecode.serial.notification;

import battlecode.world.signal.Signal;

/**
 * Signals the server to inject a signal into the next round of the running
 * match.
 *
 * @author james
 */
public class InjectNotification implements Notification {
    private final Signal signal;

    /**
     * Create a new injection signal.
     *
     * @param signal the signal to be injected
     */
    public InjectNotification(Signal signal) {
        this.signal = signal;
    }

    /**
     * @return the signal to be injected
     */
    public Signal getSignal() {
        return signal;
    }

    @Override
    public <R> R accept(NotificationHandler<R> handler) {
        return handler.visitInjectNotification(this);
    }
}
