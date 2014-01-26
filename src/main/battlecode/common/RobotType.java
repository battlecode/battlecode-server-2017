package battlecode.common;

import static battlecode.common.RobotLevel.ON_GROUND;

/**
 * Contains details on various attributes of the different robots.
 */
public enum RobotType {
    //          LEVEL,      MXE    SR  SA          AR AR   AA      AD      AP      ATTACK  BLDNG    SPLASH  COUNT CAPTURNS
  	HQ          (ON_GROUND, Integer.MAX_VALUE,
                                   35, 360,        0, 15,  360,    1,     50,      true,   false,   25,      0,    0),
    SOLDIER     (ON_GROUND, 100,   35, 360,        0, 10,  360,    2,     10,      true,   false,   0,      1,    0),
    NOISETOWER  (ON_GROUND, 100,   35, 360,        0,300,  360,    2,      0,      true,   true,    0,      3,  100),
    PASTR       (ON_GROUND, 200,    5, 360,        0,  0,  360,    0,      0,     false,   true,    0,      2,   50),
    
    //WALL        (ON_GROUND, 200,    0,   0,        0,  0,    0,    0,      0,     false,   true,    0,      0,   30),
	//MEDBAY      (ON_GROUND, 100,   14, 360,        0, 2 ,  360,    1,      2,      true,   true,    0,      1,   50),
    //SHIELDS     (ON_GROUND, 100,   14, 360,        0, 2 ,  360,    1,      10,     true,   true,    0,      1,   50),
    //ARTILLERY   (ON_GROUND, 100,   14, 360,        0, 63,  360,    20,     60,     true,   true,    0,      1,   50),
    //GENERATOR   (ON_GROUND, 100,   14, 360,        0, 0 ,  0,      0,      0,      true,   true,    0,      1,   50),
    //SUPPLIER    (ON_GROUND, 100,   14, 360,        0, 0 ,  0,      0,      0,      true,   true,    0,      1,   50),
    ;

    /**
     * The robot's level.
     */
    public final RobotLevel level;

    /**
     * The maximum amount of health the robot can have.
     */
    public final double maxHealth;

    /**
     * The square of the maximum distance that the robot can sense.
     */
    public final int sensorRadiusSquared;

    /**
     * Not used for 2014 (all angles allowable by default).
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
     * Not used for 2014 (all angles allowable by default).
     */
    public final double attackAngle;

    /**
     * Action delay due to attacks. Everything has attack delay 1, except for SOLDIERs, which have an attack delay specified as a game constant (consistent with information here).
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
   
    /**
     * Whether the robot must be built.
     */ 
    public final boolean isBuilding;

    /**
     * Splash damage of attack.
     */
    public final double splashPower;

    /**
     * Amount the robot counts towards the total robot count.
     */
    public final int count;

    /**
     * How many turns it takes to build this building.
     */
    public final int captureTurns;

    /**
     * Returns true if the robot can attack robots at the given level.
     */
    public boolean canAttack(RobotLevel level) {
				return canAttack;
    }

    RobotType(RobotLevel level,
              double maxHealth,
              int sensorRadiusSquared,
              double sensorAngle,
              int attackRadiusMinSquared,
              int attackRadiusMaxSquared,
              double attackAngle,
              int attackDelay,
              double attackPower,
              boolean canAttack,
              boolean isBuilding,
              double splashPower,
              int count,
              int captureTurns) {
        this.level = level;
        this.maxHealth = maxHealth;
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
        this.isBuilding = isBuilding;
        this.splashPower = splashPower;
        this.count = count;
        this.captureTurns = captureTurns;
    }

}
