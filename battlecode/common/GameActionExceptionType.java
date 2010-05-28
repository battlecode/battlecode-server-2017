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
	/** Indicates when a robot tries to evolve into a <code>RobotType</code> thit it is not allowed to evolve into */
	CANT_EVOLVE_INTO_THAT,
	/** Indicates when a robot tries to evolve and is not supposed to be able to */
	CANT_EVOLVE,
	/** Indicates when a robot tries to perform an action (e.g. transfer energon, spawn) for which it does not have enough energon */
	NOT_ENOUGH_ENERGON,
	/** Indicates when a robot tries to move into non-empty square */
	CANT_MOVE_THERE,
	/** Indicates when a robot tries to set its direction to <code>Direction.NONE</code> or <code>Direction.OMNI</code> */
	BAD_DIRECTION,
	/** Indicates when a robot tries to execute an action, but is not currentl IDLE */
	ALREADY_ACTIVE,
	/** Indicates a <code>GameObject</code> was used that does not correspond to an object in the gameworld */
	INVALID_OBJECT,
	/** Indicates when a robot tries to sense a <code>GameObject</code> that is no longer existant or no longer
	 * in this robot's sensor range */
	CANT_SENSE_THAT,
	/** Indicates when a robot tries to transfer energon to a square that does not contain a robot */
	CANT_TRANSFER_ENERGON_THERE,
	/** Indicates when a robot tries to attack a square that is not within its attack range */
	OUT_OF_ATTACK_RANGE,
	/** Indicates when a non-ARCHON unit tries to perform an action that only Archons can do */
	ARCHONS_ONLY,
	/** Indicates when a non-CHANNELER unit tries to perform an action that only Channelers can do */
	CHANNELERS_ONLY,
	/** Indicates when a non-TURRET unit tries to perform an action that only TURRETS can do */
	TURRETS_ONLY,
	/** Indicates when a robot tries to commit to two actions in the same round. */
	DOUBLE_ACTION_ERROR,
	/** Indicates when a robot tries to commit attack without sufficient cooldown period */
	INSUFFICIENT_ATTACK_COOLDOWN,
	/** Indicates when a robot tries to commit movement without sufficient cooldown period */
	INSUFFICIENT_MOVEMENT_COOLDOWN,
	/** Indicates when a robot tries to load a block from a non-adjacent location */
	CANT_LOAD_FROM_THERE,
	/** Indicates when a robot tries to load a block to a non-adjacent location */
	CANT_UNLOAD_TO_THERE,
	/** Indicates when a robot tires to burn, but is unable */
	CANT_BURN_FLUX,
	/** Indicates when a robot tries to load a block when it already has a full cargo */
	INSUFFICIENT_ROOM_IN_CARGO,
	/** Indicates when a robot tries to unload a block when it has none in its cargo space */
	NO_BLOCKS_IN_CARGO,
	/** Indicates that a teleporter tried to teleport a robot that can't be teleported (enemy or building) */
	CANT_TELEPORT_THAT,
	/** Indicates that the player tried to use a nonexistent or enemy teleporter */
	NOT_A_TELEPORTER,
	/** Indicates when a robot tries to perform an action (e.g. transfer energon, spawn) for which it does not have enough flux */
	NOT_ENOUGH_FLUX,
	/** Indicates when a robot tries to transfer flux to a square that does not contain a robot */
	CANT_TRANSFER_FLUX_THERE,
	/** Indicates when a robot tries to deploy or undeploy, but can't */
	CANT_DEPLOY,
        /** Indicates only an AURA can do this. */
	AURAS_ONLY,
        /** Indicates that set AURA cannot be performed. */
	CANT_SET_AURA
		
}
