package battlecode.common;

public interface BroadcastController extends ComponentController {

    /**
     * Adds a message for your robot to broadcast. At the end of your
     * robot's execution block, if a broadcast has been set, the message is removed
     * and immediately added to the incoming message queues of all robots in
     * your broadcast range (except for the sending robot). Note that robots
     * are thus limited to sending at most one message per round.
     * </p>
     * <p>
     * You are charged a small amount of energon for every message that you
     * broadcast. The cost of sending a message is equal to
     * <code>(GameConstants.BROADCAST_FIXED_COST +
     * GameConstants.BROADCAST_COST_PER_BYTE*sizeBytes)</code>
     * where <code>sizeBytes</code> is the size of the message, in bytes.
     * <p>
     * <p>
     * Each robot can only have one message to broadcast in a given round at a time.
     *
     * @cooldown 1
     *
     * @param msg
     *            the message you want to broadcast; cannot be <code>null</code>.
     * @throws GameActionException if this robot already has a message queued in the current round (ALREADY_ACTIVE).
     */
    public void broadcast(Message msg) throws GameActionException;

	/**
	 * Broadcasts a message that tells the robots with the given RobotIDs to
	 * wake up.  They will automatically turn on if they are allied and within
	 * broadcast range.
	 *
	 * @cooldown 1
	 *
	 * @param ids
	 *     the RobotIDs of the robots to wake up
     * @throws GameActionException if this robot already has a message queued in the current round (ALREADY_ACTIVE).
	 */
	public void broadcastTurnOn(int [] ids) throws GameActionException;

	/**
	 * Broadcasts a message that tells all allied robots within sensor range
	 * to wake up.
	 *
	 * @cooldown 1
     * @throws GameActionException if this robot already has a message queued in the current round (ALREADY_ACTIVE).
	 */
	public void broadcastTurnOnAll() throws GameActionException;
}
