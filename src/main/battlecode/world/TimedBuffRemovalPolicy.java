/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package battlecode.world;

/**
 *
 * @author Sasa
 */
public class TimedBuffRemovalPolicy extends BuffRemovalPolicy {

    private final int roundsLeft;
    private final int startRound;

    public TimedBuffRemovalPolicy(InternalBuff buff, int roundsLeft) {
        super(buff);
        this.roundsLeft = roundsLeft;
        this.startRound = this.getBuff().getRobot().myGameWorld.getCurrentRound();
    }

    public int getRoundsLeft() {
        int cr = this.getBuff().getRobot().myGameWorld.getCurrentRound();
        return cr - startRound - roundsLeft;
    }

    @Override
    public boolean remove() {
        int cr = this.getBuff().getRobot().myGameWorld.getCurrentRound();
        return cr - startRound >= roundsLeft;
    }
}
