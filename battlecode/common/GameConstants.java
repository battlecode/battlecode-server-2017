package battlecode.common;

/**
 * Defines game constants used throughout the packages.
 * This class is meant to be used in other classes with
 * an <code>import static battlecode.common.GameConstants.*;</code>.
 *
 */
public final class GameConstants {

    /*
     * By convention, the names of the constants should begin with
     * what they apply to, e.g. GAME_DEFAULT_SEED rather than DEFAULT_GAME_SEED.
     */
    /** Fixed cost to send a message */
    public static final double BROADCAST_FIXED_COST = 0.01;
    /** Additional cost  per byte of message size */
    public static final double BROADCAST_COST_PER_BYTE = 0.0001;
    /** The minimum possible map height. */
    public static final int MAP_MIN_HEIGHT = 20;
    /** The maximum possible map height. */
    public static final int MAP_MAX_HEIGHT = 65;
    /** The minumum possible map width. */
    public static final int MAP_MIN_WIDTH = 20;
    /** The maxiumum possible map width. */
    public static final int MAP_MAX_WIDTH = 65;
    /** Percent of upkeep returned per unused bytecode for ending the round early */
    public static final double YIELD_BONUS = 0.1;
    /** The bytecode penalty that is imposed each time an exception is thrown */
    public static final int EXCEPTION_BYTECODE_PENALTY = 500;
    /** The amount of energon per round an archon produces */
    public static final double ARCHON_PRODUCTION = 1.0;
    /** The number of indicator strings that a player can associate with a robot */
    public static final int NUMBER_OF_INDICATOR_STRINGS = 3;
    /** The number of bytecodes a robot can execute each round */
    public static final int BYTECODES_PER_ROUND = 6000;
    /** The maximum stack size a robot can use without incurring penalties */
    public static final int STACK_SIZE_LIMIT = 64;
    /** The amount of energon a robot can receive per round from transfers */
    public static final double ENERGON_TRANSFER_RATE = 1.0;
    /** The amount of energon a robot can hold in reserve */
    public static final double ENERGON_RESERVE_SIZE = 10.0;
    /** The chainer's splash damage radius */
    public final static int CHAINER_SPLASH_RADIUS_SQUARED = 2;
    /** The number of rounds it takes to capture a flux deposit */
    public static final int ROUNDS_TO_CAPTURE = 100;
    /** The number of turns it takes to complete a teleport */
    public static final int TELEPORT_DELAY = 20;
    /** The radius at which flux is mined */
    public static final int FLUX_RADIUS_SQUARED = 5;
    /** The maximum flux that can be stored in a tile */
    public static final int MAX_FLUX_PER_TILE = 64;
    /** Turns until a turret is deployed */
    public static final int TURRET_DEPLOY_TIME = 10;
    /** public static final int TURRET_UNDEPLOY_TIME = 5; */
    public static final double TURRET_DEPLOY_ATTACK_BONUS = .75;
    /** Reduction to upkeep while deployed*/
    public static final double TURRET_DEPLOY_UPKEEP_REDUCTION = 0.;
    /** Delay of a deployed Turret's turn speed */
    public static final int TURRET_TURN_SPEED_DELAY = 10;
    /** The offensive aura building increases damage by AURA_OFF_FACTOR */
    public static final double AURA_OFF_FACTOR = .25;
    /** The defensive aura building reduces damage by AURA_DEF_FACTOR */
    public static final double AURA_DEF_FACTOR = .2;
    /** The cost to teleport a unit */
    public static final int TELEPORT_FLUX_COST = 25;
    /** The max distance between buildings to contribute to the same hull */
    public static final int BUILDING_LINK_DIST = 5;
    /** The conversion multiplier from flux to energon */
    public static final double FLUX_TO_ENERGON_CONVERSION = 0.1;
    /** The conversion multiplier from energon to flux */
    public static final double ENERGON_TO_FLUX_CONVERSION = 1.0 / FLUX_TO_ENERGON_CONVERSION;
    /** How much longer movement takes after being hit by a soldier */
    public static final int SOLDIER_MOVEMENT_DECREASE_ROUNDS = 4;
    /** How many rounds a soldier will slow a Robot for */
    public static final int SOLDIER_SLOW_ROUNDS = 60;
    /** Decreace percentage of point differential needed to win per round after round limit */
    public static final double POINTS_DECREASE_PER_ROUND_FACTOR = 0.0005;
    public static final double POINTS_PER_AREA_FACTOR = 0.01;

    // This class cannot be instantiated.
    private GameConstants() {
    }
}
