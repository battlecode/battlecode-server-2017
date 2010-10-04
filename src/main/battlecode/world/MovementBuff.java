/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package battlecode.world;

/**
 *
 * @author Sasa
 */
public class MovementBuff extends InternalBuff{

    private final BuffRemovalPolicy rp;
    private int movementAdder;
    private int movementMultiplier;

    public MovementBuff(InternalRobot robot, int movementAdder, int movementMultiplier, int time) {
        super(robot);
        this.movementAdder = movementAdder;
        this.movementMultiplier = movementMultiplier;
        this.rp = new TimedBuffRemovalPolicy(this,time);
    }




    @Override
    public void processBeginningOfRound(InternalRobotBuffs buffs) {
        buffs.modifyMovementDelayAdder(movementAdder);
        buffs.modifyMovementDelayAdder(movementMultiplier);
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
		return BuffType.MOVEMENT;
	}

    @Override
    public BuffRemovalPolicy getRemovalPolicy() {
        return rp;
    }

}
