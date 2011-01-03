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
    public static final double BROADCAST_FIXED_COST = 0.01;
    /** Additional cost  per byte of message size */
    public static final double BROADCAST_COST_PER_BYTE = 0.0001;
    /** The minimum possible map height. */
    public static final int MAP_MIN_HEIGHT = 20;
    /** The maximum possible map height. */
    public static final int MAP_MAX_HEIGHT = 65;
    /** The minumum possible map width. */
    public static final int MAP_MIN_WIDTH = 20;
    /** The maxiumum possible map width. */
    public static final int MAP_MAX_WIDTH = 65;
    /** Percent of upkeep returned per unused bytecode for ending the round early */
    public static final double YIELD_BONUS = 0.1;
    /** The bytecode penalty that is imposed each time an exception is thrown */
    public static final int EXCEPTION_BYTECODE_PENALTY = 500;
    /** The number of indicator strings that a player can associate with a robot */
    public static final int NUMBER_OF_INDICATOR_STRINGS = 3;
    /** The base number of bytecodes a robot can execute each round */
    public static final int BYTECODE_LIMIT_BASE = 3000;
    public static final int BYTECODE_LIMIT_ADDON = 3000;
    /** The chainer's splash damage radius */
    public final static int CHAINER_SPLASH_RADIUS_SQUARED = 2;
    /** The radius at which flux is mined */
    public static final int SOLDIER_MOVEMENT_DECREASE_ROUNDS = 4;
    /** How many rounds a soldier will slow a Robot for */
    public static final int SOLDIER_SLOW_ROUNDS = 60;
    /** Decreace percentage of point differential needed to win per round after round limit */
    public static final double POINTS_DECREASE_PER_ROUND_FACTOR = 0.0005;
    /** The number of rounds after a component is equipped before it may be used */
    public static final int EQUIP_WAKE_DELAY = 10;

    public static final int POWER_WAKE_DELAY = 20;
    public static final double HARDENED_MAX_DAMAGE = 2.5;
    public static final double SHIELD_MIN_DAMAGE = .05;
    public static final double SHIELD_DAMAGE_REDUCTION = .2;
    public static final int IRON_EFFECT_ROUNDS = 2;
    public static final double PLATING_HP_BONUS = .1;
    public static final double REGEN_AMOUNT = .1;
    /** The number of longs that your team can remember between games. */
    public static final int TEAM_MEMORY_LENGTH = 32;
    /** The maximum capacity that dropships can transport.  Note that
     * a chassis of capacity, say, six will take up six space in the
     * dropship, regardless of its components.
     */
    public static final int TRANSPORT_CAPACITY = 24;
    public static final double MINE_RESOURCES = 1.;
    public static final double MINE_DEPLETED_RESOURCES = .15;
    public static final int MINE_ROUNDS = 2000;
    public static final double MINE_MINIMUM = 0.15;
    public static final int MINE_DEPLETION_RATE = 5;

    public static final double[] BEAM_RAMP = {0, 0, 0, 1, 1, 2, 3, 4, 5};

	public static final int BUG_DURATION = 250;
	public static final int BUG_SENSOR_RANGE = 8;

    // This class cannot be instantiated.
    private GameConstants() {
    }
}
