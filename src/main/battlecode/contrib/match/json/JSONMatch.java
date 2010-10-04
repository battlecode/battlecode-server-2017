package battlecode.contrib.match.json;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import battlecode.common.Team;
import battlecode.common.TerrainTile;
import battlecode.contrib.match.Match;
import battlecode.serial.ExtensibleMetadata;
import battlecode.serial.GameStats;
import battlecode.serial.MatchFooter;
import battlecode.serial.MatchHeader;
import battlecode.serial.MatchInfo;
import battlecode.serial.RoundDelta;
import battlecode.serial.RoundStats;
import battlecode.world.GameMap;
import battlecode.world.signal.Signal;

public class JSONMatch implements Match {
	private ArrayList<GameData> games;
	private JSONArray outputData;
	private JSONSignalHandler signalHandler;
	
	private GameData game;
	
	public JSONMatch() {
		games = new ArrayList<GameData>();
		outputData = new JSONArray();
		signalHandler = new JSONSignalHandler();
	}
	
	public void addExtensibleMetadata(ExtensibleMetadata metadata) {
		try {
			game.visitMetadata(metadata);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void addGameStats(GameStats stats) {
		// unused
	}

	public void addMatchFooter(MatchFooter footer) {
		try {
			game.visitFooter(footer);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void addMatchHeader(MatchHeader header) {
		game = new GameData(games.size());
		games.add(game);
		try {
			game.visitHeader(header);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void addMatchInfo(MatchInfo info) {
		// unused
	}

	public void addRoundDelta(RoundDelta delta) {
		game.visitRound(delta);
	}

	public void addRoundStats(RoundStats stats) {
		try {
			game.visitStats(stats);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void finish() {
		
	}
	
	public byte[] getBytes() {
		for (GameData game : games) {
			outputData.put(game.getOutputData());
		}
		return outputData.toString().getBytes();
	}

	private class GameData {
		private int currentRound = 1;
		private int currentGame;
		
		private JSONObject outputData;
		private JSONArray outputRounds;
		private JSONArray outputStats;
		
		public GameData(int currentGame) {
			this.currentGame = currentGame;
			
			outputData = new JSONObject();
			outputRounds = new JSONArray();
			outputStats = new JSONArray();
			
			try {
				outputData.put("rounds", outputRounds);
				outputData.put("stats", outputStats);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		public JSONObject getOutputData() {
			return outputData;
		}
		
		public void visitHeader(MatchHeader header) throws JSONException {
			currentRound = 1;
			
			GameMap map = header.getMap();
			JSONObject jsonHeader = new JSONObject();
			JSONObject jsonMap = new JSONObject();
			JSONObject jsonMapInfo = new JSONObject();
			
			// construct map info
			jsonMapInfo.put("width", map.getWidth());
			jsonMapInfo.put("height", map.getHeight());
			jsonMapInfo.put("rounds", map.getMaxRounds());
			jsonMapInfo.put("points", map.getMinPoints());
			jsonMapInfo.put("origin", JSONUtils.loc(map.getMapOrigin()));
			
			// construct terrain map
			JSONArray jsonMapTerrain = new JSONArray();
			TerrainTile[][] terrainTiles = map.getTerrainMatrix();
			for (int y = 0; y < map.getHeight(); y++) {
				JSONArray line = new JSONArray();
				for (int x = 0; x < map.getWidth(); x++)
					line.put(JSONUtils.type(terrainTiles[x][y].getType()));
				jsonMapTerrain.put(line);
			}
			
			// construct height map
			JSONArray jsonMapHeight = new JSONArray();
			for (int y = 0; y < map.getHeight(); y++) {
				JSONArray line = new JSONArray();
				for (int x = 0; x < map.getWidth(); x++)
					line.put(Integer.toString(terrainTiles[x][y].getHeight(), 36));
				jsonMapHeight.put(line);
			}
			
			jsonMap.put("info", jsonMapInfo);
			jsonMap.put("terrain", jsonMapTerrain);
			jsonMap.put("height", jsonMapHeight);
			
			jsonHeader.put("map", jsonMap);
			outputData.put("header", jsonHeader);
		}
		
		public void visitRound(RoundDelta round) {
			final Signal[] signals = round.getSignals();
			
			if (currentRound++ % 500 == 0)
				System.out.println("round "+currentRound+"...");
			
			// contruct signals for round
			JSONArray jsonRound = new JSONArray();
			for (int i = 0; i < signals.length; i++) {
				Signal signal = signals[i];
				JSONObject jsonSignal = signal.accept(signalHandler);
				if (jsonSignal != null) {
					jsonRound.put(jsonSignal);
				}
			}
			
			outputRounds.put(jsonRound);
		}
		
		public void visitStats(RoundStats stats) throws JSONException {
			JSONObject jsonStats = new JSONObject();
			jsonStats.put("aPoints", stats.getPoints(Team.A));
			jsonStats.put("bPoints", stats.getPoints(Team.B));
			outputStats.put(jsonStats);
		}
		
		public void visitFooter(MatchFooter footer) throws JSONException {
			JSONObject jsonFooter = new JSONObject();
			jsonFooter.put("winner", footer.getWinner().toString());
			outputData.put("footer", jsonFooter);
		}
		
		public void visitMetadata(ExtensibleMetadata metadata) throws JSONException {
			JSONObject jsonMetadata = new JSONObject();
			jsonMetadata.put("teamA", metadata.get("team-a", null));
			jsonMetadata.put("teamB", metadata.get("team-b", null));
			jsonMetadata.put("map", ((String[])metadata.get("maps", null))[currentGame]);
			outputData.put("metadata", jsonMetadata);
		}
		
	}
	
	/*
	public static void main(String[] args) {
		String matchName = args[0];
		ObjectInputStream input = null;
		System.out.println("Match: " + matchName);
		try {
			input = new ObjectInputStream(new GZIPInputStream(new FileInputStream(matchName)));
		} catch (Exception e) {
			System.err.println("Error: couldn't open match file " + matchName);
			e.printStackTrace();
			return;
		}
		
		JSONMatch output = null;
		try {
			 //output = new JSONMatchWriter(new FileOutputStream("test.xms"));
			 output = new JSONMatch(new DeflaterOutputStream(new FileOutputStream("test.xms")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		Object o;
		try {
			while ((o = input.readObject()) != null) {
				if (o instanceof MatchHeader) {
					output.write((MatchHeader)o);
				} else if (o instanceof MatchInfo) {
					output.write((MatchInfo)o);
				} else if (o instanceof MatchFooter) {
					output.write((MatchFooter)o);
				} else if (o instanceof RoundDelta) {
					output.write((RoundDelta)o);
				} else if (o instanceof RoundStats) {
					output.write((RoundStats)o);
				} else if (o instanceof GameStats) {
					output.write((GameStats)o);
				} else if (o instanceof ExtensibleMetadata) {
					output.write((ExtensibleMetadata)o);
				}
			}
		} catch (EOFException e) {
			// finished
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		output.flush();
		output.close();
	}
	*/

}
