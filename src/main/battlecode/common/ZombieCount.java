package battlecode.common;

import java.io.Serializable;

/**
 * Data about a particular (zombie type, frequency) zombie spawn for a given
 * round.
 */
public class ZombieCount implements Serializable, Comparable<ZombieCount> {
    private static final long serialVersionUID = -8945913587216072824L;

    /**
     * Zombie type spawned.
     */
    private RobotType type;
    /**
     * The number of this zombie type spawned per den for that round.
     */
    private int count;

    /**
     * Creates a new ZombieCount based on the type and count.
     *
     * @param type the type of zombie spawned.
     * @param count the number of zombies spawned.
     */
    public ZombieCount(RobotType type, int count) {
        this.type = type;
        this.count = count;
    }

    /**
     * Creates a copy of the other zombie count.
     *
     * @param other the zombie count to copy.
     */
    public ZombieCount(ZombieCount other) {
        this.type = other.getType();
        this.count = other.getCount();
    }

    /**
     * Returns the type of zombies spawned.
     *
     * @return the type of zombies spawned.
     */
    public RobotType getType() {
        return type;
    }

    /**
     * Returns the number of zombies spawned per den.
     *
     * @return the number of zombies spawned per den.
     */
    public int getCount() {
        return count;
    }

    /**
     * Implements compareTo for ZombieCount. Compares types and count (in
     * that order).
     *
     * @param other ZombieCount to compare to.
     * @return a compareTo result.
     */
    public int compareTo(ZombieCount other) {
        if (this.type != other.type) {
            return this.type.ordinal() - other.type.ordinal();
        } else {
            return this.count - other.count;
        }
    }
}
