package battlecode.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import battlecode.common.Team;
import battlecode.serial.MatchInfo;
import battlecode.util.*;
public class ScrimmageQueue implements SQLQueue {
	
	/** The number of ranked matches a team must compete in before they are non provisonal */
	private final int PROVISIONAL_MATCHES = 4;
	
	private final Connection conn;

	private Integer activeRow = null;

	private int fromID = 0, toID = 0;

	private float fromScore = 0.0f, toScore = 0.0f;
	
	private boolean fromProvisional = false, toProvisional = false;
	
	private int fromWins = 0, toWins = 0;
	private int fromLosses = 0, toLosses = 0;
	
	private String fromTeam = null;

	private String teamA = null, teamB = null;
	private int teamAID = 0, teamBID = 0;
	private String mapLabel = null;
	private String rankedLabel = null;
	private float teamAScore = 0.0f, teamBScore = 0.0f;
	private float teamADelta = 0.0f, teamBDelta = 0.0f;
	private int teamAWins = 0, teamALosses = 0;
	private int teamBWins = 0, teamBLosses = 0;
	
	private String winner = null; // "a" or "b"
	private int winner_id;
	private int loser_id;
	private boolean ranked = false;
	
	private boolean hidden = false;
	private String[] maps = null;
	
	private ScrimmageQueue(Connection conn) {
		this.conn = conn;
	}

	public Integer getActiveRow() {
		return this.activeRow;
	}

