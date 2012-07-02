package battlecode.common;

/**
 * Represents data of the terrain of a location on the map.
 */
public enum TerrainTile {

    LAND, VOID, OFF_MAP;

    /**
     * Returns <code>true</code> if robots at the given
     * height can traverse this terrain tile.
     */
    public boolean isTraversableAtHeight(RobotLevel height) {
        switch (this) {
            case VOID:
                return height == RobotLevel.IN_AIR;
            case LAND:
                return true;
            default:
                return false;
        }
    }
}
