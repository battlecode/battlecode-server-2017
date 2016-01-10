package testplayersuicide;

import battlecode.common.RobotController;

/**
 * @author james
 */
public class RobotPlayer {
    @SuppressWarnings("unused")
    public static void run(RobotController rc) {
        rc.disintegrate();

        // Ensure that we don't terminate spuriously.
        while (true);
    }
}