	public static ScrimmageQueue connect(String dsn)
			throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection conn = DriverManager.getConnection(dsn);
		return new ScrimmageQueue(conn);
	}

	private String getTeamPackage(int id) throws SQLException {
		
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(String.format(
				"select id from contestants_team where id = %d", id));
		rs.next();
		String result = String.format("team%03d", rs.getInt("id"));
		stmt.close();
		return result;
	}

	private float getTeamScore(int id) throws SQLException {
		if(id <= 0) return 0;
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(String.format(
				"select rating from contestants_team where id = %d", id));
		rs.next();
		float result = rs.getFloat("rating");
		stmt.close();
		return result;
	}
	
	private int getTeamWins(int id) throws SQLException {
		if(id <= 0) return 0;
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(String.format(
				"select ranked_wins from contestants_team where id = %d", id));
		
		rs.next();
		
		int wins = rs.getInt("ranked_wins");
		stmt.close();
		return wins;
	}
	
	private int getTeamLosses(int id) throws SQLException {
		if(id <= 0) return 0;
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(String.format(
				"select ranked_losses from contestants_team where id = %d", id));
		rs.next();
		int losses = rs.getInt("ranked_losses");
		stmt.close();
		return losses;
	}
	
	private String[] getTeamEmails(int id) throws SQLException {
		
		Statement stmt = conn.createStatement();
		
		ResultSet users = stmt.executeQuery(String.format(
				"select a.email as email from auth_user a, contestants_invitation c where a.id = c.user_id and c.team_id = %d", id));

		List<String> emails = new LinkedList<String>();
		while (users.next())
			emails.add(users.getString("email"));
		
		if (emails.size() == 0)
			throw new IllegalArgumentException("no users");
		
		stmt.close();
		
		return emails.toArray(new String[emails.size()]);
	}
	
	private boolean getEmailFlag(int id) throws SQLException {
		return false;
//		Statement stmt = conn.createStatement();
//		
//		ResultSet rs = stmt.executeQuery(String.format(
//				"select send_scrimmage_emails from teams where id = %d", id));
//
//		rs.next();
//		
//		boolean sendEmails = rs.getBoolean("send_scrimmage_emails");
//		
//		stmt.close();
//		
//		return sendEmails;
	}

	public void close() throws SQLException {
		conn.close();
	}

	public int size() throws SQLException {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt
				.executeQuery("select count(*) as count from scrimmage_scrimmage"
						+ " where status = 'queued' order by sent_time asc");

		if (!rs.next()) {
			stmt.close();
			return 0;
		}

		int result = rs.getInt("count");
		stmt.close();
		return result;
	}

	public MatchInfo dequeue() throws SQLException {
		if (activeRow != null)
			throw new IllegalStateException("the queue is currently active");

		Statement stmt = conn.createStatement();
		// Lock the scrimmage table.
		stmt.execute("lock tables scrimmage_scrimmage write, contestants_team write");
	
		// Fetch the queue.
		ResultSet rs = stmt
				.executeQuery("select scrimmage_scrimmage.id as id, scrimmage_scrimmage.from_id_id as `from`, scrimmage_scrimmage.to_id_id as `to`, scrimmage_scrimmage.maps as maps, contestants_team.name as from_team, scrimmage_scrimmage.hidden as hidden,scrimmage_scrimmage.ranked as ranked from scrimmage_scrimmage, contestants_team"
						+ " where status = 'queued' AND scrimmage_scrimmage.from_id_id=contestants_team.id order by sent_time asc");
		
		// If there's nothing to dequeue, unlock and return null.
		if (!rs.next()) {
			stmt.execute("unlock tables");
			return null;
		}
		activeRow = rs.getInt("id");
		fromID = rs.getInt("from");
		toID = rs.getInt("to");
		fromTeam = rs.getString("from_team");
		
		mapLabel = rs.getString("maps");
		maps = mapLabel.split(",");
		fromScore = getTeamScore(fromID);
		toScore = getTeamScore(toID);
		
		if (fromTeam.equals("a")) {
			teamA = getTeamPackage(fromID);
			teamB = getTeamPackage(toID);
			teamAScore = fromScore;
			teamBScore = toScore;
			teamAID = fromID;
			teamBID = toID;
		} else {
			teamB = getTeamPackage(fromID);
			teamA = getTeamPackage(toID);
			teamBScore = fromScore;
			teamAScore = toScore;
			teamAID = toID;
			teamBID = fromID;
		}
		ranked = rs.getInt("ranked")==1;
		rankedLabel = ranked ? "ranked" : "unranked";
		hidden = rs.getBoolean("hidden");

		System.err.println("+ " + teamA + " vs. " + teamB + " on " + mapLabel + " -- " + rankedLabel);
		
		fromProvisional = fromWins + fromLosses >= PROVISIONAL_MATCHES;
		toProvisional = toWins + toLosses >= PROVISIONAL_MATCHES;

		stmt.execute(String.format(
				"update scrimmage_scrimmage set status = 'running' where id = %d",
				activeRow));
		stmt.execute("unlock tables");
		stmt.close();
		return new MatchInfo(teamA, teamB, maps);
	}
	
	private float getMultiplier(float winnerScore, float loserScore, boolean winnerProvisional, boolean loserProvisional) {
		// use non-provisional team if winner is provisional
		if (winnerProvisional && !loserProvisional)
			winnerScore = loserScore;
		
		if (winnerScore > 1600) // good teams
			return 24;
		if (winnerScore >= 1400) // medium teams
			return 36;
		return 48; // bad teams
	}

	/**
	 * Run once per game -- so three times in a best of 3 match.
	 * 
	 * Each time this is called, winner is set to be the winner of
	 * the most recent match, which results in the last value being
	 * the overall winner of the match.
	 */
	public String complete(Team winner) throws SQLException {
		this.winner = winner.toString().toLowerCase();
		
		String file = System.getProperty("bc.server.scrimmage-matches")
				+ "/" + activeRow + ".rms";
		
		// This breaks best of three matches so I'm turning it off.
		// The server tears itself down after each match so it shouldn't cause any problems.
		//this.activeRow = null;
		
		return file;
	}

	public void requeue() throws SQLException {

		this.activeRow = null;

		Statement stmt = conn.createStatement();
		stmt.execute(String.format(
				"update scrimmage_scrimmage set status = 'cancelled' where id = %d",
				this.activeRow));
		stmt.close();
	}

	public void addStats(int game, Map<String, ? extends Number> stats)
			throws SQLException {
		// Do nothing (for now).
	}
	
	/**
	 * Called once upon completion of the match.
	 */
	public void finish() throws SQLException {
		System.out.println("--------------- BEGIN SCRIMMAGE/MATCH QUEUE FINISH ---------------");
		
		String completedAt = new java.sql.Timestamp(System.currentTimeMillis()).toString();
		
		// lock tables
		Statement stmt = conn.createStatement();
		stmt.execute("lock tables scrimmage_scrimmage write, contestants_team write");

		// get old number of wins and losses for each team
		fromWins = getTeamWins(fromID); fromLosses = getTeamLosses(fromID);
		toWins = getTeamWins(toID); toLosses = getTeamLosses(toID);
		
		// update wins/losses and check provisional state
		float winnerScore, loserScore;
		boolean winnerProvisional, loserProvisional;
		if (winner.equals(fromTeam)) {
			winner_id = fromID;
			loser_id = toID;
			winnerScore = fromScore;
			loserScore = toScore;
			winnerProvisional = fromProvisional;
			loserProvisional = toProvisional;
			fromWins++;
			toLosses++;
		} else {
			winner_id = toID;
			loser_id = fromID;
			winnerScore = toScore;
			loserScore = fromScore;
			winnerProvisional = toProvisional;
			loserProvisional = fromProvisional;
			toWins++;
			fromLosses++;
		}
		
		final float MULTIPLIER = getMultiplier(winnerScore, loserScore, winnerProvisional, loserProvisional);
		final float PROBABILITY_FACTOR = 200.0f;
		
		// compute new rankings
		float toExpected = 1.0f / (1.0f + (float) Math.pow(10.0,
				(fromScore - toScore) / PROBABILITY_FACTOR));
		float fromExpected = 1.0f / (1.0f + (float) Math.pow(10.0,
				(toScore - fromScore) / PROBABILITY_FACTOR));

		float fromDelta, toDelta;
		if (winner.equals(fromTeam)) {
			fromDelta = MULTIPLIER * (1.0f - fromExpected);
			toDelta = MULTIPLIER * (0.0f - toExpected);
		} else {
			fromDelta = MULTIPLIER * (0.0f - fromExpected);
			toDelta = MULTIPLIER * (1.0f - toExpected);
		}
		
		// round deltas to integer values
		toDelta = Math.round(toDelta);
		fromDelta = Math.round(fromDelta);
		
		// determine delta for each team
		if (fromTeam.equals("a")) {
			teamADelta = fromDelta;
			teamBDelta = toDelta;
			teamAWins = fromWins;
			teamBWins = toWins;
			teamALosses = fromLosses;
			teamBLosses = toLosses;
		} else {
			teamBDelta = fromDelta;
			teamADelta = toDelta;
			teamAWins = toWins;
			teamBWins = fromWins;
			teamALosses = toLosses;
			teamBLosses = fromLosses;
		}
		
		/*
		ResultSet rs = stmt.executeQuery(String.format(
				"select ranked, hidden from matches where id = %d", this.activeRow));

		// if scrim is missing from database for some reason
		if (!rs.next())
			return;
		*/
		
		System.out.println(" -- ABOUT TO UPDATE TABLES");
		System.out.println(" -- FROM DELTA: "+fromDelta);
		System.out.println(" -- TO DELTA: "+toDelta);
		System.out.println(" -- WINNER: "+winner);
		System.out.println();
		
		if (ranked && !hidden) {
			stmt.execute(String.format(
					"update contestants_team set rating = %g, ranked_wins = %d, ranked_losses = %d where id = %d",
					fromScore + fromDelta, fromWins, fromLosses, fromID));
			stmt.execute(String.format(
					"update contestants_team set rating = %g, ranked_wins = %d, ranked_losses = %d where id = %d",
					toScore + toDelta, toWins, toLosses, toID));
		}
		
		if (!hidden) {
			// mark team as having scrimmed
			stmt.execute(String.format("update contestants_team set has_scrimmed = true where id = %d", fromID));
			stmt.execute(String.format("update contestants_team set has_scrimmed = true where id = %d", toID));
		}
		
		String sql = String.format("update scrimmage_scrimmage set "
				+ "status = 'completed', winner_id = '%s', loser_id = '%s'"
				+ " where id = %d", winner_id, loser_id, this.activeRow);
		stmt.execute(sql);
		
		// if they beat refplayer and it has >=2 games in it give them credit
		int refplayerid = 1; // TODO: update each year with Teh Devs team id (2010)
		
		// check for win against refplayer
		boolean acceptableMaps = Arrays.equals(maps, new String[] {"dark_magician","adobe","garage"})
				|| Arrays.equals(maps, new String[] {"rome","rainyDay","skyscrapers_of_poland"})
				|| Arrays.equals(maps, new String[] {"quadrants","aztec","venice"});
		if (toID == refplayerid && winner.equals(fromTeam) && acceptableMaps) {
			String sql2 = String.format("update contestants_team set scrimflags=scrimflags|0x40 where id=%d", fromID);
			stmt.execute(sql2);
		}
		
		stmt.execute("unlock tables");
		
		stmt.close();
		
		StringBuilder body = new StringBuilder();
		body.append("The following scrimmage match has been successfully completed and is available for download.\r\n\r\n");
		body.append("Teams: "+teamA+" (A) vs. "+teamB+" (B)\r\n");
		body.append("Map(s): "+mapLabel+"\r\n");
		body.append("Winner: "+(winner.equals("a") ? teamA : teamB)+"\r\n");
		body.append("Ranked: "+(ranked ? "yes" : "no")+"\r\n");
		body.append("\r\n");
		if (ranked) {
			String prefixA = (teamADelta >= 0) ? "+" : "";
			String prefixB = (teamBDelta >= 0) ? "+" : "";
			body.append("Updated rating for "+teamA+": "+(int)(teamAScore+teamADelta)+" ("+prefixA+(int)teamADelta+")\r\n");
			body.append("Updated rating for "+teamB+": "+(int)(teamBScore+teamBDelta)+" ("+prefixB+(int)teamBDelta+")\r\n");
			body.append("Updated record for "+teamA+": "+teamAWins+"W / "+teamALosses+"L\r\n");
			body.append("Updated record for "+teamB+": "+teamBWins+"W / "+teamBLosses+"L\r\n");
			body.append("\r\n");
		}
		
		if (hidden) {
			body.append("** This is a hidden match -- the results will be discarded **\r\n");
			body.append("\r\n");
		}
		
		body.append("Download match: http://battlecode.mit.edu/2010/scrimmage/download/"+this.activeRow+".rms\r\n");
		body.append("Rankings page: http://battlecode.mit.edu/2010/scrimmage/rank\r\n");
		body.append("Matches page: http://battlecode.mit.edu/2010/scrimmage/matches\r\n\r\n");
		
		body.append("-- Watch this match online! --\r\n");
		body.append("Flash client: http://battlecode.mit.edu/2010/watch/"+this.activeRow+"/\r\n");
		body.append("Java applet: http://battlecode.mit.edu/2010/scrimmage/viewmatch/"+this.activeRow+"\r\n");
		
		String subject = "Battlecode scrimmage results: "+teamA+" vs. "+teamB+" on "+mapLabel;
		
		System.out.println(body.toString());
		
		if (getEmailFlag(teamAID)&&!hidden) {
			String[] emails = getTeamEmails(teamAID);
			try {
				sendMail(emails, subject, body.toString());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if (getEmailFlag(teamBID)&&!hidden) {
			String[] emails = getTeamEmails(teamBID);
			try {
				sendMail(emails, subject, body.toString());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("--------------- END SCRIMMAGE/MATCH QUEUE FINISH ---------------");
	}
	
	private int sendMail(String[] recipients, String subject, String body) throws IOException, InterruptedException {
		if (recipients.length == 0)
			throw new IllegalArgumentException("no recipients");
		
		StringBuilder sb = new StringBuilder();
		for (String s : recipients) {
			if (sb.length() > 0)
				sb.append(", ");
			sb.append(s);
		}
		
		System.out.println("Sending emails to: "+sb.toString());
		
		Process sendmail = Runtime.getRuntime().exec("sendmail -t");
		OutputStreamWriter out = new OutputStreamWriter(sendmail.getOutputStream());
		BufferedWriter writer = new BufferedWriter(out);
		
		writer.write("To: " + sb.toString() + "\r\n");
		writer.write("From: noreply@battlecode.mit.edu\r\n");
		writer.write("Subject: " + subject + "\r\n\r\n");
		writer.write(body);
		writer.write("\r\n.\r\n");
		
		writer.close();
		return sendmail.waitFor();
	}
}
