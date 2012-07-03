package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

public class UnweakenSignal extends Signal {
    
    public final int robotID;
    
    public UnweakenSignal(InternalRobot r) {
        this.robotID = r.getID();
    }
    
}
