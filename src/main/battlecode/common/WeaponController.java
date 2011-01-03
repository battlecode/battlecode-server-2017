package battlecode.common;

public interface WeaponController extends ComponentController {

    /**
     * Attacks the given location and height.
     *
     * @cooldown
     */
    public void attackSquare(MapLocation loc, RobotLevel height) throws GameActionException;
}
