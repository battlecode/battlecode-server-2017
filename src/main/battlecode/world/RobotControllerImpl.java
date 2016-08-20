package battlecode.world;

import battlecode.common.*;
import static battlecode.common.GameActionExceptionType.*;
import battlecode.instrumenter.RobotDeathException;
import battlecode.world.signal.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
public final class RobotControllerImpl implements RobotController {

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

    /**
     * @param loc the location to assert we can sense
     * @throws GameActionException if we can't sense the location
     */
    private void assertCanSenseLocation(MapLocation loc) throws GameActionException {
        if (!canSenseLocation(loc)) {
            throw new GameActionException(OUT_OF_RANGE,
                    loc + " is not within this robot's sensor range.");
        }
    }

    /**
     * @param center the center of the circle to assert we can sense
     * @param radius the radius of the circle to assert we can sense
     * @throws GameActionException if we can't sense the circle
     */
    private void assertCanSenseCircle(MapLocation center, float radius) throws GameActionException {
        if (!canSenseCircle(center, radius)){
            throw new GameActionException(OUT_OF_RANGE,
                    "The circle target is not within this robot's sensor range.")
        }
    }
    
    private void assertTreeExists(int id) throws GameActionException{
        if(!gameWorld.getObjectInfo().existsTree(id)){
            throw new GameActionException(CANT_SENSE_THAT,
                    id + " does not exist as a tree");
        }
    }

    private void assertRobotExists(int id) throws GameActionException{
        if(!gameWorld.getObjectInfo().existsRobot(id)){
            throw new GameActionException(CANT_SENSE_THAT,
                    id + " does not exist as a robot");
        }
    }

