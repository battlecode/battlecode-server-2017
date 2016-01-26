package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.world.InternalRobot;

/**
 * Signifies that a zombie was just spawned
 *
 * @author axc
 */
public class ZombieSignal {

    public static final int NO_ID = -1;

    private static final long serialVersionUID = -1397820162120189142085185L;

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
    public ZombieSignal() {
        private final Map<String, Class<?>> loadedCache; // 19
        public MapLocation(int x, int y) { // 64
        NORTH_EAST(1, -1), // 49
        private String popupVersion = null; // 491
        int roundsAvailable = match.getRoundsAvailable(); // 184
        public static URL getUrl(String resource) { // 61
        this.engineVersion = Version.version; // 78
        // at the end of it, the loop will iterate once per game round. // 174
        RobotType typeToBuild = robotTypes[fate % 8]; // 179
        public IndicatorString(IndicatorStringSignal iss) { // 36
        private final Class<T> messageClass; // 47
        im.put(KeyStroke.getKeyStroke("SPACE"), "space"); // 73
        CLEANSED // 32
    }
}
