package battlecode.world;

import battlecode.common.*;
import static battlecode.common.GameActionExceptionType.*;
import battlecode.instrumenter.RobotDeathException;
import battlecode.world.signal.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * The actual implementation of RobotController. Its methods *must* be called
 * from a player thread.
 *
 * It is theoretically possible to have multiple for a single InternalRobot, but
 * that may cause problems in practice, and anyway why would you want to?
 *
 * All overriden methods should assertNotNull() all of their (Object) arguments,
 * if those objects are not explicitly stated to be nullable.
 */
public final class RobotControllerImpl implements RobotController {

    /**
     * The world the robot controlled by this controller inhabits.
     */
    private final GameWorld gameWorld;

    /**
     * The robot this controller controls.
     */
    private final InternalRobot robot;

    /**
     * Create a new RobotControllerImpl
     *
     * @param gameWorld the relevant world
     * @param robot the relevant robot
     */
    public RobotControllerImpl(GameWorld gameWorld, InternalRobot robot) {
        this.gameWorld = gameWorld;
        this.robot = robot;
    }

    // *********************************
    // ******** INTERNAL METHODS *******
    // *********************************

    /**
     * @return the robot this controller is connected to
     */
    public InternalRobot getRobot() {
        return robot;
    }

    /**
     * Throw a null pointer exception if an object is null.
     *
     * @param o the object to test
     */
    private static void assertNotNull(Object o) {
        if (o == null) {
            throw new NullPointerException("Argument has an invalid null value");
        }
    }

    @Override
    public int hashCode() {
        return robot.getID();
    }

    // *********************************
    // ****** GLOBAL QUERY METHODS *****
    // *********************************

    @Override
    public int getRoundLimit(){
        return gameWorld.getGameMap().getRounds();
    }

    @Override
    public int getRoundNum(){
        return gameWorld.getCurrentRound();
    }

    @Override
    public float getTeamBullets(){
        return gameWorld.getTeamInfo().getBulletSupply(this.robot.getTeam());
    }

    @Override
    public int getTeamVictoryPoints(){
        return gameWorld.getTeamInfo().getVictoryPoints(this.robot.getTeam());
    }

    @Override
    public int getOpponentVictoryPoints(){
        return gameWorld.getTeamInfo().getVictoryPoints(this.robot.getTeam().opponent());
    }

    @Override
    public int getRobotCount(){
        return gameWorld.getObjectInfo().getRobotCount(this.robot.getTeam());
    }

    @Override
    public int getTreeCount(){
        return gameWorld.getObjectInfo().getTreeCount(this.robot.getTeam());
    }

    @Override
    public MapLocation[] getInitialArchonLocations(Team t){
        if (t == Team.NEUTRAL) {
            return new MapLocation[0];
        } else {
            GameMap.InitialRobotInfo[] initialRobots = gameWorld.getGameMap()
                    .getInitialRobots();
            ArrayList<MapLocation> archonLocs = new ArrayList<>();
            for (GameMap.InitialRobotInfo initial : initialRobots) {
                if (initial.type == RobotType.ARCHON && initial.team == t) {
                    archonLocs.add(initial.getLocation(gameWorld.getGameMap()
                            .getOrigin()));
                }
            }
            MapLocation[] array = archonLocs.toArray(new MapLocation[archonLocs.size()]);
            Arrays.sort(array);
            return array;
        }
    }
}
