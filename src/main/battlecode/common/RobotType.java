package battlecode.common;

import static battlecode.common.RobotLevel.ON_GROUND;

public enum RobotType {
    //          LEVEL,      MXE    SR  SA          AR AR   AA      AD      AP      ATTACK  ENCMP
  	HQ          (ON_GROUND, 500,   14, 360,        0, 0 ,  0,      0,      0,      true,   false),
    SOLDIER     (ON_GROUND, 40,    14, 360,        0, 2 ,  360,    1,      6,      true,   false),
    MEDBAY      (ON_GROUND, 100,   14, 360,        0, 2 ,  360,    1,      2,      true,   true),
    SHIELDS     (ON_GROUND, 100,   14, 360,        0, 2 ,  360,    1,      10,      true,   true),
    ARTILLERY   (ON_GROUND, 100,   14, 360,        0, 63,  360,    20,     60,     true,   true),
    GENERATOR   (ON_GROUND, 100,   14, 360,        0, 0 ,  0,      0,      0,      true,   true),
    SUPPLIER    (ON_GROUND, 100,   14, 360,        0, 0 ,  0,      0,      0,      true,   true),
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
     * Whether or not the robot can attack units.
     */
    public final boolean canAttack;

    public final double sensorCosHalfTheta;
    public final double attackCosHalfTheta;
    
    public final boolean isEncampment;

    /**
     * Returns true if the robot can attack robots at the given level.
     */
    public boolean canAttack(RobotLevel level) {
				return canAttack;
    }

    RobotType(RobotLevel level,
              double maxEnergon,
              int sensorRadiusSquared,
              double sensorAngle,
              int attackRadiusMinSquared,
              int attackRadiusMaxSquared,
              double attackAngle,
              int attackDelay,
              double attackPower,
              boolean canAttack,
              boolean isEncampment) {
        this.level = level;
        this.maxEnergon = maxEnergon;
        this.sensorRadiusSquared = sensorRadiusSquared;
        this.sensorAngle = sensorAngle;
        this.attackRadiusMaxSquared = attackRadiusMaxSquared;
        this.attackRadiusMinSquared = attackRadiusMinSquared;
        this.attackAngle = attackAngle;
        this.attackDelay = attackDelay;
        this.attackPower = attackPower;
        this.canAttack = canAttack;
        this.sensorCosHalfTheta = Math.cos(sensorAngle * Math.PI / 360.);
        this.attackCosHalfTheta = Math.cos(attackAngle * Math.PI / 360.);
        this.isEncampment = isEncampment;
    }

}