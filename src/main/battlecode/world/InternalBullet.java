package battlecode.world;

import battlecode.common.*;

/**
 * The representation of a bullet used by the server.
 */
public class InternalBullet {
    private final GameWorld gameWorld;

    private final int ID;
    private Team team;
    private double speed;
    private Direction dir;
    private MapLocation location;

    private int roundsAlive;

    /**
     * Used to avoid recreating the same BulletInfo object over and over.
     */
    private BulletInfo cachedBulletInfo;

    public InternalBullet(GameWorld gw, int id, Team team, double speed, MapLocation location,
                        Direction dir) {
        this.gameWorld = gw;

        this.ID = id;
        this.dir = dir;
        this.team = team;
        this.speed = speed;
        this.location = location;

        this.roundsAlive = 0;
    }

    // ******************************************
    // ****** GETTER METHODS ********************
    // ******************************************

    public GameWorld getGameWorld() {
        return gameWorld;
    }

    public int getID() {
        return ID;
    }

    public Team getTeam() {
        return team;
    }

    public double getSpeed() {
        return speed;
    }

    public Direction getDirection() {
        return dir;
    }

    public MapLocation getLocation() {
        return location;
    }

    public int getRoundsAlive() {
        return roundsAlive;
    }

    public BulletInfo getBulletInfo() {
        if (this.cachedBulletInfo != null
                && this.cachedBulletInfo.ID == ID
                && this.cachedBulletInfo.speed == speed
                && this.cachedBulletInfo.dir.equals(dir)
                && this.cachedBulletInfo.location.equals(location)) {
            return this.cachedBulletInfo;
        }
        return this.cachedBulletInfo = new BulletInfo(ID, location, dir, speed);
    }

    // ******************************************
    // ****** UPDATE METHODS ********************
    // ******************************************

    // ******************************************
    // ****** CALCULATIONS **********************
    // ******************************************

    public double calcHitDist(MapLocation bulletStart, MapLocation bulletFinish,
                              MapLocation targetCenter, double targetRadius){
        final double minDist = 0;
        final double maxDist = bulletStart.distanceTo(bulletFinish);
        final Direction toFinish = bulletStart.directionTo(bulletFinish);

        double degreesBetween = toFinish.degreesBetween(bulletStart.directionTo(targetCenter));

    }

    // *********************************
    // ****** MISC. METHODS ************
    // *********************************

    @Override
    public boolean equals(Object o) {
        return o != null && (o instanceof InternalBullet)
                && ((InternalBullet) o).getID() == ID;
    }

    @Override
    public int hashCode() {
        return ID;
    }

    @Override
    public String toString() {
        return String.format("%s:#%d", getTeam(), getID());
    }

}
