package battlecode.common;

/**
 * Defines constants that affect gameplay.
 */
public interface GameConstants {

    // *********************************
    // ****** MAP CONSTANTS ************
    // *********************************

    /** The minimum possible map height. */
    public static final int MAP_MIN_HEIGHT = 20;
    
    /** The maximum possible map height. */
    public static final int MAP_MAX_HEIGHT = 100;

    /** The minumum possible map width. */
    public static final int MAP_MIN_WIDTH = 20;
    
    /** The maxiumum possible map width. */
    public static final int MAP_MAX_WIDTH = 100;

    /** The minimum distance between spawn points on the map. */
    public static final int MIN_DISTANCE_BETWEEN_SPAWN_POINTS = 10;

    // *********************************
    // ****** GAME PARAMETERS **********
    // *********************************

    /** The minimum possible round at which the game may be forced to end  */
    public static final int ROUND_MIN_LIMIT = 2000;
    
    /** The maximum possible round at which the game may be forced to end */
    public static final int ROUND_MAX_LIMIT = 2000;
    
    /** The number of longs that your team can remember between games. */
    public static final int TEAM_MEMORY_LENGTH = 32;
    
    /** The number of indicator strings that a player can associate with a robot */
    public static final int NUMBER_OF_INDICATOR_STRINGS = 3;

    /** The maximum read/write-able of radio channel number */
    public static final int BROADCAST_MAX_CHANNELS = 65535;
    
    /** The bytecode penalty that is imposed each time an exception is thrown */
    public static final int EXCEPTION_BYTECODE_PENALTY = 500;

    /** Number of bytecodes per turn that don't cost supply. */
    public static final int FREE_BYTECODES = 2000;

    /** Bytecodes executed per unit of supply. */
    public static final int BYTECODES_PER_SUPPLY = 1000;

    // *********************************
    // ****** SUPPLY *******************
    // *********************************

    /** The rate at which supply decays each turn. */
    public static final double SUPPLY_DECAY = 0.00;

    /** The maximum distance over which supplies can be transferred. */
    public static final int SUPPLY_TRANSFER_RADIUS_SQUARED = 15;

	/** The amount of supply a SUPPLYDEPOT generates each turn. */
	public static final int DEPOT_SUPPLY_GEN = 100;
	
	/** The amount of supply a HQ generates each turn. */
	public static final int HQ_SUPPLY_GEN = 200;

    // *********************************
    // ****** MINING *******************
    // *********************************

    /** The amount of ore a team starts the game with. */
    public static final int ORE_INITIAL_AMOUNT = 500;

    /** The amount of ore the HQ gets for free each turn. */
    public static final int HQ_ORE_INCOME = 5;

    /** The minimum amount that is mined on a square with no ore. */
    public static final int MINIMUM_MINE_AMOUNT = 1;

    /** The maximum amount of ore that a BEAVER can mine. */
    public static final int BEAVER_MINE_MAX = 2;

    /** The fraction of ore that a BEAVER gets from a square. */
    public static final int BEAVER_MINE_RATE = 20; // means 1/20

    /** The maximum amount of ore that a MINER can mine. */
    public static final int MINER_MINE_MAX = 3;

    /** The maximum amount of ore that an upgraded MINER can mine. */
    public static final int MINER_MINE_MAX_UPGRADED = 4;

    /** The fraction of ore that a MINER gets from a square. */
    public static final int MINER_MINE_RATE = 4; // means 1/4

    /** The movement delay gained from mining. */
    public static final int MINING_MOVEMENT_DELAY = 2;

    /** The loading delay from mining. */
    public static final int MINING_LOADING_DELAY = 1;

    // *********************************
    // ****** UNIT PROPERTIES **********
    // *********************************

    /** The attack radius of a buffed HQ. */
    public static final int ATTACK_RADIUS_SQUARED_BUFFED_HQ = 35;

    /** The rate at which a launcher spawns missiles. */
    public static final int MISSILE_SPAWN_FREQUENCY = 6;

    /** The maximum number of missiles a launcher can have at a time. */
    public static final int MAX_MISSILE_COUNT = 8;

    /** Bash radius of a basher. */
    public static final int BASH_RADIUS_SQUARED = 2;

    /** Missile explosion splash radius. */
    public static final int MISSILE_RADIUS_SQUARED = 2;

    /** Splash radius of HQ with tower bonus. */
    public static final int HQ_SPLASH_RADIUS_SQUARED = 2;

    // *********************************
    // ****** COMMANDER ***** **********
    // *********************************

    public static final int XP_RANGE = 24;

    public static final double REGEN_RATE = 1.0;

    public static final int XP_REQUIRED_LEADERSHIP = 1000;
    public static final int XP_REQUIRED_FLASH = 2000;

    public static final int BURST_DELAY = 3;
    public static final int BURST_RANGE = 35;
    public static final int BURST_DAMAGE = 80;

    public static final int LEADERSHIP_RANGE = 15;

    public static final int INTERVENTION_RANGE = 15;
    public static final int INTERVENTION_LENGTH = 10;

    public static final int FLASH_RANGE = 15;
    public static final double FLASH_MOVEMENT_DELAY = 1.0;
}
