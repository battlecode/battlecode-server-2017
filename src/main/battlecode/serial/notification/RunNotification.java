package battlecode.serial.notification;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Represents a "run for n rounds" notification.
 */
public class RunNotification implements Notification {

    private static final long serialVersionUID = 2218449423201081302L;
    private final int rounds;

    @JsonCreator(mode=JsonCreator.Mode.PROPERTIES)
    public RunNotification(int rounds) {
        this.rounds = rounds;
    }

    public int getRounds() {
        return rounds;
    }

    public static RunNotification forever() {
        return new RunNotification(Integer.MAX_VALUE);
    }

    public <R> R accept(NotificationHandler<R> handler) {
        return handler.visitRunNotification(this);
    }
}
