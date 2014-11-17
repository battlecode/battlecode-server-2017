package battlecode.world;

import battlecode.common.*;

import static battlecode.common.GameActionExceptionType.*;

public abstract class ControllerShared {

    protected GameWorld gameWorld;
    protected InternalRobot robot;

    public InternalRobot getRobot() {
        return robot;
    }

    protected abstract MapLocation getLocation();

    protected ControllerShared(GameWorld gw, InternalRobot r) {
        gameWorld = gw;
        robot = r;
    }

    protected void assertWithinRange(InternalObject obj, int distance) throws GameActionException {
        if (!obj.exists())
            outOfRange();
        assertWithinRange(obj.getLocation(), distance);
    }

    protected void assertWithinRange(MapLocation loc, int distance) throws GameActionException {
        assertNotNull(loc);
        if (getLocation().distanceSquaredTo(loc) > distance)
            outOfRange();
    }

    protected static <T extends InternalObject> T castInternalObject(GameObject o, Class<T> cl) {
        assertNotNull(o);
        if (!cl.isInstance(o))
            invalidObject();
        return cl.cast(o);
    }

    protected static void invalidObject() {
        throw new IllegalArgumentException("Invalid GameObject (don't extend GameObject!)");
    }

    protected static void assertNotNull(Object o) {
        if (o == null)
            throw new NullPointerException("Argument has an invalid null value");
    }

    protected void assertSameTeam(InternalObject o) throws GameActionException {
        if (o == null || o.getTeam() != robot.getTeam())
            throw new GameActionException(CANT_SENSE_THAT, "There is no allied robot there.");
    }

    protected static void outOfRange() throws GameActionException {
        throw new GameActionException(CANT_SENSE_THAT, "That is not within range.");
    }

    protected static InternalRobot castInternalRobot(Robot r) {
        return castInternalObject(r, InternalRobot.class);
    }

    protected static InternalObject castInternalObject(GameObject o) {
        return castInternalObject(o, InternalObject.class);
    }

    protected InternalRobot robotOrException(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        InternalRobot r = gameWorld.getRobot(loc);
        if (r == null)
            throw new GameActionException(NO_ROBOT_THERE, "There is no robot there.");
        return r;
    }

}
