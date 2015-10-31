package battlecode.common;

import java.util.ArrayList;

/**
 * Contains details on various attributes of the different robots. All of this information is in the specs in a more organized form.
 */
public enum RobotType {

    // last one is strength weight
    //  isbuilding, iszombie, infectTurns, spawn-source, build-dep, part cost, turns cost, hp, attack, range, movement delay, attack delay, cooldown delay, sight range, bytecode limit, zombieType, strWeight
    ZOMBIEDEN       (true,  false, 0,  null,      0,  0,  5000, 0,  0,    0,   0, 0, 35,     10000, 0, null),
    STANDARDZOMBIE  (false, true,  10, ZOMBIEDEN, 0,  0,  50,   3,  2,    2,   1, 1, 35,     10000, 0, null),
    RANGEDZOMBIE    (false, true,  10, ZOMBIEDEN, 0,  0,  50,   3,  13,   1.4, 0, 1, 35,     10000, 0, null),
    FASTZOMBIE      (false, true,  10, ZOMBIEDEN, 0,  0,  40,   4,  2,    2,   2, 1, 35,     10000, 0, null),
    BIGZOMBIE       (false, true,  10, ZOMBIEDEN, 0,  0,  250,  50, 2,    2,   1, 1, 35,     10000, 0, null),
    ARCHON          (false, false, 0,  null,      0,  0,  1000, 0,  24,   2,   1, 1, 35,     20000, 0, BIGZOMBIE),  
    SCOUT           (false, false, 0,  ARCHON,    25, 15, 100,  0,  0,    1.4, 0, 1, 53,     20000, 0, FASTZOMBIE),
    SOLDIER         (false, false, 0,  ARCHON,    10, 25, 50,   4,  13,   2,   2, 1, 24,     10000, 0, STANDARDZOMBIE),
    GUARD           (false, false, 0,  ARCHON,    10, 25, 120,  2,  24,   2,   1, 1, 24,     10000, 0, STANDARDZOMBIE),
    VIPER           (false, false, 20, ARCHON,    50, 30, 100,  2,  13,   3,   3, 1, 24,     10000, 0, RANGEDZOMBIE),
    TURRET          (true,  false, 0,  ARCHON,    50, 50, 50,   18, 34,   0,   3, 3, 24,     2000,  0, RANGEDZOMBIE),
    TTM             (false, false, 0,  TURRET,    0,  10, 50,   0,  0,    2,   0, 2, 24,     2000,  0, RANGEDZOMBIE),
    //HANDWASHSTATION (false, false, 0,  null,   20, 80, 100,  0,  0,    0,   0, 0, 24, 2000,  0, null), RIP
    ;

    /**
     * Whether this robot is a structure (or building). Structures are the units that cannot move.
     */
    public final boolean isBuilding;

    /**
     * Whether this robot a zombie. Zombies are not player-controlled and run their own code.
     */
    public final boolean isZombie;

    /**
     * Number of turns a robot is infected for after being attacked by this robot.
     */
    public final int infectTurns;
    
    /**
     * For units, this is the structure that spawns it. For non-spawnable robots, this is null.
     */
    public final RobotType spawnSource;

    /**
     * Ore cost for building or spawning.
     */
    public final int partCost;

    /**
     * Number of turns to spawn or build.
     */
    public final int buildTurns;

    /**
     * Maximum health for the robot.
     */
    public final double maxHealth;

    /**
     * Base damage per attack.
     */
    public final double attackPower;

    /**
     * Range^2 for an attack.
     */
    public final int attackRadiusSquared;

    /**
     * Movement delay: the amount of contribution to core delay from a movement.
     */
    public final double movementDelay;

    /**
     * Attack delay: the amount of contribution to weapon delay from an attack.
     */
    public final double attackDelay;

    /**
     * Cooldown delay: the amount of contribution to core delay from an attack.
     */
    public final double cooldownDelay;

    /**
     * Range^2 for sensing.
     */
    public final int sensorRadiusSquared;

    /**
     * Base bytecode limit of this robot (halved if the robot does not have sufficient supply upkeep).
     */
    public final int bytecodeLimit;
    
    /**
     * The type of zombie this unit turns into after becoming infected. For zombies, this value is null.
     */
    public final RobotType turnsInto;
    
    /**
     * How relatively strong the unit is. This number isn't really meaningful and is just used for part of the client.
     */
    public final int strengthWeight;

    /**
     * Returns whether the robot can attack.
     *
     * @return whether the robot can attack.
     */
    public boolean canAttack() {
        return attackPower > 0;
    }

    /**
     * Returns whether the robot infects robots it attacks.
     *
     * @return whether the robot infects robots it attacks.
     */
    public boolean canInfect() {
        return infectTurns > 0;
    }

    /**
     * Returns whether the robot is infected when attacked by a robot that infects.
     *
     * @return whether the robot is infected when attacked by a robot that infects.
     */
    public boolean isInfectable() {
        return !isZombie && this != ZOMBIEDEN;
    }

    /**
     * Returns whether the robot can move.
     *
     * @return whether the robot can move.
     */
    public boolean canMove() {
        return !isBuilding;
    }

    /**
     * Returns whether the robot can build.
     *
     * @return whether the robot can build.
     */
    public boolean canBuild() {
        return this == ARCHON;
    }

    /**
     * Returns whether the robot is buildable.
     *
     * @return whether the robot is buildable.
     */
    public boolean isBuildable() {
        return spawnSource == ARCHON;
    }

    /**
     * Returns whether the robot can spawn.
     *
     * @return whether the robot can spawn.
     */
    public boolean canSpawn() { // Update later
        return this == ARCHON || this == ZOMBIEDEN;
    }
    
    RobotType(boolean isBuilding,
              boolean isZombie,
              int infectTurns,
              RobotType spawnSource,
              int partCost,
              int buildTurns,
              double maxHealth,
              double attackPower,
              int attackRadiusSquared,
              double movementDelay,
              double attackDelay,
              double cooldownDelay,
              int sensorRadiusSquared,
              int bytecodeLimit,
              int strengthWeight,
              RobotType turnsInto) {
        this.isBuilding = isBuilding;
        this.isZombie = isZombie;
        this.infectTurns = infectTurns;
        this.spawnSource = spawnSource;
        this.partCost = partCost;
        this.buildTurns = buildTurns;
        this.maxHealth = maxHealth;
        this.attackPower = attackPower;
        this.attackRadiusSquared = attackRadiusSquared;
        this.movementDelay = movementDelay;
        this.attackDelay = attackDelay;
        this.cooldownDelay = cooldownDelay;
        this.sensorRadiusSquared = sensorRadiusSquared;
        this.bytecodeLimit = bytecodeLimit;
        this.turnsInto = turnsInto;
        this.strengthWeight = strengthWeight;
    }
}
