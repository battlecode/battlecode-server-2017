package battlecode.serial;

import battlecode.world.DominationFactor;

/**
 * Used to keep track of various statistics in a given
 * battlecode match.  These should be stats that don't change from round
 * to round, but rather are given only at the end of the match.
 */
public class GameStats implements ServerEvent {

    private static final long serialVersionUID = 4678980796113812229L;

    private DominationFactor dominationFactor = null;

    public GameStats() {}

    public void setDominationFactor(DominationFactor factor) {
        this.dominationFactor = factor;
    }

    public DominationFactor getDominationFactor() {
        return this.dominationFactor;
    }

}
