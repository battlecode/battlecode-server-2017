package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

/**
 * Creates an observation to be displayed in transcribed matches
 *
 * @author jmstein
 */
public class MatchObservationSignal extends Signal {

    private static final long serialVersionUID = -8277652765201239399L;

    /**
     * TheID of the robot originating the observation.
     */
    private final int robotID;

    /**
     * The content of the observation
     */
    private final String observation;

    /**
     * Creates a signal for a robot whose indicator string has just changed
     *
     * @param robot        the robot whose indicator string just changed
     * @param stringNumber indicates which of the robot's indicator strings was changed
     * @param observation  the value of the new indicator string
     */
    public MatchObservationSignal(InternalRobot robot, String observation) {
        this.robotID = robot.getID();
        this.observation = observation;
    }

    /**
     * Returns the ID of the robot originating the observation.
     *
     * @return the robot's ID
     */
    public int getRobotID() {
        return robotID;
    }

    /**
     * Returns the robot's observation
     *
     * @return the robot's observation
     */
    public String getObservation() {
        return observation;
    }
}
