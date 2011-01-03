package battlecode.server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;

import battlecode.serial.MatchInfo;
import battlecode.server.Config;
import battlecode.server.Server;
import battlecode.server.ServerFactory;
import battlecode.server.State;
import battlecode.util.ScrimmageQueue;

public class Main {

	private static void runHeadless(Config options, String saveFile) {
		try {
			Server server = ServerFactory.createHeadlessServer(options,
															   saveFile);
			server.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void runTCP(Config options, String saveFile) {

		int port = options.getInt("bc.server.port");

		try {
			Server server = ServerFactory.createRemoteServer(options, port,
															 saveFile);
			server.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void runPipe(Config options, String saveFile) {

		try {
			Server server = ServerFactory.createPipeServer(options,
														   saveFile);
			server.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Config setupConfig(String [] args) {
		try {
			Config options = new Config(args);
			Config.setGlobalConfig(options);
			return options;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			System.exit(64);
			return null;
		}
	}

	private static void runScrimmage(Config options) {

		String dsn = options.get("bc.server.scrimmage-dsn");
		String nfs = options.get("bc.server.scrimmage-nfs");
		System.out.println("dsn in runScrimmage: " + dsn + " dir: " + nfs );
		if (dsn == null)
			return;

		ScrimmageQueue queue;
		try {
			queue = ScrimmageQueue.connect(dsn);
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
			return;
		} catch (SQLException e2) {
			e2.printStackTrace();
			return;
		}

		try {
			//poll queue
			try {
				while (queue.size() == 0)
					try {
						Thread.sleep(30000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			} catch (SQLException e1) {
				e1.printStackTrace();
				return;
			}

			boolean requeue = false;
			try {
				Server server = ServerFactory.createSQLServer(options,queue, Server.Mode.MATCH);
				server.run();
				if (server.getState() == State.ERROR)
					requeue = true;
			} catch (IOException e) {
				e.printStackTrace();
				requeue = true;
			}

			if (requeue) {
				try {
					queue.requeue();
					Thread.sleep(30000);
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} finally {
			try {
				queue.close();
				return;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean run(Config options) {
		final Server.Mode mode = Server.Mode.valueOf(options.get("bc.server.mode").toUpperCase());

		String saveFile = options.get("bc.server.save-file");	   
		
		switch (mode) {
		case SCRIMMAGE:
			runScrimmage(options);
			break;
		case HEADLESS:
			runHeadless(options, saveFile);
			break;
		case TCP:
			runTCP(options, saveFile);
			break;
		case PIPE:
			runPipe(options, saveFile);
			break;
		default:
			return false;
		}

		return true;
	}

	public static void main(String[] args) {
		
		final Config options = setupConfig(args);

		if(!run(options)) {
			System.err.println("invalid bc.server.mode");
			System.exit(64);
		}

	}
}
