package battlecode.serial;

/**
 * Determines roughly by how much the winning team won.
 */
public enum DominationFactor {
    /**
     * Beat via a HANDWASHSTATION count or by team ID.
     */
    WON_BY_DUBIOUS_REASONS,
    /**
     * Beat by tiebreaks.
     */
    BARELY_BEAT,
    /**
     * Beat by destroying enemy HQ.
     */
    BEAT,
    /**
     * N/A
     */
    OWNED,
    /**
     * N/A
     */
    PWNED,
    /**
     * N/A
     */
    DESTROYED;
}
