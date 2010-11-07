package battlecode.common;

import java.io.Serializable;

/**
 * Represents data of the terrain of a location on the map.
 */
public enum TerrainTile {

	LAND, VOID, OFF_MAP;

    public boolean isTraversableAtHeight(RobotLevel height) {
        switch (this) {
            case VOID:
                return height == RobotLevel.IN_AIR;
            case LAND:
                return height != RobotLevel.MINE;
			default:
				return false;
        }
    }
}
