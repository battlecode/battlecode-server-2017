package instrumentertest;

import battlecode.common.ZombieSpawnSchedule;

/**
 * @author james
 */
@SuppressWarnings("unused")
public class UsesSpawnSchedule {
    public static void run() {
        new ZombieSpawnSchedule().getScheduleForRound(0);
        new ZombieSpawnSchedule().getRounds();
    }
}
