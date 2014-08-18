package battlecode.common;

import static battlecode.common.RobotLevel.ON_GROUND;

/**
 * Contains details on various attributes of the different robots.
 */
public enum RobotType {
    //  isbuilding, spawn-source, ore cost, turns cost, supply upkeep, hp, attack, range, movement delay, attack delay, loading delay, cooldown delay, sight range, bytecode limit
    HQ          (false, null, 0, 0, 0, 1000, 24, 24, 0, 2, 0, 0, 35, 10000),    
    TOWER       (false, null, 0, 0, 0,  500, 15, 24, 0, 2, 0, 0, 35,  2000),

    SUPPLYDEPOT                 (true, null, 100,  20, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000),
    TECHNOLOGYINSTITUTE         (true, null, 300,  80, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000),
    BARRACKS                    (true, null, 250,  50, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000),
    METABUILDER                 (true, null, 150,  50, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000),
    HELIPAD                     (true, null, 250,  50, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000),
    TRAININGFIELD               (true, null, 400,  80, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000),
    TANKFACTORY                 (true, null, 300,  80, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000),
    MINERFACTORY                (true, null, 400, 100, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000),
    ENGINEERINGBAY              (true, null, 200, 120, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000),
    HANDWASHSTATION             (true, null, 200, 100, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000),
    BIOMECHATRONICRESEARCHLAB   (true, null, 200, 200, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000),
    AEROSPACELAB                (true, null, 500, 200, 0, 100, 0, 0, 0, 0, 0, 0, 24, 2000),

    FURBY       (false,                  HQ, 100,  20, 20,  30,  6,  8, 2, 2, 1, 1, 24, 10000),
    COMPUTER    (false, TECHNOLOGYINSTITUTE,  10,  25,  5,   1,  0,  0, 8, 0, 0, 0, 24, 10000),
    SOLDIER     (false,            BARRACKS,  10,  15, 10,  40,  8, 15, 2, 2, 1, 1, 24, 10000),
    BASHER      (false,            BARRACKS,  80,  15, 10,  40,  8, 15, 2, 2, 1, 1, 24, 10000),
    BUILDER     (false,         METABUILDER, 100,  20,  5,  80,  0,  0, 3, 0, 0, 0, 24, 10000),
    MINER       (false,        MINERFACTORY,  50,  20, 15,  50,  3,  5, 2, 2, 2, 1, 24, 10000),
    DRONE       (false,             HELIPAD, 120,  40,  5,  80, 10,  8, 1, 3, 0, 0, 24, 10000),
    TANK        (false,         TANKFACTORY, 200,  60, 30, 160, 20, 24, 2, 3, 2, 2, 24, 10000),
    COMMANDER   (false,       TRAININGFIELD, 100,  80, 10, 120,  8, 15, 2, 1, 0, 0, 24, 10000),
    LAUNCHER    (false,        AEROSPACELAB, 400, 100, 50, 400,  0,  0, 4, 0, 0, 0, 24, 10000),
    MISSILE     (false,            LAUNCHER,   0,   6,  0,   5, 20,  2, 1, 0, 0, 0, 24,   500),
    ;

    //  isbuilding, spawn-source, ore cost, turns cost, supply upkeep, hp, attack, range, movement delay, attack delay, loading delay, cooldown delay, sight range, bytecode limit

    public final RobotLevel level = ON_GROUND;
    public final boolean isBuilding;
    public final RobotType spawnSource;
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

    public boolean canAttack(RobotLevel level) {
        return attackPower > 0;
    }

    RobotType(boolean isBuilding,
              RobotType spawnSource,
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
