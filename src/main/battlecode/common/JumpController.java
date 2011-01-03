package battlecode.common;

import battlecode.common.GameActionException;

public interface JumpController extends ComponentController {

    public void jump(MapLocation loc) throws GameActionException;
}
