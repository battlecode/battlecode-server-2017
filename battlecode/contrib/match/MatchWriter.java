package battlecode.contrib.match;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import battlecode.serial.MatchFooter;
import battlecode.serial.MatchHeader;
import battlecode.serial.RoundDelta;
import battlecode.serial.RoundStats;

public abstract class MatchWriter extends Writer {

	protected OutputStream stream;
	
	public MatchWriter(OutputStream stream) {
		super();
		this.stream = stream;
	}
	
	public MatchWriter(OutputStream stream, Object lock) {
		super(lock);
		this.stream = stream;
	}
	
	public abstract void writeHeader(MatchHeader header) throws IOException;
	public abstract void writeRound(RoundDelta round) throws IOException;
	public abstract void writeStats(RoundStats stats) throws IOException;
	public abstract void writeObject(Object o) throws IOException;
	public abstract void writeFooter(MatchFooter footer) throws IOException;
	public abstract void finish() throws IOException;
	
	@Override
	public void close() throws IOException {
		stream.close();
	}

	@Override
	public void flush() throws IOException {
		stream.flush();
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		for (int i = off; i < len; i++)
			stream.write((byte)cbuf[i]);
	}
	
	@Override
	public void write(String str) throws IOException {
		stream.write(str.getBytes());
	}

}
