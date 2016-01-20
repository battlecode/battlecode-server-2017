package battlecode.serial;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * Outgoing messages from the server.
 *
 * @author james
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME,
        include=JsonTypeInfo.As.PROPERTY,
        property="event")
@JsonSubTypes({
        @Type(value = ExtensibleMetadata.class, name = "metadata"),
        @Type(value = GameStats.class, name = "gameStats"),
        @Type(value = InjectDelta.class, name = "injectDelta"),
        @Type(value = MatchFooter.class, name = "matchFooter"),
        @Type(value = MatchHeader.class, name = "matchHeader"),
        @Type(value = PauseEvent.class, name = "pauseEvent"),
        @Type(value = RoundDelta.class, name = "roundDelta"),
        @Type(value = StoredConstants.class, name = "storedConstants"),
        })
public interface ServerEvent extends Serializable {}
