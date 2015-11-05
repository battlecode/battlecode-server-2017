package battlecode.engine;

import battlecode.common.Team;
import battlecode.engine.signal.Signal;
import battlecode.engine.signal.SignalHandler;
import battlecode.serial.GameStats;
import battlecode.serial.RoundStats;
import battlecode.world.GameMap;

/**
 * An interface that includes the part of the game world that isn't expected to change
 * from year to year.
 */
public interface GenericWorld extends SignalHandler {
    public int getCurrentRound();

    public int getMapSeed();

    public GameMap getGameMap();

    public String getTeamName(Team t);

    public Team getWinner();

    public boolean isRunning();

    public Signal[] getAllSignals(boolean includeBytecodesUsedSignal);

    public RoundStats getRoundStats();

    public GameStats getGameStats();

    public void beginningOfExecution(int id);

    public void endOfExecution(int id);

    public void processBeginningOfRound();

    public void processEndOfRound();

    public long[][] getTeamMemory();

    public void resetStatic();

    public void clearAllSignals();

    public boolean wasBreakpointHit();

    public GenericRobot getRobotByID(int id);

    public int nextID();

}
