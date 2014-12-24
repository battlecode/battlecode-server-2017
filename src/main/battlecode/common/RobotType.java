package battlecode.common;

import java.util.ArrayList;

/**
 * Contains details on various attributes of the different robots.
 */
public enum RobotType {
    //  isbuilding, spawn-source, build-dep, ore cost, turns cost, supply upkeep, hp, attack, range, movement delay, attack delay, loading delay, cooldown delay, sight range, bytecode limit
    HQ          (true, null, null, 0, 0, 0, 1000, 24, 24, 0, 2, 0, 0, 35, 10000),    
    TOWER       (true, null, null, 0, 0, 0,  500, 8, 24, 0, 1, 0, 0, 35,  2000),

    SUPPLYDEPOT          (true, null,                  HQ, 100,  20, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000),
    TECHNOLOGYINSTITUTE  (true, null,                  HQ, 200,  50, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000),
    BARRACKS             (true, null,                  HQ, 300,  50, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000),
    HELIPAD              (true, null,                  HQ, 300,  50, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000),
    TRAININGFIELD        (true, null, TECHNOLOGYINSTITUTE, 200, 100, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000),
    TANKFACTORY          (true, null,            BARRACKS, 500, 100, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000),
    MINERFACTORY         (true, null,                  HQ, 500, 100, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000),
    HANDWASHSTATION      (true, null,                  HQ, 200, 100, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000),
    AEROSPACELAB         (true, null,             HELIPAD, 500, 100, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000),

    BEAVER      (false,                  HQ, null, 100,  20, 20,  30,  6,  8, 2, 2, 1, 1, 24, 10000),
    COMPUTER    (false, TECHNOLOGYINSTITUTE, null, 10,  25,  5,   1,  0,  0, 8, 0, 0, 0, 24, 10000),
    SOLDIER     (false,            BARRACKS, null, 50,  15, 10,  40,  8, 15, 2, 2, 1, 1, 24, 10000),
    BASHER      (false,            BARRACKS, null, 80,  20, 10,  40,  4,  2, 2, 2, 1, 1, 24, 10000),
    MINER       (false,        MINERFACTORY, null, 50,  20, 15,  50,  3,  5, 2, 2, 2, 1, 24, 10000),
    DRONE       (false,             HELIPAD, null, 120,  40,  5,  80, 10,  8, 1, 3, 0, 0, 24, 10000),
    TANK        (false,         TANKFACTORY, null, 200,  60, 30, 160, 20, 24, 2, 3, 2, 2, 24, 10000),
    COMMANDER   (false,       TRAININGFIELD, null, 100,  80, 10, 120,  8, 15, 2, 1, 0, 0, 24, 10000),
    LAUNCHER    (false,        AEROSPACELAB, null, 400, 100, 50, 400,  0,  0, 4, 0, 0, 0, 24, 10000),
    MISSILE     (false,            LAUNCHER, null, 0,   6,  0,   5, 20,  2, 1, 0, 0, 0, 24,   500),
    ;

    //  isbuilding, spawn-source, build-dep, ore cost, turns cost, supply upkeep, hp, attack, range, movement delay, attack delay, loading delay, cooldown delay, sight range, bytecode limit

    public final boolean isBuilding;
    public final RobotType spawnSource;
    public final RobotType dependency;
    public final int oreCost;
    public final int buildTurns;
    public final int supplyUpkeep;
    public final double maxHealth;
    public final double attackPower;
    public final int attackRadiusSquared;
    public final int movementDelay;
    public final int attackDelay;
    public final int loadingDelay;
    public final int cooldownDelay;
    public final int sensorRadiusSquared;
    public final int bytecodeLimit;

    public boolean canAttack() {
        return attackPower > 0;
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
              int bytecodeLimit) {
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
    }
}
