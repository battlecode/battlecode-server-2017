package battlecode.common;

/**
 * This enum represents the team of a GameObject; each GameObject has
 * exactly one team. Robots are either on team A or B, while objects like
 * mines are neutral.
 * <p/>
 * Since Team is a Java 1.5 enum, you can use it in <code>switch</code>
 * statements, it has all the standard enum methods (<code>valueOf</code>,
 * <code>values</code>, etc.), and you can safely use <code>==</code> for
 * equality tests.
 */
public enum Team {

    A, B, NEUTRAL;

    /**
     * Determines the team that is the opponent of this team.
     *
     * @return the opponent of this team, or NEUTRAL if this team
     *         is NEUTRAL
     */
    public Team opponent() {
        switch (this) {
            case A:
                return B;
            case B:
                return A;
            default:
                return NEUTRAL;
        }
    }
}
