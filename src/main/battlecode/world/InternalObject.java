package battlecode.world;

import battlecode.common.MapLocation;
import battlecode.common.Team;

public abstract class InternalObject extends BaseObject {

    protected volatile MapLocation myLocation;
    protected final GameWorld myGameWorld;

    protected InternalObject(GameWorld gw, MapLocation loc, Team t) {
        super(gw, t);
        myGameWorld = gw;
        myLocation = loc;
        gw.notifyAddingNewObject(this);
    }

    public void setLocation(MapLocation newLoc) {
        myGameWorld.notifyMovingObject(this, myLocation, newLoc);
        myLocation = newLoc;
    }

    public MapLocation getLocation() {
        return myLocation;
    }

    public GameWorld getGameWorld() {
        return myGameWorld;
    }

    public boolean exists() {
        return myGameWorld.exists(this);
    }

    public InternalObject container() {
        return null;
    }

    public MapLocation sensedLocation() {
        if (container() != null)
            return container().sensedLocation();
        else
            return getLocation();
    }
}
