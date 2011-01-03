package battlecode.util;

import java.sql.SQLException;
import java.util.Map;

import battlecode.common.Team;
import battlecode.serial.MatchInfo;

public interface SQLQueue {
	public MatchInfo dequeue() throws SQLException;

	public String complete(Team winner) throws SQLException;

	public int size() throws SQLException;

	public void addStats(int game, Map<String, ? extends Number> stats)
			throws SQLException;
	
	public void finish() throws SQLException;
}
