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
    public final float health;
    
    /**
     * The number of times this robot has attacked in the current turn.
     */
    public final int attackCount;
    
    /**
     * The number of times this robot has moved in the current turn.
     */
    public final int moveCount;

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
                     float health, int attackCount, int moveCount) {
        super();
        this.ID = ID;
        this.team = team;
        this.type = type;
        this.location = location;
        this.health = health;
        this.attackCount = attackCount;
        this.moveCount = moveCount;
    }

    /**
     * Returns the team that this robot is on.
     *
     * @return the team that this robot is on.
     */
    public Team getTeam() {
        return team;
    }

    /**
     * Returns the type of this robot.
     *
     * @return the type of this robot.
     */
    public RobotType getType() {
        return type;
    }

    /**
     * Returns the current health of this robot.
     *
     * @return the current health of this robot.
     */
    public float getHealth() {
        return health;
    }

    /**
     * Returns the number of times this robot has attacked this turn.
     *
     * @return the number of times this robot has attacked this turn.
     */
    public int getAttackCount() {
        return attackCount;
    }

    /**
     * Returns the number of times this robot has moved this turn.
     *
     * @return the number of times this robot has moved this turn.
     */
    public int getMoveCount() {
        return moveCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RobotInfo robotInfo = (RobotInfo) o;

        if (ID != robotInfo.ID) return false;
        if (Float.compare(robotInfo.attackCount, attackCount) != 0) return false;
        if (Float.compare(robotInfo.moveCount, moveCount) != 0) return false;
        if (Float.compare(robotInfo.health, health) != 0) return false;
        if (team != robotInfo.team) return false;
        if (type != robotInfo.type) return false;
        return location.equals(robotInfo.location);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = ID;
        result = 31 * result + team.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + location.hashCode();
        result = 31 * result + attackCount;
        result = 31 * result + moveCount;
        temp = Double.doubleToLongBits(health);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
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
