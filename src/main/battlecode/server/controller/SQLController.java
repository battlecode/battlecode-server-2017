package battlecode.server.controller;

import java.io.IOException;
import java.sql.*;

import battlecode.serial.MatchInfo;
import battlecode.util.*;

public class SQLController extends Controller {

	private final SQLQueue queue;
	
	SQLController(SQLQueue queue) {
		this.queue = queue;
	}

	public void start() throws IOException {
		
		MatchInfo info;
		try {
			info = queue.dequeue();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		
		this.setChanged();
		this.notifyObservers(info);
		this.clearChanged();
	}

	public void finish() throws IOException {
		// send emails etc
		try {
			queue.finish();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// Do nothing.
	}
}
