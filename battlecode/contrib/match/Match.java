package battlecode.contrib.match;

import java.util.List;

import battlecode.serial.ExtensibleMetadata;
import battlecode.serial.GameStats;
import battlecode.serial.MatchFooter;
import battlecode.serial.MatchHeader;
import battlecode.serial.MatchInfo;
import battlecode.serial.RoundDelta;
import battlecode.serial.RoundStats;

public interface Match {

	public void addMatchHeader(MatchHeader header);
	
	public void addMatchInfo(MatchInfo info);
	
	public void addMatchFooter(MatchFooter footer);
	
	public void addGameStats(GameStats stats);
	
	public void addRoundStats(RoundStats stats);
	
	public void addRoundDelta(RoundDelta delta);
	
	public void addExtensibleMetadata(ExtensibleMetadata metadata);
	
	public void finish();
	
}
