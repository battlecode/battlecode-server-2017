package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

public class DropSupplySignal extends Signal {

    public final int fromID;
    public final double amount;

    public DropSupplySignal(InternalRobot from, double amount) {
        this.fromID = from.getID();
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public int getID() {
        return fromID;
    }
}
