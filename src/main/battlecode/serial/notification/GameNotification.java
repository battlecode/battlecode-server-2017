package battlecode.serial.notification;

import battlecode.server.GameInfo;

/**
 * A message to a server that tells it to enqueue a game.
 *
 * @author james
 */
public class GameNotification implements Notification {
    private GameInfo info;

    /**
     * Create a GameNotification, which will signal the server
     * to enqueue a game.
     *
     * @param info the info about the game to enqueue.
     */
    public GameNotification(GameInfo info) {
        this.info = info;
    }

    /**
     * @return the info about the game to enqueue
     */
    public GameInfo getInfo() {
        return info;
    }

    @Override
    public void accept(NotificationHandler handler) {
        handler.visitGameNotification(this);
    }
}
