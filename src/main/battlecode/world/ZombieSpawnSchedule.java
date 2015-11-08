package battlecode.world;

import battlecode.common.ZombieCount;
import battlecode.common.RobotType;
import java.util.*;
import java.io.Serializable;

/**
 * A class to hold information about how many zombies spawn each round, and
 * what type of zombies spawn.
 */
public class ZombieSpawnSchedule implements Serializable {
    private static final long serialVersionUID = -8945913587066092224L;

    /**
     * A map from round numbers to a list of ZombieCounts, specifying the
     * types and numbers of zombies that spawn during each round (per den).
     */
    private Map<Integer, ArrayList<ZombieCount>> map;

    /**
     * Creates an empty zombie spawn schedule.
     */
    public ZombieSpawnSchedule() {
        map = new HashMap<>();
    }

    /**
     * Creates a deep copy of another ZombieSpawnSchedule.
     *
     * @param other the ZombieSpawnSchedule to copy.
     */
    public ZombieSpawnSchedule(ZombieSpawnSchedule other) {
        map = new HashMap<>();
        for (int round : other.getRounds()) {
            ArrayList<ZombieCount> counts = new ArrayList<>();
            for (ZombieCount zc : other.getScheduleForRound(round)) {
                counts.add(new ZombieCount(zc));
            }
            map.put(round, counts);
        }
    }

    /**
     * Adds a new zombie spawn to the schedule.
     *
     * @param round round for the spawn.
     * @param type type of zombie spawned.
     * @param count the number of zombies spawned per den.
     */
    public void add(int round, RobotType type, int count) {
        if (!map.containsKey(round)) {
            map.put(round, new ArrayList<ZombieCount>());
        }
        map.get(round).add(new ZombieCount(type, count));
    }

    /**
     * Returns the rounds for which there are zombie spawns.
     *
     * @return the rounds for which there are zombie spawns.
     */
    public Collection<Integer> getRounds() {
        return map.keySet();
    }

    /**
     * Returns the zombie spawns on a given round. MODIFYING THIS RESULT WILL
     * DIRECTLY MODIFY THE ZOMBIE SPAWN SCHEDULE.
     *
     * @param round the round for which we want to know the zombie spawn
     *              schedule for.
     * @return an array list of zombie counts for that round.
     */
    public ArrayList<ZombieCount> getScheduleForRound(int round) {
        if (!map.containsKey(round)) {
            return new ArrayList<ZombieCount>();
        } else {
            return map.get(round);
        }
    }

    /**
     * Returns whether two zombie spawn schedules are equivalent. Their
     * zombie counts can be in different orders even if they are equivalent.
     *
     * @param other the zombie spawn schedule to compare to.
     * @return whether the two zombie spawn schedules are equivalent.
     */
    public boolean equivalentTo(ZombieSpawnSchedule other) {
        if (!map.keySet().equals(other.map.keySet())) return false;

        for (int round : this.map.keySet()) {
            ArrayList<ZombieCount> mine = this.map.get(round);
            ArrayList<ZombieCount> theirs = other.map.get(round);

            if (mine.size() != theirs.size()) return false;

            ArrayList<ZombieCount> mineSorted = new ArrayList<>(mine);
            Collections.sort(mineSorted);

            ArrayList<ZombieCount> theirsSorted = new ArrayList<>(theirs);
            Collections.sort(theirsSorted);

            for (int i = 0; i < mineSorted.size(); ++i) {
                if (mineSorted.get(i).compareTo(theirsSorted.get(i)) != 0) {
                    return false;
                }
            }
        }

        return true;
    }
}
