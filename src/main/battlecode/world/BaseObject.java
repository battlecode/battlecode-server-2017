package battlecode.world;

import battlecode.common.GameObject;
import battlecode.common.Team;
import battlecode.engine.GenericWorld;

public abstract class BaseObject implements GameObject {

    private final int myID;
    private Team myTeam;

    protected BaseObject(GenericWorld gw, Team t) {
        myID = gw.nextID();
        myTeam = t;
    }

    // should be called at the beginning of every round
    public void processBeginningOfRound() {

    }

    // should be called at end of each turn
    public void processEndOfTurn() {
    }

    // should be called at the end of every round
    public void processEndOfRound() {
    }

    // should be called whenever a new game is started
    /*public static void resetIDs() {
    nextID = numRandomIDs+1;
    randomIDsInitialized = false;
    }*/

    public int getID() {
        return myID;
    }

    protected void setTeam(Team newTeam) {
        myTeam = newTeam;
    }

    public Team getTeam() {
        return myTeam;
    }

    @Override
    public String toString() {
        return myTeam.toString() + "#" + myID;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && (o instanceof BaseObject) && ((BaseObject) o).getID() == myID;
    }

    @Override
    public int hashCode() {
        return myID;
    }
}
