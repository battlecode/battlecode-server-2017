package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

public class TurnOnSignal extends Signal {

    public final int[] robotIDs;
    public final int sourceID;
    public final boolean broadcast;

    public TurnOnSignal(InternalRobot r, InternalRobot source, boolean broadcast) {
        this(new int[]{r.getID()}, source, broadcast);
    }

    public TurnOnSignal(int[] ids, InternalRobot source, boolean broadcast) {
        this.robotIDs = ids;
        this.sourceID = source.getID();
        this.broadcast = broadcast;
    }

}
