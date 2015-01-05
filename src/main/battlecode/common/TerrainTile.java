package battlecode.common;

/**
 * Represents data of the terrain of a location on the map. 
 * VOID squares cannot be moved on by any units except drones and missiles.
 * NORMAL squares can be freely moved and built on. These are the only squares that may have ore on them.
 * UNKNOWN squares have not been sensed yet, and their terrain type is not known.
 * OFF_MAP squares are not within the map boundaries.
 */
public enum TerrainTile {

    NORMAL, VOID, UNKNOWN, OFF_MAP;

    /**
     * Returns <code>true</code> if robots at the given
     * height can traverse this terrain tile.
     */
    public boolean isTraversable() {
        switch (this) {
            case VOID:
                return false;
            case NORMAL:
                return true;
            case UNKNOWN:
                return false;
            default:
                return false;
        }
    }
}
