package battlecode.common;

import org.apache.commons.lang3.StringUtils;

import battlecode.schema.*;

import java.io.Serializable;

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
        float x = Float.valueOf(coord[0].trim());
        float y = Float.valueOf(coord[1].trim());

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
     * Computes the Euclidean distance from this location to the specified
     * location.
     *
     * @param location the location to compute the distance to
     * @return the distance to the given location
     *
     * @battlecode.doc.costlymethod
     */
    public final double distanceTo(MapLocation location) {
        float dx = this.x - location.x;
        float dy = this.y - location.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Computes the squared distance from this location to the specified
     * location.
     *
     * @param location the location to compute the squared distance to
     * @return the squared distance to the given location
     *
     * @battlecode.doc.costlymethod
     */
    public final double distanceSquaredTo(MapLocation location) {
        float dx = this.x - location.x;
        float dy = this.y - location.y;
        return dx * dx + dy * dy;
    }

    /**
     * Determines whether this location is within a specified distance
     * from target location.
     *
     * @param location the location to test
     * @param dist the distance for the location to be within
     * @return true if the given location is within dist to this one,
     *         or false if it isn't
     *
     * @battlecode.doc.costlymethod
     */
    public final boolean isWithinDistance(MapLocation location, float dist) {
        return this.distanceTo(location) <= dist;
    }

    /**
     * Determines whether this location is within the action radius of the
     * given robot
     *
     * @param robot the robot to test
     * @return true if this location is within robot's action radius,
     *         false otherwise
     *
     * @battlecode.doc.costlymethod
     */
    public final boolean isWithinActionRadius(RobotInfo robot){
        return isWithinDistance(robot.location, robot.type.actionRadius);
    }

    /**
     * Determines whether this location is within the sight radius of the
     * given robot
     *
     * @param robot the robot to test
     * @return true if this location is within robot's sight radius,
     *         false otherwise
     *
     * @battlecode.doc.costlymethod
     */
    public final boolean isWithinSightRadius(RobotInfo robot){
        return isWithinDistance(robot.location, robot.type.sightRadius);
    }

    /**
     * Determines whether this location is within the bullet sight radius of the
     * given robot
     *
     * @param robot the robot to test
     * @return true if this location is within robot's bullet sight radius,
     *         false otherwise
     *
     * @battlecode.doc.costlymethod
     */
    public final boolean isWithinBulletSightRadius(RobotInfo robot){
        return isWithinDistance(robot.location, robot.type.bulletSightRadius);
    }

    /**
     * Returns the Direction from this MapLocation to <code>location</code>.
     * If <code>location</code> is null then the return value is null.
     * If <code>location</code> equals this location then the return value is null.
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
        if(this.equals(location)){
            return null;
        }
        return new Direction(this,location);
    }

    /**
     * Returns a new MapLocation object representing a location
     * one unit in distance from this one in the given direction.
     *
     * @param direction the direction to add to this location
     * @return a MapLocation for the location one unit in distance in the given
     *         direction.
     *
     * @battlecode.doc.costlymethod
     */
    public final MapLocation add(Direction direction) {
        float dx = (float)Math.cos(direction.radians);
        float dy = (float)Math.sin(direction.radians);
        return new MapLocation(x + dx, y + dy);
    }

    /**
     * Returns a new MapLocation object representing a location
     * {@code dist} units away from this one in the given direction.
     *
     * @param direction the direction to add to this location
     * @param dist  the distance the locations should be apart
     * @return a MapLocation for the location dist away from this location
     *         in the given direction.
     *
     * @battlecode.doc.costlymethod
     */
    public final MapLocation add(Direction direction, double dist) {
        float dx = (float)(dist * Math.cos(direction.radians));
        float dy = (float)(dist * Math.sin(direction.radians));
        return new MapLocation(x + dx, y + dy);
    }

    /**
     * Returns a new MapLocation object representing a location
     * one unit in distance from this one in the opposite direction
     * of the given direction.
     *
     * @param direction the direction to subtract from this location
     * @return a MapLocation for the location one unit in distance in the
     *         opposite of the given direction.
     *
     * @battlecode.doc.costlymethod
     */
    public final MapLocation subtract(Direction direction) {
        return this.add(direction.opposite());
    }

    /**
     * Returns a new MapLocation object representing a location
     * {@code dist} units in distance from this one in the opposite direction
     * of the given direction.
     *
     * @param direction the direction to subtract from this location
     * @param dist  the distance the locations should be apart
     * @return a MapLocation for the location dist away from this location
     *         in the opposite of the given direction.
     *
     * @battlecode.doc.costlymethod
     */
    public final MapLocation subtract(Direction direction, double dist) {
        return this.add(direction.opposite(), dist);
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
    public final MapLocation translate(float dx, float dy) {
        return new MapLocation(x + dx, y + dy);
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
