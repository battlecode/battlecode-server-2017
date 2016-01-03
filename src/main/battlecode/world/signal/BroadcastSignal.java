package battlecode.world.signal;

import battlecode.common.Signal;
import battlecode.common.Team;

import java.util.Map;

/**
 * Signifies that a robot has broadcast a message.
 *
 * @author Matt
 */
public class BroadcastSignal implements InternalSignal {

    private static final long serialVersionUID = 8603786984259160822L;

    /**
     * The ID of the robot that broadcasted the message.
     */
    public final int robotID;

    /**
     * The message broadcasted.
     */
    public final Signal signal;

    /**
     * The radius of the broadcast.
     */
    public final int radius;

    /**
     * Creates a signal for a robot broad
     * @param robotID       the id of the robot that broadcast the message
     * @param signal        the signal broadcasted
     */
    public BroadcastSignal(int robotID, Signal signal, int radius) {
        this.robotID = robotID;
        this.signal = signal;
        this.radius = radius;
    }

    /**
     * Returns the ID of the robot that just broadcasted.
     *
     * @return the messaging robot's ID
     */
    public int getRobotID() {
        return robotID;
    }

    /**
     * Returns the signal just broadcasted.
     *
     * @return the signal
     */
    public Signal getSignal() {
        return signal;
    }

    /**
     * Returns the radius of the broadcast.
     *
     * @return the radius of the broadcast.
     */
    public int getRadius() {
        return radius;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private BroadcastSignal() {
        this(0, null, 0);
    }
}
