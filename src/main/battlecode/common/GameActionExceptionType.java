package battlecode.common;

/**
 * Enumerates the possible errors in GameWorld interactions that cause a GameActionException to be thrown.
 */
public enum GameActionExceptionType {
	/** Internal error in the GameWorld engine.	 This is bad. */
	INTERNAL_ERROR,
	/** Indicates when a robot tries to spawn in a non-empty square */
	CANT_SPAWN_THERE,
	/** Indicates when a robot tries to spawn a <code>RobotType</code> that it is not allowed to spawn */
	CANT_SPAWN_THAT,
	/** Indicates when a robot tries to perform an action (e.g. transfer energon, spawn) for which it does not have enough energon */
	NOT_ENOUGH_ENERGON,
	/** Indicates when a robot tries to move into non-empty square */
	CANT_MOVE_THERE,
	/** Indicates when a robot tries to set its direction to <code>Direction.NONE</code> or <code>Direction.OMNI</code> */
	BAD_DIRECTION,
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
	/** Indicates when a robot tries to load a block when it already has a full cargo */
	INSUFFICIENT_ROOM_IN_CARGO,
	/** Indicates when a robot tries to unload a block when it has none in its cargo space */
	CANT_EQUIP_THAT,
	/** Indicates that the component cannot be equipped because it is already being
	 * used by a robot. */
}
