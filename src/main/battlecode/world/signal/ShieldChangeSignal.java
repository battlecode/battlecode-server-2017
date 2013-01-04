package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

/**
 * TODO: update docs
 * Signifies that a robot's shield has just changed
 *
 * @author yp
 */
public class ShieldChangeSignal extends Signal {

    private static final long serialVersionUID = 2597984834L;

    /**
     * TheID of the robot whose shield has changed.
     */
    private final int[] robotIDs;

    /**
     * The robot's new shield level
     */
    private final double[] shield;

    /**
     * Creates a signal for a robot whose shield just changed.
     *
     * @param robot the robot whose shield just changed
     */
    public ShieldChangeSignal(InternalRobot[] robots) {
        robotIDs = new int[robots.length];
        shield = new double[robots.length];
        for (int i = 0; i < robots.length; i++) {
            robotIDs[i] = robots[i].getID();
            shield[i] = robots[i].getShieldLevel();
        }
    }

    /**
     * Returns the ID of the robot whose shield just changed.
     *
     * @return the robot's ID
     */
    public int[] getRobotIDs() {
        return robotIDs;
    }

    /**
     * Returns the robot's new shield amount
     *
     * @return the robot's new shield amount
     */
    public double[] getShield() {
        return shield;
    }
}
