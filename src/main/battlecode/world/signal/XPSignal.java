package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;
import com.fasterxml.jackson.annotation.JsonProperty;

public class XPSignal extends Signal {

    @JsonProperty("type")
    private String getTypeForJson() { return "XP"; }

    private static final long serialVersionUID = 6612331359077155789L;

    private int robotID;
    private int XP;

    public XPSignal(int robotID, int XP) {
        this.robotID = robotID;
        this.XP = XP;
    }

    public int getRobotID() {
        return robotID;
    }

    public int getXP() {
        return XP;
    }
}
