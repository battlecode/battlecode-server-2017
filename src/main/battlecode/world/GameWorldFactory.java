package battlecode.world;

import static battlecode.common.GameConstants.MAP_MAX_HEIGHT;
import static battlecode.common.GameConstants.MAP_MAX_WIDTH;
import static battlecode.common.GameConstants.MAP_MIN_HEIGHT;
import static battlecode.common.GameConstants.MAP_MIN_WIDTH;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;
import battlecode.common.Chassis;
import battlecode.common.Team;
import battlecode.common.TerrainTile;
import battlecode.engine.ErrorReporter;
import battlecode.engine.PlayerFactory;
import battlecode.world.GameMap.MapProperties;
import battlecode.world.signal.SpawnSignal;
import battlecode.engine.signal.Signal;

/*
TODO:
- make the parser more robust, and with better failure modes
- maybe take out the locations, objects, and terrain nodes
- comments & javadoc
 */
public class GameWorldFactory {
    
    public static GameWorld createGameWorld(String teamA, String teamB, String mapName, String mapPath, long[][] archonMemory) throws IllegalArgumentException {
 		XMLMapHandler handler = XMLMapHandler.loadMap(mapName,mapPath);

        return handler.createGameWorld(teamA, teamB, archonMemory);
    }

	public static InternalRobot createPlayer(GameWorld gw, Chassis type, MapLocation loc, Team t, InternalRobot parent, boolean wakeDelay) {

		// first, make the robot
		InternalRobot robot = new InternalRobot(gw, type, loc, t, wakeDelay);
		loadPlayer(gw, robot, t, parent);
		return robot;
	}

	// defaults to wakeDelay = true
	public static InternalRobot createPlayer(GameWorld gw, Chassis type, MapLocation loc, Team t, InternalRobot parent) {
		return createPlayer(gw, type, loc, t, parent, true);
	}

	private static void loadPlayer(GameWorld gw, InternalRobot robot, Team t, InternalRobot parent) {
		gw.addSignal(new SpawnSignal(robot, parent));
		RobotControllerImpl rc = new RobotControllerImpl(gw, robot);
		if(robot.getChassis().motor!=null)
			robot.equip(robot.getChassis().motor);
		String teamName = gw.getTeamName(t);
		PlayerFactory.loadPlayer(rc,teamName);
	}

}
