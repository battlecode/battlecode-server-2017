/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package battlecode.world;

import battlecode.common.GameConstants;

/**
 *
 * @author Sasa
 */
public class DefensiveAuraBuff extends InternalBuff {

    private final BuffRemovalPolicy rp;

    public DefensiveAuraBuff(InternalRobot robot) {
        super(robot);
        this.rp = new AlwaysBuffRemovalPolicy(this);
    }

    @Override
    public void processBeginningOfRound(InternalRobotBuffs buffs) {
        buffs.modifyDamageReceivedMultiplier(-GameConstants.AURA_DEF_FACTOR);
    }

    @Override
    public void processEndOfRound(InternalRobotBuffs buffs) {
        //pass
    }

    @Override
    public boolean verifyAdd(InternalRobot robot) {
        return true;
    }

    public BuffType type() {
        return BuffType.AURA_DEF;
    }

    @Override
    public BuffRemovalPolicy getRemovalPolicy() {
        return rp;
    }
}
