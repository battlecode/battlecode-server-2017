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
     * Maximum size of messages
     */
    public static final int BROADCAST_SIZE_LIMIT = 256;
    /**
     * The minimum possible map height.
     */
    public static final int MAP_MIN_HEIGHT = 32;
    /**
     * The maximum possible map height.
     */
    public static final int MAP_MAX_HEIGHT = 255;
    /**
     * The minumum possible map width.
     */
    public static final int MAP_MIN_WIDTH = 32;
    /**
     * The maxiumum possible map width.
     */
    public static final int MAP_MAX_WIDTH = 255;
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
    /**
     * The square of the maximum distance a message can travel.
     */
    public static final int BROADCAST_RADIUS_SQUARED = 81;
    /**
     * The number of flux units that a Transporter can carry
     */ 
    public static final int FLUX_TRANSPORT_CAPACITY = 40;
    /**
     * The number of turns before a newly spawned robot can move or attack.
     */
    public static final int WAKE_DELAY = 20;
    /**
     * The number of turns a robot can operate without consuming flux before it becomes weakened
     */
    public static final int ROUNDS_UNTIL_WEAKENED = 10;
    /**
     * If a robot has not consumed flux within the past sustainRounds rounds, it is weakened by weakenedFactor 
     moveDelay, moveDelayDiagonal, attackDelay are multiplied
     attackRadiusSquared and attackPower are divided
     */ 
    public static final float WEAKENESS_FACTOR = 2.0f;
    /**
     * The minimum possible round at which nodes may begin taking end-of-round damage
     */
    public static final int MIN_ROUND_LIMIT = 2000;
    /**
     * The maximum possible round at which nodes may begin taking end-of-round damage
     */
    public static final int MAX_ROUND_LIMIT = 10000;
}
