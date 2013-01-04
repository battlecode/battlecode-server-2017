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
     * The minimum possible map height.
     */
    public static final int MAP_MIN_HEIGHT = 20;
    /**
     * The maximum possible map height.
     */
    public static final int MAP_MAX_HEIGHT = 100;
    /**
     * The minumum possible map width.
     */
    public static final int MAP_MIN_WIDTH = 20;
    /**
     * The maxiumum possible map width.
     */
    public static final int MAP_MAX_WIDTH = 100;
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
     * The total amount of damage to be divided evenly among each team's
     * power nodes each turn once the time limit is reached.
     */
    public static final double TIME_LIMIT_DAMAGE = .5;
    /**
     * The upkeep cost per turn for each unit that is not an archon or
     * tower.
     */
    public static final double UNIT_UPKEEP = 1.0;
    public static final double UNIT_ENERGON_UPKEEP = 5.0;
    /**
     * The minimum possible round at which nodes may begin taking end-of-round damage
     */
    public static final int MIN_ROUND_LIMIT = 2000;
    /**
     * The maximum possible round at which nodes may begin taking end-of-round damage
     */
    public static final int MAX_ROUND_LIMIT = 10000;
    
    public static final int ARTILLERY_SPLASH_RADIUS_SQUARED = 2;
    
    public static final int VISION_UPGRADE_BONUS = 22;
    
    public static final double HQ_RESOURCE_PRODUCTION = 40;
    public static final double GENERATOR_RESOURCE_PRODUCTION = 10;
    public static final double SHIELD_CAP = 100000000.0;
    public static final double ENERGY_COST_PER_BYTECODE = 0.0001;
    public static final int MAX_RADIO_CHANNEL = 10000;
    public static final double BROADCAST_MESSAGE_COST = 0.01;
    public static final double READ_MESSAGE_COST = 0.0001;
    
    public static final int ROUNDS_TO_MINE = 25;
    public static final int ROUNDS_TO_DEFUSE = 5;
    public static final double SCAN_COST = 0.01;
    public static final double MINE_DAMAGE = 10;
    
    public static final double CAPTURE_COST = 20;
    public static final int CAPTURE_DELAY = 100;
    
    public static final double RESOURCE_DECAY_RATE = 0.80;
    public static final double RESOURCE_DECAY_RATE_FUSION = 0.99;
    
    /** HQ base spawn delay */
    public static final int SPAWN_DELAY = 10;
    /** Multiplicative reduction in HQ spawn delay for each SUPPLIER */
    public static final double SUPPLIER_SPAWN_REDUCTION = 0.1;
}
