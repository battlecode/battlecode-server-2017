/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package battlecode.world;

import battlecode.world.*;
import battlecode.common.GameConstants;

/**
 *
 * @author Sasa
 */
public class MovementAuraBuff extends InternalBuff{

    private final BuffRemovalPolicy rp;
    private int movementAdder;
    private int movementMultiplier;

    public MovementAuraBuff(InternalRobot robot) {
        super(robot);
        this.rp = new AlwaysBuffRemovalPolicy(this);
    }

    @Override
    public void processBeginningOfRound(InternalRobotBuffs buffs) {
		buffs.modifyMovementDelayAdder(-1);
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
		return BuffType.AURA_MOV;
	}

    @Override
    public BuffRemovalPolicy getRemovalPolicy() {
        return rp;
    }

}
