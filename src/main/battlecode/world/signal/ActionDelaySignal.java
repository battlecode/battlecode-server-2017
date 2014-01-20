package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

/**
 * Signifies the exact quantity of action delay of each robot.
 *
 * @author axc
 */
public class ActionDelaySignal extends Signal {

    private static final long serialVersionUID = 6617731359077155785L;

    /**
     * TheID of the robot whose energon has changed.
     */
    private final int[] robotIDs;

    /**
     * The amount of action delay of each robot.
     */
    private final double[] amountActionDelay;

    public ActionDelaySignal(InternalRobot[] robots) {
        robotIDs = new int[robots.length];
        amountActionDelay = new double[robots.length];
        for (int i = 0; i < robots.length; i++) {
            robotIDs[i] = robots[i].getID();
            amountActionDelay[i] = robots[i].getActionDelay();
        }
    }

    public int[] getRobotIDs() {
        return robotIDs;
    }

    public double[] getActionDelays() {
        return amountActionDelay;
    }
}
