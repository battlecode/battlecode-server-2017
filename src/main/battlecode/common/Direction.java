package battlecode.common;

/**
 * This class is an immutable representation of a direction
 * in the battlecode world.
 */
public final strictfp class Direction {

    /**
     * Direction that represents pointing north (up on screen).
     */
    public static final Direction NORTH =  Direction.getNorth();

    /**
     * Direction that represents pointing east (right on screen).
     */
    public static final Direction EAST = Direction.getEast();

    /**
     * Direction that represents pointing south (down on screen).
     */
    public static final Direction SOUTH = Direction.getSouth();

    /**
     * Direction that represents pointing west (left on screen).
     */
    public static final Direction WEST = Direction.getWest();

    /**
     * The radians at which this direction is facing based off of
     * the unit circle; i.e. facing right would have 0.0 radians,
     * up would have PI/2 radians, etc.
     * Note: radians = (-Math.PI, Math.PI]
     */
    public final float radians;

    /**
     * @param radians the radians at which you wish this direction
     *                to represent based off of the unit circle
     */
    public Direction(float radians) {
        assertValid(radians);
        this.radians = reduce(radians);
    }

    /**
     * If another Direction equals this one. Realistically, you should never
     * want to use this due to floating point rounding errors.
     * @param other Another Direction
     * @return If the two Directions are equal
     */
    @Override
    public boolean equals(Object other) {
        if(other == null) return false;
        if(other == this) return true;
        if(!(other instanceof Direction)) return false;
        return this.radians == ((Direction) other).radians;
    }

    /**
     * If two Directions are equal to each other, with an error range epsilon radians.
     *
     * @param other Another Direction.
     * @param epsilon Amount of error (in radians) to allow in the comparison
     * @return If the two Directions are equal within error
     */
    public boolean equals(Direction other, float epsilon) {
        float difference = this.radiansBetween(other);
        return (Math.abs(difference) <= epsilon);
    }

    /**
     * Creates a new Direction instance to represent the direction
     * in which the vector created by dx and dy points. Requires
     * dx or dy to be non-zero.
     *
     * @param dx the x component of the vector
     * @param dy the y component of the vector
     */
    public Direction(float dx, float dy) {
        assertValid(dx);
        assertValid(dy);
        if (dx == 0 && dy == 0) {
            dy = 1;
        }
        this.radians = reduce((float)Math.atan2(dy, dx));
    }

    /**
     * Creates a new Direction instance to represent the direction
     * in which the vector from start to finish points. Requires
     * start and finish to not be the same location.
     *
     * @param start  the starting point of the vector
     * @param finish the ending point of the vector
     */
    public Direction(MapLocation start, MapLocation finish) {
        this(finish.x - start.x, finish.y - start.y);
    }

    /**
     * Creates a instance of Direction that represents pointing east (right on screen).
     *
     * @return Direction instance facing east.
     * @battlecode.doc.costlymethod
     */
    public static Direction getEast() {
        return new Direction(1, 0);
    }

    /**
     * Creates a instance of Direction that represents pointing north (up on screen).
     *
     * @return Direction instance facing north.
     * @battlecode.doc.costlymethod
     */
    public static Direction getNorth() {
        return new Direction(0, 1);
    }

    /**
     * Creates a instance of Direction that represents pointing west (left on screen).
     *
     * @return Direction instance facing west.
     * @battlecode.doc.costlymethod
     */
    public static Direction getWest() {
        return new Direction(-1, 0);
    }

    /**
     * Creates a instance of Direction that represents pointing south (down on screen).
     *
     * @return Direction instance facing south.
     * @battlecode.doc.costlymethod
     */
    public static Direction getSouth() {
        return new Direction(0,  -1);
    }

    /**
     * Computes the signed distance traveled on the x-axis when traveling travelDist
     * units in this Direction.
     *
     * @param travelDist the total distance to travel
     * @return the signed distance traveled on the x-axis.
     * @battlecode.doc.costlymethod
     */
    public float getDeltaX(float travelDist) {
        return (float) (travelDist * Math.cos(this.radians));
    }

    /**
     * Computes the signed distance traveled on the y-axis when traveling travelDist
     * units in this Direction.
     *
     * @param travelDist the total distance to travel
     * @return the signed distance traveled on the x-axis.
     * @battlecode.doc.costlymethod
     */
    public float getDeltaY(float travelDist) {
        return (float) (travelDist * Math.sin(this.radians));
    }

    /**
     * Computes the angle in degrees at which this direction faces.
     *
     * @return the angle in degrees this direction faces.
     * @battlecode.doc.costlymethod
     */
    public float getAngleDegrees() {
        return (float) Math.toDegrees(radians);
    }

    /**
     * Computes the direction opposite this one.
     *
     * @return the direction pointing in the opposite direction.
     * @battlecode.doc.costlymethod
     */
    public Direction opposite() {
        return rotateLeftRads((float) Math.PI);
    }

    /**
     * Computes the direction angleDegrees to the left (counter-clockwise)
     * of this one.
     *
     * @param angleDegrees number of degrees to rotate
     * @return the direction angleDegrees degrees left of this one.
     * @battlecode.doc.costlymethod
     */
    public Direction rotateLeftDegrees(float angleDegrees) {
        return rotateLeftRads((float) Math.toRadians(angleDegrees));
    }

    /**
     * Computes the direction angleDegrees to the right (clockwise) of this
     * one.
     *
     * @param angleDegrees number of degrees to rotate
     * @return the direction angleDegrees right of this one.
     * @battlecode.doc.costlymethod
     */
    public Direction rotateRightDegrees(float angleDegrees) {
        return rotateRightRads((float) Math.toRadians(angleDegrees));
    }

    /**
     * Computes the direction angleRads (radians) to the left (counter-clockwise)
     * of this one.
     *
     * @param angleRads number of radians to rotate
     * @return the direction angleRads left of this one.
     * @battlecode.doc.costlymethod
     */
    public Direction rotateLeftRads(float angleRads) {
        return new Direction(this.radians + angleRads);
    }

    /**
     * Computes the direction angleRads (radians) to the right (clockwise) of
     * this one.
     *
     * @param angleRads number of radians to rotate
     * @return the direction angleRads right of this one.
     * @battlecode.doc.costlymethod
     */
    public Direction rotateRightRads(float angleRads) {
        return rotateLeftRads(-angleRads);
    }

    /**
     * Computes the angle between the given direction and this direction in radians.
     * Returned value will be in the range (-Math.PI, Math.PI].
     * Positive values mean 'other' is to the left, negative values mean 'other' is to
     * the right.
     *
     * @param other the direction you wish to find the angle between
     * @return the angle in radians between this direction and the given direction
     * in the range of (-Math.PI, Math.PI].
     * @battlecode.doc.costlymethod
     */
    public float radiansBetween(Direction other) {
        return reduce(other.radians - this.radians);
    }

    /**
     * Computes the angle between the given direction and this direction in degrees.
     * Returned value will be in the range (-180,180].
     *
     * @param other the direction you wish to find the angle between
     * @return the angle in degrees between this direction and the given direction
     * in the range of (-180,180].
     * @battlecode.doc.costlymethod
     */
    public float degreesBetween(Direction other) {
        return (float) Math.toDegrees(radiansBetween(other));
    }

    public int hashCode() {
        return Float.floatToIntBits(radians) * 13;
    }

    @Override
    public String toString() {
        return "Direction: " +
                "radians=" + radians +
                ", degrees=" + Math.toDegrees(radians);
    }

    // Internally used to keep angles in the range (-Math.PI,Math.PI]
    private float reduce(float rads) {
        if(rads <= -(float)Math.PI) {
            int circles = (int)Math.ceil(-(rads+Math.PI)/(2*Math.PI));
            return rads + (float)(Math.PI*2*circles);
        } else if (rads > (float)Math.PI) {
            int circles = (int)Math.ceil((rads-Math.PI)/(2*Math.PI));
            return rads - (float)(Math.PI*2*circles);
        }
        return rads;
    }

    // Stop NaN or infinity directions from messing things up
    private void assertValid(float num) {
        if(Float.isNaN(num)) {
            throw new RuntimeException("Direction can not take a NaN float as an argument");
        } else if (Float.isInfinite(num)) {
            throw new RuntimeException("Direction can not take +/- infinity as an argument");
        }
    }
}
