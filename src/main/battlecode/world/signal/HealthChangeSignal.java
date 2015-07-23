package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Signifies that a robot's energon has just changed
 *
 * @author adamd
 */
public class HealthChangeSignal extends Signal {

    @JsonProperty("type")
    private String getTypeForJson() { return "HealthChange"; }

    private static final long serialVersionUID = 6617731359077112385L;

    private final int[] robotIDs;
    private final double[] health;

    public HealthChangeSignal(InternalRobot[] robots) {
        int nChangedHealth = 0;
        for (int i = 0; i < robots.length; i++) {
            if (robots[i].healthChanged() || robots[i].getRoundsAlive() <= 2) {
                nChangedHealth++;
            }
        }

        robotIDs = new int[nChangedHealth];
        health = new double[nChangedHealth];
        int curIndex = 0;
        for (int i = 0; i < robots.length; i++) {
            if (robots[i].healthChanged() || robots[i].getRoundsAlive() <= 2) { // TODO(axc) clean
                robotIDs[curIndex] = robots[i].getID();
                health[curIndex] = robots[i].getHealthLevel();
                robots[i].clearHealthChanged();
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
}
