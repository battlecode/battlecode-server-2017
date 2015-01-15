package battlecode.common;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class is an immutable representation of two-dimensional coordinates
 * in the battlecode world.
 */
public final class MapLocation implements Serializable, Comparable<MapLocation> {

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
     * A comparison function for MapLocations. Smaller rows go first, with ties broken by smaller columns.
     *
     * @param other the MapLocation to compare to.
     * @return whether this MapLocation goes before the other one.
     */
    public int compareTo(MapLocation other) {
        if (x != other.x) {
            return x - other.x;
        } else {
            return y - other.y;
        }
    }

    /**
     * Two MapLocations are regarded as equal iff
     * their coordinates are the same.
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
     * @return the new MapLocation that is the translated version of the original.
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

	/**
	 * Returns an array of all MapLocations within a certain radius squared 
	 * of a specified location (cannot be called with radiusSquared greater than 100).
	 *
	 * @param center the center of the search
	 * @param radiusSquared the radius of the search, which must be at most 100.
	 * @return all MapLocations (both on the map and outside the map) within 
	 * radiusSquared distance of center.
     * @throws IllegalArgumentException if the radiusSquared is greater than 100 or is negative.
	 */
    public static MapLocation[] getAllMapLocationsWithinRadiusSq(MapLocation center, int radiusSquared) {
        ArrayList<MapLocation> locations = new ArrayList<MapLocation>();

        if (radiusSquared > 100 || radiusSquared < 0) {
            throw new IllegalArgumentException("radiusSquared argument for getAllMapLocationsWithinRadiusSq cannot be greater than 100 or negative. However, since Battlecode is open source, you are free to use the source code of this method to implement it yourself.");
        }    

        int radius = (int) Math.sqrt(radiusSquared);

        int minXPos = center.x - radius;
        int maxXPos = center.x + radius;
        int minYPos = center.y - radius;
        int maxYPos = center.y + radius;

        for (int x = minXPos; x <= maxXPos; x++) {
            for (int y = minYPos; y <= maxYPos; y++) {
                MapLocation loc = new MapLocation(x, y);
                if (loc.distanceSquaredTo(center) <= radiusSquared)
                    locations.add(loc);
            }
        }

        return locations.toArray(new MapLocation[locations.size()]);
    }
}
