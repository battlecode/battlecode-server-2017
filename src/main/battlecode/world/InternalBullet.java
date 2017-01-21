package battlecode.world;

import battlecode.common.*;

/**
 * The representation of a bullet used by the server.
 */
public strictfp class InternalBullet implements InternalBody {
    private final GameWorld gameWorld;
    private final LiveMap gameMap;

    private final int ID;
    private Team team;
    private float speed;
    private float damage;
    private Direction dir;
    private MapLocation location;

    private int roundsAlive;

    /**
     * Used to avoid recreating the same BulletInfo object over and over.
     */
    private BulletInfo cachedBulletInfo;

    public InternalBullet(GameWorld gw, int id, Team team, float speed, float damage, MapLocation location,
                        Direction dir) {
        this.gameWorld = gw;
        this.gameMap = gw.getGameMap();

        this.ID = id;
        this.dir = dir;
        this.team = team;
        this.speed = speed;
        this.damage = damage;
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

    public float getSpeed() {
        return speed;
    }

    public float getDamage() {
        return damage;
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
                && this.cachedBulletInfo.damage == damage
                && this.cachedBulletInfo.dir.equals(dir)
                && this.cachedBulletInfo.location.equals(location)) {
            return this.cachedBulletInfo;
        }
        return this.cachedBulletInfo = new BulletInfo(ID, location, dir, speed, damage);
    }

    // ******************************************
    // ****** UPDATE METHODS ********************
    // ******************************************

    public void setLocation(MapLocation newLoc){
        this.gameWorld.getObjectInfo().moveBullet(this, newLoc);
        this.location = newLoc;
    }

    public void updateBullet(){
        MapLocation bulletStart = this.getLocation();
        MapLocation bulletFinish = bulletStart.add(this.getDirection(), this.getSpeed());
        Direction toFinish = bulletStart.directionTo(bulletFinish);
        float distToFinish = (float) bulletStart.distanceTo(bulletFinish);

        MapLocation checkCenter = bulletStart.add(toFinish, distToFinish/2);

        //Find closest hit tree
        InternalTree hitTree = null;
        float hitTreeDist = Float.MAX_VALUE;
        for(InternalTree tree : gameWorld.getObjectInfo().getAllTreesWithinRadius(checkCenter,
                GameConstants.NEUTRAL_TREE_MAX_RADIUS + distToFinish/2)){
            float hitDist = calcHitDist(bulletStart, bulletFinish,
                    tree.getLocation(), tree.getRadius());
            if(hitDist < hitTreeDist && hitDist >=0){
                hitTree = tree;
                hitTreeDist = hitDist;
            }
        }

        //Find closest hit robot
        InternalRobot hitRobot = null;
        float hitRobotDist = Float.MAX_VALUE;
        for(InternalRobot robot : gameWorld.getObjectInfo().getAllRobotsWithinRadius(checkCenter,
                GameConstants.MAX_ROBOT_RADIUS + distToFinish/2)){
            float hitDist = calcHitDist(bulletStart, bulletFinish,
                    robot.getLocation(), robot.getType().bodyRadius);
            if(hitDist < hitRobotDist && hitDist >=0){
                hitRobot = robot;
                hitRobotDist = hitDist;
            }
        }

        if(hitRobot == null && hitTree == null) {
            // If bullet didn't hit anything...
            if (!gameMap.onTheMap(bulletFinish)) {
                /// ...and went off the map, destroy it.
                gameWorld.destroyBullet(this.ID);
            } else {
                // ... and stayed on the map, keep it going.
                setLocation(bulletFinish);
            }
        } else {
            // If the bullet hit something...
            if(hitTreeDist<hitRobotDist && hitTree != null) {
                // And the closest thing hit was a tree...
                gameWorld.destroyBullet(this.ID);
                hitTree.damageTree(this.damage, this.team, false);
            } else  if (hitRobot != null){
                /// And the closest thing hit was a robot...
                gameWorld.destroyBullet(this.ID);
                hitRobot.damageRobot(this.damage);
            } else {
                // This should never happen
                throw new RuntimeException("Closest hit object was null");
            }
        }
    }

    // ******************************************
    // ****** CALCULATIONS **********************
    // ******************************************

    private float calcHitDist(MapLocation bulletStart, MapLocation bulletFinish,
                              MapLocation targetCenter, float targetRadius){
        final float minDist = 0;
        final float maxDist = bulletStart.distanceTo(bulletFinish);
        final float distToTarget = bulletStart.distanceTo(targetCenter);
        final Direction toFinish = bulletStart.directionTo(bulletFinish);
        final Direction toTarget = bulletStart.directionTo(targetCenter);

        // If toTarget is null, then bullet is on top of center of unit, distance is zero
        if(toTarget == null) {
            return 0;
        }

        if(toFinish == null) {
            // This should never happen
            throw new RuntimeException("bulletStart and bulletFinish are the same.");
        }

        float radiansBetween = toFinish.radiansBetween(toTarget);

        //Check if the target intersects with the line made between the bullet points
        float perpDist = (float)Math.abs(distToTarget * Math.sin(radiansBetween));
        if(perpDist > targetRadius){
            return -1;
        }

        //Calculate hitDist
        float halfChordDist = (float)Math.sqrt(targetRadius * targetRadius - perpDist * perpDist);
        float hitDist = distToTarget * (float)Math.cos(radiansBetween);
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
