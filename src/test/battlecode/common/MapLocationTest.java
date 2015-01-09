package battlecode.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MapLocationTest {

    @Test
    public void testConstructor() {
        MapLocation loc = new MapLocation(10123, -401823);
        assertEquals(loc.x, 10123);
        assertEquals(loc.y, -401823);
    }

    @Test
    public void testAdd() {
        MapLocation loc = new MapLocation(10, 10);
        MapLocation newLoc = loc.add(Direction.SOUTH_WEST);
        assertEquals(newLoc.x, 9);
        assertEquals(newLoc.y, 11);

        newLoc = loc.add(Direction.NORTH_WEST, 5);
        assertEquals(newLoc.x, 5);
        assertEquals(newLoc.y, 5);

        newLoc = loc.add(100, -500);
        assertEquals(newLoc.x, 110);
        assertEquals(newLoc.y, -490);
    }

    @Test
    public void testDistanceSquaredTo() {
        MapLocation loc = new MapLocation(10, -1);
        MapLocation loc2 = new MapLocation(-6, 0);
        assertEquals(loc.distanceSquaredTo(loc2), 16 * 16 + 1 * 1);
        assertEquals(loc2.distanceSquaredTo(loc), 16 * 16 + 1 * 1);
    }

    @Test
    public void testIsAdjacentTo() {
        MapLocation loc = new MapLocation(100, -50);
        assertTrue(loc.isAdjacentTo(loc.add(Direction.NORTH_EAST)));
        assertTrue(loc.isAdjacentTo(loc.add(Direction.NORTH)));
        assertTrue(loc.isAdjacentTo(loc.add(Direction.SOUTH)));
        assertTrue(loc.isAdjacentTo(loc.add(Direction.SOUTH_WEST)));
        assertFalse(loc.isAdjacentTo(loc.add(Direction.SOUTH_WEST).add(Direction.WEST)));
        assertFalse(loc.isAdjacentTo(loc.add(Direction.WEST).add(Direction.WEST)));
        assertFalse(loc.isAdjacentTo(loc));
    }

    public void testGetMapLocationsHelper(MapLocation center, int radiusSquared, int length) {
        MapLocation[] res = MapLocation.getAllMapLocationsWithinRadiusSq(center, radiusSquared);
        assertEquals(res.length, length);
        for (int i = 0; i < length; ++i) {
            assertTrue(res[i].distanceSquaredTo(center) <= radiusSquared);
            for (int j = 0; j < i; ++j) {
                assertFalse(res[i].equals(res[j]));
            }
        }
    }

    @Test
    public void testGetAllMapLocationsWithinRadiusSq() {
        MapLocation center = new MapLocation(-500, 200);

        MapLocation[] zero = MapLocation.getAllMapLocationsWithinRadiusSq(center, 0);
        assertEquals(zero.length, 1);
        assertEquals(zero[0], center);

        testGetMapLocationsHelper(center, 1, 5);
        testGetMapLocationsHelper(center, 2, 9);
        testGetMapLocationsHelper(center, 5, 21);
        testGetMapLocationsHelper(center, 10, 37);
        testGetMapLocationsHelper(center, 24, 69);
        testGetMapLocationsHelper(center, 25, 69 + 4 + 8);
        testGetMapLocationsHelper(center, 1000000, 241 * 241);
    }
}
