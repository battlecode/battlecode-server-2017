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
    
    /** The base number of bytecodes a robot can execute each round */
    public static final int BYTECODE_LIMIT = 10000;
    
    /** The number of longs that your team can remember between games. */
    public static final int TEAM_MEMORY_LENGTH = 32;

    /** The maximum number of robots you are allowed. */
    public static final int MAX_ROBOTS = 25;

    /** The minimum possible round at which the game may be forced to end  */
    public static final int ROUND_MIN_LIMIT = 2000;
    
    /** The maximum possible round at which the game may be forced to end */
    public static final int ROUND_MAX_LIMIT = 2000;
    
    /** The maximum read/write-able of radio channel number */
    public static final int BROADCAST_MAX_CHANNELS = 65535;
   
    /** Constant used to calculate the HQ spawn delay */
    public static final double HQ_SPAWN_DELAY_CONSTANT_1 = 20; // a
    
	/** Constant used to calculate the HQ spawn delay */
	public static final double HQ_SPAWN_DELAY_CONSTANT_2 = 1.5; // b -> a + N^b, where N is num robots
    
    /** Amount of milk required to wear a hat */
    public static final double HAT_MILK_COST = 500000.0;

    /** Cow value decay per turn */
    public static final double NEUTRALS_TURN_DECAY = 0.995;

    /** Range in which non-sneaking movement scares cows (from the destination location of the movement) */
    public static final int MOVEMENT_SCARE_RANGE = 9;
	
	/** Range in which attacking scares cows (from the target location of the attack) */
    public static final int ATTACK_SCARE_RANGE = 36;
	
	/** Range in which NOISETOWER light attacks scare cows */
    public static final int NOISE_SCARE_RANGE_SMALL = 9;
	
	/** Range in which NOISETOWER normal attacks scare cows */
    public static final int NOISE_SCARE_RANGE_LARGE = 36;

	/** DEPRECATED. Number of turns after taking damage before a SOLDIER begins regenerating. Will be equal to HEAL_TURN_DELAY. */
    public static final int SOLDIER_HEAL_TURN_DELAY = 30;
	
	/** DEPRECATED. Amount of health a regenerating soldier heals per turn. Will be equal to HEAL_RATE.  */
    public static final double SOLDIER_HEAL_RATE = 0.25;

	/** Number of turns after taking damage before a robot or structure begins regenerating*/
    public static final int HEAL_TURN_DELAY = 30;
	
	/** Amount of health a regenerating robot or structure heals per turn */
    public static final double HEAL_RATE = 0.25;

	/** Range in which a PASTR contains and milks cows */
    public static final int PASTR_RANGE = 5;
	
	/** Amount of milk necessary for victory */
    public static final double WIN_QTY = 10000000.0;
	
	/** Factor at which SOLDIERs herd milk */
    public static final double ROBOT_MILK_PERCENTAGE = 0.05;
	
	/** Flat damage dealt by a self destruct */
    public static final double SELF_DESTRUCT_BASE_DAMAGE = 30.0; // a
	
	/** Factor at which robot's health is converted to damage */
    public static final double SELF_DESTRUCT_DAMAGE_FACTOR = 0.5; // b -> a + health / b = damage

	/** Number of bytecodes per turn that are not converted into actiondelay */
    public static final int FREE_BYTECODES = 2000;
	
	/** Actiondelay per extra bytecode */
    public static final double BYTECODE_PENALTY = 0.00005;

	/** Actiondelay gained every time a SOLDIER attacks. Consistent with RobotType.SOLDIER.attackDelay */
    public static final double SOLDIER_ATTACK_ACTION_DELAY = 2.0;

    /** Actiondelay gained every time a SOLDIER moves (runs) */
    public static final double SOLDIER_MOVE_ACTION_DELAY = 2.0;
    
    /** Actiondelay gained every time a SOLDIER sneaks */
    public static final double SOLDIER_SNEAK_ACTION_DELAY = 3.0;

    /** Factor of increase for actiondelay for diagonal movement */
    public static final double SOLDIER_DIAGONAL_MOVEMENT_ACTION_DELAY_FACTOR = 1.4;
	
	/** Factor at which movement-related and sneaking-related actiondelay is discounted on a road */
    public static final double ROAD_ACTION_DELAY_FACTOR = 0.7;

	/** Factor of WIN_QTY milk that is gained after destroying an enemy PASTR */
    public static final double MILK_GAIN_FACTOR = 0.1; // get this * WIN_QTY after destroying pastr

	/** Factor of WIN_QTY milk that is given to your opponent if your PASTR self destructs */
    public static final double MILK_PENALTY_FACTOR = 0.2; // opponent gets this * WIN_QTY if your pastr self destructs

    /** Accuracy of milk quantity of your opponent when sensing. The quantity is rounded down to the nearest multiple of this value. */
    public static final double OPPONENT_MILK_SENSE_ACCURACY = 2500000.0;
}
