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
     * Win by bullet supply plus bullet cost of active robots and trees (tiebreak 3).
     */
    BARELY_BEAT,
    /**
     * Win by more bullet trees (tiebreak 2).
     */
    OWNED,
    /**
     * Win by more victory points (tiebreak 1).
     */
    PWNED,
    /**
     * Win by destroying all enemy robots (trees are not considered robots).
     */
    DESTROYED
}
