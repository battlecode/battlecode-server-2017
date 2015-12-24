package battlecode.world;

/**
 * Determines roughly by how much the winning team won.
 */
public enum DominationFactor {
    /**
     * Win by highest Archon ID (tiebreak 4).
     */
    WON_BY_DUBIOUS_REASONS,
    /**
     * Win by parts stockpile plus parts cost of controlled robots (tiebreak 3).
     */
    BARELY_BEAT,
    /**
     * Win by more total Archon HP (tiebreak 2).
     */
    OWNED,
    /**
     * Win by more Archons remaining (tiebreak 1).
     */
    PWNED,
    /**
     * Win by destroying enemy Archons.
     */
    DESTROYED
}
