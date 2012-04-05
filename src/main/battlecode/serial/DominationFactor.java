package battlecode.serial;

/**
 * Determines roughly by how much the winning team won.
 */
public enum DominationFactor {
    /**
     * Beat by having more energon at the end of the round limit
     */
    WON_BY_DUBIOUS_REASONS,
    /**
     * Beat by having more archons at the end of the round limit
     */
    BARELY_BEAT,
    /**
     * Beat by having more points
     */
    BEAT,
    /**
     * Beat by having greater than the minimum points and greater than the min difference as well *
     */
    OWNED,
    /**
     * Beat by killing the enemy's units other than towers
     */
    PWNED,
    /**
     * Beat by killing the enemy's units other than towers w/o losing any archons
     */
    DESTROYED;
}
