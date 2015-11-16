package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Represents a robot acquiring XP.
 */
public class XPSignal extends Signal {

    private static final long serialVersionUID = 6612331359077155789L;

    private int robotID;
    private int xp;

    public XPSignal(int robotID, int XP) {
        this.robotID = robotID;
        this.xp = XP;
    }

    public int getRobotID() {
        return robotID;
    }

    public int getXP() {
        return xp;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private XPSignal() {
        this(0, 0);
    }
}
