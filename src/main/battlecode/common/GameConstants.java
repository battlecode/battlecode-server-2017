package battlecode.common;

/**
 * Defines constants that affect gameplay.
 */
public interface GameConstants {
	
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
    
    /** The bytecode penalty that is imposed each time an exception is thrown */
    public static final int EXCEPTION_BYTECODE_PENALTY = 500;
    
    /** The number of indicator strings that a player can associate with a robot */
    public static final int NUMBER_OF_INDICATOR_STRINGS = 3;
    
    /** The number of longs that your team can remember between games. */
    public static final int TEAM_MEMORY_LENGTH = 32;

    /** The minimum possible round at which the game may be forced to end  */
    public static final int ROUND_MIN_LIMIT = 2000;
    
    /** The maximum possible round at which the game may be forced to end */
    public static final int ROUND_MAX_LIMIT = 2000;
    
    /** The maximum read/write-able of radio channel number */
    public static final int BROADCAST_MAX_CHANNELS = 65535;

    /** The amount of ore a team starts the game with. */
    public static final int ORE_INITIAL_AMOUNT = 500;

    // TODO maybe put in RobotType
    /** The attack radius of a buffed HQ. */
    public static final int ATTACK_RADIUS_SQUARED_BUFFED_HQ = 35;

    public static final int MISSILE_SPAWN_FREQUENCY = 6;
    public static final int MAX_MISSILE_COUNT = 8;

    public static final double SUPPLY_DECAY = 0.01;

    // commander constants

    public static final int XP_RANGE = 24;

    public static final int BURST_DELAY = 3;
    public static final int BURST_RANGE = 35;
    public static final int BURST_DAMAGE = 80;

    public static final int LEADERSHIP_RANGE = 15;

    public static final int INTERVENTION_RANGE = 15;
    public static final int INTERVENTION_LENGTH = 10;

    public static final int FLASH_RANGE = 15;

    // mining stuff
    public static final int MINIMUM_MINE_AMOUNT = 1;
    public static final int FURBY_MINE_MAX = 2;
    public static final int FURBY_MINE_RATE = 100;
    public static final int MINER_MINE_RATE = 30;
    public static final int MINER_MINE_MAX = 3;
    public static final int MINER_MINE_MAX_UPGRADED = 4;

    public static final int HAT_ORE_COST = 1000;
}
