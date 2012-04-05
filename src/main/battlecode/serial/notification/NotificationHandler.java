package battlecode.serial.notification;

/**
 * A flat visitor for processing arbitrary instances of Notification.
 *
 * @param <R> the return type of the processing methods in this handler
 */
public interface NotificationHandler<R> {

    /**
     * Processes a start notification.
     *
     * @param n the notification to process
     * @return the result of processing
     */
    public R visitStartNotification(StartNotification n);

    /**
     * Processes a pause notification.
     *
     * @param n the notification to process
     * @return the result of processing
     */
    public R visitPauseNotification(PauseNotification n);

    /**
     * Processes a run notification.
     *
     * @param n the notification to process
     * @return the result of processing
     */
    public R visitRunNotification(RunNotification nb);

    /**
     * Processes a resume notification.
     *
     * @param n the notification to process
     * @return the result of processing
     */
    public R visitResumeNotification(ResumeNotification n);
}
