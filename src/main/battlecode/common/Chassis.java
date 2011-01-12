package battlecode.common;

import static battlecode.common.RobotLevel.*;
import static battlecode.common.ComponentType.*;

public enum Chassis {
    //       WGT  HP UPK COS
    LIGHT(    6, 12, 0.25, 30, ON_GROUND, SMALL_MOTOR),
    MEDIUM(  12, 20, 0.45, 40, ON_GROUND, MEDIUM_MOTOR),
    HEAVY(   18, 40, 0.80, 60, ON_GROUND, LARGE_MOTOR),
    FLYING(   5, 10, 0.35, 50, IN_AIR,    FLYING_MOTOR),
    BUILDING(18, 30, 0.30, 80, ON_GROUND, BUILDING_MOTOR),
    DUMMY(    0, 20, 0   ,  0, ON_GROUND, null),
    DEBRIS(   0,200, 0   ,  0, ON_GROUND, null);

    ;
    /** The weight that this chassis can support. */
    public final int weight;
    /** The maximum hit points of this chassis. */
    public final double maxHp;
    /** The upkeep cost of this chassis. */
    public final double upkeep;
    /** The cost to build this chassis. */
    public final int cost;
    /** Determines whether or not this chassis is flying. */
    public final RobotLevel level;
    /** The type of motor this chassis uses. */
    public final ComponentType motor;
    /**
     * The time that it takes for this robot to move
     * orthogonally.  Equal to <code>motor.delay</code>.
     */
    public final int moveDelayOrthogonal;
    /**
     * The time it takes for this robot to move diagonally.
     * Equal to <code>(int)Math.round(motor.delay*Math.sqrt(2.))</code>.
     */
    public final int moveDelayDiagonal;

    /**
     * Equvalent to <code>level==RobotLevel.IN_AIR</code>.
     */
    public boolean isAirborne() {
        return level == RobotLevel.IN_AIR;
    }

    Chassis(int weight, double maxHp, double upkeep,
            int cost, RobotLevel level, ComponentType motor) {
        this.weight = weight;
        this.maxHp = maxHp;
        this.upkeep = upkeep;
        this.cost = cost;
        this.level = level;
        this.motor = motor;
        if (motor != null) {
            this.moveDelayOrthogonal = motor.delay;
            this.moveDelayDiagonal = (int) Math.round(motor.delay * Math.sqrt(2.));
        } else {
            moveDelayOrthogonal = 0;
            moveDelayDiagonal = 0;
        }
    }
}
