package battlecode.common;

import java.util.ArrayList;

/**
 * Contains details on various attributes of the different robots. All of this information is in the specs in a more organized form.
 */
public enum RobotType {

    // last one is strength weight

    //  isbuilding, spawn-source, build-dep, ore cost, turns cost, supply upkeep, hp, attack, range, movement delay, attack delay, loading delay, cooldown delay, sight range, bytecode limit
    HQ          (true, null, null, 0, 0, 0, 2000, 24, 24, 0, 2, 0, 0, 35, 10000, 0),    
    TOWER       (true, null, null, 0, 0, 0, 1000, 8, 24, 0, 1, 0, 0, 35,  2000, 0),

    SUPPLYDEPOT          (true, null,                  HQ, 100,  40, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000, 0),
    TECHNOLOGYINSTITUTE  (true, null,                  HQ, 200,  50, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000, 0),
    BARRACKS             (true, null,                  HQ, 300,  50, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000, 0),
    HELIPAD              (true, null,                  HQ, 300, 100, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000, 0),
    TRAININGFIELD        (true, null, TECHNOLOGYINSTITUTE, 200, 200, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000, 0),
    TANKFACTORY          (true, null,            BARRACKS, 500, 100, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000, 0),
    MINERFACTORY         (true, null,                  HQ, 500, 100, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000, 0),
    /**
     * Sanitation is important.
     */
    HANDWASHSTATION      (true, null,                  HQ, 200, 100, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000, 0),
    AEROSPACELAB         (true, null,             HELIPAD, 500, 100, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000, 0),

	//isbuilding, spawn-source, build-dep, ore cost, turns cost, supply upkeep, hp, attack, range, movement delay, attack delay, loading delay, cooldown delay, sight range, bytecode limit
    BEAVER      (false,                  HQ, null, 100,  20, 10,  30,  4,  5, 2, 2, 1, 1, 24, 10000, 2),
    COMPUTER    (false, TECHNOLOGYINSTITUTE, null,  10,  25,  2,   1,  0,  0, 8, 0, 0, 0, 24, 20000, 0),
    SOLDIER     (false,            BARRACKS, null,  60,  20,  5,  40,  4,  8, 2, 1, 1, 1, 24, 10000, 6),
    BASHER      (false,            BARRACKS, null,  80,  20,  6,  64,  4,  2, 2, 1, 0, 1, 24, 10000, 8),
    MINER       (false,        MINERFACTORY, null,  60,  20,  8,  50,  3,  5, 2, 2, 2, 1, 24, 10000, 3),
    DRONE       (false,             HELIPAD, null, 125,  30, 10,  70,  8,  5, 1, 3, 1, 1, 24, 10000, 10),
    TANK        (false,         TANKFACTORY, null, 250,  50, 15, 144, 20, 15, 2, 3, 2, 2, 24, 10000, 20),
    COMMANDER   (false,       TRAININGFIELD, null, 100, 200, 15, 200,  6, 10, 2, 1, 0, 0, 24, 10000, 40),
    LAUNCHER    (false,        AEROSPACELAB, null, 400, 100, 25, 200,  0,  0, 4, 0, 0, 0, 24, 10000, 35),
    MISSILE     (false,            LAUNCHER, null,   0,   6,  0,   3, 18,  2, 1, 0, 0, 0, 24,   500, 0),
    ;

    //  isbuilding, spawn-source, build-dep, ore cost, turns cost, supply upkeep, hp, attack, range, movement delay, attack delay, loading delay, cooldown delay, sight range, bytecode limit

    /**
     * Whether this robot is a structure (or building). Structures are the units that cannot move.
     */
    public final boolean isBuilding;

    /**
     * For units, this is the structure that spawns it. For non-spawnable robots, this is null.
     */
    public final RobotType spawnSource;

    /**
     * For structures, this is the pre-requisite structure needed to build this structure (null if not applicable or if there is no dependency).
     */
    public final RobotType dependency;

    /**
     * Ore cost for building or spawning.
     */
    public final int oreCost;

    /**
     * Number of turns to spawn or build.
     */
    public final int buildTurns;

    /**
     * Base supply upkeep (not including bytecode supply upkeep).
     */
    public final int supplyUpkeep;

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
    public final int movementDelay;

    /**
     * Attack delay: the amount of contribution to weapon delay from an attack.
     */
    public final int attackDelay;

    /**
     * Loading delay: the amount of contribution to weapon delay from a movement.
     */
    public final int loadingDelay;

    /**
     * Cooldown delay: the amount of contribution to core delay from an attack.
     */
    public final int cooldownDelay;

    /**
     * Range^2 for sensing.
     */
    public final int sensorRadiusSquared;

    /**
     * Base bytecode limit of this robot (halved if the robot does not have sufficient supply upkeep).
     */
    public final int bytecodeLimit;

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
     * Returns whether the robot can move.
     *
     * @return whether the robot can move.
     */
    public boolean canMove() {
        return !isBuilding;
    }

    /**
     * Returns whether the robot can mine.
     *
     * @return whether the robot can mine.
     */
    public boolean canMine() {
        return this == MINER || this == BEAVER;
    }

    /**
     * Returns whether the robot can launch missiles.
     *
     * @return whether the robot can launch missiles.
     */
    public boolean canLaunch() {
        return this == LAUNCHER;
    }

    /**
     * Returns whether the robot can build.
     *
     * @return whether the robot can build.
     */
    public boolean canBuild() {
        return this == BEAVER;
    }

    /**
     * Returns whether the robot is buildable.
     *
     * @return whether the robot is buildable.
     */
    public boolean isBuildable() {
        return isBuilding && this != HQ && this != TOWER;
    }

    /**
     * Returns whether the robot can spawn.
     *
     * @return whether the robot can spawn.
     */
    public boolean canSpawn() {
        return isBuilding && this != TOWER && this != SUPPLYDEPOT;
    }

    /**
     * Returns whether the robot uses supply.
     *
     * @return whether the robot uses supply.
     */
    public boolean needsSupply() {
        return supplyUpkeep > 0;
    }

    RobotType(boolean isBuilding,
              RobotType spawnSource,
              RobotType dependency,
              int oreCost,
              int buildTurns,
              int supplyUpkeep,
              double maxHealth,
              double attackPower,
              int attackRadiusSquared,
              int movementDelay,
              int attackDelay,
              int loadingDelay,
              int cooldownDelay,
              int sensorRadiusSquared,
              int bytecodeLimit,
              int strengthWeight) {
        this.isBuilding = isBuilding;
        this.spawnSource = spawnSource;
        this.dependency = dependency;
        this.oreCost = oreCost;
        this.buildTurns = buildTurns;
        this.supplyUpkeep = supplyUpkeep;
        this.maxHealth = maxHealth;
        this.attackPower = attackPower;
        this.attackRadiusSquared = attackRadiusSquared;
        this.movementDelay = movementDelay;
        this.attackDelay = attackDelay;
        this.loadingDelay = loadingDelay;
        this.cooldownDelay = cooldownDelay;
        this.sensorRadiusSquared = sensorRadiusSquared;
        this.bytecodeLimit = bytecodeLimit;
        this.strengthWeight = strengthWeight;
    }
}
