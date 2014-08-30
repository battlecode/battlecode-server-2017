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
    private final double[] amountAttackDelay;
    private final double[] amountMovementDelay;
    private final double[] amountCooldownDelay;
    private final double[] amountLoadingDelay;

    public ActionDelaySignal(InternalRobot[] robots) {
        robotIDs = new int[robots.length];
        amountAttackDelay = new double[robots.length];
        amountMovementDelay = new double[robots.length];
        amountCooldownDelay = new double[robots.length];
        amountLoadingDelay = new double[robots.length];
        for (int i = 0; i < robots.length; i++) {
            robotIDs[i] = robots[i].getID();
            amountAttackDelay[i] = robots[i].getAttackDelay();
            amountMovementDelay[i] = robots[i].getMovementDelay();
            amountLoadingDelay[i] = robots[i].getLoadingDelay();
            amountCooldownDelay[i] = robots[i].getCooldownDelay();
        }
    }

    public int[] getRobotIDs() {
        return robotIDs;
    }

    public double[] getAttackDelays() {
        return amountAttackDelay;
    }

    public double[] getMovementDelays() {
        return amountMovementDelay;
    }

    public double[] getCooldownDelays() {
        return amountCooldownDelay;
    }

    public double[] getLoadingDelays() {
        return amountLoadingDelay;
    }
}
