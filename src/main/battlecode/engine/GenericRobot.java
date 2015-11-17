package battlecode.engine;

import battlecode.common.Team;

/*
 * Interface containing all of the functions needed by the instrumenter.
 */
public interface GenericRobot {

    int getID();

    Team getTeam();

    int getBytecodesUsed();

    int getBytecodeLimit();

    void suicide();
}
