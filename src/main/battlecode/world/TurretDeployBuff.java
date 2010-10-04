/*
 *
 */
package battlecode.world;

import battlecode.common.GameConstants;

/**
 *
 * @author Sasa
 */
public class TurretDeployBuff extends InternalBuff {

    //these should go in GameConstants if we decide to have them
    //private final static int turnRoundsDelay = 5;
    //private final static double energonRegen = 0.3;
    // this buff gets removed when the robot calls
    // undeploy()
    private final BuffRemovalPolicy rp = new LocationBuffRemovalPolicy(this);
    //turrets can't attack while deploying so this isn't needed
    //private final int deployRound;

    public TurretDeployBuff(InternalRobot robot) {
        super(robot);
        //deployRound = robot.myGameWorld.getCurrentRound() + GameConstants.TURRET_DEPLOY_TIME;
    }

    @Override
    public void processBeginningOfRound(InternalRobotBuffs buffs) {
        buffs.modifyTurnDelayAdder(GameConstants.TURRET_TURN_SPEED_DELAY);
        //if (this.getRobot().myGameWorld.getCurrentRound() >= deployRound) {
        buffs.modifyDamageDealtAdder(GameConstants.TURRET_DEPLOY_ATTACK_BONUS);
        buffs.modifyEnergonRegen(GameConstants.TURRET_DEPLOY_UPKEEP_REDUCTION);
        //}
    }

    public BuffType type() {
        return BuffType.TURRET_DEPLOY;
    }

    @Override
    public void processEndOfRound(InternalRobotBuffs buffs) {
        return; //pass
    }

    @Override
    public boolean verifyAdd(InternalRobot robot) {
        return true;
    }

    @Override
    public BuffRemovalPolicy getRemovalPolicy() {
        return rp;
    }
}
