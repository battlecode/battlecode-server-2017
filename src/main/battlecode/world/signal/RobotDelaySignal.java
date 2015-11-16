package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.common.RobotInfo;
import battlecode.world.InternalRobot;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * The delays associated with a group of robots.
 *
 * @author axc
 */
public class RobotDelaySignal extends Signal {

    private static final long serialVersionUID = 6617731214077155785L;

    private final int[] robotIDs;
    private final double[] coreDelays;
    private final double[] weaponDelays;

    /**
     * Convenience constructor.
     *
     * @param robots the robots to poll for delays.
     */
    public RobotDelaySignal(InternalRobot[] robots) {
        robotIDs = new int[robots.length];
        weaponDelays = new double[robots.length];
        coreDelays = new double[robots.length];
        for (int i = 0; i < robots.length; i++) {
            robotIDs[i] = robots[i].getID();
            weaponDelays[i] = robots[i].getWeaponDelay();
            coreDelays[i] = robots[i].getCoreDelay();
        }
    }

    /**
     * Create a list of robots and delays.
     *
     * @param robotIDs      robots with delays
     * @param coreDelays    core delays of the robots; must have same length as robotIDs
     * @param weaponDelays  weapon delays of the robots; must have same length as robotIDs
     */
    public RobotDelaySignal(int[] robotIDs, double[] coreDelays, double[] weaponDelays) {
        assert robotIDs.length == coreDelays.length && coreDelays.length == weaponDelays.length;

        this.robotIDs = robotIDs;
        this.coreDelays = coreDelays;
        this.weaponDelays = weaponDelays;
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

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private RobotDelaySignal() {
        this.robotIDs = null;
        this.coreDelays = null;
        this.weaponDelays = null;

    }
}
