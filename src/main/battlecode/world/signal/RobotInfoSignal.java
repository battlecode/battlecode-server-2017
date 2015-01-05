package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.common.RobotInfo;

/**
 * Signifies the RobotInfo associated with each robot.
 *
 * @author axc
 */
public class RobotInfoSignal extends Signal {

    private static final long serialVersionUID = 6617731214077155785L;

    private final int robotID;

    public final double coreDelay;
    public final double weaponDelay;
    public final double health;
    public final double supplyLevel;
    public final int xp;
    public final int missileCount;

    public RobotInfoSignal(int robotID, RobotInfo info) {
        this.robotID = robotID;
        this.coreDelay = info.coreDelay;
        this.weaponDelay = info.weaponDelay;
        this.health = info.health;
        this.supplyLevel = info.supplyLevel;
        this.xp = info.xp;
        this.missileCount = info.missileCount;
    }

    public int getID() {
        return this.robotID;
    }
}
