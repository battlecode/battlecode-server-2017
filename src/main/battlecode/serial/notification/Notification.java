package battlecode.serial.notification;

import java.io.Serializable;

/**
 * Represents a notification to the server - some state-changing message that the server
 * should obey.
 */
public interface Notification extends Serializable {

    /**
     * Accept the notification handler (a visitor) for processing.
     *
     * @param handler the handler to accept
     */
    void accept(NotificationHandler handler);
}
