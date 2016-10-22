package battlecode.common;

/**
 * Struct that stores basic information that was 'sensed' of a Tree. This
 * info is ephemeral and there is no guarantee any of it will remain the same
 * between rounds.
 */
public class TreeInfo implements BodyInfo{

    /**
     * The unique ID of the tree.
     */
    public final int ID;

    /**
     * The Team that the tree is on.
     */
    public final Team team;

    /**
     * The radius of the tree.
     */
    public final float radius;

    /**
     * The current location of the tree.
     */
    public final MapLocation location;

    /**
     * The current health of the tree.
     */
    public final float health;

    /**
     * The current amount of bullets contained within the tree.
     * Note: only NEUTRAL trees can contain bullets
     */
    public final int containedBullets;

    /**
     * The robot contained within the tree that is obtained upon
     * destroying the tree using the cut() method.  Has a value
     * of null if no robot is contained within the tree
     * Note: only NEUTRAL trees can contain robots
     */
    public final RobotType containedRobot;

    public TreeInfo(int ID, Team team, MapLocation location,
                    float radius, float health,
                    int containedBullets, RobotType containedRobot) {
        this.ID = ID;
        this.team = team;
        this.location = location;
        this.radius = radius;
        this.health = health;
        this.containedBullets = containedBullets;
        this.containedRobot = containedRobot;
    }

    @Override
    public MapLocation getLocation() {
        return this.location;
    }

    @Override
    public float getRadius() {
        return this.radius;
    }

    @Override
    public boolean isRobot() {
        return false;
    }

    @Override
    public boolean isTree() {
        return true;
    }

    @Override
    public boolean isBullet() {
        return false;
    }

    public int hashCode() {
        return ID;
    }

    @Override
    public String toString() {
        return "TreeInfo{" +
                "ID=" + ID +
                ", team=" + team +
                ", location=" + location +
                ", radius=" + radius +
                ", health=" + health +
                ", containedBullets=" + containedBullets +
                ", containedRobot=" + containedRobot +
                '}';
    }

}
