package battlecode.common;

/**
 * Enumerates the possible errors in GameWorld interactions that cause a GameActionException to be thrown.
 */
public enum GameActionExceptionType {
	/** Internal error in the GameWorld engine.	 This is bad. */
	INTERNAL_ERROR,
	/** Indicates when a robot tries to perform an action (e.g. transfer energon, spawn) for which it does not have enough energon */
	NOT_ENOUGH_ENERGON,
	/** Indicates when a robot tries to perform an action (e.g. transfer energon, spawn) for which it does not have enough resources */
	NOT_ENOUGH_RESOURCES,
	/** Indicates when a robot tries to move into non-empty square */
	CANT_MOVE_THERE,
	/** Indicates when a robot tries to execute an action, but is not currentl IDLE */
	ALREADY_ACTIVE,
	/** Indicates when a robot tries to sense a <code>GameObject</code> that is no longer existant or no longer
	 * in this robot's sensor range */
	CANT_SENSE_THAT,
	/** Indicates when a robot tries to perform an action on a location that is outside
	 * its range. */
	OUT_OF_RANGE,
	/** Indicates when a robot tries to perform an action without sufficient cooldown period */
	INSUFFICIENT_COOLDOWN,
	/** Indicates when a robot tries to transport another robot but does not have sufficient room. */
	INSUFFICIENT_ROOM_IN_CARGO,
	/** Indicates when a robot tries to perform an action on another robot, but there is
	 * no suitable robot there. */
	NO_ROBOT_THERE,

}
