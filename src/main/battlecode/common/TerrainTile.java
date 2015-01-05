package battlecode.common;

/**
 * Represents data of the terrain of a location on the map. <br>
 * <code>VOID</code> squares cannot be moved on by any units except drones and missiles. <br>
 * <code>NORMAL</code> squares can be freely moved and built on. These are the only squares that may have ore on them. <br>
 * <code>UNKNOWN</code> squares have not been sensed yet, and their terrain type is not known. <br>
 * <code>OFF_MAP</code> squares are not within the map boundaries. <br>
 */
public enum TerrainTile {

    NORMAL, VOID, UNKNOWN, OFF_MAP;

    /**
     * Returns <code>true</code> if robots
     * can traverse this terrain tile.
     *
     * @return whether the tile is traversible.
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
