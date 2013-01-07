package battlecode.common;

import static battlecode.common.RobotLevel.ON_GROUND;

public enum RobotType {
    //          LEVEL,      MXE         MXF     COS     MD MVC  SR  SA          AR AR   AA      AD      AP      AIR     GROUND  ENCMP
	HQ          (ON_GROUND, 500,        0,      0,      0, 0,   14, 360,        0, 0 ,  0,      0,      0,      true,   true,   false),
    SOLDIER     (ON_GROUND, 40,         0,      0,      0, 0,   14, 360,        0, 2 ,  360,    1,      8,      true,   true,   false),
    MEDBAY      (ON_GROUND, 100,        0,      0,      0, 0,   14, 360,        0, 2 ,  360,    1,      2,      true,   true,   true),
    SHIELDS     (ON_GROUND, 100,        0,      0,      0, 0,   14, 360,        0, 2 ,  360,    1,      5,      true,   true,   true),
    ARTILLERY   (ON_GROUND, 100,        0,      0,      0, 0,   14, 360,        0, 63,  360,    20,     40,     true,   true,   true),
    GENERATOR   (ON_GROUND, 100,        0,      0,      0, 0,   14, 360,        0, 0 ,  0,      0,      0,      true,   true,   true),
    SUPPLIER    (ON_GROUND, 100,        0,      0,      0, 0,   14, 360,        0, 0 ,  0,      0,      0,      true,   true,   true),
    ;

    /**
     * The robot's level.
     */
    public final RobotLevel level;

    /**
     * The maximum amount of energon the robot can have.
     */
    public final double maxEnergon;

    /**
     * The maximum amount of flux the robot can have.
     */
    public final double maxFlux;

    /**
     * The amount of flux needed to spawn the robot.
     */
    public final double spawnCost;

    /**
     * The number of turns it takes the robot to move orthogonally.
     */
    public final int moveDelayOrthogonal;

    /**
     * The number of turns it takes the robot to move diagonally.
     * Equal to <code>(int)Math.round(moveDelayOrthogonal*Math.sqrt(2.))</code>.
     */
    public final int moveDelayDiagonal;

    /**
     * The cost in flux to move the robot one square.
     */
    public final double moveCost;

    /**
     * The square of the maximum distance that the robot can sense.
     */
    public final int sensorRadiusSquared;

    /**
     * The range of angles that the robot can sense.
     */
    public final double sensorAngle;

    /**
     * The square of the maximum distance that the robot can attack.
     */
    public final int attackRadiusMaxSquared;

    /**
     * The square of the minimum distance that the robot can attack.
     */
    public final int attackRadiusMinSquared;

    /**
     * The range of angles that this robot can attack.
     */
    public final double attackAngle;

    /**
     * The number of turns that it takes this robot to attack.
     */
    public final int attackDelay;

    /**
     * The amount of damage that this robot does when it attacks.
     */
    public final double attackPower;

    /**
     * Whether or not the robot can attack air units.
     */
    public final boolean canAttackAir;

    /**
     * Whether or not the robot can attack ground units.
     */
    public final boolean canAttackGround;

    public final double sensorCosHalfTheta;
    public final double attackCosHalfTheta;
    
    public final boolean isEncampment;

    /**
     * Returns true if the robot can attack robots at the given level.
     */
    public boolean canAttack(RobotLevel level) {
        switch (level) {
            case ON_GROUND:
                return canAttackGround;
            case IN_AIR:
                return canAttackAir;
            default:
                return false;
        }
    }

    /**
     * Returns <code>true</code> if this robot's level is
     * <code>RobotLevel.IN_AIR</code>.
     */
    public boolean isAirborne() {
        return level == RobotLevel.IN_AIR;
    }

    RobotType(RobotLevel level,
              double maxEnergon,
              double maxFlux,
              double spawnCost,
              int moveDelayOrthogonal,
              double moveCost,
              int sensorRadiusSquared,
              double sensorAngle,
              int attackRadiusMinSquared,
              int attackRadiusMaxSquared,
              double attackAngle,
              int attackDelay,
              double attackPower,
              boolean canAttackAir,
              boolean canAttackGround,
              boolean isEncampment) {
        this.level = level;
        this.maxEnergon = maxEnergon;
        this.maxFlux = maxFlux;
        this.spawnCost = spawnCost;
        this.moveDelayOrthogonal = moveDelayOrthogonal;
        this.moveDelayDiagonal = (int) Math.round(moveDelayOrthogonal * Math.sqrt(2.));
        this.moveCost = moveCost;
        this.sensorRadiusSquared = sensorRadiusSquared;
        this.sensorAngle = sensorAngle;
        this.attackRadiusMaxSquared = attackRadiusMaxSquared;
        this.attackRadiusMinSquared = attackRadiusMinSquared;
        this.attackAngle = attackAngle;
        this.attackDelay = attackDelay;
        this.attackPower = attackPower;
        this.canAttackAir = canAttackAir;
        this.canAttackGround = canAttackGround;
        this.sensorCosHalfTheta = Math.cos(sensorAngle * Math.PI / 360.);
        this.attackCosHalfTheta = Math.cos(attackAngle * Math.PI / 360.);
        this.isEncampment = isEncampment;
    }

}