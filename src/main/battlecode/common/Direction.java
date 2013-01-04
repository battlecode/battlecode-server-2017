package battlecode.common;

/**
 * This enumeration represents a direction from one MapLocation to another.
 * There is a direction for each of the cardinals (north, south, east, west),
 * and each of diagonals (northwest, southwest, northeast, southeast).
 * There is also NONE, representing no direction, and OMNI, representing
 * all directions.
 * <p/>
 * Since Direction is a Java 1.5 enum, you can use it in <code>switch</code>
 * statements, it has all the standard enum methods (<code>valueOf</code>,
 * <code>values</code>, etc.), and you can safely use <code>==</code> for
 * equality tests.
 */
public enum Direction {

    NORTH(0, -1),
    NORTH_EAST(1, -1),
    EAST(1, 0),
    SOUTH_EAST(1, 1),
    SOUTH(0, 1),
    SOUTH_WEST(-1, 1),
    WEST(-1, 0),
    NORTH_WEST(-1, -1),
    /**
     * No direction.
     */
    NONE(0, 0),
    /**
     * All directions.
     */
    OMNI(0, 0);
    public final int dx, dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Determines whether or not this direction is a diagonal one.
     *
     * @return true if this Direction is diagonal (northwest, northeast,
     *         southwest, southeast) or false if it's a cardinal, NONE, or OMNI.
     */
    public boolean isDiagonal() {
        return (ordinal() < 8 && ordinal() % 2 == 1);
    }

    /**
     * Computes the direction opposite this one.
     *
     * @return the direction pointing in the opposite direction
     *         to this one, or NONE if it's NONE, or OMNI if it's OMNI
     */
    public Direction opposite() {
        if (ordinal() >= 8) {
            return this;
        }
        return Direction.values()[(ordinal() + 4) % 8];
    }

    /**
     * Computes the direction 45 degrees to the left (counter-clockwise)
     * of this one.
     *
     * @return the direction 45 degrees left of this one, or NONE if it's NONE,
     *         or OMNI if it's OMNI
     */
    public Direction rotateLeft() {
        if (ordinal() >= 8) {
            return this;
        }
        if (ordinal() == 0) {
            return Direction.values()[7];
        }
        return Direction.values()[(ordinal() - 1)];
    }

    /**
     * Computes the direction 45 degrees to the right (clockwise)
     * of this one.
     *
     * @return the direction 45 degrees right of this one, or NONE if it's NONE,
     *         or OMNI if it's OMNI
     */
    public Direction rotateRight() {
        if (ordinal() >= 8) {
            return this;
        }
        if (ordinal() == 7) {
            return Direction.values()[0];
        }
        return Direction.values()[(ordinal() + 1)];
    }
}
