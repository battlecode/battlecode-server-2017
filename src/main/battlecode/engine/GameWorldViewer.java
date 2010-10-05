package battlecode.engine;

import battlecode.common.*;
import battlecode.engine.signal.Signal;
import battlecode.serial.*;

import java.io.Serializable;
import java.util.Map;

public interface GameWorldViewer {

	public int getCurrentRound();

	public int getMapSeed();

	public Serializable getGameMap();

	public String getTeamName(Team t);

	public Team getWinner();

	public boolean isRunning();

	public Signal[] getAllSignals(boolean includeBytecodesUsedSignal);

	public RoundStats getRoundStats();

	public GameStats getGameStats();
		
}
