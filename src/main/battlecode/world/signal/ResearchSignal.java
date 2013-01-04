package battlecode.world.signal;

import battlecode.common.Team;
import battlecode.common.Upgrade;
import battlecode.engine.signal.Signal;
import battlecode.world.InternalRobot;

public class ResearchSignal extends Signal {
	private static final long serialVersionUID = -142841534234435L;

    /**
     * Robot doing research
     */
    private final int robotID;

    /**
     * Upgrade being researched
     */
    private final Upgrade upgrade;
    
    private final Team team;


    /**
     * Creates a signal for starting a research
     */
    public ResearchSignal(InternalRobot r, Upgrade u) {
        robotID = r.getID();
        upgrade = u;
        team = r.getTeam();
    }

    public int getRobotID() {
        return robotID;
    }
    public Team getTeam() {
        return team;
    }

    public Upgrade getUpgrade() {
        return upgrade;
    }
}
