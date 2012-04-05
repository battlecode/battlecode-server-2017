package battlecode.world;

import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;

/**
 * This class represents a 3D location,
 * consisting of a (regular two-dimensional) MapLocation
 * and a MapHeight.
 *
 * @author Matt
 */
public class MapLocation3D {

    /**
     * The 2D location of this MapLocation3D.
     */
    private final MapLocation location;

    /**
     * The height of this MapLocation3D.
     */
    private final RobotLevel height;

    /**
     * Creates a new 3D MapLocation given the specified location
     * and height.
     *
     * @param location the horizontal components of this 3D location
     * @param height   the vertical components of this 3D location
     */
    public MapLocation3D(MapLocation location, RobotLevel height) {
        this.location = location;
        this.height = height;
    }

    /**
     * Returns the horizontal compontents of this 3D location
     * as a MapLocation.
     *
     * @return the MapLocation part of this 3D location
     */
    public MapLocation getLocation() {
        return this.location;
    }

    /**
     * Returns the vertical component of this 3D location
     * as a MapHeight.
     *
     * @return the MapHeight part of this 3D location
     */
    public RobotLevel getHeight() {
        return this.height;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return location.hashCode() * 3 + height.hashCode() * 7;
    }

    /**
     * Two MapLocation3Ds are considered equal if they have the
     * same x- and y-coordinates and height.
     * <p/>
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof MapLocation3D))
            return false;

        MapLocation3D that = (MapLocation3D) obj;
        return (this.location.equals(that.location) && this.height == that.height);
    }
}
