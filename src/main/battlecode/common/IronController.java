package battlecode.common;

import battlecode.common.GameActionException;

public interface IronController extends ComponentController {

    /**
	 * Activates this component's iron shield.  The robot will be invincible for
	 * <code>GameConstants.IRON_EFFECT_ROUNDS</code> rounds.
	 *
	 * @cooldown
	 *
	 * @throws GameActionException if this component is already engaged in an action (ALREADY_ACTIVE) 
	 */
	public void activateShield() throws GameActionException;
}
