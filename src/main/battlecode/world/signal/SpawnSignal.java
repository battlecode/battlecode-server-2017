package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

/**
 * Signifies that a robot was just spawned
 *
 * @author adamd
 */
public class SpawnSignal extends Signal {

    private static final long serialVersionUID = -5655877873179815892L;

    /**
     * The new robot's ID
     */
    private final int robotID;

    /**
     * The parent robot's ID, or 0 if there was no parent
     */
    private final int parentID;

    /**
     * The new robot's location
     */
    private final MapLocation loc;

    /**
     * The type of the new robot
     */
    private final RobotType type;

    /**
     * The new robot's team
     */
    private final Team team;

    private final int delay;

    // /**
    // * The new robot's direction
    // */
    // private final Direction dir;

    /**
     * Creates a signal for a robot that was just spawned
     */
    public SpawnSignal(InternalRobot child, InternalRobot parent, int delay) {
        robotID = child.getID();
        this.delay = delay;
        if (parent == null)
            parentID = 0;
        else
            parentID = parent.getID();
        loc = child.getLocation();
        type = child.type;
        team = child.getTeam();
        // dir = child.getDirection();
    }

    /**
     * Creates a spawn signal for a robot that hasn't been spawned yet
     */
    public SpawnSignal(MapLocation loc, RobotType type, Team team, InternalRobot parent, int delay) {
        this.loc = loc;
        this.type = type;
        this.team = team;
        this.delay = delay;
        robotID = 0;
        if (parent == null)
            parentID = 0;
        else
            parentID = parent.getID();
        // dir = null;
    }

    public int getRobotID() {
        return robotID;
    }

    public MapLocation getLoc() {
        return loc;
    }

    public RobotType getType() {
        return type;
    }

    public Team getTeam() {
        return team;
    }

    // public Direction getDirection() {
    // return dir;
    // }

    public int getParentID() {
        return parentID;
    }

    public int getDelay() {
        return delay;
    }
}
