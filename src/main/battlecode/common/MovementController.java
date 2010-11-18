package battlecode.common;

public interface MovementController extends ComponentController {

	/**
     * Queues a forward movement to be performed at the end of this robot's turn.
	 *
     * When this action is executed, the robot will attempt to move forward one
     * square in its current direction. If the move succeeds, this robot's new
     * location will immediately change to the destination square, but this robot's
     * motor will be engaged for some number of rounds ({@code type().delay} for
	 * orthogonal movement and {@code Math.round(type().delay*Math.sqrt(2))} for
	 * diagonal movement).
     *
	 * @cooldown <code>type().delay</code> if orthogonal, <code>Math.round(type().delay*Math.sqrt(2.))</code> if diagonal
	 *
	 * @throws java.lang.IllegalStateException if this robot is a building
	 * @throws GameActionException if this component is already in use (ALREADY_ACTIVE)
	 * @throws GameActionException if the destination terrain is not traversable by
	 * this robot (CANT_MOVE_THERE)
	 * @throws GameActionException if the destination is occupied by another {@code GameObject}
	 * at the same height (CANT_MOVE_THERE)
     */
	public void moveForward() throws GameActionException;

	/**
     * Queues a backward movement to be performed at the end of this robot's turn.
	 * Moving backward does not change this robot's direction.
     *
     * @see #moveForward()
     */
	public void moveBackward() throws GameActionException;

	/**
     * Queues a direction change to be performed at the end of this robot's turn.
	 * When the action is executed, the robot will change its direction.
     *
	 * @cooldown 1
	 *
     * @param dir
     *            the direction the robot should face
	 * @throws GameActionException if this component is already in use (ALREADY_ACTIVE)
	 */
	public void setDirection(Direction dir) throws GameActionException;
	
	
	/**
     * Tells whether this robot can move in the given direction. Takes into
     * account only the map terrain and positions of other robots. Does not take
     * into account whether this robot is currently active or otherwise
     * incapable of moving.
     *
     * @return true if there are no robots or walls preventing this robot from
     *         moving in the given direction; false otherwise
	 *
     */
	public boolean canMove(Direction dir);

}
