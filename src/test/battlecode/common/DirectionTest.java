package battlecode.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DirectionTest {

    @Test
    public void testBasic() {
        assertFalse(Direction.NORTH.isDiagonal());
        assertTrue(Direction.NORTH_EAST.isDiagonal());
        assertFalse(Direction.EAST.isDiagonal());
        assertTrue(Direction.SOUTH_EAST.isDiagonal());
        assertFalse(Direction.SOUTH.isDiagonal());
        assertTrue(Direction.SOUTH_WEST.isDiagonal());
        assertFalse(Direction.WEST.isDiagonal());
        assertTrue(Direction.NORTH_WEST.isDiagonal());

        assertEquals(Direction.NORTH.opposite(), Direction.SOUTH);
        assertEquals(Direction.SOUTH.opposite(), Direction.NORTH);
        assertEquals(Direction.EAST.opposite(), Direction.WEST);
        assertEquals(Direction.WEST.opposite(), Direction.EAST);
        assertEquals(Direction.NORTH_EAST.opposite(), Direction.SOUTH_WEST);
        assertEquals(Direction.SOUTH_WEST.opposite(), Direction.NORTH_EAST);
        assertEquals(Direction.NORTH_WEST.opposite(), Direction.SOUTH_EAST);
        assertEquals(Direction.SOUTH_EAST.opposite(), Direction.NORTH_WEST);

        assertEquals(Direction.NORTH.rotateRight(), Direction.NORTH_EAST);
        assertEquals(Direction.NORTH_EAST.rotateRight(), Direction.EAST);
        assertEquals(Direction.EAST.rotateRight(), Direction.SOUTH_EAST);
        assertEquals(Direction.SOUTH_EAST.rotateRight(), Direction.SOUTH);
        assertEquals(Direction.SOUTH.rotateRight(), Direction.SOUTH_WEST);
        assertEquals(Direction.SOUTH_WEST.rotateRight(), Direction.WEST);
        assertEquals(Direction.WEST.rotateRight(), Direction.NORTH_WEST);
        assertEquals(Direction.NORTH_WEST.rotateRight(), Direction.NORTH);

        assertEquals(Direction.NORTH.rotateLeft(), Direction.NORTH_WEST);
        assertEquals(Direction.NORTH_EAST.rotateLeft(), Direction.NORTH);
        assertEquals(Direction.EAST.rotateLeft(), Direction.NORTH_EAST);
        assertEquals(Direction.SOUTH_EAST.rotateLeft(), Direction.EAST);
        assertEquals(Direction.SOUTH.rotateLeft(), Direction.SOUTH_EAST);
        assertEquals(Direction.SOUTH_WEST.rotateLeft(), Direction.SOUTH);
        assertEquals(Direction.WEST.rotateLeft(), Direction.SOUTH_WEST);
        assertEquals(Direction.NORTH_WEST.rotateLeft(), Direction.WEST);
    }
}
