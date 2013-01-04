package battlecode.world.signal;

import battlecode.common.Direction;
import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

/**
 * Signifies that a robot just changed its direction
 *
 * @author adamd
 */
public class SetDirectionSignal extends Signal {

    private static final long serialVersionUID = 4324248072036566798L;

    /**
     * The new robot's ID
     */
    private final int robotID;

    /**
     * The new robot's direction
     */
    private final Direction dir;

    /**
     * Creates a signal for a robot that just changed its direction
     *
     * @param robot the robot that just changed its direction (after it changed)
     */
    public SetDirectionSignal(InternalRobot robot, Direction dir) {
        robotID = robot.getID();
        this.dir = dir;
    }

    public int getRobotID() {
        return robotID;
    }

    public Direction getDirection() {
        return dir;
    }

}
