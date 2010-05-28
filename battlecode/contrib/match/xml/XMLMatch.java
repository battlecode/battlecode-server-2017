package battlecode.contrib.match.xml;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang.StringUtils;

import battlecode.common.MapLocation;
import battlecode.common.Team;
import battlecode.common.TerrainTile;
import battlecode.common.TerrainTile.TerrainType;
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

public class XMLMatch implements Match {
	private XMLSignalHandler signalHandler;
	private LinkedList<String> outputData;
	private ArrayList<GameData> games;
	
	private GameData game;
	
	public XMLMatch() {
		signalHandler = new XMLSignalHandler();
		outputData = new LinkedList<String>();
		games = new ArrayList<GameData>();
		
		outputData.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		outputData.add("<match>\n");
	}

	public void addExtensibleMetadata(ExtensibleMetadata metadata) {
		game.visitMetadata(metadata);
	}

	public void addMatchFooter(MatchFooter footer) {
		game.visitFooter(footer);
	}

	public void addMatchHeader(MatchHeader header) {
		game = new GameData(games.size());
		games.add(game);
		game.visitHeader(header);
	}

	public void addRoundDelta(RoundDelta delta) {
		game.visitRound(delta);
	}

	public void addRoundStats(RoundStats stats) {
		game.visitStats(stats);
	}
	
	// unused
	public void addGameStats(GameStats stats) { }
	public void addMatchInfo(MatchInfo info) { }
	
	public void finish() {
		outputData.add("</match>");
	}

	public List<String> getLines() {
		return outputData;
	}
	
	private class GameData {
		private String tabs = "\t";
		private int currentRound = 1;
		private int currentGame;
		
		public GameData(int currentGame) {
		    this.currentGame = currentGame;
		}
		
		private void increaseTab() {
			tabs += "\t";
		}
		
		private void decreaseTab() {
			if (tabs.length() > 0)
				tabs = tabs.substring(0, tabs.length()-1);
		}
		
		private String line(String str) {
			return tabs + str + "\n";
		}
		
		private String type(TerrainType type) {
			return type == TerrainType.LAND ? " " : "#";
		}
		
		public void visitHeader(MatchHeader header) {
			StringBuilder headerStr = new StringBuilder();
			currentRound = 1;
			GameMap map = header.getMap();
			
			System.out.println("generating match header...");
			
			headerStr.append("<game num='"+header.getMatchNumber()+"'>\n");
			headerStr.append(line("<map>"));
			increaseTab();
			
			MapLocation origin = map.getMapOrigin();
			
			headerStr.append(line("<info width='"+map.getWidth()+"' height='"+map.getHeight()+"' " +
									"origin='"+origin.getX()+","+origin.getY()+"' " +
									"rounds='"+map.getMaxRounds()+"' points='"+map.getMinPoints()+"' />"));
			headerStr.append(line("<terrain>"));
			increaseTab();
			headerStr.append(line("<![CDATA["));
			increaseTab();
			
			TerrainTile[][] terrainTiles = map.getTerrainMatrix();
			for (int y = 0; y < map.getHeight(); y++) {
				String lineString = "";
				for (int x = 0; x < map.getWidth(); x++)
					lineString += type(terrainTiles[x][y].getType());
				headerStr.append(line(lineString));
			}
			
			decreaseTab();
			headerStr.append(line("]]>"));
			decreaseTab();
			headerStr.append(line("</terrain>"));
			
			headerStr.append(line("<height>"));
			increaseTab();
			headerStr.append(line("<![CDATA["));
			increaseTab();
			
			for (int y = 0; y < map.getHeight(); y++) {
				String lineString = "";
				for (int x = 0; x < map.getWidth(); x++)
					lineString += Integer.toString(terrainTiles[x][y].getHeight(), 36);
				headerStr.append(line(lineString));
			}
			
			decreaseTab();
			headerStr.append(line("]]>"));
			decreaseTab();
			headerStr.append(line("</height>"));
			
			decreaseTab();
			headerStr.append(line("</map>"));
			
			outputData.add(headerStr.toString());
		}
		
		public void visitFooter(MatchFooter footer) {
			outputData.add(line("<footer winner='"+footer.getWinner()+"' />"));
			outputData.add("</game>\n");
		}
		
		public void visitMetadata(ExtensibleMetadata metadata) {
			outputData.add(line("<metadata teamA='"+metadata.get("team-a", null)+"' " +
							"teamB='"+metadata.get("team-b", null)+"' " +
							"map='"+((String[])metadata.get("maps", null))[currentGame]+"' />"));
		}
		
		public void visitStats(RoundStats stats) {
			outputData.add(line("<stats aPoints='"+(int)stats.getPoints(Team.A)+"' " +
							"bPoints='"+(int)stats.getPoints(Team.B)+" '/>"));
		}

		public void visitRound(RoundDelta round) {
			final Signal[] signals = round.getSignals();
			
			if (currentRound % 500 == 0)
				System.out.println("round "+currentRound+"...");
			currentRound++;
			
			outputData.add(line("<round>"));
			increaseTab();
			
			for (int i = 0; i < signals.length; i++) {
				Signal signal = signals[i];
				String signalLine = signal.accept(signalHandler);
				if (signalLine != null) {
					outputData.add(line(signalLine));
				}
			}
			
			decreaseTab();
			outputData.add(line("</round>"));
		}

	}
	
}
