package battlecode.common;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * This class is an immutable representation of two-dimensional coordinates
 * in the battlecode world.
 */
public final strictfp class MapLocation implements Serializable, Comparable<MapLocation> {

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
        this.x = x == Float.NaN ? 0 : x;
        this.y = y == Float.NaN ? 0 : y;
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
     * Checks where two given circles collide at any point.
     *
     * @param center1 the center of the first circle
     * @param radius1 the radius of the first circle
     * @param center2 the center of the second circle
     * @param radius2 the radius of the second circle
     * @return true if the given circles collide at any point, false otherwise.
     *
     * @battlecode.doc.costlymethod
     */
    public static boolean doCirclesCollide(MapLocation center1, float radius1, MapLocation center2, float radius2){
        return center1.distanceTo(center2) <= radius1 + radius2;
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
    public final float distanceTo(MapLocation location) {
        float dx = this.x - location.x;
        float dy = this.y - location.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
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
    public final float distanceSquaredTo(MapLocation location) {
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
     * @return true if the given location is within dist to this one; false otherwise
     *
     * @battlecode.doc.costlymethod
     */
    public final boolean isWithinDistance(MapLocation location, float dist) {
        return this.distanceTo(location) <= dist;
    }

    /**
     * Determines whether this location is within one stride of the given robot.
     *
     * @param robot the robot to test
     * @return true if this location is within one stride of the given robot; false otherwise
     *
     * @battlecode.doc.costlymethod
     */
    public final boolean isWithinStrideDistance(RobotInfo robot){
        return isWithinDistance(robot.location, 2*robot.type.bodyRadius);
    }

    /**
     * Determines whether this location is within the sensor radius of the
     * given robot.
     *
     * @param robot the robot to test
     * @return true if this location is within the robot's sensor radius,
     *         false otherwise
     *
     * @battlecode.doc.costlymethod
     */
    public final boolean isWithinSensorRadius(RobotInfo robot){
        return isWithinDistance(robot.location, robot.type.sensorRadius);
    }

    /**
     * Determines whether this location is within the bullet sight radius of the
     * given robot.
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
        if(direction == null) {
            return new MapLocation(x ,y);
        }
        float dx = (float)Math.cos(direction.radians);
        float dy = (float)Math.sin(direction.radians);
        return new MapLocation(x + dx, y + dy);
    }

    /**
     * Returns a new MapLocation object representing a location
     * one unit in distance from this one in the given direction
     * represented in radians.
     *
     * @param radians the radians of the direction to add to this location,
     *                note that 0 radians points right
     * @return a MapLocation for the location one unit in distance in the given
     *         direction.
     *
     * @battlecode.doc.costlymethod
     */
    public final MapLocation add(float radians) {
        return this.add(new Direction(radians));
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
    public final MapLocation add(Direction direction, float dist) {
        if(direction == null) {
            return new MapLocation(x ,y);
        }
        float dx = (float)(dist * Math.cos(direction.radians));
        float dy = (float)(dist * Math.sin(direction.radians));
        return new MapLocation(x + dx, y + dy);
    }

    /**
     * Returns a new MapLocation object representing a location
     * {@code dist} units away from this one in the given direction
     * represented in radians.
     *
     * @param radians the radians of the direction to add to this location,
     *                note that 0 radians points right
     * @param dist  the distance the locations should be apart
     * @return a MapLocation for the location dist away from this location
     *         in the given direction.
     *
     * @battlecode.doc.costlymethod
     */
    public final MapLocation add(float radians, float dist) {
        return this.add(new Direction(radians), dist);
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
        if(direction == null) {
            return new MapLocation(x,y);
        }
        return this.add(direction.opposite());
    }

    /**
     * Returns a new MapLocation object representing a location
     * one unit in distance from this one in the opposite direction of the
     * given direction represented in radians.
     *
     * @param radians the radians of the direction to subtract from this location,
     *                note that 0 radians points right
     * @return a MapLocation for the location one unit in distance in the given
     *         direction.
     *
     * @battlecode.doc.costlymethod
     */
    public final MapLocation subtract(float radians) {
        return this.subtract(new Direction(radians));
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
    public final MapLocation subtract(Direction direction, float dist) {
        if(direction == null) {
            return new MapLocation(x,y);
        }
        return this.add(direction.opposite(), dist);
    }

    /**
     * Returns a new MapLocation object representing a location
     * {@code dist} units in distance from this one in the opposite direction of the
     * given direction represented in radians.
     *
     * @param radians the radians of the direction to subtract from this location,
     *                note that 0 radians points right
     * @param dist  the distance the locations should be apart
     * @return a MapLocation for the location one unit in distance in the given
     *         direction.
     *
     * @battlecode.doc.costlymethod
     */
    public final MapLocation subtract(float radians, float dist) {
        return this.subtract(new Direction(radians), dist);
    }

    /**
     * Returns a new MapLocation object translated from this location
     * by a fixed amount.
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
