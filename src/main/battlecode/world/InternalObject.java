package battlecode.world;

import battlecode.common.*;

public abstract class InternalObject extends BaseObject {

    protected volatile MapLocation myLocation;
    protected final RobotLevel myHeight;
    protected final GameWorld myGameWorld;

    protected InternalObject(GameWorld gw, MapLocation loc, RobotLevel height, Team t) {
        super(gw, t);
        myGameWorld = gw;
        myLocation = loc;
        myHeight = height;
        gw.notifyAddingNewObject(this);
    }

    public void setLocation(MapLocation newLoc) {
        myGameWorld.notifyMovingObject(this, myLocation, newLoc);
        myLocation = newLoc;
    }

    public MapLocation getLocation() {
        return myLocation;
    }

    public RobotLevel getRobotLevel() {
        return myHeight;
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
}
