package battlecode.world;

import battlecode.common.*;

/**
 * The representation of a bullet used by the server.
 */
public strictfp class InternalBullet {
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

    public void updateBullet() {
        MapLocation bulletStart = this.getLocation();
        MapLocation bulletFinish = bulletStart.add(this.getDirection(), this.getSpeed());

        // THIS DOES NOT FOLLOW THE SPEC
        // but it works

        InternalRobot hitRobot = gameWorld.getObjectInfo().getRobotAtLocation(bulletFinish);
        if (hitRobot != null) {
            gameWorld.destroyBullet(this.ID);
            hitRobot.damageRobot(this.damage);
            return;
        }

        InternalTree hitTree = gameWorld.getObjectInfo().getTreeAtLocation(bulletFinish);
        if (hitTree != null) {
            gameWorld.destroyBullet(this.ID);
            hitTree.damageTree(this.damage, this.team);
            return;
        }

        if (gameMap.onTheMap(bulletFinish)) {
            gameWorld.destroyBullet(this.ID);
        }

    }

    //TODO: Simplify this somehow
    /*public void updateBullet(){
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

        //Check if ends off map
        float mapEdgeDist = calcBoundaryDist(bulletStart, bulletFinish);

        if (hitRobot != null) {

        } else if (hitTree != null) {

        } else if ()
        //Update GameWorld when tree or robot not hit
        if(hitTree == null && hitRobot == null){
            if(mapEdgeDist >= 0){
                setLocation(bulletStart.add(toFinish, mapEdgeDist));
                gameWorld.destroyBullet(this.ID);
            }else{
                setLocation(bulletStart.add(toFinish, this.speed));
            }
        }//Update GameWorld when tree or robot is hit
        else{
            if(hitTreeDist < hitRobotDist){
                setLocation(bulletStart.add(toFinish, hitTreeDist));
                gameWorld.destroyBullet(this.ID);
                hitTree.damageTree(this.damage, this.team);
            }else{
                setLocation(bulletStart.add(toFinish, hitRobotDist));
                gameWorld.destroyBullet(this.ID);
                hitRobot.damageRobot(this.damage);
            }
        }
    }*/

    // ******************************************
    // ****** CALCULATIONS **********************
    // ******************************************

    /*private float calcHitDist(MapLocation bulletStart, MapLocation bulletFinish,
                              MapLocation targetCenter, float targetRadius){
        final float minDist = 0;
        final float maxDist = bulletStart.distanceTo(bulletFinish);
        final float distToTarget = bulletStart.distanceTo(targetCenter);
        final Direction toFinish = bulletStart.directionTo(bulletFinish);
        final Direction toTarget = bulletStart.directionTo(targetCenter);

        float radiansBetween = toFinish.radiansBetween(bulletStart.directionTo(targetCenter));

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
    }*/

    /*private float calcBoundaryDist(MapLocation bulletStart, MapLocation bulletFinish){
        float distTotal = bulletStart.distanceTo(bulletFinish);
        float distToLeft  = gameWorld.getGameMap().getOrigin().x - bulletFinish.x;
        float distToRight = bulletFinish.x -
                (gameWorld.getGameMap().getOrigin().x + gameWorld.getGameMap().getWidth());
        float distToTop   = gameWorld.getGameMap().getOrigin().y - bulletFinish.y;
        float distToBottom= bulletFinish.y -
                (gameWorld.getGameMap().getOrigin().y + gameWorld.getGameMap().getHeight());

        float distCollide = -1;
        if(distToLeft > 0){
            Direction toRight = new Direction(0);
            Direction toStart = bulletFinish.directionTo(bulletStart);
            float angle = toRight.radiansBetween(toStart);
            float hyp   = distToLeft / (float) Math.cos(angle);
            MapLocation testLoc = bulletFinish.add(toStart, hyp);
            if(testLoc.y >= gameMap.getOrigin().y &&
                    testLoc.y <= gameMap.getOrigin().y + gameMap.getHeight()){
                distCollide = distTotal - hyp;
            }
        }
        if(distToRight > 0){
            Direction toLeft = new Direction((float)Math.PI);
            Direction toStart = bulletFinish.directionTo(bulletStart);
            float angle = toLeft.radiansBetween(toStart);
            float hyp   = distToRight / (float) Math.cos(angle);
            MapLocation testLoc = bulletFinish.add(toStart, hyp);
            if(testLoc.y >= gameMap.getOrigin().y &&
                    testLoc.y <= gameMap.getOrigin().y + gameMap.getHeight()){
                distCollide = distTotal - hyp;
            }
        }
        if(distToTop > 0){
            Direction toBottom = new Direction(3 * (float)Math.PI / 2);
            Direction toStart = bulletFinish.directionTo(bulletStart);
            float angle = toBottom.radiansBetween(toStart);
            float hyp   = distToTop * (float) Math.cos(angle);
            MapLocation testLoc = bulletFinish.add(toStart, hyp);
            if(testLoc.x >= gameMap.getOrigin().x &&
                    testLoc.x <= gameMap.getOrigin().x + gameMap.getWidth()){
                distCollide = distTotal - hyp;
            }
        }
        if(distToBottom > 0){
            Direction toTop = new Direction((float)Math.PI / 2);
            Direction toStart = bulletFinish.directionTo(bulletStart);
            float angle = toTop.radiansBetween(toStart);
            float hyp   = distToBottom * (float) Math.cos(angle);
            MapLocation testLoc = bulletFinish.add(toStart, hyp);
            if(testLoc.x >= gameMap.getOrigin().x &&
                    testLoc.x <= gameMap.getOrigin().x + gameMap.getWidth()){
                distCollide = distTotal - hyp;
            }
        }
        return distCollide;
    }*/

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
