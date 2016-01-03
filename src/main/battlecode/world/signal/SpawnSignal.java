package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.world.InternalRobot;

/**
 * Signifies that a robot was just spawned
 *
 * @author adamd
 */
public class SpawnSignal implements InternalSignal {

    public static final int NO_ID = -1;

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

    /**
     * InternalSignal the world to a robot.
     *
     * @param robotID the robot's id, or NO_ID if the id is TBD.
     * @param parentID the robot's parent id, or NO_ID if there is no parent.
     * @param loc the location of the robot
     * @param type the type of the robot
     * @param team the team of the robot
     * @param delay the spawn delay of the parent
     */
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
        this(NO_ID, parent != null ? parent.getID() : NO_ID, loc, type, team, delay);
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
