package battlecode.common;


/*
TODO:

 */

/**
 * A GameObject instance represents an object in the game world.
 * <p/>
 * A GameObject should have a final, globally unique integer ID.
 * These fields can be accessed by the accessors in GameObject.  Any other, mutable information about the
 * GameObject must be sensed, e.g. by <code>myRobotController.senseLocationOf(someGameObject)</code>.
 * <p/>
 * The only valid <code>GameObject</code>s are those returned by sensing objects on the map.  If a contestant writes
 * a class that implements <code>GameObject</code>, that <code>GameObject</code> will be invalid, and will result in a
 * <code>GameActionException</code> of type <code>GameActionExceptionType.INVALID_OBJECT</code> if it is used.
 * <p/>
 *
 * @author Teh Devs
 * @see battlecode.common.Team
 * @see battlecode.common.RobotController
 */
public interface GameObject {
    /*
    GameObject is visible to the contestants.  The only accessors it has should be for things the robots don't have to "sense."
    For the contestants to access the rest of the fields, like the object's location, they need to call something like
    myRobotPlayer.getLocationOf(someGameObject);
     */

    /**
     * Gives the globally unique integer ID of this GameObject
     *
     * @return this GameObject's ID
     */
    public int getID();

    /**
     * Gives this GameObject's team
     */
    public Team getTeam();

    /**
     * Gives the level at which this object appears.
     */
    public RobotLevel getRobotLevel();
}
