package battlecode.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.*;

/**
 * A class to hold information about how many zombies spawn each round, and
 * what type of zombies spawn.
 */
public class ZombieSpawnSchedule implements Serializable {
    private static final long serialVersionUID = -8945913587066092224L;

    // Note: we provide deterministic accessors, so usage of this class is
    // deterministic, and custom serializers (at least for XML), so that
    // serialization is deterministic, even though HashMap is nondeterministic.
    // TODO: deterministic serialization for JSON.

    /**
     * A map from round numbers to a list of ZombieCounts, specifying the
     * types and numbers of zombies that spawn during each round (per den).
     *
     * Each zombie type only appears at most once.
     */
    private Map<Integer, Map<RobotType, Integer>> map;

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
        map = new HashMap<>(other.map.size());
        for (int round : other.map.keySet()) {
            map.put(round, new HashMap<>(other.map.get(round)));
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
        if (map.containsKey(round)) {
            map.get(round).put(type, count);
        } else {
            Map<RobotType, Integer> mapForRound = new HashMap<>();
            mapForRound.put(type, count);
            map.put(round, mapForRound);
        }
    }

    /**
     * Returns the rounds for which there are zombie spawns,
     * sorted.
     *
     * @return the rounds for which there are zombie spawns.
     */
    @JsonIgnore
    public int[] getRounds() {
        final int[] result = new int[map.size()];
        int i = 0;
        for (int key : map.keySet()) {
            result[i] = key;
            i++;
        }

        Arrays.sort(result);

        return result;
    }

    /**
     * Returns the zombie spawns on a given round, sorted by type (in enum order).
     *
     * @param round the round for which we want to know the zombie spawn
     *              schedule for.
     * @return an array of zombie counts for that round.
     */
    @JsonIgnore
    public ZombieCount[] getScheduleForRound(int round) {
        if (!map.containsKey(round)) {
            return new ZombieCount[0];
        } else {
            return map.get(round).entrySet().stream()
                    .map(e -> new ZombieCount(e.getKey(), e.getValue()))
                    .sorted()
                    .toArray(ZombieCount[]::new);
        }
    }

    /**
     * Returns whether two zombie spawn schedules are equivalent. Their
     * zombie counts can be in different orders even if they are equivalent.
     *
     * @param otherObj the zombie spawn schedule to compare to.
     * @return whether the two zombie spawn schedules are equivalent.
     */
    @Override
    public boolean equals(Object otherObj) {
        if (!(otherObj instanceof ZombieSpawnSchedule)) return false;

        ZombieSpawnSchedule other = (ZombieSpawnSchedule) otherObj;

        return this.map.equals(other.map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}
