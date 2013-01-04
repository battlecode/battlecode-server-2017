package battlecode.world.signal;

import java.util.HashMap;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

/**
 * Signifies that a robot has broadcast a message.
 *
 * @author Matt
 */
public class BroadcastSignal extends Signal {

    private static final long serialVersionUID = 8603786984259160822L;

    /**
     * TheID of the robot that broadcasted the message.
     */
    public final int robotID;
    public transient HashMap<Integer, Integer> broadcastMap;

    /**
     * Creates a signal for a robot broadcast.
     *
     * @param robot the robot that broadcast the message
     */
    public BroadcastSignal(InternalRobot robot, HashMap<Integer, Integer> broadcastMap) {
        this.robotID = robot.getID();
        this.broadcastMap = broadcastMap;
    }

    /**
     * Returns the ID of the robot that just broadcast.
     *
     * @return the messaging robot's ID
     */
    public int getRobotID() {
        return robotID;
    }
}
