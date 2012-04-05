package battlecode.serial.notification;

import java.io.ObjectStreamException;

/**
 * Represents a notification that the running server should pause the
 * match.
 */
public class PauseNotification implements Notification {

    private static final long serialVersionUID = 9147823569187364569L;

    /**
     * The singleton instance for this notification.
     */
    public static final PauseNotification INSTANCE = new PauseNotification();

    /**
     * Creates a new instance of PauseNotification
     */
    private PauseNotification() {
    }

    /**
     * {@inheritDoc}
     */
    public <R> R accept(NotificationHandler<R> handler) {
        return handler.visitPauseNotification(this);
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }
}
