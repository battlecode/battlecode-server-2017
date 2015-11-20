package battlecode.engine;

import battlecode.engine.instrumenter.IndividualClassLoader;
import battlecode.engine.instrumenter.RobotMonitor;
import battlecode.engine.instrumenter.lang.RoboRandom;
import battlecode.engine.scheduler.Scheduler;
import battlecode.engine.signal.Signal;
import battlecode.server.Config;
import battlecode.world.GameWorld;
import battlecode.world.XMLMapHandler;

//~ import java.lang.Thread;
/*
TODO:
- comments
 */
public class Engine {

    private final GameWorld gameWorld;
    private final boolean garbageCollectEnabled;
    private final int garbageCollectRounds;
    private final boolean breakpointsEnabled;

    public Engine(String teamA, String teamB, String mapName, String mapPath, long[][] teamMemory) {
        Config options = Config.getGlobalConfig();
        this.garbageCollectEnabled = options.getBoolean("bc.engine.gc");
        this.garbageCollectRounds = options.getInt("bc.engine.gc-rounds");
        this.breakpointsEnabled = options.getBoolean("bc.engine.breakpoints");
        IndividualClassLoader.reset();
        Scheduler.reset();
        RobotMonitor.reset();

        try {
            XMLMapHandler handler = XMLMapHandler.loadMap(mapName, mapPath);

            gameWorld = new GameWorld(handler.getParsedMap(), teamA, teamB, teamMemory);
        } catch (IllegalArgumentException e) {
            System.out.println("[Engine] Error while loading map '" + mapName + "'");
            throw e;
        } catch (Exception e) {
            ErrorReporter.report(e);
            throw e;
        }

        RobotMonitor.setGameWorld(gameWorld);
        RoboRandom.setMapSeed(gameWorld.getMapSeed());
        Scheduler.start();
    }

    public GameWorld getGameWorld() {
        return gameWorld;
    }

    public GameState runRound() {
        if (gameWorld == null) {
            return GameState.DONE;
        }
        if (!gameWorld.isRunning()) {
            return GameState.DONE;
        }
        try {
            if (gameWorld.getCurrentRound() != -1) {
                gameWorld.clearAllSignals();
            }
            gameWorld.processBeginningOfRound();
            if (getRoundNum() % 500 == 0) {
                System.out.println("Round: " + getRoundNum());
            }
            Scheduler.startNextThread();
            Scheduler.endTurn();
            gameWorld.processEndOfRound();
            if (!gameWorld.isRunning()) {
                // Let all of the threads return so we don't leak
                // memory.  GameWorld has already told RobotMonitor
                // to kill all the robots;
                //System.out.println("Trying to clean up robots");
                Scheduler.passToNextThread();
                //System.out.println("Time spent in scheduler: "+(Scheduler.timeInScheduler*1.E-9));
            }
        } catch (Exception e) {
            ErrorReporter.report(e);
            return GameState.DONE;
        }
        if (garbageCollectEnabled && getRoundNum() % garbageCollectRounds == 0) {
            java.lang.System.gc();
        }
        return ((breakpointsEnabled && gameWorld.wasBreakpointHit()) ? GameState.BREAKPOINT : GameState.RUNNING);
    }

    /**
     * TODO: update this, since energon change signal breaks this
     *
     * @return true if the gamestate may have changed, false if the gamestate did not change
     */
    public boolean receiveSignal(Signal s) {
        gameWorld.clearAllSignals();
        try {
            gameWorld.visitSignal(s);
        } catch (RuntimeException e) {
            return false;
        }
        return true;
    }

    public boolean isRunning() {
        return (gameWorld != null) && gameWorld.isRunning();
    }

    public int getRoundNum() {
        return gameWorld.getCurrentRound();
    }

    public long[][] getTeamMemory() {
        return gameWorld.getTeamMemory();
    }
}
