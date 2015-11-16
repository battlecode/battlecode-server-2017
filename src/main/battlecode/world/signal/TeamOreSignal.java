package battlecode.world.signal;

import java.util.Arrays;

import battlecode.common.Team;
import battlecode.engine.signal.Signal;
import com.fasterxml.jackson.annotation.JsonCreator;


/**
 * Signifies a new quantity of ore for a team.
 */
public class TeamOreSignal extends Signal {

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
