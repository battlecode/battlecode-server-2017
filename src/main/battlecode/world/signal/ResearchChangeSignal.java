package battlecode.world.signal;

import battlecode.common.Team;
import battlecode.common.Upgrade;
import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;
import java.util.Map;

public class ResearchChangeSignal extends Signal {
	private static final long serialVersionUID = -142841533234435L;

	/**
	 * The current research levels.
	 */
	public final double[][] progress;

    /**
     * Creates a signal for updating research
     */
    public ResearchChangeSignal(Map<Team, Map<Upgrade, Integer>> rmap) {
		progress = new double[2][5];

		for (int t = 0; t < progress.length; t++) {
			Team team = Team.values()[t];
			for (int r = 0; r < progress[t].length; r++) {
				Upgrade res = Upgrade.values()[r];
				if (rmap.get(team) != null && rmap.get(team).get(res) != null)
					progress[t][r] = 1.0 * rmap.get(team).get(res) / res.numRounds;
				else
					progress[t][r] = 0.0;
			}
		}
    }
}
