package battlecode.serial.notification;

import java.io.ObjectStreamException;

public class ResumeNotification implements Notification {

    private static final long serialVersionUID = 1192877459158275306L;

    /**
     * The singleton instance for this notification.
     */
    public static final ResumeNotification INSTANCE = new ResumeNotification();

    private ResumeNotification() {
    }

    /**
     * {@inheritDoc}
     */
    public <R> R accept(NotificationHandler<R> handler) {
        return handler.visitResumeNotification(this);
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }
}
