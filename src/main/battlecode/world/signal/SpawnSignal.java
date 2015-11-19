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
public class SpawnSignal implements Signal {

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

    public SpawnSignal(int robotID, int parentID, MapLocation loc, RobotType type, Team team, int delay) {
        this.robotID = robotID;
        this.parentID = parentID;
        this.loc = loc;
        this.type = type;
        this.team = team;
        this.delay = delay;
    }

    /**
     * Convenience constructor; creates a spawn signal for a robot that hasn't been spawned yet
     */
    public SpawnSignal(MapLocation loc, RobotType type, Team team, InternalRobot parent, int delay) {
        this(0, parent == null ? parent.getID() : 0, loc, type, team, delay);
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

    public int getParentID() {
        return parentID;
    }

    public int getDelay() {
        return delay;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private SpawnSignal() {
        this(0, 0, null, null, null, 0);
    }
}
