package battlecode.common;

import org.apache.commons.lang3.StringUtils;

import battlecode.schema.*;

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
    public final float x;
    /**
     * The y-coordinate.
     */
    public final float y;

    /**
     * Creates a new MapLocation representing the location
     * with the given coordinates.
     *
     * @param x the x-coordinate of the location
     * @param y the y-coordinate of the location
     *
     * @battlecode.doc.costlymethod
     */
    public MapLocation(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Creates a new MapLocation from a Vec (flatbuffer object)
     * 
     * @param v Vec object
     */
    public MapLocation(Vec v) {
        this.x = v.x();
        this.y = v.y();
    }

    /**
     * A comparison function for MapLocations. Smaller x values go first, with ties broken by smaller y values.
     *
     * @param other the MapLocation to compare to.
     * @return whether this MapLocation goes before the other one.
     *
     * @battlecode.doc.costlymethod
     */
    public int compareTo(MapLocation other) {
        if (x != other.x) {
            if(x < other.x)
                return -1;
            else
                return 1;
        } else {
            if(y < other.y)
                return -1;
            else if (y > other.y)
                return 1;
            else
                return 0;
        }
    }

    /**
     * Two MapLocations are regarded as equal iff
     * their coordinates are the same.
     * {@inheritDoc}
     *
     * @battlecode.doc.costlymethod
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
     *
     * @battlecode.doc.costlymethod
     */
    @Override
    public int hashCode() {
        return Float.floatToIntBits(this.x) * 13 + Float.floatToIntBits(this.y) * 23;
    }

    public static MapLocation valueOf(String s) {
        String[] coord = StringUtils.replaceChars(s, "[](){}", null).split(",");
        if (coord.length != 2)
            throw new IllegalArgumentException("Invalid map location string");
        int x = Integer.valueOf(coord[0].trim());
        int y = Integer.valueOf(coord[1].trim());

        return new MapLocation(x, y);
    }

    /**
     * {@inheritDoc}
     *
     * @battlecode.doc.costlymethod
     */
    public String toString() {
        return String.format("[%f, %f]", this.x, this.y);
    }

    /**
     * Computes the square of the distance from this location to the specified
     * location.
     *
     * @param location the location to compute the distance squared to
     * @return the distance to the given location squared
     *
     * @battlecode.doc.costlymethod
     */
    public final double distanceTo(MapLocation location) {
        float dx = this.x - location.x;
        float dy = this.y - location.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Determines whether this location is adjacent to the specified
     * location. Note that squares cannot be adjacent to themselves.
     *
     * @param location the location to test
     * @return true if the given location is adjacent to this one,
     *         or false if it isn't
     *
     * @battlecode.doc.costlymethod
     */
    public final boolean isWithinDistance(MapLocation location, float dist) {
        return this.distanceTo(location) <= dist;
    }

    /**
     * Returns the Direction from this MapLocation to <code>location</code>.
     * If the locations are equal this method returns Direction.OMNI. If
     * <code>location</code> is null then the return value is Direction.NONE.
     *
     * @param location The location to which the Direction will be calculated
     * @return The Direction to <code>location</code> from this MapLocation.
     *
     * @battlecode.doc.costlymethod
     */
    public final Direction directionTo(MapLocation location) {
        if(location == null) {
            return null;
        }
        
        float dx = location.x - this.x;
        float dy = location.y - this.y;

        return new Direction(dx,dy);
    }

    /**
     * Returns a new MapLocation object representing a location
     * one square from this one in the given direction.
     *
     * @param direction the direction to add to this location
     * @return a MapLocation for the location one square in the given
     *         direction, or this location if the direction is NONE or OMNI
     *
     * @battlecode.doc.costlymethod
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
     *
     * @battlecode.doc.costlymethod
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
     *
     * @battlecode.doc.costlymethod
     */
    public final MapLocation add(float dx, float dy) {
        return new MapLocation(x + dx, y + dy);
    }

    /**
     * Returns a new MapLocation object representing a location
     * one square from this one in the opposite of the given direction.
     *
     * @param direction the direction to subtract from this location
     * @return a MapLocation for the location one square opposite the given
     *         direction, or this location if the direction is NONE or OMNI
     *
     * @battlecode.doc.costlymethod
     */
    public final MapLocation subtract(Direction direction) {
        return this.add(direction.opposite());
    }

    /**
     * For use by serializers.
     *
     * @battlecode.doc.costlymethod
     */
    private MapLocation() {
        this(0,0);
    }
}
