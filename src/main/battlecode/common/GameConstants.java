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
    public static final double YIELD_BONUS = 0.1;
    /** The bytecode penalty that is imposed each time an exception is thrown */
    public static final int EXCEPTION_BYTECODE_PENALTY = 500;
    /** The number of indicator strings that a player can associate with a robot */
    public static final int NUMBER_OF_INDICATOR_STRINGS = 3;
    /** The base number of bytecodes a robot can execute each round */
    public static final int BYTECODE_LIMIT_BASE = 3000;
    /** The number of additioal bytecodes granted by a processor */
    public static final int BYTECODE_LIMIT_ADDON = 4500;
    /** The number of rounds after a component is equipped before it may be used */
    public static final int EQUIP_WAKE_DELAY = 10;
    /** The number of rounds it takes for components to wake up after a robot is powered on */
    public static final int POWER_WAKE_DELAY = 20;
    /** The maximum amount of damage done to a robot with Hardened from any one attack */
    public static final double HARDENED_MAX_DAMAGE = 2.;
    /** A shield can reduce the damage done no lower than this value */
    public static final double SHIELD_MIN_DAMAGE = .15;
    /** The amount by which each shield reduces the damage from any attack */
    public static final double SHIELD_DAMAGE_REDUCTION = .6;
    /** Number of rounds Iron stays in effect after it is activated */
    public static final int IRON_EFFECT_ROUNDS = 4;
    /** Hp granted by every plate on a robot */
    public static final double PLATING_HP_BONUS = 7.;
    /** Hp regen bonus per round for each regen equipped */
    public static final double REGEN_AMOUNT = .8;
    /** The number of longs that your team can remember between games. */
    public static final int TEAM_MEMORY_LENGTH = 32;
    /** The maximum capacity that dropships can transport.  Note that
     * a chassis of capacity, say, six will take up six space in the
     * dropship, regardless of its components.
     */
    public static final int TRANSPORT_CAPACITY = 30;
    /** Maximum resources granted per flux mine before it starts to deplete */
    public static final double MINE_RESOURCES = 1.0;
    /** Minimum resources granted per flux mine after it is fully depleted */
    public static final double MINE_DEPLETED_RESOURCES = 0.15;
    /** Rounds that a mine is fully operational */
    public static final int MINE_ROUNDS = 2000;
    /** Rounds for the mine to lose 0.01 */
    public static final int MINE_DEPLETION_RATE = 5;
    /** Damage done by successive attacks of the beam as long as it is focused */
    public static final double[] BEAM_RAMP = {0, 1, 2, 3, 4, 6};
    /** Duration for which bug is in effect after it is planted */
    public static final int BUG_DURATION = 250;
    /** Radius squared that the bug can sense */
    public static final int BUG_SENSOR_RANGE = 8;
	/** Initial flux for each team */
	public static final double INITIAL_FLUX = 30.;
	/** Minimum number of mines allowed on official maps. */
	public static final int MINES_MIN = 8;
	/** Maximum number of mines allowed on official maps. */
	public static final int MINES_MAX = 100;
	/** The maximum lifetime of a DUMMY */
	public static final int DUMMY_LIFETIME = 200;
	
	public static final double MAX_NODE_HEALTH = 100.0;
	public static final double NODE_CAPTURE_LIMIT = 10.0;

    // This class cannot be instantiated.
    private GameConstants() {
    }
}
