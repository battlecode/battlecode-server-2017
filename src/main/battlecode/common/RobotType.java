package battlecode.common;

/**
 * Contains details on various attributes of the different robots. All of this information is in the specs in a more organized form.
 */
public enum RobotType {

    // last one is strength weight
    //  isbuilding, iszombie, infectTurns, spawn-source, build-dep, part cost, turns cost, hp, attack, range, movement delay, attack delay, cooldown delay, sight range, bytecode limit, zombieType, strWeight
    /**
     * An immobile unit that periodically spawns zombies; difficult to
     * destroy.
     *
     * @battlecode.doc.robottype
     */
    ZOMBIEDEN       (true,  true,  0,  null,      0,  0,2000,   0,   0,    0,  0, 0, -1,     10000, 0, null, false),
    //                                            PC  TC  HP    ATK RNG   MVD  AD CD
    /**
     * A normal, boring zombie unit.
     *
     * @battlecode.doc.robottype
     */
    STANDARDZOMBIE  (false, true,  10, ZOMBIEDEN, 0,  0,  60, 2.5,   2,    3,  2, 1, -1,     10000, 0, null, false),
    //                                            PC  TC  HP    ATK RNG   MVD  AD CD
    /**
     * A zombie unit with a ranged attack.
     *
     * @battlecode.doc.robottype
     */
    RANGEDZOMBIE    (false, true,  10, ZOMBIEDEN, 0,  0,  60,   3,  13,   3,   1, 1, -1,     10000, 0, null, false),
    //                                            PC  TC  HP    ATK RNG   MVD  AD CD
    /**
     * A fast zombie unit.
     *
     * @battlecode.doc.robottype
     */
    FASTZOMBIE      (false, true,  10, ZOMBIEDEN, 0,  0,  80,   3,  2,    1.4, 1, 1, -1,     10000, 0, null, true),
    //                                            PC  TC  HP    ATK RNG   MVD  AD CD
    /**
     * A big, tough zombie unit.
     *
     * @battlecode.doc.robottype
     */
    BIGZOMBIE       (false, true,  10, ZOMBIEDEN, 0,  0,  500,  25, 2,    4,   3, 2, -1,     10000, 0, null, true),
    //                                            PC  TC  HP    ATK RNG   MVD  AD CD
    /**
     * An important unit that cannot be constructed; builds other robots.
     *
     * @battlecode.doc.robottype
     */
    ARCHON          (false, false, 0,  null,      0,  0,  1000, 0,  24,   2,   1, 1, 35,     20000, 0, BIGZOMBIE, false),
    //                                            PC  TC  HP    ATK RNG   MVD  AD CD
    /**
     * A fast unit, unobstructed by rubble.
     *
     * @battlecode.doc.robottype
     */
    SCOUT           (false, false, 0,  ARCHON,    40, 15, 100,  0,  0,    1.4, 0, 1, 53,     20000, 0, FASTZOMBIE, true),
    //                                            PC  TC  HP    ATK RNG   MVD  AD CD
    /**
     * An all-around ranged unit.
     *
     * @battlecode.doc.robottype
     */
    SOLDIER         (false, false, 0,  ARCHON,    30, 12, 60,   4,  13,   2,   2, 1, 24,     10000, 0, STANDARDZOMBIE, false),
    //                                            PC  TC  HP    ATK RNG   MVD  AD CD
    /**
     * A melee unit equipped for zombie combat.
     *
     * @battlecode.doc.robottype
     */
    GUARD           (false, false, 0,  ARCHON,    30, 10, 145,  1.5, 2,   2,   1, 1, 24,     10000, 0, STANDARDZOMBIE, false),
    //                                            PC  TC  HP    ATK RNG   MVD  AD CD
    /**
     * A special unit cabable of infecting robots with a damaging strain of the
     * zombie virus.
     *
     * @battlecode.doc.robottype
     */
    VIPER           (false, false, 20, ARCHON,    120,30, 120,  2,  20,   2,   3, 1, 24,     10000, 0, RANGEDZOMBIE, false),
    //                                            PC  TC  HP    ATK RNG   MVD  AD CD
    /**
     * An immobile unit designed to reinforce an area; transforms into a
     * {@link #TTM TTM} in order to move.
     *
     * @battlecode.doc.robottype
     */
    TURRET          (false,  false, 0,  ARCHON,   130,25, 100,  13, 48,   0,   3, 3, 24,     10000,  0, RANGEDZOMBIE, false),
    //                                            PC  TC  HP    ATK RNG   MVD  AD CD
    /**
     * Turret - Transport Mode: the mobile version of a {@link #TURRET TURRET}.
     * Cannot attack.
     *
     * @battlecode.doc.robottype
     */
    TTM             (false, false, 0,  TURRET,    130,10, 100,  0,  0,    2,   0, 2, 24,     10000,  0, RANGEDZOMBIE, false),
    //                                            PC  TC  HP    ATK RNG   MVD  AD CD
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
     * Maximum health for the robot (for zombies, it's the BASE value).
     */
    public final double maxHealth;

    /**
     * Base damage per attack (for zombies, it's the BASE value).
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
     * Range^2 for sensing, or -1 if the robot can see everything.
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
     * Whether the unit ignores rubble.
     */
    public final boolean ignoresRubble;

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
        return this != ZOMBIEDEN && this != TURRET;
    }

