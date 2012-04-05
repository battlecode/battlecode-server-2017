package battlecode.engine;

import battlecode.common.Team;

/*
 * Interface containing all of the functions needed by the instrumenter.
 */
public interface GenericRobot {

    public int getID();

    public Team getTeam();

    public int getBytecodesUsed();

    public int getBytecodeLimit();

    public void suicide();
}
