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
    private final int[][] amounts;
    public NeutralsDensitySignal(NeutralsMap nm) {
        this.amounts = nm.copyOfCurrentAmountsAsInt();
    }

    public int[][] getAmounts() {
        return amounts;
    }
}
