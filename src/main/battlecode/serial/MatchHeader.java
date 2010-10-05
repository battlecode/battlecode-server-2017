package battlecode.serial;

import java.io.*;

public class MatchHeader implements Serializable {

	private static final long serialVersionUID = -4228142649046586925L; 
	private final Serializable map;
	private final long[][] state;
	private final int matchNumber, matchCount;
	
	public MatchHeader(Serializable map, long[][] state, int matchNumber, int matchCount) {
		this.map = map;
		this.state = state;
		this.matchNumber = matchNumber;
		this.matchCount = matchCount;
	}
	
	public int getMatchCount() {
		return matchCount;
	}

	public int getMatchNumber() {
		return matchNumber;
	}

	public Serializable getMap() {
		return map;
	}
	
	public long[][] getState() {
		return state;
	}
}
