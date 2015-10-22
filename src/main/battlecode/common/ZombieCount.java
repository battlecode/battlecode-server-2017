package battlecode.common;

import java.util.*;
import java.io.Serializable;

public class ZombieCount implements Serializable {
    private static final long serialVersionUID = -8945913587216072824L;

    private RobotType type;
    private int count;

    public ZombieCount(RobotType type, int count) {
        this.type = type;
        this.count = count;
    }

    public RobotType getType() {
        return type;
    }

    public int getCount() {
        return count;
    }
}
