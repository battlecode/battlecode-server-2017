package battlecode.common;

/**
 * Struct that stores basic information that was 'sensed' of a Bullet. This
 * info is ephemeral and there is no guarantee any of it will remain the same
 * between rounds.
 */
public class BulletInfo implements BodyInfo{

    /**
     * The unique ID of the bullet.
     */
    public final int ID;

    /**
     * The speed at which the bullet is traveling in
     * terms of units per turn
     */
    public final float speed;

    /**
     * The damage this bullet deals on impact
     */
    public final float damage;

    /**
     * The direction in which the bullet is moving
     */
    public final Direction dir;

    /**
     * The current location of the tree.
     */
    public final MapLocation location;

    public BulletInfo(int ID, MapLocation location, Direction dir, float speed, float damage) {
        this.ID = ID;
        this.dir = dir;
        this.speed = speed;
        this.damage = damage;
        this.location = location;
    }

    /**
     * @return the speed at which the bullet is traveling in units per turn
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * @return the damage this bullet deals on impact
     */
    public float getDamage() {
        return damage;
    }

    /**
     * @return the direction in which the bullet is moving
     */
    public Direction getDir() {
        return dir;
    }

    @Override
    public int getID() {
        return this.ID;
    }

    @Override
    public MapLocation getLocation() {
        return this.location;
    }

    @Override
    public float getRadius() {
        return 0;
    }

    @Override
    public boolean isRobot() {
        return false;
    }

    @Override
    public boolean isTree() {
        return false;
    }

    @Override
    public boolean isBullet() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BulletInfo that = (BulletInfo) o;

        if (ID != that.ID) return false;
        if (Float.compare(that.speed, speed) != 0) return false;
        if (Float.compare(that.damage, damage) != 0) return false;
        if (!dir.equals(that.dir)) return false;
        return location.equals(that.location);

    }

    @Override
    public int hashCode() {
        int result = ID;
        result = 31 * result + (speed != +0.0f ? Float.floatToIntBits(speed) : 0);
        result = 31 * result + (damage != +0.0f ? Float.floatToIntBits(damage) : 0);
        result = 31 * result + dir.hashCode();
        result = 31 * result + location.hashCode();
        return result;
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
