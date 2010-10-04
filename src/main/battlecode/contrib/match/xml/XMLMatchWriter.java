package battlecode.contrib.match.xml;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import battlecode.common.MapLocation;
import battlecode.common.Team;
import battlecode.common.TerrainTile;
import battlecode.common.TerrainTile.TerrainType;
import battlecode.contrib.match.MatchWriter;
import battlecode.serial.ExtensibleMetadata;
import battlecode.serial.MatchFooter;
import battlecode.serial.MatchHeader;
import battlecode.serial.RoundDelta;
import battlecode.serial.RoundStats;
import battlecode.world.GameMap;
import battlecode.world.signal.Signal;
import battlecode.world.signal.SignalHandler;

public class XMLMatchWriter extends MatchWriter {
	
	protected SignalHandler<String> signalHandler = new XMLSignalHandler();
	
	private String tabs = "\t";
	private int currentRound = 1;
	private int currentGame = 0;
	
	public XMLMatchWriter(OutputStream stream) throws IOException {
		super(stream);
		write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		write("<match>\n");
	}

	@Override
	public void writeHeader(MatchHeader header) throws IOException {
		currentRound = 1;
		GameMap map = header.getMap();
		
		write("<game num='"+header.getMatchNumber()+"'>\n");
		write(line("<map>"));
		increaseTab();
		
		MapLocation origin = map.getMapOrigin();
		
		write(line("<info width='"+map.getWidth()+"' height='"+map.getHeight()+"' " +
								"origin='"+origin.getX()+","+origin.getY()+"' " +
								"rounds='"+map.getMaxRounds()+"' points='"+map.getMinPoints()+"' />"));
		write(line("<terrain>"));
		increaseTab();
		write(line("<![CDATA["));
		increaseTab();
		
		TerrainTile[][] terrainTiles = map.getTerrainMatrix();
		for (int y = 0; y < map.getHeight(); y++) {
			String lineString = "";
			for (int x = 0; x < map.getWidth(); x++)
				lineString += type(terrainTiles[x][y].getType());
			write(line(lineString));
		}
		
		decreaseTab();
		write(line("]]>"));
		decreaseTab();
		write(line("</terrain>"));
		
		write(line("<height>"));
		increaseTab();
		write(line("<![CDATA["));
		increaseTab();
		
		for (int y = 0; y < map.getHeight(); y++) {
			String lineString = "";
			for (int x = 0; x < map.getWidth(); x++)
				lineString += Integer.toString(terrainTiles[x][y].getHeight(), 36);
			write(line(lineString));
		}
		
		decreaseTab();
		write(line("]]>"));
		decreaseTab();
		write(line("</height>"));
		
		decreaseTab();
		write(line("</map>"));
	}

	@Override
	public void writeRound(RoundDelta round) throws IOException {
		final Signal[] signals = round.getSignals();
		
		currentRound++;
		
		write(line("<round>"));
		increaseTab();
		
		for (int i = 0; i < signals.length; i++) {
			Signal signal = signals[i];
			String signalLine = signal.accept(signalHandler);
			if (signalLine != null) {
				write(line(signalLine));
			}
		}
		
		decreaseTab();
		write(line("</round>"));
		
		flush();
	}

	@Override
	public void writeStats(RoundStats stats) throws IOException {
		write(line("<stats aPoints='"+(int)stats.getPoints(Team.A)+"' " +
			"bPoints='"+(int)stats.getPoints(Team.B)+" '/>"));
	}
	
	@Override
	public void writeObject(Object o) throws IOException {
		if (o instanceof ExtensibleMetadata)
			writeMetadata((ExtensibleMetadata)o);
	}
	
	public void writeMetadata(ExtensibleMetadata metadata) throws IOException {
		write(line("<metadata teamA='"+metadata.get("team-a", null)+"' " +
			"teamB='"+metadata.get("team-b", null)+"' " +
			"map='"+((String[])metadata.get("maps", null))[currentGame]+"' />"));
	}
	
	@Override
	public void writeFooter(MatchFooter footer) throws IOException {
		write(line("<footer winner='"+footer.getWinner()+"' />"));
		write("</game>\n");
		currentGame++;
	}
	
	@Override
	public void finish() throws IOException {
		write("</match>");
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
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
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
		
		XMLMatchWriter matchWriter = new XMLMatchWriter(new GZIPOutputStream(new FileOutputStream("matches/test.xms")));
		
		Object o;
		try {
			while ((o = input.readObject()) != null) {
				if (o instanceof MatchHeader) {
					matchWriter.writeHeader((MatchHeader)o);
				} else if (o instanceof MatchFooter) {
					matchWriter.writeFooter((MatchFooter)o);
				} else if (o instanceof RoundDelta) {
					matchWriter.writeRound((RoundDelta)o);
				} else if (o instanceof RoundStats) {
					matchWriter.writeStats((RoundStats)o);
				} else {
					matchWriter.writeObject(o);
				}
			}
		} catch (EOFException e) {
			matchWriter.finish();
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
		
		matchWriter.flush();
		matchWriter.close();
	}

}
