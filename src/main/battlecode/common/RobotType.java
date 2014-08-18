package battlecode.common;

import static battlecode.common.RobotLevel.ON_GROUND;

/**
 * Contains details on various attributes of the different robots.
 */
public enum RobotType {
    // MXE = max energon / health
    // SR = sight radius squared
    // AR = attack radius squared
    // AD = attack delay
    // AP = attack power
    // ATTACK = can attack?
    // BLDNG = is building?
    // BUILDTURNS = number of turns required to build
    //          MXE    SR   AR    AD      AP      ATTACK  BLDNG   BUILDTURNS
  	HQ          (1000,  35, 24,    2,     24,      true,   false,    0),
    TOWER       ( 500,  35, 24,    2,     15,      true,   false,    0),
    SOLDIER     ( 100,  35, 24,    2,    200,      true,   false,    0),
    ;

    /**
     * The robot's level.
     */
    public final RobotLevel level = ON_GROUND;

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
    public final int attackRadiusSquared;

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

    RobotType(double maxHealth,
              int sensorRadiusSquared,
              int attackRadiusSquared,
              int attackDelay,
              double attackPower,
              boolean canAttack,
              boolean isBuilding,
              int buildTurns) {
        this.maxHealth = maxHealth;
        this.sensorRadiusSquared = sensorRadiusSquared;
        this.attackRadiusSquared = attackRadiusSquared;
        this.attackDelay = attackDelay;
        this.attackPower = attackPower;
        this.canAttack = canAttack;
        this.isBuilding = isBuilding;
        this.buildTurns = buildTurns;
    }

}
