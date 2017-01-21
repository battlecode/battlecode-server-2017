package battlecode.common;

import java.util.Objects;

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
     * The current maximum health of the tree.
     */
    public final float maxHealth;

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

        if(team == Team.NEUTRAL){
            this.maxHealth = GameConstants.NEUTRAL_TREE_HEALTH_RATE * radius;
        } else {
            this.maxHealth = GameConstants.BULLET_TREE_MAX_HEALTH;
        }
    }

    /**
     * @return the team that this tree is on
     */
    public Team getTeam() {
        return team;
    }

    /**
     * @return the current health of the tree
     */
    public float getHealth() {
        return health;
    }

    /**
     * @return the maximum health of this tree
     */
    public float getMaxHealth() {
        return maxHealth;
    }

    /**
     * @return the amount of bullets contained within this tree
     */
    public int getContainedBullets() {
        return containedBullets;
    }

    /**
     * @return the type of robot contained within this tree, or null if there is no robot
     */
    public RobotType getContainedRobot() {
        return containedRobot;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TreeInfo treeInfo = (TreeInfo) o;

        if (ID != treeInfo.ID) return false;
        if (Float.compare(treeInfo.radius, radius) != 0) return false;
        if (Float.compare(treeInfo.health, health) != 0) return false;
        if (containedBullets != treeInfo.containedBullets) return false;
        if (team != treeInfo.team) return false;
        if (!location.equals(treeInfo.location)) return false;
        return containedRobot == treeInfo.containedRobot;

    }

    @Override
    public int hashCode() {
        int result = ID;
        result = 31 * result + team.hashCode();
        result = 31 * result + (radius != +0.0f ? Float.floatToIntBits(radius) : 0);
        result = 31 * result + location.hashCode();
        result = 31 * result + (health != +0.0f ? Float.floatToIntBits(health) : 0);
        result = 31 * result + containedBullets;
        result = 31 * result + Objects.hashCode(containedRobot);
        return result;
    }

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
