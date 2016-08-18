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
    public final double radians;

    /**
     * @param radians the radians at which you wish this direction
     *                to represent based off of the unit circle
     */
    Direction(double radians){
        this.radians = radians % (2*Math.PI);
    }

    /**
     * Creates a new Direction instance to represent the direction
     * in which the vector created by dx and dy points. Requires
     * dx or dy to be non-zero
     *
     * @param dx the x component of the vector
     * @param dy the y component of the vector
     */
    Direction(double dx, double dy) {
        this.radians = Math.atan2(dy, dx) % (2*Math.PI);
    }

    /**
     * Creates a new Direction instance to represent the direction
     * in which the vector from start to finish points. Requires
     * start and finish to not be the same location
     *
     * @param start the starting point of the vector
     * @param finish the ending point of the vector
     */
    Direction(MapLocation start, MapLocation finish) {
        this(finish.x - start.x, finish.y - start.y);
    }

    /**
     * Computes the angle in degrees at which this direction faces
     *
     * @return the angle in degrees this direction faces
     *
     * @battlecode.doc.costlymethod
     */
    public double getAngleDegrees() {
        return Math.toDegrees(radians);
    }

    /**
     * Computes the direction opposite this one.
     *
     * @return the direction pointing in the opposite direction
     *
     * @battlecode.doc.costlymethod
     */
    public Direction opposite() {
        return rotateLeftRads(Math.PI);
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
    public Direction rotateLeftDegrees(double angleDegrees) {
        return rotateLeftRads(Math.toRadians(angleDegrees));
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
    public Direction rotateRightDegrees(double angleDegrees) {
        return rotateRightRads(Math.toRadians(angleDegrees));
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
    public Direction rotateLeftRads(double angleRads) {
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
    public Direction rotateRightRads(double angleRads) {
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
    public double radiansBetween(Direction other){
        double radiansBetween = this.radians - other.radians;
        radiansBetween = radiansBetween % (2 * Math.PI);
        if(radiansBetween > Math.PI){
            radiansBetween = (2 * Math.PI) - radiansBetween;
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
    public double degreesBetween(Direction other){
        return Math.toDegrees(radiansBetween(other));
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
