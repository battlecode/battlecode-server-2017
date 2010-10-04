package battlecode.engine;

import battlecode.engine.instrumenter.*;
import battlecode.engine.instrumenter.lang.RoboRandom;
import battlecode.engine.scheduler.Scheduler;
import battlecode.world.*;
import battlecode.world.signal.*;
import battlecode.common.*;

//~ import java.lang.Thread;
/*
TODO:
 - constructor
 - comments
*/

public class Engine {

	private final GenericWorld gameWorld;
		
	private final boolean garbageCollectEnabled;
	private final int garbageCollectRounds;
	private final boolean breakpointsEnabled;
		
	private static Engine theInstance = null;

	public Engine(String teamA, String teamB, String mapName, String mapPath,
            boolean debugMethodsEnabled, boolean silenceA, boolean silenceB,
            boolean garbageCollectEnabled, int garbageCollectRounds,
            boolean upkeepEnabled, boolean spawnRadiusEnforced, boolean breakpointsEnabled,
            boolean bytecodesUsedEnabled, long[][] archonMemory) {
		theInstance = this;
		this.garbageCollectEnabled = garbageCollectEnabled;
		this.garbageCollectRounds = garbageCollectRounds;
		this.breakpointsEnabled = breakpointsEnabled;
		GameWorld tempGameWorld = null;
		//InternalObject.resetIDs();
		InternalRobot.setProperties(upkeepEnabled);
		InternalRobot.setSpawnRadiusEnforcement(spawnRadiusEnforced);
		InternalArchon.reset();
		IndividualClassLoader.reset();
		Scheduler.reset();
		RobotMonitor.reset();
		PlayerFactory.setProperties(debugMethodsEnabled, silenceA, silenceB, teamA.equals(teamB));
		try{
			try{
				tempGameWorld = GameWorldFactory.createGameWorld(teamA, teamB, mapName, mapPath, archonMemory);
			} catch(MapFileError mfe) {
				java.lang.System.out.println("[Engine] Error while loading map '" + mapName + "'");
				return;
			} catch(Exception e) {
				ErrorReporter.report(e);
				return;
			}
		} finally {
			gameWorld = tempGameWorld;
		}
		RobotMonitor.setGameWorld(gameWorld);
		RobotMonitor.setSilenceProperties(silenceA, silenceB);
		battlecode.engine.instrumenter.lang.System.setProperties(gameWorld, silenceA, silenceB, teamA.equals(teamB));
		RoboRandom.setMapSeed(gameWorld.getMapSeed());
		Scheduler.start();
	}
		
	public GameWorldViewer getGameWorldViewer() {
		return gameWorld;
	}
	
	public GameState runRound() {
		if(gameWorld == null)
			return GameState.DONE;
		if(!gameWorld.isRunning()) {
			return GameState.DONE;
		}
		try {
			if(gameWorld.getCurrentRound() != -1)
				gameWorld.clearAllSignals();
			gameWorld.processBeginningOfRound();
			if (Clock.getRoundNum() % 500 == 0)
			    System.out.println("Round: " + Clock.getRoundNum());
			Scheduler.passToNextThread();
			gameWorld.processEndOfRound();
			if(!gameWorld.isRunning()) {
				// Let all of the threads return so we don't leak
				// memory.  GameWorld has already told RobotMonitor
				// to kill all the robots;
				//System.out.println("Trying to clean up robots");
				Scheduler.passToNextThread();
			}
		} catch(Exception e) {
			ErrorReporter.report(e);
			return GameState.DONE;
		}
		if(garbageCollectEnabled && Clock.getRoundNum() % garbageCollectRounds == 0)
			java.lang.System.gc();
		return ((breakpointsEnabled && gameWorld.wasBreakpointHit()) ? GameState.BREAKPOINT : GameState.RUNNING);
	}
	
	/**
	 * TODO: update this, since energon change signal breaks this
	 * @return true if the gamestate may have changed, false if the gamestate did not change
	 */
	public boolean receiveSignal(Signal s) {
		gameWorld.clearAllSignals();
		Exception result = s.accept(gameWorld);
		return (result == null);
	}
	
	public boolean isRunning() {
		return (gameWorld != null) && gameWorld.isRunning();
	}
	
	public static int getRoundNum() {
		return theInstance.gameWorld.getCurrentRound();
	}
	
	public long[][] getArchonMemory() {
		return gameWorld.getArchonMemory();
	}
		
	// for testing only -- just simulates some rounds
	public static void main(String[] args) throws Exception{
		final int NUM_ROUNDS = 200;
		long[][] archonMemory = new long[2][6];
		Engine myEngine = new Engine("team000", "team000", "glass", java.lang.System.getProperty("user.dir") + "/../maps", true, false, false, false, 0, true, true, true, true, archonMemory);
		if(!myEngine.isRunning()) {
			java.lang.System.out.println("error initializing engine; dummy main is now bailing");
			return;
		}
		int i;
		//~ Signal[] signals = myEngine.gameWorld.getAllSignals();
		//~ for(Signal s : signals) {
			//~ System.out.println(s.class.getName());
			//~ System.out.println(s);
		//~ }
		for(i = 0; i < NUM_ROUNDS; i++) {
			if(!myEngine.isRunning())
				break;
			myEngine.runRound();
			//~ System.out.println(roundNum);
			//~ Signal[] signals = myEngine.gameWorld.getAllSignals();
			//~ SignalHandler<String> handler = new SignalDisplayHandler();
			//~ for(Signal s : signals) {
				//~ System.out.println(s.accept(handler));
			//~ }
			//~ java.lang.System.out.println("Round " + i);
			//~ java.lang.System.out.println("World Round " + gameWorld.);
			//~ java.lang.System.gc();
			//~ for(InternalObject o : myEngine.getAllGameObjects()) {
				//~ java.lang.System.out.println(((InternalRobot)o).getCurrentAction());
			//~ }
			//~ java.lang.System.out.println("A: " +myEngine.getGameStats().getPoints(Team.A) + "    B: " + myEngine.getGameStats().getPoints(Team.B));
			//~ java.lang.System.out.println("A: " + myEngine.getGameStats().getEnergon(Team.A) + "    B: " + myEngine.getGameStats().getEnergon(Team.B));
			//~ InternalObject[] objects = myEngine.getGameWorld().getAllGameObjects();
			//~ for(InternalObject o : objects) {
				//~ java.lang.System.out.println(o.toString() + " @ " + o.getLocation().toString());
			//~ }
		}
		//~ Thread.sleep(1000);
		java.lang.System.out.println("done");
		java.lang.System.out.println(i);
	}

}
