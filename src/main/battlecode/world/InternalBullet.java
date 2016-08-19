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
        final double distToTarget = bulletStart.distanceTo(targetCenter);
        final Direction toFinish = bulletStart.directionTo(bulletFinish);
        final Direction toTarget = bulletStart.directionTo(targetCenter);

        double radiansBetween = toFinish.radiansBetween(bulletStart.directionTo(targetCenter));

        //Check if the target intersects with the line made between the bullet points
        double perpDist = Math.abs(distToTarget * Math.sin(radiansBetween));
        if(perpDist > targetRadius){
            return -1;
        }

        //Calculate hitDist
        double halfChordDist = Math.sqrt(targetRadius * targetRadius - perpDist * perpDist);
        double hitDist = distToTarget * Math.cos(radiansBetween);
        if(hitDist < 0){
            hitDist += halfChordDist;
            hitDist = hitDist >= 0 ? 0 : hitDist;
        }else{
            hitDist -= halfChordDist;
            hitDist = hitDist < 0 ? 0 : hitDist;
        }

        //Check invalid hitDists
        if(hitDist < minDist || hitDist > maxDist){
            return -1;
        }
        return hitDist;
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
