package battlecode.common;

/**
 * Represents data of the terrain of a location on the map.
 */
public enum TerrainTile {

    NORMAL, VOID, OFF_MAP;

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
            default:
                return false;
        }
    }
}
