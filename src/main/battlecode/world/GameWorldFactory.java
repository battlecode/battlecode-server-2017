package battlecode.world;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.engine.PlayerFactory;
import battlecode.world.signal.SpawnSignal;

public class GameWorldFactory {

    public static GameWorld createGameWorld(String teamA, String teamB,
            String mapName, String mapPath, long[][] teamMemory)
            throws IllegalArgumentException {
        XMLMapHandler handler = XMLMapHandler.loadMap(mapName, mapPath);

        return new GameWorld(handler.getParsedMap(), teamA, teamB, teamMemory);
    }

    public static InternalRobot createPlayer(GameWorld gw, RobotType type,
            MapLocation loc, Team t, InternalRobot parent, boolean wakeDelay,
            int buildDelay) {

        InternalRobot robot;
        robot = new InternalRobot(gw, type, loc, t, wakeDelay, buildDelay);

        loadPlayer(gw, robot, t, parent, buildDelay);
        return robot;
    }

    // defaults to wakeDelay = true
    public static InternalRobot createPlayer(GameWorld gw, RobotType type,
            MapLocation loc, Team t, InternalRobot parent, int buildDelay) {
        return createPlayer(gw, type, loc, t, parent, true, buildDelay);
    }

    private static void loadPlayer(GameWorld gw, InternalRobot robot, Team t,
            InternalRobot parent, int buildDelay) {
        gw.addSignal(new SpawnSignal(robot, parent, buildDelay));
        RobotControllerImpl rc = new RobotControllerImpl(gw, robot);
        if(rc.getTeam() == Team.ZOMBIE){
            PlayerFactory.loadZombiePlayer(rc);
        } else {
            String teamName = gw.getTeamName(t);
            PlayerFactory.loadPlayer(rc, teamName);
        }
    }
    

}
