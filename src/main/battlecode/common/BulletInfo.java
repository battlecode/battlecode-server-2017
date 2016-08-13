package battlecode.common;

/**
 * Struct that stores basic information that was 'sensed' of a Bullet. This
 * info is ephemeral and there is no guarantee any of it will remain the same
 * between rounds.
 */
public class BulletInfo {

    /**
     * The unique ID of the bullet.
     */
    public final int ID;

    /**
     * The speed at which the bullet is traveling in
     * terms of units per turn
     */
    public final double speed;

    /**
     * The direction in which the bullet is moving
     */
    public final Direction dir;

    /**
     * The current location of the tree.
     */
    public final MapLocation location;

    public BulletInfo(int ID, MapLocation location, Direction dir, double speed) {
        this.ID = ID;
        this.dir = dir;
        this.speed = speed;
        this.location = location;
    }

    public int hashCode() {
        return ID;
    }

    @Override
    public String toString() {
        return "BulletInfo{" +
                "ID=" + ID +
                ", location=" + location +
                ", speed=" + speed +
                ", direction=" + dir +
                '}';
    }

}
