package battlecode.common;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

// We don't want contestants to be able to subclass MapLocation because
// that would allow them to pass arbitrary data structures in messages

/**
 * This class is an immutable representation of two-dimensional coordinates
 * in the battlecode world.
 */
public final class MapLocation implements Serializable {

    private static final long serialVersionUID = -8945913587066072824L;
    /**
     * The x-coordinate.
     */
    public final int x;
    /**
     * The y-coordinate.
     */
    public final int y;

    /**
     * Creates a new MapLocation representing the location
     * with the given coordinates.
     *
     * @param x the x-coordinate of the location
     * @param y the y-coordinate of the location
     */
    public MapLocation(int x, int y) {

        this.x = x;
        this.y = y;

    }

    /**
     * Two MapLocations are regarded as equal iff
     * their coordinates are the same.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof MapLocation)) {
            return false;
        }

        return (((MapLocation) obj).x == this.x) && (((MapLocation) obj).y == this.y);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        return this.x * 13 + this.y * 23;

    }

    public static MapLocation valueOf(String s) {
        String[] coord = StringUtils.replaceChars(s, "[](){}", null).split(",");
        if (coord.length != 2)
            throw new IllegalArgumentException("Invalid map location string");
        int x = Integer.valueOf(coord[0].trim());
        int y = Integer.valueOf(coord[1].trim());

        MapLocation ml = new MapLocation(x, y);

        return ml;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return String.format("[%d, %d]", this.x, this.y);
    }

    /**
     * Computes the square of the distance from this location to the specified
     * location.
     *
     * @param location the location to compute the distance squared to
     * @return the distance to the given location squared
     */
    public final int distanceSquaredTo(MapLocation location) {
        int dx = this.x - location.x;
        int dy = this.y - location.y;
        return dx * dx + dy * dy;
    }

    /**
     * Determines whether this location is adjacent to the specified
     * location. Note that squares cannot be adjacent to themselves.
     *
     * @param location the location to test
     * @return true if the given location is adjacent to this one,
     *         or false if it isn't
     */
    public final boolean isAdjacentTo(MapLocation location) {

        int distTo;
        if ((distTo = this.distanceSquaredTo(location)) == 1 || distTo == 2) {
            return true;
        }

        return false;

    }

    /**
     * Returns the Direction from this MapLocation to <code>location</code>.
     * If the locations are equal this method returns Direction.OMNI. If
     * <code>location</code> is null then the return value is Direction.NONE.
     *
     * @param location The location to which the Direction will be calculated
     * @return The Direction to <code>location</code> from this MapLocation.
     */
    public final Direction directionTo(MapLocation location) {
        double dx = location.x - this.x;
        double dy = location.y - this.y;

        if (Math.abs(dx) >= 2.414 * Math.abs(dy)) {
            if (dx > 0) {
                return Direction.EAST;
            } else if (dx < 0) {
                return Direction.WEST;
            } else {
                return Direction.OMNI;
            }
        } else if (Math.abs(dy) >= 2.414 * Math.abs(dx)) {
            if (dy > 0) {
                return Direction.SOUTH;
            } else {
                return Direction.NORTH;
            }
        } else {
            if (dy > 0) {
                if (dx > 0) {
                    return Direction.SOUTH_EAST;
                } else {
                    return Direction.SOUTH_WEST;
                }
            } else {
                if (dx > 0) {
                    return Direction.NORTH_EAST;
                } else {
                    return Direction.NORTH_WEST;
                }
            }
        }
    }

    /**
     * Returns a new MapLocation object representing a location
     * one square from this one in the given direction.
     *
     * @param direction the direction to add to this location
     * @return a MapLocation for the location one square in the given
     *         direction, or this location if the direction is NONE or OMNI
     */
    public final MapLocation add(Direction direction) {

        return new MapLocation(x + direction.dx, y + direction.dy);
    }

    /**
     * Returns a new MapLocation object representing a location
     * {@code multiple} squares from this one in the given direction.
     *
     * @param direction the direction to add to this location
     * @param multiple  the number of squares to add
     * @return a MapLocation for the location one square in the given
     *         direction, or this location if the direction is NONE or OMNI
     */
    public final MapLocation add(Direction direction, int multiple) {
        return new MapLocation(x + multiple * direction.dx, y + multiple * direction.dy);
    }

    /**
     * Returns a new MapLocation object translated from this location
     * by a fixed amount
     *
     * @param dx the amount to translate in the x direction
     * @param dy the amount to translate in the y direction
     */
    public final MapLocation add(int dx, int dy) {
        return new MapLocation(x + dx, y + dy);
    }

    /**
     * Returns a new MapLocation object representing a location
     * one square from this one in the opposite of the given direction.
     *
     * @param direction the direction to subtract from this location
     * @return a MapLocation for the location one square opposite the given
     *         direction, or this location if the direction is NONE or OMNI
     */
    public final MapLocation subtract(Direction direction) {
        return this.add(direction.opposite());
    }
}
