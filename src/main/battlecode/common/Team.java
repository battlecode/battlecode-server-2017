package battlecode.common;

/**
 * This enum represents the team of a GameObject; each GameObject has
 * exactly one team. Robots are either on team A or B, while objects like
 * mines are neutral.
 *
 * Since Team is a Java 1.5 enum, you can use it in <code>switch</code>
 * statements, it has all the standard enum methods (<code>valueOf</code>,
 * <code>values</code>, etc.), and you can safely use <code>==</code> for
 * equality tests.
 */
public enum Team {

    /** Represents team A. */
    A {

        /**
         * {@inheritDoc}
         */
        public Team opponent() {
            return B;
        }
    },
    /** Represents team B. */
    B {

        /**
         * {@inheritDoc}
         */
        public Team opponent() {
            return A;
        }
    },
    /** Represents neither team A nor team B. */
    NEUTRAL {

        /**
         * {@inheritDoc}
         */
        public Team opponent() {
            return NEUTRAL;
        }
    };

    /**
     * Determines the team that is the opponent of this team.
     *
     * @return the opponent of this team, or NEUTRAL if this team
     * is NEUTRAL
     */
    public abstract Team opponent();
}
