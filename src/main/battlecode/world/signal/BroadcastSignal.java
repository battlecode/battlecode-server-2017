package battlecode.world.signal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import battlecode.common.Team;
import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Signifies that a robot has broadcast a message.
 *
 * @author Matt
 */
public class BroadcastSignal extends Signal {

    private static final long serialVersionUID = 8603786984259160822L;

    /**
     * The ID of the robot that broadcasted the message.
     */
    public final int robotID;

    /**
     * The team of the robot that broadcasted the message.
     */
    public final Team robotTeam;

    /**
     * The map from broadcast index to broadcasted integer.
     */
    public final Map<Integer, Integer> broadcastMap;

    /**
     * Creates a signal for a robot broad
     * @param robotID       the id of the robot that broadcast the message
     * @param robotTeam     the team the robot broadcast to
     * @param broadcastMap  the map of broadcast index to new broadcast value used in this broadcast.
     */
    public BroadcastSignal(int robotID, Team robotTeam, Map<Integer, Integer> broadcastMap) {
        this.robotID = robotID;
        this.robotTeam = robotTeam;
        this.broadcastMap = broadcastMap;
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
     * Returns the team of the robot that just broadcasted.
     *
     * @return the messaging robot's Team
     */
    public Team getRobotTeam() {
        return robotTeam;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private BroadcastSignal() {
        this(0, null, null);
    }
}
