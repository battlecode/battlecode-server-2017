package battlecode.serial.notification;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * Represents a notification to the server - some state-changing message that the server
 * should obey.
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME,
        include=JsonTypeInfo.As.PROPERTY,
        property="notification")
@JsonSubTypes({
        @Type(value=GameNotification.class, name="game"),
        @Type(value=InjectNotification.class, name="inject"),
        @Type(value=PauseNotification.class, name="pause"),
        @Type(value=ResumeNotification.class, name="resume"),
        @Type(value=RunNotification.class, name="run"),
        @Type(value=StartNotification.class, name="start"),
})
public interface Notification extends Serializable {

    /**
     * Accept the notification handler (a visitor) for processing.
     *
     * @param handler the handler to accept
     */
    void accept(NotificationHandler handler);
}
