package battlecode.world.signal;

import battlecode.common.Team;
import battlecode.engine.signal.Signal;


/**
 * Signifies a new quantity of resources (parts) for a team.
 */
public class TeamResourceSignal implements Signal {

    /**
     * The team
     */
    public final Team team;

    /**
     * The team's new resource level
     */
    public final double resource;

    public TeamResourceSignal(Team team, double resource) {
        this.team = team;
        this.resource = resource;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private TeamResourceSignal() {
        this(null, 0);
    }
}
