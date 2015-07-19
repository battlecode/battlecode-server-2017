package battlecode.world.signal;

import java.util.Arrays;

import battlecode.engine.signal.Signal;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TeamOreSignal extends Signal {

    @JsonProperty("type")
    private String getTypeForJson() { return "TeamOre"; }

    /**
     * The robot's new energon level
     */
    public final double[] ore;

    public TeamOreSignal(double[] ore) {
    	this.ore = Arrays.copyOf(ore, ore.length);
    }

    /**
     * Returns the robot's new energon amount
     *
     * @return the robot's new energon amount
     */
    public double[] getOre() {
        return ore;
    }
}
