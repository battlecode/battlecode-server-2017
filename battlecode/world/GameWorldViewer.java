package battlecode.world;

import battlecode.common.*;
import battlecode.world.signal.Signal;
import battlecode.serial.*;

import java.io.Serializable;
import java.util.Map;

public interface GameWorldViewer {

	public int getCurrentRound();

	public int getMapSeed();

	public GameMap getGameMap();

	public String getTeamName(Team t);

	public Team getWinner();

	public boolean isRunning();

	public Signal[] getAllSignals(boolean includeBytecodesUsedSignal);

	public RoundStats getRoundStats();

	public GameStats getGameStats();
		
}