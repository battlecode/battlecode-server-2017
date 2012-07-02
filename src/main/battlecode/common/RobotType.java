package battlecode.common;

import static battlecode.common.RobotLevel.IN_AIR;
import static battlecode.common.RobotLevel.ON_GROUND;

public enum RobotType {

    NEXUS(ON_GROUND, 1000, 0, 0, 0, 0, 0, 100, false, false, 0, 0, 1.0), 
    TRANSPORTER(IN_AIR, 50, 4, 0, 0, 0, 0, 8, false, false, 40, 0, 1.0),
    SOLDIER(ON_GROUND, 30, 6, 25, 180, 4, 10, 49, true, true, 0, 10, 1.0);

    /**
     * The robot's level (air or ground)
     */
    public final RobotLevel level;

    /**
     * The maximum amount of energon (hit points) the robot can have.
     */
    public final double maxEnergon;

    /**
     * The number of turns it takes the robot to move orthogonally.
     */
    public final int moveDelay;
    public final int weakenedMoveDelay;

    /**
     * The number of turns it takes the robot to move diagonally.
     * Equal to <code>(int)Math.round(moveDelay*Math.sqrt(2.))</code>.
     */
    public final int moveDelayDiagonal;
    public final int weakenedMoveDelayDiagonal;

    /**
     * The square of the maximum distance that the robot can attack.
     */
    public final int attackRadiusSquared;
    public final int weakenedAttackRadiusSquared;

    /**
     * The range of angles that this robot can attack.
     */
    public final double attackAngle;

    /**
     * The number of turns that it takes this robot to attack.
     */
    public final int attackDelay;
    public final int weakenedAttackDelay;

    /**
     * The amount of damage that this robot does when it attacks.
     */
    public final double attackPower;
    public final int weakenedAttackPower;

    /**
     * The radius at which robots can sense other Robots and GameObjects
     */
    public final int sensorRadiusSquared;

    /**
     * Whether or not the robot can attack air units.
     */
    public final boolean canAttackAir;

    /**
     * Whether or not the robot can attack ground units.
     */
    public final boolean canAttackGround;

    /**
     * Returns how much flux the robot can transport around
     */
    public final int fluxTransportCapacity; 

    /**
     * Returns the number of rounds a robot can remain energized after  consuming flux
     */
    public final int sustainRounds;

    /**
    * If a robot has not consumed flux within the past sustainRounds rounds, it is weakened by weakenedFactor 
    * moveDelay = round(moveDelay * weakenedFactor)
    * moveDelayDiagonal = round(moveDelayDiagonal * weakenedFactor)
    * attackDelay = round(attackDelay * weakenedFactor)
    * attackRadiusSquared = round(attackRadiusSquared / weakenedFactor)
    * attackPower = round(attackPower / weakenedFactor)
    */ 
    public final float weakenedFactor;

    /**
     * Returns true if the robot can attack robots at the given level.
     */
    public boolean canAttack(RobotLevel level) {
        switch (level) {
            case ON_GROUND:
                return canAttackGround;
            case IN_AIR:
                return canAttackAir;
            default:
                return false;
        }
    }

    /**
     * Returns <code>true</code> if this robot's level is
     * <code>RobotLevel.IN_AIR</code>.
     */
    public boolean isAirborne() {
        return level == RobotLevel.IN_AIR;
    }


    RobotType(RobotLevel level,
              double maxEnergon,
              int moveDelay,
              int attackRadiusSquared,
              double attackAngle,
              int attackDelay,
              double attackPower,
              int sensorRadiusSquared,
              boolean canAttackAir,
              boolean canAttackGround,
              int fluxTransportCapacity, 
              int sustainRounds, 
              float weakenedFactor
              ) {
        this.level = level;
        this.maxEnergon = maxEnergon;
        this.moveDelay = moveDelay;
        this.moveDelayDiagonal = (int) Math.round(moveDelay * Math.sqrt(2.0));
        this.attackRadiusSquared = attackRadiusSquared;
        this.attackAngle = attackAngle;
        this.attackDelay = attackDelay;
        this.attackPower = attackPower;
        this.sensorRadiusSquared = sensorRadiusSquared;
        this.canAttackAir = canAttackAir;
        this.canAttackGround = canAttackGround;
        this.fluxTransportCapacity = fluxTransportCapacity;
        this.sustainRounds = sustainRounds;
        this.weakenedFactor = weakenedFactor;

        this.weakenedMoveDelay = (int) Math.round(moveDelay * weakenedFactor);
        this.weakenedMoveDelayDiagonal = (int) Math.round(moveDelay * weakenedFactor * Math.sqrt(2.0));
        this.weakenedAttackRadiusSquared = (int) Math.round(attackRadiusSquared / weakenedFactor);
        this.weakenedAttackDelay = (int) Math.round(attackDelay * weakenedFactor);
        this.weakenedAttackPower = (int) Math.round(attackPower / weakenedFactor);
        
    }

}

