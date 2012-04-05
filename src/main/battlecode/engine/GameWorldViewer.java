package battlecode.engine;

import battlecode.common.Team;
import battlecode.engine.signal.Signal;
import battlecode.serial.GameStats;
import battlecode.serial.GenericGameMap;
import battlecode.serial.RoundStats;

public interface GameWorldViewer {

    public int getCurrentRound();

    public int getMapSeed();

    public GenericGameMap getGameMap();

    public String getTeamName(Team t);

    public Team getWinner();

    public boolean isRunning();

    public Signal[] getAllSignals(boolean includeBytecodesUsedSignal);

    public RoundStats getRoundStats();

    public GameStats getGameStats();

}
