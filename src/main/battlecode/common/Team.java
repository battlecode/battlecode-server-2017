package battlecode.common;

/**
 * This enum represents the team of a robot. A robot is on exactly one team.
 * Player robots are on either team A or team B. Zombie robots are on team
 * ZOMBIE.
 * <p>
 * Since Team is a Java 1.5 enum, you can use it in <code>switch</code>
 * statements, it has all the standard enum methods (<code>valueOf</code>,
 * <code>values</code>, etc.), and you can safely use <code>==</code> for
 * equality tests.
 */
public enum Team {
    /**
     * Team A.
     */
    A,
    /**
     * Team B.
     */
    B,
    /**
     * Neutral robots.
     */
    NEUTRAL,
    /**
     * Zombie robot.
     */
    ZOMBIE;

    /**
     * Determines the team that is the opponent of this team.
     *
     * @return the opponent of this team, or ZOMBIE if this team is ZOMBIE.
     */
    public Team opponent() {
        switch (this) {
            case A:
                return B;
            case B:
                return A;
            case ZOMBIE:
                return ZOMBIE;
            default:
                return NEUTRAL;
        }
    }

    /**
     * Returns whether a robot of this team is a player-controlled entity
     * (team A or team B).
     *
     * @return whether a robot of this team is player-controlled.
     */
    public boolean isPlayer() {
        return this == A || this == B;
    }
}
