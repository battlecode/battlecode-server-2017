package battlecode.world.signal;

import java.util.Arrays;

import battlecode.engine.signal.Signal;
import battlecode.world.NeutralsMap;

/**
 * TODO: update docs
 *
 * @author axc
 */
public class NeutralsDensitySignal extends Signal {
    private final double[][] amounts;
    public NeutralsDensitySignal(NeutralsMap nm) {
        amounts = nm.copyOfCurrentAmounts();
    }

    public double[][] getAmounts() {
        return amounts;
    }
}
