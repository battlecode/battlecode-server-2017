package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

/**
 * Signifies that one of the robot's indicator strings has been changed
 *
 * @author adamd
 */
public class IndicatorStringSignal extends Signal {

    private static final long serialVersionUID = -8277652765209879399L;

    /**
     * TheID of the robot whose indicator string has changed.
     */
    private final int robotID;

    /**
     * Indicates which indicator string was changed
     */
    private final int stringIndex;

    /**
     * The new indicator string
     */
    private final String newString;

    /**
     * Creates a signal for a robot whose indicator string has just changed
     *
     * @param robot        the robot whose indicator string just changed
     * @param stringNumber indicates which of the robot's indicator strings was changed
     * @param newString    the value of the new indicator string
     */
    public IndicatorStringSignal(InternalRobot robot, int stringIndex, String newString) {
        this.robotID = robot.getID();
        this.stringIndex = stringIndex;
        this.newString = newString;
    }

    /**
     * Returns the ID of the robot whose indicator string just changed.
     *
     * @return the robot's ID
     */
    public int getRobotID() {
        return robotID;
    }

    /**
     * Indicates which indicator string just changed
     *
     * @return the index of the indicator string that was changed
     */
    public int getStringIndex() {
        return stringIndex;
    }

    /**
     * Returns the robot's new indicator string
     *
     * @return the robot's new indicator string
     */
    public String getNewString() {
        return newString;
    }
}
