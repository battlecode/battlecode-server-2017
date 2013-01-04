/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package battlecode.world;

/**
 * @author Sasa
 */
public abstract class BuffRemovalPolicy {
    private final InternalBuff buff;

    public BuffRemovalPolicy(InternalBuff buff) {
        this.buff = buff;
    }

    public InternalBuff getBuff() {
        return buff;
    }

    public abstract boolean remove();
}
