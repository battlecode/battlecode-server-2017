package battlecode.world;

import battlecode.common.*;
import static battlecode.common.GameActionExceptionType.*;
import battlecode.instrumenter.RobotDeathException;
import battlecode.schema.Action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The actual implementation of RobotController. Its methods *must* be called
 * from a player thread.
 *
 * It is theoretically possible to have multiple for a single InternalRobot, but
 * that may cause problems in practice, and anyway why would you want to?
 *
 * All overriden methods should assertNotNull() all of their (Object) arguments,
 * if those objects are not explicitly stated to be nullable.
 */
public final strictfp class RobotControllerImpl implements RobotController {

    /**
     * The world the robot controlled by this controller inhabits.
     */
    private final GameWorld gameWorld;

    /**
     * The robot this controller controls.
     */
    private final InternalRobot robot;

    /**
     * Create a new RobotControllerImpl
     *
     * @param gameWorld the relevant world
     * @param robot the relevant robot
     */
    public RobotControllerImpl(GameWorld gameWorld, InternalRobot robot) {
        this.gameWorld = gameWorld;
        this.robot = robot;
    }

    // *********************************
    // ******** INTERNAL METHODS *******
    // *********************************

    /**
     * @return the robot this controller is connected to
     */
    public InternalRobot getRobot() {
        return robot;
    }

    /**
     * Throw a null pointer exception if an object is null.
     *
     * @param o the object to test
     */
    private static void assertNotNull(Object o) {
        if (o == null) {
            throw new NullPointerException("Argument has an invalid null value");
        }
    }

    @Override
    public int hashCode() {
        return robot.getID();
    }

    // *********************************
    // ****** GLOBAL QUERY METHODS *****
    // *********************************

    @Override
    public int getRoundLimit(){
        return gameWorld.getGameMap().getRounds();
    }

    @Override
    public int getRoundNum(){
        return gameWorld.getCurrentRound();
    }

    @Override
    public float getTeamBullets(){
        return gameWorld.getTeamInfo().getBulletSupply(getTeam());
    }

    @Override
    public int getTeamVictoryPoints(){
        return gameWorld.getTeamInfo().getVictoryPoints(getTeam());
    }

    @Override
    public int getOpponentVictoryPoints() {
        return gameWorld.getTeamInfo().getVictoryPoints(getTeam().opponent());
    }

    @Override
    public int getRobotCount(){
        return gameWorld.getObjectInfo().getRobotCount(getTeam());
    }

    @Override
    public int getTreeCount(){
        return gameWorld.getObjectInfo().getTreeCount(getTeam());
    }

    @Override
    public MapLocation[] getInitialArchonLocations(Team t){
        assertNotNull(t);
        if (t == Team.NEUTRAL) {
            return new MapLocation[0];
        } else {
            BodyInfo[] initialRobots = gameWorld.getGameMap().getInitialBodies();
            ArrayList<MapLocation> archonLocs = new ArrayList<>();
            for (BodyInfo initial : initialRobots) {
                if(initial.isRobot()){
                    RobotInfo robot = (RobotInfo) initial;
                    if (robot.type == RobotType.ARCHON && robot.team == t) {
                        archonLocs.add(robot.getLocation());
                    }
                }
            }
            MapLocation[] array = archonLocs.toArray(new MapLocation[archonLocs.size()]);
            Arrays.sort(array);
            return array;
        }
    }

    // *********************************
    // ****** UNIT QUERY METHODS *******
    // *********************************

    @Override
    public int getID(){
        return this.robot.getID();
    }

    @Override
    public Team getTeam(){
        return this.robot.getTeam();
    }

    @Override
    public RobotType getType(){
        return this.robot.getType();
    }

    @Override
    public MapLocation getLocation(){
        return this.robot.getLocation();
    }

    @Override
    public float getHealth(){
        return this.robot.getHealth();
    }
    
    @Override
    public int getAttackCount(){
        return this.robot.getAttackCount();
    }
    
    @Override
    public int getMoveCount(){
        return this.robot.getMoveCount();
    }
    

    // ***********************************
    // ****** GENERAL SENSOR METHODS *****
    // ***********************************

    private void assertCanSenseLocation(MapLocation loc) throws GameActionException{
        if(!canSenseLocation(loc)){
            throw new GameActionException(CANT_SENSE_THAT,
                    "Target location not within sensor range");
        }
    }

    private void assertCanSensePartOfCircle(MapLocation center, float radius) throws GameActionException{
        if(!canSensePartOfCircle(center, radius)){
            throw new GameActionException(CANT_SENSE_THAT,
                    "Target circle not within sensor range");
        }
    }

    private void assertCanSenseAllOfCircle(MapLocation center, float radius) throws GameActionException{
        if(!canSenseAllOfCircle(center, radius)){
            throw new GameActionException(CANT_SENSE_THAT,
                    "Target circle not completely within sensor range");
        }
    }

    @Override
    public boolean onTheMap(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanSenseLocation(loc);
        return gameWorld.getGameMap().onTheMap(loc);
    }

    @Override
    public boolean onTheMap(MapLocation center, float radius) throws GameActionException{
        assertNotNull(center);
        assertCanSenseAllOfCircle(center, radius);
        return gameWorld.getGameMap().onTheMap(center, radius);
    }

    @Override
    public boolean canSenseLocation(MapLocation loc) {
        assertNotNull(loc);
        return this.robot.canSenseLocation(loc);
    }

    @Override
    public boolean canSenseBulletLocation(MapLocation loc) {
        assertNotNull(loc);
        return this.robot.canSenseBulletLocation(loc);
    }

    @Override
    public boolean canSenseRadius(float radius) {
        return this.robot.canSenseRadius(radius);
    }

    @Override
    public boolean canSensePartOfCircle(MapLocation center, float radius){
        assertNotNull(center);
        return canSenseRadius(getLocation().distanceTo(center)-radius);
    }

    @Override
    public boolean canSenseAllOfCircle(MapLocation center, float radius){
        assertNotNull(center);
        return canSenseRadius(getLocation().distanceTo(center)+radius);
    }

    @Override
    public boolean isLocationOccupied(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanSenseLocation(loc);
        return !gameWorld.getObjectInfo().isEmpty(loc, 0);
    }

    @Override
    public boolean isLocationOccupiedByTree(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanSenseLocation(loc);
        return gameWorld.getObjectInfo().getTreeAtLocation(loc) != null;
    }

    @Override
    public boolean isLocationOccupiedByRobot(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanSenseLocation(loc);
        return gameWorld.getObjectInfo().getRobotAtLocation(loc) != null;
    }

    @Override
    public boolean isCircleOccupied(MapLocation center, float radius) throws GameActionException{
        assertNotNull(center);
        assertCanSenseAllOfCircle(center, radius);
        return !gameWorld.getObjectInfo().isEmpty(center, radius);
    }

    @Override
    public boolean isCircleOccupiedExceptByThisRobot(MapLocation center, float radius) throws GameActionException{
        assertNotNull(center);
        assertCanSenseAllOfCircle(center, radius);
        return !gameWorld.getObjectInfo().isEmptyExceptForRobot(center, radius, robot);
    }

    @Override
    public TreeInfo senseTreeAtLocation(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanSenseLocation(loc);
        InternalTree tree = gameWorld.getObjectInfo().getTreeAtLocation(loc);
        if(tree != null) {
            return tree.getTreeInfo();
        }
        return null;
    }

    @Override
    public RobotInfo senseRobotAtLocation(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanSenseLocation(loc);
        InternalRobot bot = gameWorld.getObjectInfo().getRobotAtLocation(loc);
        if(bot != null) {
            return bot.getRobotInfo();
        }
        return null;
    }

    @Override
    public boolean canSenseTree(int id) {
        if(!gameWorld.getObjectInfo().existsTree(id)){
            return false;
        }
        InternalTree tree = gameWorld.getObjectInfo().getTreeByID(id);
        return canSensePartOfCircle(tree.getLocation(), tree.getRadius());
    }

    @Override
    public boolean canSenseRobot(int id) {
        if(!gameWorld.getObjectInfo().existsRobot(id)){
            return false;
        }
        InternalRobot robot = gameWorld.getObjectInfo().getRobotByID(id);
        return canSensePartOfCircle(robot.getLocation(), robot.getType().bodyRadius);
    }

    @Override
    public boolean canSenseBullet(int id) {
        return gameWorld.getObjectInfo().existsBullet(id) &&
                canSenseBulletLocation(gameWorld.getObjectInfo().getBulletByID(id).getLocation());
    }

    @Override
    public TreeInfo senseTree(int id) throws GameActionException {
        if(!canSenseTree(id)){
            throw new GameActionException(CANT_SENSE_THAT,
                    "Can't sense given tree; It may not exist anymore");
        }
        return gameWorld.getObjectInfo().getTreeByID(id).getTreeInfo();
    }

    @Override
    public RobotInfo senseRobot(int id) throws GameActionException {
        if(!canSenseRobot(id)){
            throw new GameActionException(CANT_SENSE_THAT,
                    "Can't sense given robot; It may not exist anymore");
        }
        return gameWorld.getObjectInfo().getRobotByID(id).getRobotInfo();
    }

    @Override
    public BulletInfo senseBullet(int id) throws GameActionException {
        if(!canSenseBullet(id)){
            throw new GameActionException(CANT_SENSE_THAT,
                    "Can't sense given bullet; It may not exist anymore");
        }
        return gameWorld.getObjectInfo().getBulletByID(id).getBulletInfo();
    }

    @Override
    public RobotInfo[] senseNearbyRobots() {
        return senseNearbyRobots(-1);
    }

    @Override
    public RobotInfo[] senseNearbyRobots(float radius) {
        return senseNearbyRobots(radius, null);
    }

    @Override
    public RobotInfo[] senseNearbyRobots(float radius, Team team) {
        return senseNearbyRobots(getLocation(), radius, team);
    }

    @Override
    public RobotInfo[] senseNearbyRobots(MapLocation center, float radius, Team team) {
        assertNotNull(center);
        InternalRobot[] allSensedRobots = gameWorld.getObjectInfo().getAllRobotsWithinRadius(center,
                radius == -1 ? getType().sensorRadius : radius);
        List<RobotInfo> validSensedRobots = new ArrayList<>();
        for(InternalRobot sensedRobot : allSensedRobots){
            // check if this robot
            if(sensedRobot.equals(this.robot)){
                continue;
            }
            // check if can sense
            if(!canSensePartOfCircle(sensedRobot.getLocation(), sensedRobot.getType().bodyRadius)){
                continue;
            }
            // check if right team
            if(team != null && sensedRobot.getTeam() != team){
                continue;
            }

            validSensedRobots.add(sensedRobot.getRobotInfo());
        }
        return validSensedRobots.toArray(new RobotInfo[validSensedRobots.size()]);
    }

    @Override
    public TreeInfo[] senseNearbyTrees() {
        return senseNearbyTrees(-1);
    }

    @Override
    public TreeInfo[] senseNearbyTrees(float radius) {
        return senseNearbyTrees(radius, null);
    }

    @Override
    public TreeInfo[] senseNearbyTrees(float radius, Team team) {
        return senseNearbyTrees(getLocation(), radius, team);
    }

    @Override
    public TreeInfo[] senseNearbyTrees(MapLocation center, float radius, Team team) {
        assertNotNull(center);
        InternalTree[] allSensedTrees = gameWorld.getObjectInfo().getAllTreesWithinRadius(center,
                radius == -1 ? getType().sensorRadius : radius);
        List<TreeInfo> validSensedTrees = new ArrayList<>();
        for(InternalTree sensedTree : allSensedTrees){
            // check if can sense
            if(!canSensePartOfCircle(sensedTree.getLocation(), sensedTree.getRadius())){
                continue;
            }
            // check if right team
            if(team != null && sensedTree.getTeam() != team){
                continue;
            }

            validSensedTrees.add(sensedTree.getTreeInfo());
        }
        return validSensedTrees.toArray(new TreeInfo[validSensedTrees.size()]);
    }

    @Override
    public BulletInfo[] senseNearbyBullets() {
        return senseNearbyBullets(-1);
    }

    @Override
    public BulletInfo[] senseNearbyBullets(float radius) {
        return senseNearbyBullets(getLocation(), radius);
    }

    @Override
    public BulletInfo[] senseNearbyBullets(MapLocation center, float radius) {
        assertNotNull(center);
        InternalBullet[] allSensedBullets = gameWorld.getObjectInfo().getAllBulletsWithinRadius(center,
                radius == -1 ? getType().bulletSightRadius : radius);
        List<BulletInfo> validSensedBullets = new ArrayList<>();
        for(InternalBullet sensedBullet : allSensedBullets){
            // check if can sense
            if(!canSenseBulletLocation(sensedBullet.getLocation())){
                continue;
            }

            validSensedBullets.add(sensedBullet.getBulletInfo());
        }
        return validSensedBullets.toArray(new BulletInfo[validSensedBullets.size()]);
    }

    @Override
    public MapLocation[] senseBroadcastingRobotLocations() {
        List<MapLocation> validLocs = new ArrayList<>();
        for(RobotInfo robot : gameWorld.getPreviousBroadcasters()){
            validLocs.add(robot.location);
        }
        return validLocs.toArray(new MapLocation[validLocs.size()]);
    }

    // ***********************************
    // ****** READINESS METHODS **********
    // ***********************************

    private void assertMoveReady() throws GameActionException{
        if(hasMoved()){
            throw new GameActionException(NOT_ACTIVE,
                    "This robot has already moved this turn.");
        }
    }

    private void assertIsWeaponReady() throws GameActionException{
        if(hasAttacked()){
            throw new GameActionException(NOT_ACTIVE,
                    "This robot has already attacked this turn.");
        }
    }
    
    private void assertIsBuildReady() throws GameActionException{
        if(!isBuildReady()){
            throw new GameActionException(NOT_ACTIVE,
                    "This robot's build cooldown has not expired.");
        }
    }

    @Override
    public boolean hasMoved() {
        return getMoveCount() > 0;
    }

    @Override
    public boolean hasAttacked() {
        return getAttackCount() > 0;
    }
    
    @Override
    public boolean isBuildReady() {
        return this.robot.getBuildCooldownTurns() == 0;
    }

    @Override
    public int getBuildCooldownTurns() {
        return this.robot.getBuildCooldownTurns();
    }

    // ***********************************
    // ****** MOVEMENT METHODS ***********
    // ***********************************

  /*  private void assertIsPathable(MapLocation loc) throws GameActionException{
        if(!onTheMap(loc, getType().bodyRadius) ||
                isCircleOccupiedExceptByThisRobot(loc, getType().bodyRadius)){
            throw new GameActionException(CANT_MOVE_THERE,
                    "Cannot move to target location " + loc + ".");
        }
    }*/
    private void assertCanMove(MapLocation loc) throws GameActionException{
        if(!canMove(loc))
            throw new GameActionException(CANT_MOVE_THERE,
                    "Cannot move to the target location " + loc +".");
    }

    @Override
    public boolean canMove(Direction dir) {
        return canMove(dir, getType().strideRadius);
    }

    @Override
    public boolean canMove(Direction dir, float dist) {
        assertNotNull(dir);
        dist = Math.max(0, Math.min(dist, getType().strideRadius));
        MapLocation center = getLocation().add(dir, dist);
        return canMove(center);
    }
    
    @Override
    public boolean canMove(MapLocation center) {
        assertNotNull(center);
        float dist = getLocation().distanceTo(center);
        if(dist > getType().strideRadius) {
            Direction dir = getLocation().directionTo(center);
            center = getLocation().add(dir, getType().strideRadius);
        }
        boolean newLocationIsEmpty;
        if(getType() != RobotType.TANK && getType() != RobotType.SCOUT) {
            newLocationIsEmpty = gameWorld.getObjectInfo().isEmptyExceptForRobot(center, getType().bodyRadius, robot);
        } else { // Tanks have special condition due to body attack, Scouts can just go over trees
            newLocationIsEmpty = gameWorld.getObjectInfo().noRobotsExceptForRobot(center, getType().bodyRadius, robot);
        }
        return gameWorld.getGameMap().onTheMap(center, getType().bodyRadius) &&
                newLocationIsEmpty;
    }

    @Override
    public void move(Direction dir) throws GameActionException {
        move(dir, getType().strideRadius);
    }

    @Override
    public void move(Direction dir, float dist) throws GameActionException {
        assertNotNull(dir);
        assertMoveReady();
        dist = Math.max(0, Math.min(dist, getType().strideRadius));
        MapLocation center = getLocation().add(dir, dist);
        move(center);
    }
    
    @Override
    public void move(MapLocation center) throws GameActionException {
        assertNotNull(center);
        assertMoveReady();
        float dist = getLocation().distanceTo(center);
        if(dist > getType().strideRadius) {
            Direction dir = getLocation().directionTo(center);
            center = getLocation().add(dir, getType().strideRadius);
        }
        assertCanMove(center);
        
        this.robot.incrementMoveCount();
        if(getType() == RobotType.TANK) { // If Tank, see if can actually move, as opposed to just body attack
            InternalTree[] trees = gameWorld.getObjectInfo().getAllTreesWithinRadius(center, RobotType.TANK.bodyRadius);
            if(trees.length > 0) { // Body attack will happen
                
                // Find closest Tree
                InternalTree closestTree = null;
                float closestDist = Float.MAX_VALUE;
                for(InternalTree tree : trees) {
                    float treeDist = tree.getLocation().distanceTo(robot.getLocation());
                    if(treeDist < closestDist) {
                        closestDist = treeDist;
                        closestTree = tree;
                    }
                }
                
                // Damage the closest tree
                closestTree.damageTree(GameConstants.TANK_BODY_DAMAGE, getTeam(), false);
            
                // Now that damage has been done, refresh list of trees to see if it is still there
                trees = gameWorld.getObjectInfo().getAllTreesWithinRadius(center, RobotType.TANK.bodyRadius);
                if(trees.length > 0) // If something still obstructs the movement, don't actually move
                    return;
            }
        }
        this.robot.setLocation(center);

        gameWorld.getMatchMaker().addMoved(getID(), getLocation());
    }

    // ***********************************
    // ****** ATTACK METHODS *************
    // ***********************************

    // TODO: Make this a player method
    private boolean haveBulletCosts(float cost){
        return gameWorld.getTeamInfo().getBulletSupply(getTeam()) >= cost;
    }

    private void assertHaveBulletCosts(float cost) throws GameActionException{
        if(!haveBulletCosts(cost)){
            throw new GameActionException(NOT_ENOUGH_RESOURCE,
                    "Not sufficient funds in bullet supply");
        }
    }

    private void assertNonNegative(float cost) throws GameActionException{
        if(cost < 0) {
            throw new GameActionException(CANT_DO_THAT,
                    "Can't purchase negative victory points");
        }
    }

    /**
     * Fires specified bullet spread.  Assumes odd number of bullets to fire.
     *
     * @param centerDir direction the center bullet should travel
     * @param toFire number of bullets to fire.
     * @param spreadDegrees the spread in degrees at which the bullets should fire.
     */
    private void fireBulletSpread(Direction centerDir, int toFire, float spreadDegrees){
        byte actionType;
        switch (toFire){
            case 5:
                actionType = Action.FIRE_PENTAD;
                break;
            case 3:
                actionType = Action.FIRE_TRIAD;
                break;
            default:
                actionType = Action.FIRE;
        }

        int bulletsPerSide = (toFire - 1) / 2;

        // Fire center bullet
        int bulletID = gameWorld.spawnBullet(getTeam(), getType().bulletSpeed, getType().attackPower,
                getLocation().add(centerDir, getType().bodyRadius + GameConstants.BULLET_SPAWN_OFFSET), centerDir, this.robot);
        gameWorld.getMatchMaker().addAction(getID(), actionType, bulletID);

        // Fire side bullets
        for(int i = 1; i <= bulletsPerSide; i++){
            // Fire left bullet
            Direction dirLeft = centerDir.rotateLeftDegrees(i * spreadDegrees);
            bulletID = gameWorld.spawnBullet(getTeam(), getType().bulletSpeed, getType().attackPower,
                    getLocation().add(dirLeft, getType().bodyRadius + GameConstants.BULLET_SPAWN_OFFSET), dirLeft, this.robot);
            gameWorld.getMatchMaker().addAction(getID(), actionType, bulletID);

            // Fire right bullet
            Direction dirRight = centerDir.rotateRightDegrees(i * spreadDegrees);
            bulletID = gameWorld.spawnBullet(getTeam(), getType().bulletSpeed, getType().attackPower,
                    getLocation().add(dirRight, getType().bodyRadius + GameConstants.BULLET_SPAWN_OFFSET), dirRight, this.robot);
            gameWorld.getMatchMaker().addAction(getID(), actionType, bulletID);
        }
    }

    @Override
    public boolean canStrike() {
        boolean correctType = getType() == RobotType.LUMBERJACK;

        return correctType && !hasAttacked();
    }

    @Override
    public void strike() throws GameActionException {
        if(getType() != RobotType.LUMBERJACK){
            throw new GameActionException(CANT_DO_THAT,
                    "Only lumberjacks can strike");
        }
        assertIsWeaponReady();

        this.robot.incrementAttackCount(); // Striking counts as attack.

        // Hit adjacent robots
        for(InternalRobot hitRobot :
                gameWorld.getObjectInfo().getAllRobotsWithinRadius(getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS)){
            if(hitRobot.equals(this.robot)){
                continue;
            }
            hitRobot.damageRobot(getType().attackPower);
        }
        // Hit adjacent trees
        for(InternalTree hitTree :
                gameWorld.getObjectInfo().getAllTreesWithinRadius(getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS)){
            hitTree.damageTree(getType().attackPower, getTeam(), false);
        }

        gameWorld.getMatchMaker().addAction(getID(), Action.LUMBERJACK_STRIKE, -1);
    }

    @Override
    public boolean canFireSingleShot() {
        boolean correctType = getType() != RobotType.ARCHON && getType() != RobotType.GARDENER &&
                getType() != RobotType.LUMBERJACK;
        return correctType && haveBulletCosts(GameConstants.SINGLE_SHOT_COST) && !hasAttacked();
    }

    @Override
    public boolean canFireTriadShot() {
        boolean correctType = getType() != RobotType.ARCHON && getType() != RobotType.GARDENER &&
                getType() != RobotType.LUMBERJACK && getType() != RobotType.SCOUT;
        return correctType && haveBulletCosts(GameConstants.TRIAD_SHOT_COST) && !hasAttacked();
    }

    @Override
    public boolean canFirePentadShot() {
        boolean correctType = getType() != RobotType.ARCHON && getType() != RobotType.GARDENER &&
                getType() != RobotType.LUMBERJACK && getType() != RobotType.SCOUT;
        return correctType && haveBulletCosts(GameConstants.PENTAD_SHOT_COST) && !hasAttacked();
    }

    @Override
    public void fireSingleShot(Direction dir) throws GameActionException {
        assertNotNull(dir);
        assertIsWeaponReady();
        if(!canFireSingleShot()){
            throw new GameActionException(CANT_DO_THAT,
                    "This robot cannot fire a single shot possibly due to wrong type or " +
                            "insufficient funds");
        }

        this.robot.incrementAttackCount();

        gameWorld.getTeamInfo().adjustBulletSupply(getTeam(), -GameConstants.SINGLE_SHOT_COST);
        fireBulletSpread(dir, 1, 0);
    }

    @Override
    public void fireTriadShot(Direction dir) throws GameActionException {
        assertNotNull(dir);
        assertIsWeaponReady();
        if(!canFireTriadShot()){
            throw new GameActionException(CANT_DO_THAT,
                    "This robot cannot fire a triad shot possibly due to wrong type or " +
                            "insufficient funds");
        }

        this.robot.incrementAttackCount();

        gameWorld.getTeamInfo().adjustBulletSupply(getTeam(), -GameConstants.TRIAD_SHOT_COST);
        fireBulletSpread(dir, 3, GameConstants.TRIAD_SPREAD_DEGREES);
    }

    @Override
    public void firePentadShot(Direction dir) throws GameActionException {
        assertNotNull(dir);
        assertIsWeaponReady();
        if(!canFirePentadShot()){
            throw new GameActionException(CANT_DO_THAT,
                    "This robot cannot fire a pentad shot possibly due to wrong type or " +
                            "insufficient funds");
        }

        this.robot.incrementAttackCount();

        gameWorld.getTeamInfo().adjustBulletSupply(getTeam(), -GameConstants.PENTAD_SHOT_COST);
        fireBulletSpread(dir, 5, GameConstants.PENTAD_SPREAD_DEGREES);
    }

    // ***********************************
    // ****** TREE METHODS ***************
    // ***********************************

    private boolean canInteractWithLocation(MapLocation loc){
        assertNotNull(loc);
        return this.robot.canInteractWithLocation(loc);
    }

    private boolean canInteractWithCircle(MapLocation center, float radius){
        assertNotNull(center);
        return this.robot.canInteractWithCircle(center,radius);
    }

    private void assertCanWater() throws GameActionException{
        if(!canWater()){
            throw new GameActionException(CANT_DO_THAT,
                    "Gardeners can only water once per turn");
        }
    }

    private void assertCanShake() throws GameActionException{
        if(!canShake()){
            throw new GameActionException(CANT_DO_THAT,
                    "Robots can only shake one tree per turn");
        }
    }

    private void assertCanInteractWithTree(MapLocation treeLoc) throws GameActionException{
        if(!canInteractWithTree(treeLoc)){
            throw new GameActionException(CANT_DO_THAT,
                    "Can't interact with a tree that doesn't exist or is outside" +
                            " this robot's stride.");
        }
    }

    private void assertCanInteractWithTree(int treeID) throws GameActionException{
        if(!canInteractWithTree(treeID)){
            throw new GameActionException(CANT_DO_THAT,
                    "Can't interact with a tree that doesn't exist or is outside" +
                            " this robot's stride.");
        }
    }
    
    private void assertOwnedTree(InternalTree tree) throws GameActionException {
        if(tree.getTeam().equals(Team.NEUTRAL)) {
            throw new GameActionException(CANT_DO_THAT,
                    "Can't water a neutral tree.");
        }
    }

    @Override
    public boolean canChop(MapLocation loc) {
        boolean correctType = (getType() == RobotType.LUMBERJACK);
        boolean canInteract = canInteractWithTree(loc);

        return correctType && canInteract && !hasAttacked();
    }

    @Override
    public boolean canChop(int id) {
        boolean correctType = (getType() == RobotType.LUMBERJACK);
        boolean canInteract = canInteractWithTree(id);

        return correctType && canInteract && !hasAttacked();
    }

    @Override
    public void chop(MapLocation loc) throws GameActionException {
        if(getType() != RobotType.LUMBERJACK){
            throw new GameActionException(CANT_DO_THAT,
                    "Only lumberjacks can chop");
        }
        assertNotNull(loc);
        assertIsWeaponReady(); // Chop counts as attack
        assertCanInteractWithTree(loc);
        InternalTree tree = gameWorld.getObjectInfo().getTreeAtLocation(loc);
        chopTree(tree);
    }

    @Override
    public void chop(int id) throws GameActionException {
        if(getType() != RobotType.LUMBERJACK){
            throw new GameActionException(CANT_DO_THAT,
                    "Only lumberjacks can chop");
        }
        assertIsWeaponReady();  // Chop counts as attack
        assertCanInteractWithTree(id);
        InternalTree tree = gameWorld.getObjectInfo().getTreeByID(id);
        chopTree(tree);
    }

    private void chopTree(InternalTree tree){
        this.robot.incrementAttackCount(); // Chopping counts as attack

        float chopDamage = GameConstants.LUMBERJACK_CHOP_DAMAGE;

        tree.damageTree(chopDamage, getTeam(), true);

        gameWorld.getMatchMaker().addAction(getID(), Action.CHOP, -1);
    }

    @Override
    public boolean canShake(MapLocation loc) {
        return canInteractWithTree(loc) && canShake();
    }

    @Override
    public boolean canShake(int id) {
        return canInteractWithTree(id) && canShake();
    }

    @Override
    public void shake(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanShake();
        assertCanInteractWithTree(loc);
        InternalTree tree = gameWorld.getObjectInfo().getTreeAtLocation(loc);
        shakeTree(tree);
    }

    @Override
    public void shake(int id) throws GameActionException {
        assertCanShake();
        assertCanInteractWithTree(id);
        InternalTree tree = gameWorld.getObjectInfo().getTreeByID(id);
        shakeTree(tree);
    }

    private void shakeTree(InternalTree tree){
        this.robot.incrementShakeCount();
        gameWorld.getTeamInfo().adjustBulletSupply(getTeam(), tree.getContainedBullets());
        tree.resetContainedBullets();

        gameWorld.getMatchMaker().addAction(getID(), Action.SHAKE_TREE, tree.getID());
    }

    @Override
    public boolean canWater(MapLocation loc) {
        assertNotNull(loc);
        InternalTree tree = gameWorld.getObjectInfo().getTreeAtLocation(loc);
        boolean notNeutralTree = (tree != null && tree.getTeam() != Team.NEUTRAL);
        return canWater() && canInteractWithTree(loc) && notNeutralTree;
    }

    @Override
    public boolean canWater(int id) {
        InternalTree tree = gameWorld.getObjectInfo().getTreeByID(id);
        boolean notNeutralTree = (tree != null && tree.getTeam() != Team.NEUTRAL);
        return canWater() && canInteractWithTree(id) && notNeutralTree;
    }

    @Override
    public void water(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanWater();
        assertCanInteractWithTree(loc);
        InternalTree tree = gameWorld.getObjectInfo().getTreeAtLocation(loc);
        assertOwnedTree(tree);
        waterTree(tree);
    }

    @Override
    public void water(int id) throws GameActionException {
        assertCanWater();
        assertCanInteractWithTree(id);
        InternalTree tree = gameWorld.getObjectInfo().getTreeByID(id);
        assertOwnedTree(tree);
        waterTree(tree);
    }

    private void waterTree(InternalTree tree){
        this.robot.incrementWaterCount();
        tree.waterTree();

        gameWorld.getMatchMaker().addAction(getID(), Action.WATER_TREE, tree.getID());
    }

    @Override
    public boolean canWater(){
        boolean correctType = getType() == RobotType.GARDENER;
        return correctType && this.robot.getWaterCount() < 1;
    }

    @Override
    public boolean canShake(){
        return this.robot.getShakeCount() < 1;
    }

    @Override
    public boolean canInteractWithTree(MapLocation loc){
        assertNotNull(loc);
        InternalTree tree = gameWorld.getObjectInfo().getTreeAtLocation(loc);
        return tree != null &&
                canInteractWithCircle(tree.getLocation(), tree.getRadius());
    }

    @Override
    public boolean canInteractWithTree(int id){
        InternalTree tree = gameWorld.getObjectInfo().getTreeByID(id);
        return tree != null &&
                canInteractWithCircle(tree.getLocation(), tree.getRadius());
    }

    // ***********************************
    // ****** SIGNALING METHODS **********
    // ***********************************

    private void assertValidChannel(int channel) throws GameActionException{
        if(channel < 0 || channel >= GameConstants.BROADCAST_MAX_CHANNELS){
            throw new GameActionException(CANT_DO_THAT,
                    "Broadcasting channel invalid");
        }
    }

    @Override
    public void broadcast(int channel, int data) throws GameActionException {
        assertValidChannel(channel);
        gameWorld.addBroadcaster(this.robot.getRobotInfo());
        gameWorld.getTeamInfo().broadcast(getTeam(), channel, data);
    }

    @Override
    public int readBroadcast(int channel) throws GameActionException {
        assertValidChannel(channel);
        return gameWorld.getTeamInfo().readBroadcast(getTeam(), channel);
    }

    @Override
    public void broadcastBoolean(int channel, boolean data) throws GameActionException {
        broadcast(channel, data ? 1 : 0);
    }

    @Override
    public boolean readBroadcastBoolean(int channel) throws GameActionException {
        return readBroadcast(channel) != 0;
    }

    @Override
    public void broadcastInt(int channel, int data) throws GameActionException {
        broadcast(channel, data);
    }

    @Override
    public int readBroadcastInt(int channel) throws GameActionException {
        return readBroadcast(channel);
    }

    @Override
    public void broadcastFloat(int channel, float data) throws GameActionException {
        broadcast(channel, Float.floatToRawIntBits(data));
    }

    @Override
    public float readBroadcastFloat(int channel) throws GameActionException {
        return Float.intBitsToFloat(readBroadcast(channel));
    }

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************

    private void assertCanBuildRobot(RobotType type, Direction dir) throws GameActionException{
        if(!canBuildRobot(type, dir)){
            throw new GameActionException(CANT_DO_THAT,
                    "Can't build desired robot in given direction, possibly due to " +
                            "insufficient bullet supply, this robot can't build, " +
                            "cooldown not expired, or the spawn location is occupied");
        }
    }

    private void assertCanBuildTree(Direction dir) throws GameActionException{
        if(!canPlantTree(dir)){
            throw new GameActionException(CANT_DO_THAT,
                    "Can't build a bullet tree in given direction, possibly due to " +
                            "insufficient bullet supply, this robot can't build, " +
                            "cooldown not expired, or the spawn location is occupied");
        }
    }

    @Override
    public boolean hasRobotBuildRequirements(RobotType type) {
        assertNotNull(type);
        boolean hasBulletCosts = haveBulletCosts(type.bulletCost);
        boolean validBuilder = getType() == type.spawnSource;
        return hasBulletCosts && validBuilder;
    }

    @Override
    public boolean hasTreeBuildRequirements() {
        boolean hasBulletCosts = haveBulletCosts(GameConstants.BULLET_TREE_COST);
        boolean validBuilder = getType() == RobotType.GARDENER;
        return hasBulletCosts && validBuilder;
    }

    @Override
    public boolean canBuildRobot(RobotType type, Direction dir) {
        assertNotNull(type);
        assertNotNull(dir);
        boolean hasBuildRequirements = hasRobotBuildRequirements(type);
        float spawnDist = getType().bodyRadius +
                GameConstants.GENERAL_SPAWN_OFFSET +
                type.bodyRadius;
        MapLocation spawnLoc = getLocation().add(dir, spawnDist);
        boolean isClear = gameWorld.getGameMap().onTheMap(spawnLoc, type.bodyRadius) &&
                gameWorld.getObjectInfo().isEmpty(spawnLoc, type.bodyRadius);
        boolean cooldownExpired = isBuildReady();
        return hasBuildRequirements && isClear && cooldownExpired;
    }

    @Override
    public boolean canPlantTree(Direction dir) {
        assertNotNull(dir);
        boolean hasBuildRequirements = hasTreeBuildRequirements();
        float spawnDist = getType().bodyRadius +
                GameConstants.GENERAL_SPAWN_OFFSET +
                GameConstants.BULLET_TREE_RADIUS;
        MapLocation spawnLoc = getLocation().add(dir, spawnDist);
        boolean isClear =
                gameWorld.getGameMap().onTheMap(spawnLoc, GameConstants.BULLET_TREE_RADIUS) &&
                gameWorld.getObjectInfo().isEmpty(spawnLoc, GameConstants.BULLET_TREE_RADIUS);
        boolean cooldownExpired = isBuildReady();
        return hasBuildRequirements && isClear && cooldownExpired;
    }
    
    @Override
    public boolean canHireGardener(Direction dir) {
        return canBuildRobot(RobotType.GARDENER,dir);
    }

    @Override
    public void hireGardener(Direction dir) throws GameActionException {
        assertNotNull(dir);
        assertCanBuildRobot(RobotType.GARDENER, dir);

        this.robot.setBuildCooldownTurns(RobotType.GARDENER.buildCooldownTurns);
        
        gameWorld.getTeamInfo().adjustBulletSupply(getTeam(), -RobotType.GARDENER.bulletCost);

        float spawnDist = getType().bodyRadius +
                GameConstants.GENERAL_SPAWN_OFFSET +
                RobotType.GARDENER.bodyRadius;
        MapLocation spawnLoc = getLocation().add(dir, spawnDist);

        int robotID = gameWorld.spawnRobot(RobotType.GARDENER, spawnLoc, getTeam());

        gameWorld.getMatchMaker().addAction(getID(), Action.SPAWN_UNIT, robotID);
    }

    @Override
    public void buildRobot(RobotType type, Direction dir) throws GameActionException {
        assertNotNull(dir);
        assertCanBuildRobot(type, dir);

        this.robot.setBuildCooldownTurns(type.buildCooldownTurns);
        
        gameWorld.getTeamInfo().adjustBulletSupply(getTeam(), -type.bulletCost);

        float spawnDist = getType().bodyRadius +
                GameConstants.GENERAL_SPAWN_OFFSET +
                type.bodyRadius;
        MapLocation spawnLoc = getLocation().add(dir, spawnDist);

        int robotID = gameWorld.spawnRobot(type, spawnLoc, getTeam());

        gameWorld.getMatchMaker().addAction(getID(), Action.SPAWN_UNIT, robotID);
    }

    @Override
    public void plantTree(Direction dir) throws GameActionException {
        assertNotNull(dir);
        assertIsBuildReady();
        assertCanBuildTree(dir);

        this.robot.setBuildCooldownTurns(GameConstants.BULLET_TREE_CONSTRUCTION_COOLDOWN);
        
        gameWorld.getTeamInfo().adjustBulletSupply(getTeam(), -GameConstants.BULLET_TREE_COST);

        float spawnDist = getType().bodyRadius +
                GameConstants.GENERAL_SPAWN_OFFSET +
                GameConstants.BULLET_TREE_RADIUS;
        MapLocation spawnLoc = getLocation().add(dir, spawnDist);

        int treeID = gameWorld.spawnTree(getTeam(), GameConstants.BULLET_TREE_RADIUS, spawnLoc,
                0, null);

        gameWorld.getMatchMaker().addAction(getID(), Action.PLANT_TREE, treeID);
    }

    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************

    @Override
    public float getVictoryPointCost() {
        return GameConstants.VP_BASE_COST + GameConstants.VP_INCREASE_PER_ROUND * getRoundNum();
    }

    @Override
    public void donate(float bullets) throws GameActionException{
        assertNonNegative(bullets);
        assertHaveBulletCosts(bullets);
        int gainedVictorPoints = (int)Math.floor(bullets / getVictoryPointCost());
        gameWorld.getTeamInfo().adjustBulletSupply(getTeam(), -bullets);
        gameWorld.getTeamInfo().adjustVictoryPoints(getTeam(), gainedVictorPoints);
        gameWorld.setWinnerIfVictoryPoints();
    }

    @Override
    public void disintegrate(){
        throw new RobotDeathException();
    }

    @Override
    public void resign(){
        gameWorld.getObjectInfo().eachRobot((robot) -> {
            if(robot.getTeam() == getTeam()){
                gameWorld.destroyRobot(robot.getID());
            }
            return true;
        });
    }

    // ***********************************
    // **** INDICATOR STRING METHODS *****
    // ***********************************

    @Override
    public void setIndicatorDot(MapLocation loc, int red, int green, int blue) {
        assertNotNull(loc);
        gameWorld.getMatchMaker().addIndicatorDot(getID(), loc, red, green, blue);
    }

    @Override
    public void setIndicatorLine(MapLocation startLoc, MapLocation endLoc, int red, int green, int blue) {
        assertNotNull(startLoc);
        assertNotNull(endLoc);
        gameWorld.getMatchMaker().addIndicatorLine(getID(), startLoc, endLoc, red, green, blue);
    }

    // ***********************************
    // ******** TEAM MEMORY **************
    // ***********************************

    @Override
    public void setTeamMemory(int index, long value) {
        gameWorld.getTeamInfo().setTeamMemory(robot.getTeam(), index, value);
    }

    @Override
    public void setTeamMemory(int index, long value, long mask) {
        gameWorld.getTeamInfo().setTeamMemory(robot.getTeam(), index, value, mask);
    }

    @Override
    public long[] getTeamMemory() {
        long[] arr = gameWorld.getTeamInfo().getOldTeamMemory()[robot.getTeam().ordinal()];
        return Arrays.copyOf(arr, arr.length);
    }

    // ***********************************
    // ******** DEBUG METHODS ************
    // ***********************************

    @Override
    public long getControlBits() {
        return robot.getControlBits();
    }
}
