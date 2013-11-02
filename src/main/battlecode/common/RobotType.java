package battlecode.common;

import static battlecode.common.RobotLevel.ON_GROUND;

public enum RobotType {
    //          LEVEL,      MXE    SR  SA          AR AR   AA      AD      AP      ATTACK  ENCMP    SPLASH  COUNT CAPTURNS
  	HQ          (ON_GROUND, Integer.MAX_VALUE,
                                   24, 360,        0, 16,  360,    0,     50,      true,   false,   25,     0,    0),
    SOLDIER     (ON_GROUND, 100,   24, 360,        0, 16,  360,    1,     10,      true,   false,   0,      1,    0),
    WALL        (ON_GROUND, 200,    0,   0,        0,  0,    0,    0,      0,     false,   true,    0,      0,   30),
    NOISETOWER  (ON_GROUND, 100,    0,   0,        0,400,  360,    0,      0,      true,   true,    0,      5,  100),
    PASTR       (ON_GROUND,  50,    0,   0,        0,  0,  360,    0,      0,     false,   true,    0,      1,   50),
    MEDBAY      (ON_GROUND, 100,   14, 360,        0, 2 ,  360,    1,      2,      true,   true,    0,      1,   50),
    SHIELDS     (ON_GROUND, 100,   14, 360,        0, 2 ,  360,    1,      10,     true,   true,    0,      1,   50),
    ARTILLERY   (ON_GROUND, 100,   14, 360,        0, 63,  360,    20,     60,     true,   true,    0,      1,   50),
    GENERATOR   (ON_GROUND, 100,   14, 360,        0, 0 ,  0,      0,      0,      true,   true,    0,      1,   50),
    SUPPLIER    (ON_GROUND, 100,   14, 360,        0, 0 ,  0,      0,      0,      true,   true,    0,      1,   50),
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

    public final double splashPower;

    public final int count;
    public final int captureTurns;

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
              boolean isEncampment,
              double splashPower,
              int count,
              int captureTurns) {
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
        this.splashPower = splashPower;
        this.count = count;
        this.captureTurns = captureTurns;
    }

}
