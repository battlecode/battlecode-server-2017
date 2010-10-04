package battlecode.engine;

import static battlecode.common.GameConstants.ARCHON_PRODUCTION;
import battlecode.common.*;
import battlecode.engine.instrumenter.*;
import battlecode.engine.scheduler.ScheduledRunnable;
import battlecode.world.*;

import battlecode.world.signal.SpawnSignal;

/*
TODO:
 - better error reporting
 */
public class PlayerFactory {

	private static boolean _debugMethodsEnabled = false;
	private static boolean _silenceA = false;
	private static boolean _silenceB = false;

	private PlayerFactory() {}

	public static void setProperties(boolean debugMethodsEnabled, boolean silenceA, boolean silenceB, boolean sameTeams) {
		_debugMethodsEnabled = debugMethodsEnabled;
		if(sameTeams && silenceA != silenceB) {
			_silenceA = _silenceB = false;
		} else {
			_silenceA = silenceA;
			_silenceB = silenceB;
		}
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

		// then, make a controller for the robot
		RobotController rc = new RobotControllerImpl(gw, robot);

		// now, we instantiate and instrument the player's class
		Class playerClass;
		String teamName = gw.getTeamName(t);
		boolean silenced;
		switch(t) {
			case A:
				silenced = _silenceA;
				break;
			case B:
				silenced = _silenceB;
				break;
			case NEUTRAL:
				silenced = _silenceA;
				break;
			default:
				ErrorReporter.report("Error in PlayerFactory.loadPlayer: unexpected team when determining silence", true);
				return;
		}
		try{
			ClassLoader icl = new IndividualClassLoader(teamName, _debugMethodsEnabled, silenced);
			playerClass = icl.loadClass(teamName + ".RobotPlayer");
			//~ System.out.println("PF done loading");
		} catch(InstrumentationException ie) {
			// if we get an InstrumentationException, then the error should have been reported, so we just kill the robot
			System.out.println("[Engine] Error during instrumentation of " + robot.toString() + ".\n[Engine] Robot will self-destruct in 3...2...1...");
			robot.suicide();
			return;
		} catch(Exception e) {
			ErrorReporter.report(e);
			robot.suicide();
			return;
		}

		// finally, create the player's thread, and let it loose
		new ScheduledRunnable(new RobotRunnable(playerClass, rc), robot.getID());

	}
}