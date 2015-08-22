package battlecode.serial;

/**
 * Determines roughly by how much the winning team won.
 */
public enum DominationFactor {
    /**
     * Team HQ ID win (tiebreak 6).
     */
    WON_BY_DUBIOUS_REASONS, /**
                             * Win by more ore stockpile + ore of surviving
                             * robots (tiebreak 5).
                             */
    BARELY_BARELY_BEAT, /**
                         * Win by superior sanitation (tiebreak 4).
                         */
    BARELY_BEAT, /**
                  * Win by more total tower HP (tiebreak 3).
                  */
    BEAT, /**
           * Win by more HQ HP (tiebreak 2).
           */
    OWNED, /**
            * Win by more towers remaining (tiebreak 1).
            */
    PWNED, /**
            * Win by destroying enemy HQ.
            */
    DESTROYED;
}
