package battlecode.world.signal;

import battlecode.engine.signal.Signal;

/**
 * Creates an observation to be displayed in transcribed matches
 *
 * @author jmstein
 */
public class MatchObservationSignal implements Signal {

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
     * @param robotID      the id of the robot whose indicator string just changed
     * @param observation  the value of the new indicator string
     */
    public MatchObservationSignal(int robotID, String observation) {
        this.robotID = robotID;
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

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private MatchObservationSignal() {
        this(0, null);
    }
}
