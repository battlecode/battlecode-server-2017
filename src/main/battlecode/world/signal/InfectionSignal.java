package battlecode.world.signal;

import battlecode.world.InternalRobot;

/**
 * The infection turns associated with a group of robots.
 *
 * @author axc
 */
public class InfectionSignal implements Signal {

    private static final long serialVersionUID = 6617731219527155785L;

    private final int[] robotIDs;
    private final int[] zombieInfectedTurns;
    private final int[] viperInfectedTurns;

    /**
     * Convenience constructor.
     *
     * @param robots the robots to poll for delays.
     */
    public InfectionSignal(InternalRobot[] robots) {
        robotIDs = new int[robots.length];
        zombieInfectedTurns = new int[robots.length];
        viperInfectedTurns = new int[robots.length];
        for (int i = 0; i < robots.length; i++) {
            robotIDs[i] = robots[i].getID();
            zombieInfectedTurns[i] = robots[i].getZombieInfectedTurns();
            viperInfectedTurns[i] = robots[i].getViperInfectedTurns();
        }
    }

    /**
     * Creates an infection signal based on a list of turn counts.
     *
     * @param robotIDs the robot IDs
     * @param zombieInfectedTurns the zombie infection turn counts
     * @param viperInfectedTurns the viper infection turn counts
     */
    public InfectionSignal(int[] robotIDs, int[] zombieInfectedTurns, int[]
            viperInfectedTurns) {
        assert robotIDs.length == zombieInfectedTurns.length && zombieInfectedTurns
                .length == viperInfectedTurns.length;

        this.robotIDs = robotIDs;
        this.zombieInfectedTurns = zombieInfectedTurns;
        this.viperInfectedTurns = viperInfectedTurns;
    }

    public int[] getRobotIDs() {
        return robotIDs;
    }

    public int[] getZombieInfectedTurns() {
        return zombieInfectedTurns;
    }

    public int[] getViperInfectedTurns() {
        return viperInfectedTurns;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private InfectionSignal() {
        this.robotIDs = null;
        this.zombieInfectedTurns = null;
        this.viperInfectedTurns = null;

    }
}
