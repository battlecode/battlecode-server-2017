package battlecode.common;

/**
 * Struct that stores basic information that was 'sensed' of a Tree. This
 * info is ephemeral and there is no guarantee any of it will remain the same
 * between rounds.
 */
public class TreeInfo {

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
    public final double radius;

    /**
     * The current location of the tree.
     */
    public final MapLocation location;

    /**
     * The current health of the tree.
     */
    public final double health;

    /**
     * The current amount of bullets contained within the tree.
     * Note: only NEUTRAL trees can contain bullets
     */
    public final double containedBullets;

    /**
     * The robot contained within the tree that is obtained upon
     * destroying the tree using the cut() method.  Has a value
     * of null if no robot is contained within the tree
     * Note: only NEUTRAL trees can contain robots
     */
    public final RobotType containedRobot;

    public TreeInfo(int ID, Team team, MapLocation location,
                    double radius, double health,
                    double containedBullets, RobotType containedRobot) {
        this.ID = ID;
        this.team = team;
        this.location = location;
        this.radius = radius;
        this.health = health;
        this.containedBullets = containedBullets;
        this.containedRobot = containedRobot;
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
