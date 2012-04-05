package battlecode.serial.notification;

import java.io.ObjectStreamException;

/**
 * Represents a notification that server waiting from a pause or breakpoint
 * should resume running.
 */
public class StartNotification implements Notification {

    private static final long serialVersionUID = 1623471361235454533L;

    /**
     * The singleton instance for this notification.
     */
    public static final StartNotification INSTANCE = new StartNotification();

    /**
     * Creates a new instance of StartNotification
     */
    private StartNotification() {
    }

    /**
     * {@inheritDoc}
     */
    public <R> R accept(NotificationHandler<R> handler) {
        return handler.visitStartNotification(this);
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }
}
