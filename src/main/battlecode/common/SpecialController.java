package battlecode.common;

import battlecode.common.GameActionException;

public interface SpecialController extends ComponentController {

	public void useAbility() throws GameActionException;
	public void useAbility(MapLocation loc) throws GameActionException;

}
