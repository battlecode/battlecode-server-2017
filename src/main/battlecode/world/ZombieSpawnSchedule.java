package battlecode.world;

import battlecode.common.ZombieCount;
import battlecode.common.RobotType;
import java.util.*;
import java.io.Serializable;

public class ZombieSpawnSchedule implements Serializable {
    private static final long serialVersionUID = -8945913587066092224L;

    private Map<Integer, ArrayList<ZombieCount>> map; // keys are round numbers

    public ZombieSpawnSchedule() {
        map = new HashMap<Integer, ArrayList<ZombieCount>>();
    }

    public void add(int round, RobotType type, int count) {
        if (!map.containsKey(round)) {
            map.put(round, new ArrayList<ZombieCount>());
        }
        map.get(round).add(new ZombieCount(type, count));
    }

    public ArrayList<ZombieCount> getScheduleForRound(int round) {
        if (!map.containsKey(round)) {
            return new ArrayList<ZombieCount>();
        } else {
            return map.get(round);
        }
    }
}
