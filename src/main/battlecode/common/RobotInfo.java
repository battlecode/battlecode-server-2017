package battlecode.common;

/**
 * Struct that stores basic information that was 'sensed' of another Robot. This
 * info is ephemeral and there is no guarantee any of it will remain the same
 * between rounds.
 */
public class RobotInfo implements  BodyInfo{

    /**
     * The unique ID of the robot.
     */
    public final int ID;

    /**
     * The Team that the robot is on.
     */
    public final Team team;

    /**
     * The type of the robot.
     */
    public final RobotType type;

    /**
     * The current location of the robot.
     */
    public final MapLocation location;

    /**
     * The current health of the robot.
     */
    public final double health;
    
    /**
     * The number of times this robot has attacked in the current turn
     */
    public final int attackCount;
    
    /**
     * The number of times this robot has moved in the current turn
     */
    public final int moveCount;

    @Override
    public MapLocation getLocation() {
        return this.location;
    }

    @Override
    public float getRadius() {
        return this.type.bodyRadius;
    }

    @Override
    public boolean isRobot() {
        return true;
    }

    @Override
    public boolean isTree() {
        return false;
    }

    @Override
    public boolean isBullet() {
        return false;
    }

    public RobotInfo(int ID, Team team, RobotType type, MapLocation location,
                     double health, int attackCount, int moveCount) {
        super();
        this.ID = ID;
        this.team = team;
        this.type = type;
        this.location = location;
        this.health = health;
        this.attackCount = attackCount;
        this.moveCount = moveCount;
    }

    public int hashCode() {
        return ID;
    }

    @Override
    public String toString() {
        return "RobotInfo{" +
                "ID=" + ID +
                ", team=" + team +
                ", type=" + type +
                ", location=" + location +
                ", health=" + health +
                ", attackCount=" + attackCount +
                ", moveCount=" + moveCount +
                '}';
    }
}
