package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MissileCountSignal extends Signal {

    @JsonProperty("type")
    private String getTypeForJson() { return "MissileCount"; }

    private static final long serialVersionUID = 6617731359077155789L;

    private int robotID;
    private int missileCount;

    public MissileCountSignal(int robotID, int missileCount) {
        this.robotID = robotID;
        this.missileCount = missileCount;
    }

    public int getRobotID() {
        return robotID;
    }

    public int getMissileCount() {
        return missileCount;
    }
}
