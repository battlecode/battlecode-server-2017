package battlecode.engine;

import battlecode.engine.instrumenter.IndividualClassLoader;
import battlecode.engine.instrumenter.RobotMonitor;
import battlecode.engine.instrumenter.lang.RoboRandom;
import battlecode.engine.scheduler.Scheduler;
import battlecode.engine.signal.Signal;
import battlecode.server.Config;
import battlecode.world.GameWorldFactory;

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
    private Runnable ioCallback;

    public Engine(String teamA, String teamB, String mapName, String mapPath, long[][] archonMemory) {
        theInstance = this;
        Config options = Config.getGlobalConfig();
        this.garbageCollectEnabled = options.getBoolean("bc.engine.gc");
        this.garbageCollectRounds = options.getInt("bc.engine.gc-rounds");
        this.breakpointsEnabled = options.getBoolean("bc.engine.breakpoints");
        GenericWorld tempGameWorld = null;
        //InternalObject.resetIDs();
        IndividualClassLoader.reset();
        Scheduler.reset();
        RobotMonitor.reset();
        PlayerFactory.checkOptions();
        try {
            try {
                tempGameWorld = GameWorldFactory.createGameWorld(teamA, teamB, mapName, mapPath, archonMemory);
            } catch (IllegalArgumentException e) {
                java.lang.System.out.println("[Engine] Error while loading map '" + mapName + "'");
                return;
            } catch (Exception e) {
                ErrorReporter.report(e);
                return;
            }
        } finally {
            gameWorld = tempGameWorld;
        }
        gameWorld.resetStatic();
        RobotMonitor.setGameWorld(gameWorld);
        RoboRandom.setMapSeed(gameWorld.getMapSeed());
        Scheduler.start();
    }

    public GameWorldViewer getGameWorldViewer() {
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
            ioCallback.run();
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
            s.accept(gameWorld);
        } catch (RuntimeException e) {
            return false;
        }
        return true;
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

    public void setIOCallback(Runnable callback) {
        ioCallback = callback;
    }
}
