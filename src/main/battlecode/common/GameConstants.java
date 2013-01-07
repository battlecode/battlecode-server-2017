package battlecode.common;

/**
 * Defines constants that affect gameplay.
 */
public interface GameConstants {

    /*
     * By convention, the names of the constants should begin with
     * what they apply to, e.g. GAME_DEFAULT_SEED rather than DEFAULT_GAME_SEED.
     */
	
	
    /** The minimum possible map height. */
    public static final int MAP_MIN_HEIGHT = 20;
    
    /** The maximum possible map height. */
    public static final int MAP_MAX_HEIGHT = 100;

    /** The minumum possible map width. */
    public static final int MAP_MIN_WIDTH = 20;
    
    /** The maxiumum possible map width. */
    public static final int MAP_MAX_WIDTH = 100;
    
    /** The bytecode penalty that is imposed each time an exception is thrown */
    public static final int EXCEPTION_BYTECODE_PENALTY = 500;
    
    /** The number of indicator strings that a player can associate with a robot */
    public static final int NUMBER_OF_INDICATOR_STRINGS = 3;
    
    /** The base number of bytecodes a robot can execute each round */
    public static final int BYTECODE_LIMIT = 10000;
    
    /** The number of longs that your team can remember between games. */
    public static final int TEAM_MEMORY_LENGTH = 32;

    /**
     * The total amount of damage to be applied to a team's HQ
     * once the round limit is reached
     */
    public static final double TIME_LIMIT_DAMAGE = 1.0;
 
    /** The upkeep cost of a unit per round. */
    public static final double UNIT_POWER_UPKEEP = 1.0;
    
    /** 
     * If a team cannot pay a unit's upkeep in power,
     * it pays this cost in energon instead.
     */
    public static final double UNIT_ENERGON_UPKEEP = 5.0;
    
    /** The minimum possible round at which nodes may begin taking end-of-round damage */
    public static final int ROUND_MIN_LIMIT = 2000;
    
    /** The maximum possible round at which nodes may begin taking end-of-round damage */
    public static final int ROUND_MAX_LIMIT = 2000;
    
    /** Radius of artillery splash damage */
    public static final int ARTILLERY_SPLASH_RADIUS_SQUARED = 2;
    public static final double ARTILLERY_SPLASH_RATIO = 0.5;
   
    /** Rate at which SHIELDS decay every round */
    public static final double SHIELD_DECAY_RATE = 1.0;
   
    /** Extra sight radius bonus to unit vision when VISION is researched */
    public static final int VISION_UPGRADE_BONUS = 19;
   
    /** Base power production per HQ */
    public static final double HQ_POWER_PRODUCTION = 40;
    
    /** Additiona power provided by each generator */
    public static final double GENERATOR_POWER_PRODUCTION = 10;
    
    /** Maximum amount of shields a single robot can carry */
    public static final double SHIELD_CAP = 100000000.0;
    
    /** The energy to bytecode converstion rate */
    public static final double POWER_COST_PER_BYTECODE = 0.0001;
    
    /** The total number of radio channels that are read/writeable */
    public static final int BROADCAST_MAX_CHANNELS = 10000;
    
    /** The power cost required to broadcast a message to a single radio channel */
    public static final double BROADCAST_SEND_COST = 0.05;
    
    /** The power cost erquired to read a message from a single radio channel */
    public static final double BROADCAST_READ_COST = 0.01;
   
    /** The number of rounds required by a soldier to lay a mind */
    public static final int MINE_LAY_DELAY = 25;
    
    /** The number of rounds required by a soldier to defuse a mine */
    public static final int MINE_DEFUSE_DELAY = 12;

    /** The number of rounds required by a soldier to defuse a mine if they have DIFFUSION upgrade */
    public static final int MINE_DEFUSE_DIFFUSION_DELAY = 5;
   
    /** The power cost required to begin an encampement capture */
    public static final double CAPTURE_POWER_COST = 10;
    
    /** The number of rounds required by a SOLDIER to capture an encampment */
    public static final int CAPTURE_ROUND_DELAY = 50;
   
    /** The amount of damage that a mine deals to a robot standing on it per round */
    public static final double MINE_DAMAGE = 10;
    
    /** The rate at which stockpiled power decays */
    public static final double POWER_DECAY_RATE = 0.80;
    
    /** The rate at which stockpiled energy decays with the FUSION upgrade */
    public static final double POWER_DECAY_RATE_FUSION = 0.99;
    
    /** Rounds required to spawn a unit at the start of the game */
    public static final int HQ_SPAWN_DELAY = 10;

    /** Constant used to calculate how suppliers factor into the HQ spawn delay */
    public static final double HQ_SPAWN_DELAY_CONSTANT = 10;
    
    /** A valid map must have at least this many encampment locations. */
    public static final int MAP_MINIMUM_ENCAMPMENTS = 5;
}
