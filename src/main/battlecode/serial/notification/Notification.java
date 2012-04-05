package battlecode.serial.notification;

import java.io.Serializable;

/**
 * Represents a notification to the server. Sort of the opposite of a signal,
 * at least in the direction that they travel.
 */
public interface Notification extends Serializable {

    /**
     * Accept the notification handler (a visitor) for processing.
     *
     * @param handler the handler to accept
     * @param <R>     the return type of the notification handler's processing methods
     */
    <R> R accept(NotificationHandler<R> handler);
}