    private void assertBulletExists(int id) throws GameActionException{
        if(!gameWorld.getObjectInfo().existsBullet(id)){
            throw new GameActionException(CANT_SENSE_THAT,
                    id + " does not exist as a bullet");
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
        return gameWorld.getTeamInfo().getBulletSupply(this.robot.getTeam());
    }

    @Override
    public int getTeamVictoryPoints(){
        return gameWorld.getTeamInfo().getVictoryPoints(this.robot.getTeam());
    }

    @Override
    public int getOpponentVictoryPoints(){
        return gameWorld.getTeamInfo().getVictoryPoints(this.robot.getTeam().opponent());
    }

    @Override
    public int getRobotCount(){
        return gameWorld.getObjectInfo().getRobotCount(this.robot.getTeam());
    }

    @Override
    public int getTreeCount(){
        return gameWorld.getObjectInfo().getTreeCount(this.robot.getTeam());
    }

    @Override
    public MapLocation[] getInitialArchonLocations(Team t){
        if (t == Team.NEUTRAL) {
            return new MapLocation[0];
        } else {
            GameMap.InitialRobotInfo[] initialRobots = gameWorld.getGameMap()
                    .getInitialRobots();
            ArrayList<MapLocation> archonLocs = new ArrayList<>();
            for (GameMap.InitialRobotInfo initial : initialRobots) {
                if (initial.type == RobotType.ARCHON && initial.team == t) {
                    archonLocs.add(initial.getLocation(gameWorld.getGameMap()
                            .getOrigin()));
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
    public float getCoreDelay(){
        return this.robot.getCoreDelay();
    }

    @Override
    public float getWeaponDelay(){
        return this.robot.getWeaponDelay();
    }

    // ***********************************
    // ****** GENERAL SENSOR METHODS *****
    // ***********************************

    @Override
    public boolean onTheMap(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanSenseLocation(loc);
        return gameWorld.getGameMap().onTheMap(loc);
    }

    @Override
    public boolean canSenseLocation(MapLocation loc) {
        assertNotNull(loc);
        return this.robot.canSenseLocation(loc);
    }

    @Override
    public boolean canSenseCircle(MapLocation center, float radius){
        assertNotNull(center);
        MapLocation closestPointOnCircle = center.add(center.directionTo(this.robot.getLocation()), radius);
        return canSenseLocation(closestPointOnCircle);
    }

    @Override
    public boolean isLocationOccupied(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanSenseLocation(loc);
        return !gameWorld.getObjectInfo().isEmpty(loc, 0);
    }

    @Override
    public TreeInfo senseTreeAtLocation(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanSenseLocation(loc);
        return gameWorld.getObjectInfo().getTreeAtLocation(loc).getTreeInfo();
    }

    @Override
    public RobotInfo senseRobotAtLocation(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanSenseLocation(loc);
        return gameWorld.getObjectInfo().getRobotAtLocation(loc).getRobotInfo();
    }

    @Override
    public boolean canSenseTree(int id) {
        if(!gameWorld.getObjectInfo().existsTree(id)){
            return false;
        }
        InternalTree tree = gameWorld.getObjectInfo().getTreeByID(id);
        return canSenseCircle(tree.getLocation(), tree.getRadius());
    }

    @Override
    public boolean canSenseRobot(int id) {
        if(!gameWorld.getObjectInfo().existsRobot(id)){
            return false;
        }
        InternalRobot robot = gameWorld.getObjectInfo().getRobotByID(id);
        return canSenseCircle(robot.getLocation(), robot.getType().bodyRadius);
    }

    @Override
    public boolean canSenseBullet(int id) {
        return gameWorld.getObjectInfo().existsBullet(id) &&
                canSenseLocation(gameWorld.getObjectInfo().getBulletByID(id).getLocation());
    }

    @Override
    public TreeInfo senseTree(int id) throws GameActionException {
        assertTreeExists(id);
        InternalTree tree = gameWorld.getObjectInfo().getTreeByID(id);
        assertCanSenseCircle(tree.getLocation(), tree.getRadius());
        return tree.getTreeInfo();
    }

    @Override
    public RobotInfo senseRobot(int id) throws GameActionException {
        assertRobotExists(id);
        InternalRobot robot = gameWorld.getObjectInfo().getRobotByID(id);
        assertCanSenseCircle(robot.getLocation(), robot.getType().bodyRadius);
        return robot.getRobotInfo();
    }

    @Override
    public BulletInfo senseBullet(int id) throws GameActionException {
        assertBulletExists(id);
        InternalBullet bullet = gameWorld.getObjectInfo().getBulletByID(id);
        assertCanSenseLocation(bullet.getLocation());
        return bullet.getBulletInfo();
    }

    @Override
    public RobotInfo[] senseNearbyRobots() {
        return senseNearbyRobots(-1);
    }

    @Override
    public RobotInfo[] senseNearbyRobots(float radius) {
        return senseNearbyRobots(radius == -1 ? this.robot.getType().sensorRadius : radius, null);
    }

    @Override
    public RobotInfo[] senseNearbyRobots(float radius, Team team) {
        return senseNearbyRobots(this.robot.getLocation(),
                radius == -1 ? this.robot.getType().sensorRadius : radius, team);
    }

    @Override
    public RobotInfo[] senseNearbyRobots(MapLocation center, float radius, Team team) {
        InternalRobot[] allSensedRobots = gameWorld.getObjectInfo().getAllRobotsWithinRadius(center,
                radius == -1 ? this.robot.getType().sensorRadius : radius);
        List<RobotInfo> validSensedRobots = new ArrayList<>();
        for(InternalRobot sensedRobot : allSensedRobots){
            // check if this robot
            if(sensedRobot.equals(this.robot)){
                continue;
            }
            // check if can sense
            if(!canSenseCircle(sensedRobot.getLocation(), sensedRobot.getType().bodyRadius)){
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
        return senseNearbyTrees(radius == -1 ? this.robot.getType().sensorRadius : radius, null);
    }

    @Override
    public TreeInfo[] senseNearbyTrees(float radius, Team team) {
        return senseNearbyTrees(this.robot.getLocation(),
                radius == -1 ? this.robot.getType().sensorRadius : radius, team);
    }

    @Override
    public TreeInfo[] senseNearbyTrees(MapLocation center, float radius, Team team) {
        InternalTree[] allSensedTrees = gameWorld.getObjectInfo().getAllTreesWithinRadius(center,
                radius == -1 ? this.robot.getType().sensorRadius : radius);
        List<TreeInfo> validSensedTrees = new ArrayList<>();
        for(InternalTree sensedTree : allSensedTrees){
            // check if can sense
            if(!canSenseCircle(sensedTree.getLocation(), sensedTree.getRadius())){
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
        return senseNearbyBullets(this.robot.getLocation(),
                radius == -1 ? this.robot.getType().sensorRadius : radius);
    }

    @Override
    public BulletInfo[] senseNearbyBullets(MapLocation center, float radius) {
        InternalBullet[] allSensedBullets = gameWorld.getObjectInfo().getAllBulletsWithinRadius(center,
                radius == -1 ? this.robot.getType().sensorRadius : radius);
        List<BulletInfo> validSensedBullets = new ArrayList<>();
        for(InternalBullet sensedBullet : allSensedBullets){
            // check if can sense
            if(!canSenseLocation(sensedBullet.getLocation()){
                continue;
            }

            validSensedBullets.add(sensedBullet.getBulletInfo());
        }
        return validSensedBullets.toArray(new BulletInfo[validSensedBullets.size()]);
    }
}
