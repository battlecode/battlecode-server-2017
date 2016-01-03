package battlecode.world.signal;

import battlecode.common.MapLocation;

/**
 * Signifies that a robot is clearing rubble.
 *
 * @author axc
 */
public class ClearRubbleSignal implements InternalSignal {

    private static final long serialVersionUID = -6853620834787044985L;

    /**
     * The ID of the robot that is clearing rubble.
     */
    private final int robotID;

    /**
     * The location of rubble being cleared.
     */
    private final MapLocation loc;

    /**
     * The delay of the movement (?)
     */
    private final int delay;

    /**
     * Creates a signal for a robot movement.
     * @param robotID         the ID of the robot that is moving
     * @param loc          the robot's new location
     * @param delay           the delay of the movement
     */
    public ClearRubbleSignal(int robotID, MapLocation loc, int delay) {
        this.robotID = robotID;
        this.loc = loc;
        this.delay = delay;
    }

    /**
     * Returns the ID of the robot that just broadcast.
     *
     * @return the messaging robot's ID
     */
    public int getRobotID() {
        return robotID;
    }

    /**
     * The location losing rubble.
     *
     * @return the location losing rubble.
     */
    public MapLocation getLoc(){
        return loc;
    }

    /**
     * The delay of the movement (?)
     */
    public int getDelay() {
        return delay;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private ClearRubbleSignal() {
        this(0, null, 0);
    }
}
