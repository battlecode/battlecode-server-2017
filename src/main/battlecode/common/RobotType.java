package battlecode.common;

/**
 * Contains details on various attributes of the different robots. All of this information is in the specs in a more organized form.
 */
public enum RobotType {

    // spawnSource, buildCooldownTurns, maxHealth, bulletCost, bodyRadius, bulletSpeed, attackPower, sensorRadius, bulletSightRadius, strideRadius, bytecodeLimit
    /**
     * An important unit that cannot be constructed; builds other robots.
     *
     * @battlecode.doc.robottype
     */
    ARCHON          (null,    0,    1000,   -1,   2,  -1,  -1,   7,  15, 1,  20000),
    //                              HP      BC   BR   BS   AP   SR  BSR  STR   BCL
    /**
     * The main producer unit to make other units and trees; can't build Archons or other Gardeners
     *
     * @battlecode.doc.robottype
     */
    GARDENER        (ARCHON,  10,   100,  100,   1,  -1,  -1,   5,  10,   1, 10000),
    //                              HP    BC   BR   BS   AP   SR  BSR  STR   BCL
    /**
     * A melee based unit that specializes at cutting down trees
     *
     * @battlecode.doc.robottype
     */
    LUMBERJACK      (GARDENER,  10, 70,  100,   1,  -1, 1.5F,   5,  10,  1, 10000),
    //                              HP    BC   BR   BS   AP    SR  BSR  STR   BCL
    /**
     * The basic fighting unit
     *
     * @battlecode.doc.robottype
     */
    RECRUIT         (GARDENER,  10, 70,  100,   1,   1,   1,   5,  10,   2, 10000),
    //                              HP    BC   BR   BS   AP   SR  BSR  STR   BCL
    /**
     * A slightly better fighting unit
     *
     * @battlecode.doc.robottype
     */
    SOLDIER         (GARDENER,  10, 80,  130,   1,   1,   1,   5,  10,   2, 10000),
    //                              HP    BC   BR   BS   AP   SR   BSR  STR   BCL
    /**
     * A strong fighting unit
     *
     * @battlecode.doc.robottype
     */
    TANK            (GARDENER, 10,  125,  200,   1,   1,   1,   5,  10,  (float)1.5, 10000),
    //                              HP    BC   BR   BS   AP   SR  BSR        STR   BCL
    /**
     * An unit that specializes in movement
     *
     * @battlecode.doc.robottype
     */
    SCOUT           (GARDENER,  10, 80,   80,   1,   1,   1,   5,  10,  (float)2.5, 20000),
    //                              HP    BC   BR   BS   AP   SR  BSR         STR   BCL
    ;
    
    /**
     * For units, this is the structure that spawns it. For non-spawnable robots, this is null.
     */
    public final RobotType spawnSource;

    /**
     * Cooldown turns for structure that spawns it.
     */
    public final int buildCooldownTurns;
    
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
     * Maximum distance the robot can move per turn
     */
    public final float strideRadius;

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
    
    RobotType(RobotType spawnSource, int buildCooldownTurns, int maxHealth, int bulletCost, float bodyRadius, float bulletSpeed, float attackPower,
              float sensorRadius, float bulletSightRadius, float strideRadius, int bytecodeLimit) {
        this.spawnSource        = spawnSource;
        this.buildCooldownTurns = buildCooldownTurns;
        this.maxHealth          = maxHealth;
        this.bulletCost         = bulletCost;
        this.bodyRadius         = bodyRadius;
        this.bulletSpeed        = bulletSpeed;
        this.attackPower        = attackPower;
        this.sensorRadius       = sensorRadius;
        this.bulletSightRadius  = bulletSightRadius;
        this.strideRadius       = strideRadius;
        this.bytecodeLimit      = bytecodeLimit;
    }

    public float getStrideRadius(){
        return 2*bodyRadius;
    }
}
