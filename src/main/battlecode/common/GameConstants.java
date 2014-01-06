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
    public static final int MAP_MIN_HEIGHT = 10; // TODO(axc): update this later
    
    /** The maximum possible map height. */
    public static final int MAP_MAX_HEIGHT = 70;

    /** The minumum possible map width. */
    public static final int MAP_MIN_WIDTH = 10; // TODO(axc): update this later
    
    /** The maxiumum possible map width. */
    public static final int MAP_MAX_WIDTH = 70;

    /** A valid map must have at least this many encampment locations. */
    //public static final int MAP_MINIMUM_ENCAMPMENTS = 5;

    /** The minimum distance between spawn points on the map. */
    public static final int MIN_DISTANCE_BETWEEN_SPAWN_POINTS = 10;
    
    /** The bytecode penalty that is imposed each time an exception is thrown */
    public static final int EXCEPTION_BYTECODE_PENALTY = 500;
    
    /** The number of indicator strings that a player can associate with a robot */
    public static final int NUMBER_OF_INDICATOR_STRINGS = 3;
    
    /** The base number of bytecodes a robot can execute each round */
    public static final int BYTECODE_LIMIT = 10000;
    
    /** The number of longs that your team can remember between games. */
    public static final int TEAM_MEMORY_LENGTH = 32;

    /** The maximum number of robots you are allowed. */
    public static final int MAX_ROBOTS = 25; // TODO(axc): decide this number

    /** The total amount of damage to be applied to a team's HQ once the round limit is reached */
    //public static final double TIME_LIMIT_DAMAGE = Integer.MAX_VALUE;
 
    /** The upkeep cost of a unit per round. Note that units pay even more than this base cost to execute bytecodes */
    //public static final double UNIT_POWER_UPKEEP = 1.0;
    
    /** If a team cannot pay a unit's upkeep in power, it pays this cost in energon instead. */
    //public static final double UNIT_ENERGON_UPKEEP = 5.0;
    
    /** The minimum possible round at which nodes may begin taking end-of-round damage */
    public static final int ROUND_MIN_LIMIT = 2000;
    
    /** The maximum possible round at which nodes may begin taking end-of-round damage */
    public static final int ROUND_MAX_LIMIT = 2000;
    
    /** Radius of artillery splash damage in units squared */
    //public static final int ARTILLERY_SPLASH_RADIUS_SQUARED = 2;
    
    /** Percantage of direct artillery damage that the splash damage does */
    //public static final double ARTILLERY_SPLASH_RATIO = 0.3;
   
    /** Rate at which SHIELDS decay. This number will be subtracted from each unit's shield pool after its turn. */
    //public static final double SHIELD_DECAY_RATE = 1.0;
   
    /** Extra sight radius bonus to unit vision when VISION is researched */
    //public static final int VISION_UPGRADE_BONUS = 19;
   
    /** Base power production per HQ */
    //public static final double HQ_POWER_PRODUCTION = 40;
    
    /** Additiona power provided by each generator */
    //public static final double GENERATOR_POWER_PRODUCTION = 10;
    
    /** Maximum amount of shields a single robot can carry */
    //public static final double SHIELD_CAP = 100000000.0;
    
    /** The energy to bytecode converstion rate */
    //public static final double POWER_COST_PER_BYTECODE = 0.0001;
    
    /** The maximum read/write-able of radio channel number */
    public static final int BROADCAST_MAX_CHANNELS = 65535;
    
    /** The power cost required to broadcast a message to a single radio channel */
    public static final double BROADCAST_SEND_COST = 0.03;
    
    /** The power cost required to read a message from a single radio channel */
    public static final double BROADCAST_READ_COST = 0.003;
   
    /** Constant used to calculate how suppliers factor into the HQ spawn delay */
    public static final double HQ_SPAWN_DELAY_CONSTANT_1 = 30; // a
    
	/** Constant used to calculate how suppliers factor into the HQ spawn delay */
	public static final double HQ_SPAWN_DELAY_CONSTANT_2 = 1.5; // b -> a + N^b, where N is num robots
    
    /** Amount of power required to wear a hat */
    public static final double HAT_POWER_COST = 40.0;

    /** Cow value decay per turn */
    public static final double NEUTRALS_TURN_DECAY = 0.995; // TODO(axc): .995

    /** Range in which non-sneaking movement scares cows (from the destination location of the movement) */
    public static final int MOVEMENT_SCARE_RANGE = 9;
	
	/** Range in which attacking scares cows (from the target location of the attack) */
    public static final int ATTACK_SCARE_RANGE = 36;
	
	/** Range in which NOISETOWER light attacks scare cows */
    public static final int NOISE_SCARE_RANGE_SMALL = 9;
	
	/** Range in which NOISETOWER normal attacks scare cows */
    public static final int NOISE_SCARE_RANGE_LARGE = 36;

	/** Number of turns after taking damage before a SOLDIER begins regenerating*/
    public static final int SOLDIER_HEAL_TURN_DELAY = 30;
	
	/** Amount of health a regenerating soldier heals per turn */
    public static final double SOLDIER_HEAL_RATE = 0.25;

	/** Range in which a PASTR contains and milks cows */
    public static final int PASTR_RANGE = 5;
	
	/** Amount of milk necessary for victory */
    public static final double WIN_QTY = 10000000.0;
	
	/** Factor at which SOLDIERs herd milk */
    public static final double ROBOT_MILK_PERCENTAGE = 0.05;
	
	/** Flat damage dealt by a self destruct */
    public static final double SELF_DESTRUCT_BASE_DAMAGE = 30.0; // a
	
	/** Factor at which robot's health is converted to damage*/
    public static final double SELF_DESTRUCT_DAMAGE_FACTOR = 0.5; // b -> a + health / b = damage

	/** Number of bytecodes per turn that are not converted into actiondelay */
    public static final int FREE_BYTECODES = 2000;
	
	/** Actiondelay per extra bytecode */
    public static final double BYTECODE_PENALTY = 0.0001;

	/** Actiondelay gained every time a SOLDIER attacks */
    public static final double SOLDIER_ATTACK_ACTION_DELAY = 2.0;
	
	/** Factor at which movement-related and sneaking-related actiondelay is discounted on a road */
    public static final double ROAD_ACTION_DELAY_FACTOR = 0.7;

	/** Factor of WIN_QTY milk that is gained after destroying an enemy PASTR */
    public static final double MILK_GAIN_FACTOR = 0.1; // get this * WIN_QTY after destroying pastr
}
