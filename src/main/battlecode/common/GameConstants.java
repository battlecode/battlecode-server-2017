package battlecode.common;

/**
 * Defines game constants used throughout the packages.
 * This class is meant to be used in other classes with
 * an <code>import static battlecode.common.GameConstants.*;</code>.
 *
 */
public final class GameConstants {

    /*
     * By convention, the names of the constants should begin with
     * what they apply to, e.g. GAME_DEFAULT_SEED rather than DEFAULT_GAME_SEED.
     */
    /** Fixed cost to send a message */
    public static final double BROADCAST_FIXED_COST = 0.0002;
    /** Additional cost  per byte of message size */
    public static final double BROADCAST_COST_PER_BYTE = 0.000002;
    /** The minimum possible map height. */
    public static final int MAP_MIN_HEIGHT = 20;
    /** The maximum possible map height. */
    public static final int MAP_MAX_HEIGHT = 70;
    /** The minumum possible map width. */
    public static final int MAP_MIN_WIDTH = 20;
    /** The maxiumum possible map width. */
    public static final int MAP_MAX_WIDTH = 70;
    /** The maximum fraction of upkeep that can be regained by yielding */
    public static final double YIELD_BONUS = 0.998;
    /** The bytecode penalty that is imposed each time an exception is thrown */
    public static final int EXCEPTION_BYTECODE_PENALTY = 500;
    /** The number of indicator strings that a player can associate with a robot */
    public static final int NUMBER_OF_INDICATOR_STRINGS = 3;
    /** The base number of bytecodes a robot can execute each round */
    public static final int BYTECODE_LIMIT_BASE = 10000;
    /** The number of longs that your team can remember between games. */
    public static final int TEAM_MEMORY_LENGTH = 32;
    /** The maximum capacity that dropships can transport.  Note that
     * a chassis of capacity, say, six will take up six space in the
     * dropship, regardless of its components.
     */
	/** The square of the maximum distance a message can travel. */
	public static final int BROADCAST_RADIUS_SQUARED = 64;
	public static final double REGEN_COST = .4;
	/** The amount of energon gained when a unit regenerates. */
	public static final double REGEN_AMOUNT = .2;
	/** The number of archons each team starts with. */
	public static final int NUMBER_OF_ARCHONS = 6;
	/** The increase in a unit's attack timeout when it is hit by a
	 * DISRUPTER. */
	public static final int DISRUPTER_DELAY = 2;
	/** The amount of damage dealt each turn to a node that is not connected
	 * to its team's power core. */
	public static final double DISCONNECTED_NODE_DAMAGE = .2;
	/** The total amount of damage to be divided evenly among each team's
	 * power nodes each turn once the time limit is reached. */
	public static final double TIME_LIMIT_DAMAGE = .4;
	/** The upkeep cost per turn for each unit that is not an archon or
	 * tower */
	public static final double UNIT_UPKEEP = .15;
	/**
	 * The minimum amount of flux that an archon can produce in a turn.
	 */
	public static final double MIN_PRODUCTION = .5;
	/**
	 * The amount of flux that an archon would produce if there
	 * was an archon at distance zero from it.
	 */
	public static final double MAX_PRODUCTION = 1.;
	/**
	 * The amount of flux that an archon will produce if there
	 * are no allied archons within PRODUCTION_PENALTY_R2 of it
	 */
	public static final int PRODUCTION_PENALTY_R2 = 16;

	/** The number of turns before a newly spawned robot can move or attack */
	public static final int WAKE_DELAY = 20;

	public static final int MIN_POWER_NODES = 4;
	public static final int MAX_POWER_NODES = 30;

    // This class cannot be instantiated.
    private GameConstants() {
    }
}
