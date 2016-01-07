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
    int MAP_MAX_HEIGHT = 100;

    /** The minumum possible map width. */
    int MAP_MIN_WIDTH = 30;

    /** The maxiumum possible map width. */
    int MAP_MAX_WIDTH = 100;

    // *********************************
    // ****** GAME PARAMETERS **********
    // *********************************

    /** The number of longs that your team can remember between games. */
    int TEAM_MEMORY_LENGTH = 32;

    /** The number of indicator strings that a player can associate with a robot */
    int NUMBER_OF_INDICATOR_STRINGS = 3;

    /** The bytecode penalty that is imposed each time an exception is thrown */
    int EXCEPTION_BYTECODE_PENALTY = 500;

    /** Maximum archons that can appear on a map (per team). */
    int NUMBER_OF_ARCHONS_MAX = 6;

    /** Represents the multiple of the sightRange for which the cost is fixed. */
    double BROADCAST_RANGE_MULTIPLIER = 2;

    /** Represents the base delay increase of a broadcast. */
    double BROADCAST_BASE_DELAY_INCREASE = 0.05;

    /** The additional delay increase of broadcasting beyond the base cost.
     *  See specs for the formula. */
    double BROADCAST_ADDITIONAL_DELAY_INCREASE = 0.03;

    // *********************************
    // ****** PARTS *******************
    // *********************************

    /** The amount that each team starts with */
    double PARTS_INITIAL_AMOUNT = 200;
    
    /** The part income per turn per archon */
    double ARCHON_PART_INCOME = 0.75;

    /** The parts reward for destroying a zomie den */
    double DEN_PART_REWARD = 300;

    // *********************************
    // ****** RUBBLE *******************
    // *********************************

    /** The threshold of rubble that robots (except SCOUTs) can't move
     * through. */
    double RUBBLE_OBSTRUCTION_THRESH = 100;

    /** The threshold of rubble that slows robots (except SCOUTS). */
    double RUBBLE_SLOW_THRESH = 50;

    /** Percentage of rubble removed with each clear. */
    double RUBBLE_CLEAR_PERCENTAGE = 0.05;

    /** Flat amount of rubble removed with each clear. */
    double RUBBLE_CLEAR_FLAT_AMOUNT = 10;

    // *********************************
    // ****** UNIT PROPERTIES **********
    // *********************************
   
    /** Guard's attack is scaled by this when attacking a Zombie opponent. */
    double GUARD_ZOMBIE_MULTIPLIER = 2;
    
    /** Damage a robot receives from a Viper's infection per turn */
    double VIPER_INFECTION_DAMAGE = 2;

    /** Minimum attack range (range squared) of a Turret */
    int TURRET_MINIMUM_RANGE = 6;

    /** Time to transform between Turret and TTM */
    int TURRET_TRANSFORM_DELAY = 10;

    /** The factor that delays are multiplied by when a unit moves diagonally. */
    double DIAGONAL_DELAY_MULTIPLIER = 1.4;

    /** Amount an archon repairs another bot for. */
    double ARCHON_REPAIR_AMOUNT = 1.0;

    /** Archon activation range (ranged squared). */
    int ARCHON_ACTIVATION_RANGE = 2;

    /** Amount of damage robots take when standing next to dens that are spawning. **/
    double DEN_SPAWN_PROXIMITY_DAMAGE = 10.0;

    /** Number of turns that elapse for the zombie outbreak level to increase */
    int OUTBREAK_TIMER = 300;

    // *********************************
    // ****** GAMEPLAY PROPERTIES ******
    // *********************************

    /** The default game seed. **/
    int GAME_DEFAULT_SEED = 6370;

    /** The default game maxiumum number of rounds. **/
    int GAME_DEFAULT_ROUNDS = 3000;
}
