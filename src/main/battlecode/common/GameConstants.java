package battlecode.common;

/**
 * Defines constants that affect gameplay.
 */
public interface GameConstants {

    /*
     * By convention, the names of the constants should begin with
     * what they apply to, e.g. GAME_DEFAULT_SEED rather than DEFAULT_GAME_SEED.
     */
    /**
     * Fixed cost to send a message
     */
    public static final double BROADCAST_FIXED_COST = 0.01;
    /**
     * Additional cost  per byte of message size
     */
    public static final double BROADCAST_COST_PER_BYTE = 0.0001;
    /**
     * The minimum possible map height.
     */
    public static final int MAP_MIN_HEIGHT = 30;
    /**
     * The maximum possible map height.
     */
    public static final int MAP_MAX_HEIGHT = 60;
    /**
     * The minumum possible map width.
     */
    public static final int MAP_MIN_WIDTH = 30;
    /**
     * The maxiumum possible map width.
     */
    public static final int MAP_MAX_WIDTH = 60;
    /**
     * The maximum fraction of upkeep that can be regained by yielding
     */
    public static final double YIELD_BONUS = 0.998;
    /**
     * The bytecode penalty that is imposed each time an exception is thrown
     */
    public static final int EXCEPTION_BYTECODE_PENALTY = 500;
    /**
     * The number of indicator strings that a player can associate with a robot
     */
    public static final int NUMBER_OF_INDICATOR_STRINGS = 3;
    /**
     * The base number of bytecodes a robot can execute each round
     */
    public static final int BYTECODE_LIMIT = 10000;
    /**
     * The number of longs that your team can remember between games.
     */
    public static final int TEAM_MEMORY_LENGTH = 32;
    /** The maximum capacity that dropships can transport.  Note that
     * a chassis of capacity, say, six will take up six space in the
     * dropship, regardless of its components.
     */
    /**
     * The square of the maximum distance a message can travel.
     */
    public static final int BROADCAST_RADIUS_SQUARED = 64;
    /**
     * The cost in flux to regenerate.
     */
    public static final double REGEN_COST = .4;
    /**
     * The amount of energon gained when a unit regenerates.
     */
    public static final double REGEN_AMOUNT = .2;
    /**
     * The number of archons each team starts with.
     */
    public static final int NUMBER_OF_ARCHONS = 6;
    /**
     * The increase in a unit's attack timeout when it is hit by a
     * DISRUPTER.
     */
    public static final int DISRUPTER_DELAY = 3;
    /**
     * The amount of damage dealt each turn to a node that is not connected
     * to its team's power core.
     */
    public static final double DISCONNECTED_NODE_DAMAGE = .2;
    /**
     * The total amount of damage to be divided evenly among each team's
     * power nodes each turn once the time limit is reached.
     */
    public static final double TIME_LIMIT_DAMAGE = .4;
    /**
     * The upkeep cost per turn for each unit that is not an archon or
     * tower.
     */
    public static final double UNIT_UPKEEP = .15;
    /**
     * The minimum amount of flux that an archon can produce in a turn.
     *
     * @see #MAX_PRODUCTION
     * @see #PRODUCTION_PENALTY_R2
     */
    public static final double MIN_PRODUCTION = .5;
    /**
     * The maximum amount of flux that an archon can produce in a turn.
     *
     * @see #MIN_PRODUCTION
     * @see #PRODUCTION_PENALTY_R2
     */
    public static final double MAX_PRODUCTION = 1.;
    /**
     * An archon's production will decrease if it is within this
     * distance squared of an allied archon.  If <code>d</code>
     * is the minimum distance squared to an allied archon, then the amount
     * of flux produced is <code>GameConstants.MIN_PRODUCTION+(GameConstants.MAX_PRODUCTION-GameConstants.MIN_PRODUCTION)*Math.min(1.,Math.sqrt((double)d/GameConstants.PRODUCTION_PENALTY_R2))</code>.
     *
     * @see #MAX_PRODUCTION
     * @see #MIN_PRODUCTION
     */
    public static final int PRODUCTION_PENALTY_R2 = 16;
    /**
     * The number of turns before a newly spawned robot can move or attack.
     */
    public static final int WAKE_DELAY = 20;
    /**
     * The minimum number of power nodes that can appear on a map.
     */
    public static final int MIN_POWER_NODES = 6;
    /**
     * The maximum number of power nodes that can appear on a map.
     */
    public static final int MAX_POWER_NODES = 50;
    /**
     * The minimum possible round at which nodes may begin taking end-of-round damage
     */
    public static final int MIN_ROUND_LIMIT = 2000;
    /**
     * The maximum possible round at which nodes may begin taking end-of-round damage
     */
    public static final int MAX_ROUND_LIMIT = 10000;
}