    /**
     * Returns whether the robot can build.
     *
     * @return whether the robot can build.
     */
    public boolean canBuild() {
        return this == ARCHON || this == ZOMBIEDEN;
    }
    
    /**
     * Returns whether the robot can send a message signal.
     *
     * @return whether the robot can send a message signal.
     */

    public boolean canMessageSignal() {
        return this == ARCHON || this == SCOUT;
    }


    /**
     * Returns whether the robot is buildable.
     *
     * @return whether the robot is buildable.
     */
    public boolean isBuildable() {
        return spawnSource == ARCHON || spawnSource == ZOMBIEDEN;
    }

    /**
     * Returns whether the robot can clear rubble.
     *
     * @return whether the robot can clear rubble.
     */
    public boolean canClearRubble() { // Update later
        return this != TURRET && this != TTM;
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
              RobotType turnsInto,
              boolean ignoresRubble) {
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
        this.ignoresRubble = ignoresRubble;
    }

    /**
     * Returns the multiplier for attack and max health for a given round
     * (for zombie outbreaks).
     *
     * @param round round number
     * @return a double representing the attack and max health multiplier
     */
    public double getOutbreakMultiplier(int round) {
        int outbreakLevel = round / GameConstants.OUTBREAK_TIMER;
        switch (outbreakLevel) {
            case 0: return 1.00;
            case 1: return 1.10;
            case 2: return 1.20;
            case 3: return 1.30;
            case 4: return 1.50;
            case 5: return 1.70;
            case 6: return 2.00;
            case 7: return 2.30;
            case 8: return 2.60;
            case 9: return 3.00;
            default:
                return 3.00 + (outbreakLevel - 9);
        }
    }

    /**
     * Returns the attack power of a unit spawned on the given round, taking
     * outbreak into account.
     *
     * @param round round number
     * @return the attack power of this unit if spawned on the given round
     */
    public double attackPower(int round) {
        if (this.isZombie) {
            return attackPower * getOutbreakMultiplier(round);
        } else {
            return attackPower;
        }
    }

    /**
     * Returns the max health of a unit spawned on the given round, taking
     * outbreak into account.
     *
     * @param round round number
     * @return the max health of this unit if spawned on the given round
     */
    public double maxHealth(int round) {
        if (this.isZombie) {
            return maxHealth * getOutbreakMultiplier(round);
        } else {
            return maxHealth;
        }
    }
}
