package battlecode.world;

import static battlecode.common.GameConstants.MAP_MAX_HEIGHT;
import static battlecode.common.GameConstants.MAP_MAX_WIDTH;
import static battlecode.common.GameConstants.MAP_MIN_HEIGHT;
import static battlecode.common.GameConstants.MAP_MIN_WIDTH;
import static battlecode.common.GameConstants.ARCHON_PRODUCTION;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;
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

	public static void createPlayer(GameWorld gw, RobotType type, MapLocation loc, Team t, InternalRobot parent, boolean wakeDelay) {
		// note that the order in which all these calls are made is very important

		if(type == RobotType.ARCHON) {
			createArchonPlayer(gw, loc, t, parent, wakeDelay, ARCHON_PRODUCTION);
		}
		else if(type == RobotType.AURA) {
			InternalRobot robot = new InternalAura(gw, type, loc, t, wakeDelay);

			loadPlayer(gw, robot, t, parent);
		}
		else {
			// first, make the robot
			InternalRobot robot = new InternalWorker(gw, type, loc, t, wakeDelay);

			loadPlayer(gw, robot, t, parent);
		}
	}

	// defaults to wakeDelay = true
	public static void createPlayer(GameWorld gw, RobotType type, MapLocation loc, Team t, InternalRobot parent) {
		createPlayer(gw, type, loc, t, parent, true);
	}

	public static void createArchonPlayer(GameWorld gw, MapLocation loc, Team t, InternalRobot parent, boolean wakeDelay, double production) {
		InternalRobot robot = new InternalArchon(gw, loc, t, wakeDelay, production);

		loadPlayer(gw, robot, t, parent);
	}
	
	public static void createWorkerPlayer(GameWorld gw, MapLocation loc, Team t, InternalRobot parent, boolean wakeDelay) {
		InternalRobot robot = new InternalWorker(gw, RobotType.WOUT, loc, t, wakeDelay);

		loadPlayer(gw, robot, t, parent);
	}

	private static void loadPlayer(GameWorld gw, InternalRobot robot, Team t, InternalRobot parent) {
		gw.addSignal(new SpawnSignal(robot, parent));
		RobotControllerImpl rc = new RobotControllerImpl(gw, robot);
		String teamName = gw.getTeamName(t);
		PlayerFactory.loadPlayer(rc,teamName);
	}

}
