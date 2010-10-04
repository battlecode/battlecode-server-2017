package battlecode.server.proxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import battlecode.contrib.match.xml.XMLMatch;
import battlecode.serial.ExtensibleMetadata;
import battlecode.serial.MatchFooter;
import battlecode.serial.MatchHeader;
import battlecode.serial.RoundDelta;
import battlecode.serial.RoundStats;

public class MatchProxy extends Proxy {

	protected OutputStream stream;
	protected File file;
	protected boolean compressed;
	protected String fileName;
	
	protected XMLMatch match;
	
	public MatchProxy(String fileName) throws IOException {
		this(fileName, true);
	}
	
	public MatchProxy(String fileName, boolean compressed) throws IOException {
		super();
		
		this.fileName = fileName;
		this.compressed = compressed;
		
		// Create directories if necessary.
		file = new File(fileName);
		if (!file.exists() && file.getParentFile() != null)
			file.getParentFile().mkdirs();
		
		match = new XMLMatch();
	}

	protected OutputStream getOutputStream() throws IOException {
		return null;
	}
	
	@Override
	public void writeObject(Object o) throws IOException {
		if (o instanceof ExtensibleMetadata) {
			match.addExtensibleMetadata((ExtensibleMetadata)o);
		}
	}
	
	@Override
	public void writeHeader(MatchHeader header) throws IOException {
		match.addMatchHeader(header);
	}

	@Override
	public void writeRound(RoundDelta round) throws IOException {
		match.addRoundDelta(round);
	}
	
	@Override
	public void writeFooter(MatchFooter footer) throws IOException {
		match.addMatchFooter(footer);
	}
	
	@Override
	public void writeStats(RoundStats stats) throws IOException {
		match.addRoundStats(stats);
	}
	
	public void close() throws IOException {
		match.finish();
		
		if (compressed) {
			stream = new GZIPOutputStream(new FileOutputStream(fileName));
		} else {
			stream = new FileOutputStream(fileName);
		}
		
		List<String> lines = match.getLines();
		for (String line : lines) {
			stream.write(line.getBytes());
		}
		
		stream.flush();
		stream.close();
	}

}
