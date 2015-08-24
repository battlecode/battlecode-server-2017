package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.common.RobotInfo;
import battlecode.world.InternalRobot;

/**
 * Signifies the RobotInfo associated with each robot.
 *
 * @author axc
 */
public class RobotInfoSignal extends Signal {

    private static final long serialVersionUID = 6617731214077155785L;

    private final int[] robotIDs;

    private final double[] coreDelays;
    private final double[] weaponDelays;
    //public final double health;
    //public final int xp;
    //public final int missileCount;

    public RobotInfoSignal(InternalRobot[] robots) {
        robotIDs = new int[robots.length];
        weaponDelays = new double[robots.length];
        coreDelays = new double[robots.length];
        for (int i = 0; i < robots.length; i++) {
            robotIDs[i] = robots[i].getID();
            weaponDelays[i] = robots[i].getWeaponDelay();
            coreDelays[i] = robots[i].getCoreDelay();
        }
    }

    public int[] getRobotIDs() {
        return robotIDs;
    }

    public double[] getCoreDelays() {
        return coreDelays;
    }

    public double[] getWeaponDelays() {
        return weaponDelays;
    }
}
