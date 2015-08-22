package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

public class TransferSupplySignal extends Signal {

    public final int fromID;
    public final int toID;
    public final double amount;

    public TransferSupplySignal(InternalRobot from, InternalRobot to, double amount) {
        this.fromID = from.getID();
        this.toID = to.getID();
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }
}
