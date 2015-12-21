package battlecode.serial.notification;

/**
 * A flat visitor for processing arbitrary instances of Notification.
 */
public interface NotificationHandler {

    /**
     * Processes a start notification.
     *
     * @param n the notification to process
     * @return the result of processing
     */
    void visitStartNotification(StartNotification n);

    /**
     * Processes a pause notification.
     *
     * @param n the notification to process
     * @return the result of processing
     */
    void visitPauseNotification(PauseNotification n);

    /**
     * Processes a run notification.
     *
     * @param n the notification to process
     * @return the result of processing
     */
    void visitRunNotification(RunNotification n);

    /**
     * Processes a resume notification.
     *
     * @param n the notification to process
     * @return the result of processing
     */
    void visitResumeNotification(ResumeNotification n);

    /**
     * Processes an inject notification.
     *
     * @param n the notification to process
     * @return the result of processing
     */
    void visitInjectNotification(InjectNotification n);

    /**
     * Processes a game notification.
     *
     * @param n the notification to process
     * @return the result of processing
     */
    void visitGameNotification(GameNotification n);
}
