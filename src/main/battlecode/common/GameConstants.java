package battlecode.common;

/**
 * Defines constants that affect gameplay.
 */
@SuppressWarnings("unused")
public interface GameConstants {

    // *********************************
    // ****** MAP CONSTANTS ************
    // *********************************

    /** The minimum possible map height. */
    int MAP_MIN_HEIGHT = 30;

    /** The maximum possible map height. */
    int MAP_MAX_HEIGHT = 120;

    /** The minumum possible map width. */
    int MAP_MIN_WIDTH = 30;

    /** The maxiumum possible map width. */
    int MAP_MAX_WIDTH = 120;

    // *********************************
    // ****** GAME PARAMETERS **********
    // *********************************

    /** The minimum possible round at which the game may be forced to end */
    int ROUND_MIN_LIMIT = 2000;

    /** The maximum possible round at which the game may be forced to end */
    int ROUND_MAX_LIMIT = 3000;

    /** The number of longs that your team can remember between games. */
    int TEAM_MEMORY_LENGTH = 32;

    /** The number of indicator strings that a player can associate with a robot */
    int NUMBER_OF_INDICATOR_STRINGS = 3;

    /** The maximum read/write-able of radio channel number */
    int BROADCAST_MAX_CHANNELS = 65535;

    /** The bytecode penalty that is imposed each time an exception is thrown */
    int EXCEPTION_BYTECODE_PENALTY = 500;

    /** Number of bytecodes per turn that don't cost supply. */
    int FREE_BYTECODES = 2000;

    /** Bytecodes executed per unit of supply. */
    int BYTECODES_PER_SUPPLY = 1000;

    /** Maximum archons that can appear on a map (per team). */
    int NUMBER_OF_ARCHONS_MAX = 6;

    // *********************************
    // ****** PARTS *******************
    // *********************************

    /** The amount that each team starts with */
    int PARTS_INITIAL_AMOUNT = 200;
    
    /** The part income per turn per archon */
    int ARCHON_PART_INCOME = 1;

    // *********************************
    // ****** RUBBLE *******************
    // *********************************

    /** The threshold of rubble that robots (except SCOUTs) can't move
     * through. */
    int RUBBLE_OBSTRUCTION_THRESH = 100;

    /** The threshold of rubble that slows robots (except SCOUTS). */
    int RUBBLE_SLOW_THRESH = 50;

    /** Percentage of rubble removed with each clear. */
    double RUBBLE_CLEAR_PERCENTAGE = 0.05;

    /** Flat amount of rubble removed with each clear. */
    double RUBBLE_CLEAR_FLAT_AMOUNT = 20;

    // *********************************
    // ****** UNIT PROPERTIES **********
    // *********************************
   
    /** Guard's attack is scaled by this when attacking a Zombie opponent. */
    double GUARD_ZOMBIE_MULTIPLIER = 1.5;
    
    /** Damage a robot receives from a Viper's infection per turn */
    double VIPER_INFECTION_DAMAGE = 2;
    
    /** Minimum attack range of a Turret */
    int TURRET_MINIMUM_RANGE = 5;

    /** The factor that delays are multiplied by when a unit moves diagonally. */
    double DIAGONAL_DELAY_MULTIPLIER = 1.4;

    /** The default game seed. **/
    int GAME_DEFAULT_SEED = 6370;

    /** The default game maxiumum number of rounds. **/
    int GAME_DEFAULT_ROUNDS = 2000;
}
