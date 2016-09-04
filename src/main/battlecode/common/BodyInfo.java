package battlecode.common;

/**
 * Stores information about a Object/Body in the game world
 */
public interface BodyInfo {

    /**
     * @return the center location of this body
     */
    MapLocation getLocation();

    /**
     * @return the radius of this body
     */
    float getRadius();

    /**
     * @return true if this body is a robot, false otherwise
     */
    boolean isRobot();

    /**
     * @return true if this body is a tree, false otherwise
     */
    boolean isTree();

    /**
     * @return true if this body is a bullet, false otherwise
     */
    boolean isBullet();

}
