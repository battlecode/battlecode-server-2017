package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

/**
 * Signifies that a robot just attacked
 *
 * @author adamd
 */
public class SelfDestructSignal extends Signal {

    private static final long serialVersionUID = 8064711239305833273L;

    /**
     * TheID of the robot that attacked.
     */
    public final int robotID;

    /**
     * The location that the robot attacked
     */
    public final MapLocation loc;

    public double damageFactor;

    public SelfDestructSignal(InternalRobot robot, MapLocation loc) {
        this.robotID = robot.getID();
        this.loc = loc;
        this.damageFactor = 1.0;
    }

    public SelfDestructSignal(InternalRobot robot, MapLocation loc, double damageFactor) {
        this.robotID = robot.getID();
        this.loc = loc;
        this.damageFactor = damageFactor;
    }
    /**
     * Returns the ID of the robot that just attacked.
     *
     * @return the messaging robot's ID
     */
    public int getRobotID() {
        return robotID;
    }

    /**
     * Returns the location that the robot attacked
     *
     * @return the location that the robot attacked
     */
    public MapLocation getLoc() {
        return loc;
    }

    public double getDamageFactor() {
        return damageFactor;
    }
}
