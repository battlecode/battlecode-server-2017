package battlecode.serial.notification;

/**
 * A flat visitor for processing arbitrary instances of Notification.
 */
public interface NotificationHandler {

    /**
     * Processes a start notification.
     *
     * @param n the notification to process
     */
    void visitStartNotification(StartNotification n);

    /**
     * Processes a pause notification.
     *
     * @param n the notification to process
     */
    void visitPauseNotification(PauseNotification n);

    /**
     * Processes a run notification.
     *
     * @param n the notification to process
     */
    void visitRunNotification(RunNotification n);

    /**
     * Processes a resume notification.
     *
     * @param n the notification to process
     */
    void visitResumeNotification(ResumeNotification n);

    /**
     * Processes an inject notification.
     *
     * @param n the notification to process
     */
    void visitInjectNotification(InjectNotification n);

    /**
     * Processes a game notification.
     *
     * @param n the notification to process
     */
    void visitGameNotification(GameNotification n);

    /**
     * Processes a termination notification.
     *
     * @param n the notification to process
     */
    void visitTerminateNotification(TerminateNotification n);
}
