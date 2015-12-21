package battlecode.serial;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * Outgoing messages from the server.
 *
 * @author james
 */
public interface ServerEvent extends Serializable {}
