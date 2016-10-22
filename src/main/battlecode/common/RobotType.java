package battlecode.common;

/**
 * Contains details on various attributes of the different robots. All of this information is in the specs in a more organized form.
 */
public enum RobotType {

    // spawnSource, maxHealth, bulletCost, bodyRadius, bulletSpeed, attackPower, sensorRadius, bulletSightRadius, movementDelay, attackDelay, cooldownDelay, bytecodeLimit
    /**
     * An important unit that cannot be constructed; builds other robots.
     *
     * @battlecode.doc.robottype
     */
    ARCHON          (null,    1000,   -1,   2,  -1,  -1,   7,  15,   2,  -1,   1, 20000),
    //                          HP    BC   BR   BS   AP   SR  BSR  MVD   AD   CD    BCL
    /**
     * The main producer unit to make other units and trees; can't build Archons or other Gardeners
     *
     * @battlecode.doc.robottype
     */
    GARDENER        (ARCHON,   100,  100,   1,  -1,  -1,   5,  10,   2,  -1,   1, 10000),
    //                          HP    BC   BR   BS   AP   SR  BSR  MVD   AD   CD    BCL
    /**
     * A melee based unit that specializes at cutting down trees
     *
     * @battlecode.doc.robottype
     */
    LUMBERJACK      (GARDENER,  70,  100,   1,  -1, 1.5F,   5,  10,   2,   2,   1, 10000),
    //                          HP    BC   BR   BS   AP    SR  BSR  MVD   AD   CD    BCL
    /**
     * The basic fighting unit
     *
     * @battlecode.doc.robottype
     */
    RECRUIT         (GARDENER,  70,  100,   1,   1,   1,   5,  10,   1,   2,   1, 10000),
    //                          HP    BC   BR   BS   AP   SR  BSR  MVD   AD   CD    BCL
    /**
     * A slightly better fighting unit
     *
     * @battlecode.doc.robottype
     */
    SOLDIER         (GARDENER,  80,  130,   1,   1,   1,   5,  10,   1,   2,   1, 10000),
    //                          HP    BC   BR   BS   AP   SR  BSR  MVD   AD   CD    BCL
    /**
     * A strong fighting unit
     *
     * @battlecode.doc.robottype
     */
    TANK            (GARDENER, 125,  200,   1,   1,   1,   5,  10,   1,   2,   1, 10000),
    //                          HP    BC   BR   BS   AP   SR  BSR  MVD   AD   CD    BCL
    /**
     * An unit that specializes in movement
     *
     * @battlecode.doc.robottype
     */
    SCOUT           (GARDENER,  80,   80,   1,   1,   1,   5,  10,   1,   2,   1, 20000),
    //                          HP    BC   BR   BS   AP   SR  BSR  MVD   AD   CD    BCL
    ;
    
    /**
     * For units, this is the structure that spawns it. For non-spawnable robots, this is null.
     */
    public final RobotType spawnSource;

    /**
     * Maximum health for the robot.
     */
    public final int maxHealth;

    /**
     * Cost for creating the robot.
     */
    public final int bulletCost;

    /**
     * Radius for the robot.
     */
    public final float bodyRadius;

    /**
     * Speed of bullets produced from the robot.
     */
    public final float bulletSpeed;

    /**
     * Base damage per attack.
     */
    public final float attackPower;

    /**
     * Range for sensing robots and trees.
     */
    public final float sensorRadius;

    /**
     * Range for sensing bullets.
     */
    public final float bulletSightRadius;

    /**
     * Movement delay: the amount of contribution to core delay from a movement.
     */
    public final float movementDelay;

    /**
     * Attack delay: the amount of contribution to weapon delay from an attack.
     */
    public final float attackDelay;

    /**
     * Cooldown delay: the amount of contribution to core delay from an attack.
     */
    public final float cooldownDelay;

    /**
     * Base bytecode limit of this robot (halved if the robot does not have sufficient supply upkeep).
     */
    public final int bytecodeLimit;

    /**
     * Returns whether the robot can attack.
     *
     * @return whether the robot can attack.
     */
    public boolean canAttack() {
        return attackPower > 0;
    }

    /**
     * Returns whether the robot can hire Gardeners
     *
     * @return whether the robot can build.
     */
    public boolean canHire() {
        return this == ARCHON;
    }

    /**
     * Returns whether the robot can plant trees and all units except Gardeners and Archons
     *
     * @return whether the robot can build.
     */
    public boolean canPlant() {
        return this == GARDENER;
    }

    /**
     * Returns whether the robot is hireable.
     *
     * @return whether the robot is buildable.
     */
    public boolean isHireable() {
        return spawnSource == ARCHON;
    }

    /**
     * Returns whether the robot is plantable.
     *
     * @return whether the robot is buildable.
     */
    public boolean isPlantable() {
        return spawnSource == GARDENER;
    }

    /**
     * Returns the starting health of this type of robot
     *
     * @return the starting health of this type of robot
     */
    public float getStartingHealth() {
        return this == RobotType.ARCHON || this == RobotType.GARDENER ? this.maxHealth : .20F * this.maxHealth;
    }
    
    RobotType(RobotType spawnSource, int maxHealth, int bulletCost, float bodyRadius, float bulletSpeed, float attackPower,
              float sensorRadius, float bulletSightRadius, float movementDelay, float attackDelay,
              float cooldownDelay, int bytecodeLimit) {
        this.spawnSource        = spawnSource;
        this.maxHealth          = maxHealth;
        this.bulletCost         = bulletCost;
        this.bodyRadius         = bodyRadius;
        this.bulletSpeed        = bulletSpeed;
        this.attackPower        = attackPower;
        this.sensorRadius       = sensorRadius;
        this.bulletSightRadius  = bulletSightRadius;
        this.movementDelay      = movementDelay;
        this.attackDelay        = attackDelay;
        this.cooldownDelay      = cooldownDelay;
        this.bytecodeLimit      = bytecodeLimit;
    }

    public float getStrideRadius(){
        return 2*bodyRadius;
    }
}
