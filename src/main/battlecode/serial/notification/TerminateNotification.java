package battlecode.serial.notification;

/**
 * Signals the server to terminate after it finishes computing all of the games
 * in its queue.
 *
 * @author james
 */
public class TerminateNotification implements Notification {
    @Override
    public void accept(NotificationHandler handler) {
        handler.visitTerminateNotification(this);
    }
}
