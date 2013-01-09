package battlecode.world;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.engine.PlayerFactory;
import battlecode.world.signal.SpawnSignal;

/*
TODO:
- make the parser more robust, and with better failure modes
- maybe take out the locations, objects, and terrain nodes
- comments & javadoc
 */
public class GameWorldFactory {

    public static GameWorld createGameWorld(String teamA, String teamB, String mapName, String mapPath, long[][] archonMemory) throws IllegalArgumentException {
        XMLMapHandler handler = XMLMapHandler.loadMap(mapName, mapPath);

        return handler.createGameWorld(teamA, teamB, archonMemory);
    }

    public static InternalRobot createPlayer(GameWorld gw, RobotType type, MapLocation loc, Team t, InternalRobot parent, boolean wakeDelay) {

        // first, make the robot
        InternalRobot robot;
				robot = new InternalRobot(gw, type, loc, t, wakeDelay);
				loadPlayer(gw, robot, t, parent);
        return robot;
    }

    // defaults to wakeDelay = true
    public static InternalRobot createPlayer(GameWorld gw, RobotType type, MapLocation loc, Team t, InternalRobot parent) {
        return createPlayer(gw, type, loc, t, parent, true);
    }

    private static void loadPlayer(GameWorld gw, InternalRobot robot, Team t, InternalRobot parent) {
        gw.addSignal(new SpawnSignal(robot, parent));
        RobotControllerImpl rc = new RobotControllerImpl(gw, robot);
        String teamName = gw.getTeamName(t);
        PlayerFactory.loadPlayer(rc, teamName);
    }

}
