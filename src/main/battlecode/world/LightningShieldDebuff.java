/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package battlecode.world;

import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;
import battlecode.world.signal.LightningShieldSignal;

/**
 *
 * @author Sasa
 */
public class LightningShieldDebuff extends InternalBuff {

    private final TimedBuffRemovalPolicy rp;
    private final int radius, power;

    public LightningShieldDebuff(InternalRobot robot, int rounds, int radius, double power) {
        super(robot);
        this.radius = radius;
        this.power = 0 - (int) Math.abs(power); // that is to prevent my own stupidity
        rp = new TimedBuffRemovalPolicy(this, rounds);
    }

    @Override
    public void processBeginningOfRound(InternalRobotBuffs buffs) {
        buffs.modifyEnergonRegen(power);

        MapLocation[] locs = this.getRobot().myGameWorld.getAllMapLocationsWithinRadiusSq(this.getRobot().getLocation(), radius*radius);
        for (int i = 0; i < locs.length; i++) {
            MapLocation ml = locs[i];
            InternalRobot r = this.getRobot().myGameWorld.getRobot(ml, RobotLevel.ON_GROUND);
            if (r != null && r != this.getRobot())
                new LightningShieldSignal(r, rp.getRoundsLeft(), radius, power).accept(this.getRobot().myGameWorld);
        }

    }

    public int getPower() {
        return power;
    }

    @Override
    public void processEndOfRound(InternalRobotBuffs buffs) {
        return; //pass
    }

	public BuffType type() {
		return BuffType.LIGHTNING_SHIELD;
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
