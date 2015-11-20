package battlecode.world.control;

import battlecode.world.GameWorld;
import battlecode.world.InternalRobot;

/**
 * RobotControlProvider that does nothing.
 *
 * @author james
 */
public class NullControlProvider implements RobotControlProvider {

    @Override
    public void matchStarted(GameWorld world) {}

    @Override
    public void matchEnded() {}

    @Override
    public void robotSpawned(InternalRobot robot) {}

    @Override
    public void robotKilled(InternalRobot robot) {}

    @Override
    public void runRound() {}
}
