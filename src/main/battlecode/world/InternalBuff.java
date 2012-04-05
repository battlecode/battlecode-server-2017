/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package battlecode.world;

/**
 * @author Sasa
 */
public abstract class InternalBuff {

    private final InternalRobot robot;

    public InternalRobot getRobot() {
        return robot;
    }

    public InternalBuff(InternalRobot robot) {
        this.robot = robot;
    }

    public abstract void processBeginningOfRound(InternalRobotBuffs buffs);

    public abstract void processEndOfRound(InternalRobotBuffs buffs);

    public abstract boolean verifyAdd(InternalRobot robot);

    public abstract BuffRemovalPolicy getRemovalPolicy();

    public abstract BuffType type();
}
