package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

/**
 * TODO: update docs
 * Signifies that a robot's energon has just changed
 *
 * @author adamd
 */
public class EnergonChangeSignal extends Signal {

    private static final long serialVersionUID = 4031486761507401544L;

    /**
     * TheID of the robot whose energon has changed.
     */
    private final int[] robotIDs;

    /**
     * The robot's new energon level
     */
    private final double[] energon;

    /**
     * Creates a signal for a robot whose energon just changed.
     *
     * @param robot the robot whose energon just changed
     */
    public EnergonChangeSignal(InternalRobot[] robots) {
        robotIDs = new int[robots.length];
        energon = new double[robots.length];
        for (int i = 0; i < robots.length; i++) {
            robotIDs[i] = robots[i].getID();
            energon[i] = robots[i].getEnergonLevel();
        }
    }

    /**
     * Returns the ID of the robot whose energon just changed.
     *
     * @return the robot's ID
     */
    public int[] getRobotIDs() {
        return robotIDs;
    }

    /**
     * Returns the robot's new energon amount
     *
     * @return the robot's new energon amount
     */
    public double[] getEnergon() {
        return energon;
    }
}
