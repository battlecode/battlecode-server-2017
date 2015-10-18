package battlecode.common;

/**
 * Defines constants that affect gameplay.
 */
public interface GameConstants {

    // *********************************
    // ****** MAP CONSTANTS ************
    // *********************************

    /** The minimum possible map height. */
    public static final int MAP_MIN_HEIGHT = 30;

    /** The maximum possible map height. */
    public static final int MAP_MAX_HEIGHT = 120;

    /** The minumum possible map width. */
    public static final int MAP_MIN_WIDTH = 30;

    /** The maxiumum possible map width. */
    public static final int MAP_MAX_WIDTH = 120;

    // *********************************
    // ****** GAME PARAMETERS **********
    // *********************************

    /** The minimum possible round at which the game may be forced to end */
    public static final int ROUND_MIN_LIMIT = 2000;

    /** The maximum possible round at which the game may be forced to end */
    public static final int ROUND_MAX_LIMIT = 3000;

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

    /** Maximum archons that can appear on a map (per team). */
    public static final int NUMBER_OF_ARCHONS_MAX = 6;

    // *********************************
    // ****** PARTS *******************
    // *********************************

    /** The amount that each team starts with */
    public static final int PARTS_INITIAL_AMOUNT = 1000;
    
    /** The part income per turn per archon */
    public static final int ARCHON_PART_INCOME = 10;

    // *********************************
    // ****** RUBBLE *******************
    // *********************************

    /** The threshold of rubble that robots (except SCOUTs) can't move through */
    public static final int RUBBLE_OBSTRUCTION_THRESH = 100;

    // *********************************
    // ****** UNIT PROPERTIES **********
    // *********************************

    /** The attack radius of a buffed HQ (2 towers). */
    public static final int HQ_BUFFED_ATTACK_RADIUS_SQUARED = 35;

    /** The fraction of damage taken per attack for a buffed HQ (1 tower). */
    public static final double HQ_BUFFED_DAMAGE_RATIO_LEVEL_1 = 0.8;

    /**
     * The fraction of damage taken per attack for a 4-tower buffed HQ (4
     * towers).
     */
    public static final double HQ_BUFFED_DAMAGE_RATIO_LEVEL_2 = 0.5;

    /**
     * The fraction of damage taken per attack for a 6-tower buffed HQ (6
     * towers).
     */
    public static final double HQ_BUFFED_DAMAGE_RATIO_LEVEL_3 = 0.3;

    /** The damage multiplier for a buffed HQ (3 towers). */
    public static final double HQ_BUFFED_DAMAGE_MULTIPLIER_LEVEL_1 = 1.5;

    /** The damage multiplier for a buffed HQ (6 towers). */
    public static final double HQ_BUFFED_DAMAGE_MULTIPLIER_LEVEL_2 = 10.0;

    /** The attack delay of a buffed HQ (5 towers). */
    public static final int HQ_BUFFED_ATTACK_DELAY = 1;

    /** The fraction of damage done by splash on a buffed HQ (5 towers). */
    public static final double HQ_BUFFED_SPLASH_RATE = 0.5;

    /** Splash radius of a buffed HQ (5 towers). */
    public static final int HQ_BUFFED_SPLASH_RADIUS_SQUARED = 2;

    /** Bash radius of a basher. */
    public static final int BASH_RADIUS_SQUARED = 2;

    /** Missile explosion splash radius. */
    public static final int MISSILE_RADIUS_SQUARED = 2;

    /** The rate at which a launcher spawns missiles. */
    public static final int MISSILE_SPAWN_FREQUENCY = 8;

    /** Maximum damage a missile can take. */
    public static final double MISSILE_MAXIMUM_DAMAGE = 1.0;

    /** The maximum number of missiles a launcher can have at a time. */
    public static final int MISSILE_MAX_COUNT = 5;

    /** After this many turns, a missile automatically detonates. */
    public static final int MISSILE_LIFESPAN = 5;
   
    /** Guard's attack is scaled by this when attacking a Zombie opponent. */
    public static final double GUARD_ZOMBIE_MULTIPLIER = 1.5;
    
    /** Damage a robot receives from a Viper's infection per turn */
    public static final double VIPER_INFECTION_DAMAGE = 2;
    
    /**
     * The factor that the delays are multiplied by when a DRONE travels on a
     * void.
     */
    public static final double DRONE_VOID_DELAY_MULTIPLIER = 2.0;

    /** The factor that delays are multiplied by when a unit moves diagonally. */
    public static final double DIAGONAL_DELAY_MULTIPLIER = 1.4;

    // *********************************
    // ****** COMMANDER ***** **********
    // *********************************

    /** The range inside which commanders gain xp for destroyed enemy units. **/
    public static final int XP_RANGE = 24;

    /** The hp that a commander regenerates per turn. **/
    public static final double REGEN_RATE = 1.0;

    /** The xp required to activate the 'flash' skill. **/
    public static final int XP_REQUIRED_FLASH = 0;

    /** The xp required to activate the 'leadership' skill. **/
    public static final int XP_REQUIRED_LEADERSHIP = 1000;

    /** The xp required to activate the 'heavy hands' skill. **/
    public static final int XP_REQUIRED_HEAVY_HANDS = 1500;

    /** The xp required to activate the 'leadership' skill. **/
    public static final int XP_REQUIRED_IMPROVED_LEADERSHIP = 2000;

    /** The range of the 'leadership' skill. **/
    public static final int LEADERSHIP_RANGE_SQUARED = 24;

    /**
     * The damage increase applied to all allied units within range of
     * 'leadership' when the skill is active.
     **/
    public static final double LEADERSHIP_DAMAGE_BONUS = 1;
    public static final double IMPROVED_LEADERSHIP_DAMAGE_BONUS = 2;

    /**
     * Cooldown for the FLASH skill (minimum number of turns between each usage
     * of FLASH).
     */
    public static final int FLASH_COOLDOWN = 20;

    /** The range of the 'flash' skill. **/
    public static final int FLASH_RANGE_SQUARED = 10;

    /** Movement delay increase upon using the 'flash' skill. **/
    public static final double FLASH_MOVEMENT_DELAY = 1.0;

    /** Movement delay incurred by using the 'heavy hands' skill. **/
    public static final double HEAVY_HANDS_MOVEMENT_DELAY = 3.0;

    /** Attack delay incurred by using the 'heavy hands' skill. **/
    public static final double HEAVY_HANDS_ATTACK_DELAY = 3.0;
}
