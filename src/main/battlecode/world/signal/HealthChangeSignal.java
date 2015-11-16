package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

/**
 * Signifies that a robot's energon has just changed
 *
 * @author adamd
 */
public class HealthChangeSignal implements Signal {

    private static final long serialVersionUID = 6617731359077112385L;

    private final int[] robotIDs;
    private final double[] health;

    /**
     * Create a new health change signal.
     *
     * @param robotIDs the ids of the robots whose health has changed
     * @param health   the new healths of those robots (robot with id robotIds[i] has health health[i]);
     *                 must have same length as robotIDs
     */
    public HealthChangeSignal(int[] robotIDs, double[] health) {
        assert robotIDs.length == health.length;

        this.robotIDs = robotIDs;
        this.health = health;
    }

    /**
     * Convenience constructor to read the health changes for a list of robots.
     * DOES NOT MODIFY ROBOTS. You'll need to reset their health changes yourself.
     *
     * @param robots the robots to check for health changes.
     */
    public HealthChangeSignal(InternalRobot[] robots) {
        int nChangedHealth = 0;
        for (InternalRobot robot : robots) {
            if (robot.healthChanged() || robot.getRoundsAlive() <= 2) {
                nChangedHealth++;
            }
        }

        robotIDs = new int[nChangedHealth];
        health = new double[nChangedHealth];
        int curIndex = 0;
        for (InternalRobot robot : robots) {
            if (robot.healthChanged() || robot.getRoundsAlive() <= 2) { // TODO(axc) clean
                robotIDs[curIndex] = robot.getID();
                health[curIndex] = robot.getHealthLevel();
                curIndex++;
            }
        }
    }

    public int[] getRobotIDs() {
        return robotIDs;
    }

    public double[] getHealth() {
        return health;
    }


    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private HealthChangeSignal() {
        this.robotIDs = null;
        this.health = null;
    }}
