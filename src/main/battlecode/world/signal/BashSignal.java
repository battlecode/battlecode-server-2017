package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Signifies that a robot just attacked
 *
 * @author adamd
 */
public class BashSignal extends Signal {

    @JsonProperty("type")
    private String getTypeForJson() { return "Bash"; }

    private static final long serialVersionUID = 8064711239305833273L;

    /**
     * The ID of the robot that bashed.
     */
    public final int robotID;

    /**
     * The location that the robot bashed.
     */
    public final MapLocation targetLoc;

    /**
     * Creates a signal for a robot bash.
     *
     * @param robot     the robot that attacked
     * @param targetLoc the location that the robot attacked
     */
    public BashSignal(InternalRobot robot, MapLocation targetLoc) {
        this.robotID = robot.getID();
        this.targetLoc = targetLoc;
    }

    /**
     * Returns the ID of the robot that just attacked.
     *
     * @return the messaging robot's ID
     */
    public int getRobotID() {
        return robotID;
    }

    /**
     * Returns the location that the robot attacked
     *
     * @return the location that the robot attacked
     */
    public MapLocation getTargetLoc() {
        return targetLoc;
    }
}
