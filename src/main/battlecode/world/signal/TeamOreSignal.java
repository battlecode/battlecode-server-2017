package battlecode.world.signal;

import battlecode.common.Team;
import battlecode.engine.signal.Signal;


/**
 * Signifies a new quantity of ore for a team.
 */
public class TeamOreSignal implements Signal {

    /**
     * The team
     */
    public final Team team;

    /**
     * The team's new ore level
     */
    public final double ore;

    public TeamOreSignal(Team team, double ore) {
        this.team = team;
        this.ore = ore;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private TeamOreSignal() {
        this(null, 0);
    }
}
