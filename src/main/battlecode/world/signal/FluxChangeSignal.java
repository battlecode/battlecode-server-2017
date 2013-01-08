package battlecode.world.signal;

import java.util.Arrays;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

/**
 * TODO: update docs
 * Signifies that a robot's energon has just changed
 *
 * @author adamd
 */
public class FluxChangeSignal extends Signal {

    /**
     * TheID of the robot whose energon has changed.
     */
    public final int[] robotIDs;

    /**
     * The robot's new energon level
     */
    public final double[] flux;

    public FluxChangeSignal(double[] flux) {
    	this.robotIDs = null;
    	this.flux = Arrays.copyOf(flux, flux.length);
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
    public double[] getFlux() {
        return flux;
    }
}
