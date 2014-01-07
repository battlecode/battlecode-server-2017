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
        double[][] temp = nm.copyOfCurrentAmounts();
        amounts = new int[temp.length][temp[0].length];
        for (int i = 0; i < temp.length; i++) {
            for (int j = 0; j < temp[i].length; j++) {
                amounts[i][j] = (int) Math.round(temp[i][j]);
            }
        }
    }

    public int[][] getAmounts() {
        return amounts;
    }
}
