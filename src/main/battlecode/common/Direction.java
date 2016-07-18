package battlecode.common;

public class Direction {
    
    public final float dx, dy;

    Direction(MapLocation start, MapLocation finish) {
        this.dx = finish.x-start.x;
        this.dy = finish.y-start.y;
    }
    
    Direction(float dx, float dy) {
        this.dx = dx;
        this.dy = dy;
    }
    
    Direction(double angleDegrees, double radius) {
        this.dx = (float)(radius*Math.cos(Math.toRadians(angleDegrees)));
        this.dy = (float)(radius*Math.sin(Math.toRadians(angleDegrees)));
    }

    /**
     * Computes the direction opposite this one.
     *
     * @return the direction pointing in the opposite direction
     *
     * @battlecode.doc.costlymethod
     */
    public Direction opposite() {
        return new Direction(-dx,-dy);
    }

    /**
     * Computes the direction angleDegrees to the left (counter-clockwise)
     * of this one.
     * 
     * @param angleDegrees number of degrees to rotate.
     *
     * @return the direction angleDegrees degrees left of this one.
     * 
     * @battlecode.doc.costlymethod
     */
    public Direction rotateLeft(double angleDegrees) {
        double angleRads = Math.toDegrees(angleDegrees);
        return rotateLeftRads(angleRads);
    }

    /**
     * Computes the direction angleDegrees to the right (clockwise) of this
     * one.
     * 
     * @param angleDegrees number of degrees to rotate.
     *
     * @return the direction angleDegrees right of this one.
     *
     * @battlecode.doc.costlymethod
     */
    public Direction rotateRight(double angleDegrees) {
        double angleRads = Math.toDegrees(angleDegrees);
        return rotateLeftRads(-angleRads);
    }
    
    /**
     * Computes the direction angleRads (radians) to the left (counter-clockwise)
     * of this one.
     * 
     * @param angleRads number of radians to rotate.
     * 
     * @return the direction angleRads left of this one.
     */
    public Direction rotateLeftRads(double angleRads) {
        double newDx = dx * Math.cos(angleRads) - dy * Math.sin(angleRads);
        double newDy = dx * Math.sin(angleRads) + dy * Math.cos(angleRads);
        return new Direction((float)newDx,(float)newDy);
    }
    
    /**
     * Computes the direction angleRads (radians) to the right (clockwise) of
     * this one.
     * 
     * @param angleRads number of radians to rotate.
     * 
     * @return the direction angleRads right of this one.
     */
    public Direction rotateRightRads(double angleRads) {
        return rotateLeftRads(-angleRads);
    }
    
    /**
     * Rescales the direction vector to have a new radius
     * 
     * @param radius the new radius for the direction
     * 
     * @return a direction with the new radius
     */
    public Direction rescale(double radius) {
        double scalar = radius/Math.sqrt(dx*dx+dy*dy);
        float newDx = (float)(dx*scalar);
        float newDy = (float)(dy*scalar);
        return new Direction(newDx,newDy);
    }
}
