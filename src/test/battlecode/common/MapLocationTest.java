package battlecode.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class MapLocationTest {

    // TODO: write a bunch more of these

    /**
     * Epsilon for float comparisons.
     */
    final float EPS = .000000001f;

    @Test
    public void testConstructor() {
        MapLocation loc = new MapLocation(10123, -401823);
        assertEquals(loc.x, 10123, EPS);
        assertEquals(loc.y, -401823, EPS);
    }
}
