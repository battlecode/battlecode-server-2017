package battlecode.engine.instrumenter.lang;

import java.util.Random;

/**
 * A wrapper for java.util.Random, that uses the map seed in place of system time for the default Random constructor
 *
 * @author adamd
 */
public class RoboRandom extends Random {

    private static final long serialVersionUID = 0; // don't serialize

    private static long mapSeed = 0;

    public RoboRandom() {
        super(mapSeed);
    }

    public RoboRandom(long seed) {
        super(seed);
    }

    /**
     * Sets the seed used for the default Random cosntructor.  Should be called at the beginning of each game.
     */
    public static void setMapSeed(long seed) {
        mapSeed = seed;
    }

    public static long getMapSeed() {
        return mapSeed;
    }
}
