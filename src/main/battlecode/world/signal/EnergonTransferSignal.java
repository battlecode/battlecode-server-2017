package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;
import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

/**
 * Signifies that a robot has transferred energon.
 *
 * @author adamd
 */
public class EnergonTransferSignal extends Signal {

    private static final long serialVersionUID = -488610493011448403L;

    /**
     * The ID of the robot that just transferred energon.
     */
    private final int robotID;

    /**
     * The location to which energon was just transferred
     */
    private final MapLocation targetLoc;

    /**
     * The height to which energon was just transferred
     */
    private final RobotLevel targetHeight;

    /**
     * The amount of energon that was transferred
     */
    private final double amount;

    /**
     * Creates a signal for a robot broadcast.
     *
     * @param robot     the robot that transferred energon
     * @param targetLoc the location to which the robot just transferred energon
     */
    public EnergonTransferSignal(InternalRobot robot, MapLocation targetLoc, RobotLevel targetHeight, double amount) {
        this.robotID = robot.getID();
        this.targetLoc = targetLoc;
        this.targetHeight = targetHeight;
        this.amount = amount;
    }

    /**
     * Returns the ID of the robot that just transferred energon.
     *
     * @return the transferring robot's ID
     */
    public int getRobotID() {
        return robotID;
    }

    /**
     * Returns the location to which energon was just transferred
     *
     * @return the location to which energon was just transferred
     */
    public MapLocation getTargetLoc() {
        return targetLoc;
    }

    /**
     * Returns the height to which energon was just transferred
     *
     * @return the height to which energon was just transferred
     */
    public RobotLevel getTargetHeight() {
        return targetHeight;
    }

    /**
     * Returns the amount of energon that was transferred
     *
     * @retun the amount of energon thas was transferred
     */
    public double getAmount() {
        return amount;
    }
}
