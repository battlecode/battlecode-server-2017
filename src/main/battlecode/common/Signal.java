package battlecode.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * This class contains information about a signal that can be broadcasted
 * between robots as a form of communication.
 *
 * There are two types of signals. Basic signals contain just the robotID, team,
 * and location of the robot that broadcasted it. Message signals can only be
 * broadcasted by ARCHONs and SCOUTs and can also hold a message, which
 * contains two integers.
 */
public class Signal implements Serializable {

    private static final long serialVersionUID = -8945913587066072824L;

    /**
     * The location of the broadcasting robot.
     */
    private MapLocation location;

    /**
     * The robotID of the broadcasting robot.
     */
    private int robotID;

    /**
     * The team of the broadcasting robot.
     */
    private Team team;

    /**
     * Creates a signal with the given location, robotID, and team.
     *
     * @param location the location to include in the signal.
     * @param robotID the robotID to include in the signal.
     * @param team the team to include in the signal.
     *
     * @battlecode.doc.costlymethod
     */
    public Signal(MapLocation location, int robotID, Team team) {

        this.location = location;
        this.robotID = robotID;
        this.team = team;

    }

    /**
     * Returns the robotID included in the signal.
     * @return the robotID included in the signal.
     *
     * @battlecode.doc.costlymethod
     */
    public int getRobotID() {
        return robotID;
    }

    /**
     * Returns the robotID included in the signal. Same as getRobotID().
     * @return the robotID included in the signal.
     *
     * @battlecode.doc.costlymethod
     */
    @JsonIgnore
    public int getID() {
        return robotID;
    }

    /**
     * Returns the location included in the signal.
     * @return the location included in the signal.
     *
     * @battlecode.doc.costlymethod
     */
    public MapLocation getLocation() {
        return location;
    }

    /**
     * Returns the team included in the signal.
     * @return the team included in the signal.
     *
     * @battlecode.doc.costlymethod
     */
    public Team getTeam() {
        return team;
    }

    /**
     * For use by serializers.
     *
     * @battlecode.doc.costlymethod
     */
    private Signal() { this(null, 0, Team.A); }
}
