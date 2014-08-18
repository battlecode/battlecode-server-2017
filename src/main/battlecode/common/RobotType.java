package battlecode.common;

import static battlecode.common.RobotLevel.ON_GROUND;

/**
 * Contains details on various attributes of the different robots.
 */
public enum RobotType {
    // MXE = max energon / health
    // SR = sight range
    // AR = attack radius
    // AR = attack range
    // AD = attack delay
    // AP = attack power
    // ATTACK = can attack?
    // BLDNG = is building?
    // BUILDTURNS = number of turns required to build
    //          LEVEL,      MXE    SR   AR  AR    AD      AP      ATTACK  BLDNG   BUILDTURNS
  	HQ          (ON_GROUND, 1000,  35,   0, 24,    2,     24,      true,   false,    0),
    TOWER       (ON_GROUND,  500,  35,   0, 24,    2,     15,      true,   false,    0),
    SOLDIER     (ON_GROUND,  100,  35,   0, 24,    2,    200,      true,   false,    0),
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
     * The square of the maximum distance that the robot can attack.
     */
    public final int attackRadiusMaxSquared;

    /**
     * The square of the minimum distance that the robot can attack.
     */
    public final int attackRadiusMinSquared;

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

    /**
     * Whether the robot must be built.
     */ 
    public final boolean isBuilding;

    /**
     * How many turns it takes to build this building.
     */
    public final int buildTurns;

    /**
     * Returns true if the robot can attack robots at the given level.
     */
    public boolean canAttack(RobotLevel level) {
				return canAttack;
    }

    RobotType(RobotLevel level,
              double maxHealth,
              int sensorRadiusSquared,
              int attackRadiusMinSquared,
              int attackRadiusMaxSquared,
              int attackDelay,
              double attackPower,
              boolean canAttack,
              boolean isBuilding,
              int buildTurns) {
        this.level = level;
        this.maxHealth = maxHealth;
        this.sensorRadiusSquared = sensorRadiusSquared;
        this.attackRadiusMaxSquared = attackRadiusMaxSquared;
        this.attackRadiusMinSquared = attackRadiusMinSquared;
        this.attackDelay = attackDelay;
        this.attackPower = attackPower;
        this.canAttack = canAttack;
        this.isBuilding = isBuilding;
        this.buildTurns = buildTurns;
    }

}
