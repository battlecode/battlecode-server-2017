package battlecode.world.control;

import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.world.GameWorld;
import battlecode.world.InternalRobot;

/**
 * Represents a tool that can handle controlling robots during a
 * match. This could be the scheduler-instrumenter infrastructure,
 * or something simpler to e.g. control the neutral or zombie team.
 *
 * Behavior *must* be deterministic or matches won't be reproducible.
 *
 * @author james
 */
public interface RobotControlProvider {

    /**
     * Signals to the provider that a match has started,
     * and gives it access to a GameWorld.
     *
     * The world must be initialized but might not have any robots
     * spawned yet.
     */
    void matchStarted(GameWorld world);

    /**
     * Tells the provider that the match has ended, and it can
     * release its resources.
     */
    void matchEnded();

    /**
     * Signals to the provider that a robot has spawned, and
     * gives it a handle to that robot.
     *
     * @param robot the newly spawned robot
     */
    void robotSpawned(InternalRobot robot);

    /**
     * Signals to the provider that the robot with the
     * given info has been killed.
     *
     * @param robot the freshly executed robot
     */
    void robotKilled(InternalRobot robot);

    /**
     * Tells the provider to process the current round.
     *
     * THE IMPLEMENTER MUST CALL processBeginningOfTurn() AND
     * processEndOfTurn() ON ALL ROBOTS IT CONTROLS. This is
     * awkward, and will be fixed after another round of refactoring.
     */
    void runRound();
}
