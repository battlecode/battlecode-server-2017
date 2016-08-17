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
    int MAP_MAX_HEIGHT = 80;

    /** The minumum possible map width. */
    int MAP_MIN_WIDTH = 30;

    /** The maxiumum possible map width. */
    int MAP_MAX_WIDTH = 80;

    // *********************************
    // ****** GAME PARAMETERS **********
    // *********************************

    /** The number of victory points required to win the game. */
    int VICTORY_POINTS_TO_WIN = 1000;

    /** The number of longs that your team can remember between games. */
    int TEAM_MEMORY_LENGTH = 32;

    /** The number of indicator strings that a player can associate with a robot */
    int NUMBER_OF_INDICATOR_STRINGS = 3;

    /** The bytecode penalty that is imposed each time an exception is thrown */
    int EXCEPTION_BYTECODE_PENALTY = 500;

    /** Maximum archons that can appear on a map (per team). */
    int NUMBER_OF_ARCHONS_MAX = 3;

    // *********************************
    // ****** TREES ********************
    // *********************************

    /** The max health of bullet trees */
    int BULLET_TREE_MAX_HEALTH = 100;

    /** The radius of bullet trees */
    double BULLET_TREE_RADIUS = .5;

    /** The min radius a neutral tree can have */
    double NEUTRAL_TREE_MIN_RADIUS = .5;

    /** The max radius a neutral tree can have */
    double NEUTRAL_TREE_MAX_RADIUS = 10;

    /**
     * The rate at which the max health of neutral trees are determined;
     * i.e. maxHealth = NEUTRAL_TREE_HEALTH_RATE * treeRadius
     */
    double NEUTRAL_TREE_HEALTH_RATE = 100;

    // *********************************
    // ****** BULLETS ******************
    // *********************************

    /** The amount that each team starts with */
    double BULLETS_INITIAL_AMOUNT = 300;
    
    /** The bullet income per turn (independent of number of archons).  */
    double ARCHON_BULLET_INCOME = 2;

    /** The decrease in bullet income per turn per bullet that you have. */
    double BULLET_INCOME_UNIT_PENALTY = 0.01;
    
    // *********************************
    // ****** MESSAGING ****************
    // *********************************

    /** The size of the team-shared array for signaling*/
    int BROADCAST_MAX_CHANNELS = 1000;
    
    // *********************************
    // ****** GAMEPLAY PROPERTIES ******
    // *********************************

    /** The default game seed. **/
    int GAME_DEFAULT_SEED = 6370;

    /** The default game maxiumum number of rounds. **/
    int GAME_DEFAULT_ROUNDS = 3000;
}
