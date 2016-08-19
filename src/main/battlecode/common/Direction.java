package battlecode.common;

/**
 * This class is an immutable representation of a direction
 * in the battlecode world.
 */
public class Direction {

    /**
     * The radians at which this direction is facing based off of
     * the unit circle; i.e. facing right would have 0.0 radians,
     * up would have PI/2 radians, etc.
     * Note: radians = [0, 2*Math.PI)
     */
    public final float radians;

    /**
     * @param radians the radians at which you wish this direction
     *                to represent based off of the unit circle
     */
    public Direction(float radians){
        this.radians = radians % (2*(float) Math.PI);
    }

    /**
     * Creates a new Direction instance to represent the direction
     * in which the vector created by dx and dy points. Requires
     * dx or dy to be non-zero
     *
     * @param dx the x component of the vector
     * @param dy the y component of the vector
     */
    public Direction(float dx, float dy) {
        this.radians = (float) Math.atan2(dy, dx) % (2 * (float) Math.PI);
    }

    /**
     * Creates a new Direction instance to represent the direction
     * in which the vector from start to finish points. Requires
     * start and finish to not be the same location
     *
     * @param start the starting point of the vector
     * @param finish the ending point of the vector
     */
    public Direction(MapLocation start, MapLocation finish) {
        this(finish.x - start.x, finish.y - start.y);
    }

    /**
     * Computes the angle in degrees at which this direction faces
     *
     * @return the angle in degrees this direction faces
     *
     * @battlecode.doc.costlymethod
     */
    public float getAngleDegrees() {
        return (float) Math.toDegrees(radians);
    }

    /**
     * Computes the direction opposite this one.
     *
     * @return the direction pointing in the opposite direction
     *
     * @battlecode.doc.costlymethod
     */
    public Direction opposite() {
        return rotateLeftRads((float) Math.PI);
    }

    /**
     * Computes the direction angleDegrees to the left (counter-clockwise)
     * of this one.
     * 
     * @param angleDegrees number of degrees to rotate.
     * @return the direction angleDegrees degrees left of this one.
     * 
     * @battlecode.doc.costlymethod
     */
    public Direction rotateLeftDegrees(float angleDegrees) {
        return rotateLeftRads((float) Math.toRadians(angleDegrees));
    }

    /**
     * Computes the direction angleDegrees to the right (clockwise) of this
     * one.
     * 
     * @param angleDegrees number of degrees to rotate.
     * @return the direction angleDegrees right of this one.
     *
     * @battlecode.doc.costlymethod
     */
    public Direction rotateRightDegrees(float angleDegrees) {
        return rotateRightRads((float) Math.toRadians(angleDegrees));
    }
    
    /**
     * Computes the direction angleRads (radians) to the left (counter-clockwise)
     * of this one.
     * 
     * @param angleRads number of radians to rotate.
     * @return the direction angleRads left of this one.
     *
     * @battlecode.doc.costlymethod
     */
    public Direction rotateLeftRads(float angleRads) {
        return new Direction(this.radians + angleRads);
    }
    
    /**
     * Computes the direction angleRads (radians) to the right (clockwise) of
     * this one.
     * 
     * @param angleRads number of radians to rotate.
     * @return the direction angleRads right of this one.
     *
     * @battlecode.doc.costlymethod
     */
    public Direction rotateRightRads(float angleRads) {
        return rotateLeftRads(-angleRads);
    }

    /**
     * Computes the angle between the given direction and this direction in radians.
     * Returned value will be in the range [0, Math.PI]
     *
     * @param other the direction you wish to find the angle between
     * @return the angle in radians between this direction and the given direction
     * in the range of [0, Math.PI]
     */
    public float radiansBetween(Direction other){
        float radiansBetween = this.radians - other.radians;
        radiansBetween = radiansBetween % (2 * (float) Math.PI);
        if(radiansBetween > Math.PI){
            radiansBetween = (2 * (float) Math.PI) - radiansBetween;
        }
        return radiansBetween;
    }

    /**
     * Computes the angle between the given direction and this direction in degrees.
     * Returned value will be in the range [0, 180]
     *
     * @param other the direction you wish to find the angle between
     * @return the angle in degrees between this direction and the given direction
     * in the range of [0, 180]
     */
    public float degreesBetween(Direction other){
        return (float) Math.toDegrees(radiansBetween(other));
    }

    public int hashCode() {
        return Float.floatToIntBits((float)radians) * 13;
    }

    @Override
    public String toString() {
        return  "Direction: " +
                "radians=" + radians +
                ", degrees=" + Math.toDegrees(radians);
    }
}
