package battlecode.world;

import static battlecode.common.GameActionExceptionType.NOT_ACTIVE;
import static battlecode.common.GameActionExceptionType.CANT_DO_THAT_BRO;
import static battlecode.common.GameActionExceptionType.CANT_SENSE_THAT;
import static battlecode.common.GameActionExceptionType.MISSING_DEPENDENCY;
import static battlecode.common.GameActionExceptionType.NOT_ENOUGH_RESOURCE;
import static battlecode.common.GameActionExceptionType.NO_ROBOT_THERE;
import static battlecode.common.GameActionExceptionType.OUT_OF_RANGE;

import java.util.*;
import java.util.Map.Entry;

import battlecode.common.CommanderSkillType;
import battlecode.common.DependencyProgress;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TerrainTile;
import battlecode.engine.GenericController;
import battlecode.engine.instrumenter.RobotDeathException;
import battlecode.engine.instrumenter.RobotMonitor;
import battlecode.server.Config;
import battlecode.world.signal.AttackSignal;
import battlecode.world.signal.BuildSignal;
import battlecode.world.signal.CastSignal;
import battlecode.world.signal.IndicatorDotSignal;
import battlecode.world.signal.IndicatorLineSignal;
import battlecode.world.signal.IndicatorStringSignal;
import battlecode.world.signal.MatchObservationSignal;
import battlecode.world.signal.MineSignal;
import battlecode.world.signal.MovementSignal;
import battlecode.world.signal.SpawnSignal;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/*
 TODO:
 - tweak player
 -specs & software page?
 - performance tips
 - figure out why it's not deterministic

 - optimize
 - non-constant java.util costs
 - player's navigation class, and specs about it
 - fix execute action hack w/ robotmonitor ??
 - fix playerfactory/spawnsignal hack??
 - better suicide() ??
 - pare down GW, GWviewer methods; add engine.getallsignals?
 */
public final class RobotControllerImpl implements RobotController,
        GenericController {
    private GameWorld gameWorld;
    private InternalRobot robot;

    public RobotControllerImpl(GameWorld gw, InternalRobot r) {
        gameWorld = gw;
        robot = r;
    }

    // *********************************
    // ******** INTERNAL METHODS *******
    // *********************************

    public int hashCode() {
        return robot.getID();
    }

    private static void assertNotNull(Object o) {
        if (o == null)
            throw new NullPointerException("Argument has an invalid null value");
    }

    // *********************************
    // ****** GLOBAL QUERY METHODS *****
    // *********************************
    public int getRoundLimit() {
        return robot.myGameWorld.getGameMap().getMaxRounds();
    }

    public int getMapWidth() {
        return robot.myGameWorld.getGameMap().getWidth();
    }

    public int getMapHeight() {
        return robot.myGameWorld.getGameMap().getHeight();
    }

    public double getTeamOre() {
        return gameWorld.resources(getTeam());
    }

    public void assertHaveResource(double amount) throws GameActionException {
        if (amount > gameWorld.resources(getTeam())) {
            throw new GameActionException(NOT_ENOUGH_RESOURCE,
                    "You do not have enough ORE to do that.");
        }
    }

    // *********************************
    // ****** UNIT QUERY METHODS *******
    // *********************************
    public InternalRobot getRobot() {
        return robot;
    }

    public int getID() {
        return robot.getID();
    }

    public Team getTeam() {
        return robot.getTeam();
    }

    public RobotType getType() {
        return robot.type;
    }

    public MapLocation getLocation() {
        return robot.getLocation();
    }

    public double getCoreDelay() {
        return robot.getCoreDelay();
    }

    public double getWeaponDelay() {
        return robot.getWeaponDelay();
    }

    public double getHealth() {
        return robot.getHealthLevel();
    }

    public boolean isBuildingSomething() {
        return robot.getMyBuilding() >= 0;
    }

    // ***********************************
    // ****** GENERAL SENSOR METHODS *****
    // ***********************************

    public TerrainTile senseTerrainTile(MapLocation loc) {
        assertNotNull(loc);
        if (canSense(loc)) {
            return gameWorld.getMapTerrain(loc);
        }
        return gameWorld.senseMapTerrain(getTeam(), loc);
    }

    public boolean canSense(MapLocation loc) {
        return gameWorld.canSense(getTeam(), loc);
    }

    public boolean canSense(InternalRobot obj) {
        return obj.exists()
                && (obj.getTeam() == getTeam() || canSense(obj.getLocation()));
    }

    public void assertCanSense(MapLocation loc) throws GameActionException {
        if (!canSense(loc)) {
            throw new GameActionException(CANT_SENSE_THAT,
                    "That location is not within the robot's sensor range.");
        }
    }

    public void assertCanSense(InternalRobot obj) throws GameActionException {
        if (!canSense(obj)) {
            throw new GameActionException(CANT_SENSE_THAT,
                    "That object is not within the robot's sensor range.");
        }
    }

    public boolean canSenseLocation(MapLocation loc) {
        return canSense(loc);
    }

    public boolean isLocationOccupied(MapLocation loc)
            throws GameActionException {
        assertNotNull(loc);
        assertCanSense(loc);
        InternalRobot obj = (InternalRobot) gameWorld.getObject(loc);
        return obj != null;
    }

    public RobotInfo senseRobotAtLocation(MapLocation loc)
            throws GameActionException {
        assertNotNull(loc);
        assertCanSense(loc);
        InternalRobot obj = (InternalRobot) gameWorld.getObject(loc);
        if (obj != null && canSense(obj)) {
            return obj.getRobotInfo();
        } else {
            return null;
        }
    }

    public boolean canSenseRobot(int id) {
        InternalRobot obj = (InternalRobot) gameWorld.getObjectByID(id);
        if (obj == null) {
            return false;
        }
        return canSense(obj);
    }

    public RobotInfo senseRobot(int id) throws GameActionException {
        InternalRobot obj = (InternalRobot) gameWorld.getObjectByID(id);
        if (obj != null && canSense(obj)) {
            return obj.getRobotInfo();
        } else {
            throw new GameActionException(
                    CANT_SENSE_THAT,
                    "Could not sense robot with given ID. It might be out of sight range or it might not exist.");
        }
    }

    // Note: A radius^2 < 0 will return all visible robots on the map;
    // A null team will return robots of any team.
    public RobotInfo[] senseNearbyRobots(final MapLocation center,
            final int radiusSquared, final Team team) {
        final Collection<InternalRobot> allRobots = gameWorld.allObjects();
        final List<RobotInfo> robots = new ArrayList<>();

        final boolean useRadius = radiusSquared >= 0;
        final boolean useTeam = team != null;

        for (final InternalRobot o : allRobots) {
            if (!canSense(o))
                continue;
            if (o.equals(robot))
                continue;
            if (useRadius
                    && o.myLocation.distanceSquaredTo(center) > radiusSquared)
                continue;
            if (useTeam && o.getTeam() != team)
                continue;

            robots.add(((InternalRobot) o).getRobotInfo());
        }

        return robots.toArray(new RobotInfo[0]);
    }

    public RobotInfo[] senseNearbyRobots() {
        return senseNearbyRobots(-1);
    }

    public RobotInfo[] senseNearbyRobots(int radiusSquared) {
        return senseNearbyRobots(radiusSquared, null);
    }

    public RobotInfo[] senseNearbyRobots(final int radiusSquared,
            final Team team) {
        return senseNearbyRobots(robot.myLocation, radiusSquared, team);
    }

    // ***********************************
    // ****** READINESS METHODS **********
    // ***********************************
    public boolean isCoreReady() {
        return getCoreDelay() < 1;
    }

    public boolean isWeaponReady() {
        return getWeaponDelay() < 1;
    }

    // ***********************************
    // ****** MOVEMENT METHODS ***********
    // ***********************************
    public boolean isPathableInternal(RobotType type, MapLocation loc) {
        return gameWorld.canMove(loc, type);
    }

    public boolean isPathable(RobotType type, MapLocation loc) {
        if (!canSense(loc)) {
            return false;
        }
        return isPathableInternal(type, loc);
    }

    public boolean isMovingUnit() {
        return robot.type.canMove();
    }

    public boolean isValidDirection(Direction dir) {
        return dir != null && dir != Direction.NONE && dir != Direction.OMNI;
    }

    public void assertIsCoreReady() throws GameActionException {
        if (!isCoreReady()) {
            throw new GameActionException(NOT_ACTIVE,
                    "This robot has core delay.");
        }
    }

    public void assertIsPathable(RobotType type, MapLocation loc)
            throws GameActionException {
        if (!isPathableInternal(type, loc)) {
            throw new GameActionException(
                    GameActionExceptionType.CANT_MOVE_THERE,
                    "Cannot move robot of given type to that location.");
        }
    }

    public void assertIsMovingUnit() throws GameActionException {
        if (!isMovingUnit()) {
            throw new GameActionException(CANT_DO_THAT_BRO,
                    "This unit cannot move.");
        }
    }

    private void assertIsValidDirection(Direction d) {
        if (!isValidDirection(d)) {
            throw new IllegalArgumentException(
                    "You cannot move in the direction NONE, OMNI or in a null direction.");
        }
    }

    public boolean canMove(Direction dir) {
        return isMovingUnit() && isValidDirection(dir)
                && isPathableInternal(robot.type, getLocation().add(dir));
    }

    public void move(Direction d) throws GameActionException {
        assertIsCoreReady();
        assertIsMovingUnit();
        assertIsValidDirection(d);
        assertIsPathable(robot.type, getLocation().add(d));

        double factor1 = (d.isDiagonal() ? GameConstants.DIAGONAL_DELAY_MULTIPLIER
                : 1.0);
        double factor2 = 1.0;
        // if (robot.type == RobotType.DRONE &&
        // gameWorld.getMapTerrain(getLocation().add(d)) == TerrainTile.VOID) {
        // factor1 *= GameConstants.DRONE_VOID_DELAY_MULTIPLIER;
        // factor2 *= GameConstants.DRONE_VOID_DELAY_MULTIPLIER;
        // } TODO: Will we have flying units?

        robot.activateMovement(new MovementSignal(robot, getLocation().add(d),
                true, (int) (robot.getMovementDelayForType() * factor1)),
                robot.getCooldownDelayForType() * factor2,
                robot.getMovementDelayForType() * factor1);
    }

    // ***********************************
    // ****** ATTACK METHODS *************
    // ***********************************
    public boolean isAttackingUnit() {
        return robot.type.canAttack();
    }

    public boolean isValidAttackLocation(MapLocation loc) {
        assertNotNull(loc);
        return isAttackingUnit() && gameWorld.canAttackSquare(robot, loc);
    }

    private void assertIsWeaponReady() throws GameActionException {
        if (!isWeaponReady())
            throw new GameActionException(NOT_ACTIVE,
                    "This robot has weapon delay and cannot attack. "
                            + getWeaponDelay());
    }

    private void assertIsAttackingUnit() throws GameActionException {
        if (!isAttackingUnit()) {
            throw new GameActionException(CANT_DO_THAT_BRO,
                    "Not an attacking unit.");
        }
    }

    private void assertValidAttackLocation(MapLocation loc)
            throws GameActionException {
        if (!isValidAttackLocation(loc)) {
            throw new GameActionException(OUT_OF_RANGE,
                    "That location is out of this robot's attack range");
        }
    }

    public boolean canAttackLocation(MapLocation loc) {
        return isAttackingUnit() && isValidAttackLocation(loc);
    }

    public void attackLocation(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertIsWeaponReady();
        assertIsAttackingUnit();
        assertValidAttackLocation(loc);

        robot.activateAttack(new AttackSignal(robot, loc),
                robot.getAttackDelayForType(), robot.getCooldownDelayForType());
    }

    public void explode() throws GameActionException {
        throw new RobotDeathException();
    }

    // ***********************************
    // ****** BROADCAST METHODS **********
    // ***********************************
    public boolean hasBroadcasted() {
        return robot.hasBroadcasted();
    }

    public void broadcast(int channel, int data) throws GameActionException {
        if (channel < 0 || channel > GameConstants.BROADCAST_MAX_CHANNELS)
            throw new GameActionException(CANT_DO_THAT_BRO,
                    "Can only use radio channels from 0 to "
                            + GameConstants.BROADCAST_MAX_CHANNELS
                            + ", inclusive");

        robot.addBroadcast(channel, data);
    }

    @Override
    public int readBroadcast(int channel) throws GameActionException {
        if (channel < 0 || channel > GameConstants.BROADCAST_MAX_CHANNELS)
            throw new GameActionException(CANT_DO_THAT_BRO,
                    "Can only use radio channels from 0 to "
                            + GameConstants.BROADCAST_MAX_CHANNELS
                            + ", inclusive");

        Integer queued = robot.getQueuedBroadcastFor(channel);
        if (queued != null) {
            return queued.intValue();
        }

        int m = gameWorld.getMessage(robot.getTeam(), channel);
        return m;
    }

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************
    public DependencyProgress checkDependencyProgress(RobotType type) {
        if (gameWorld.getActiveRobotTypeCount(robot.getTeam(), type) > 0) {
            return DependencyProgress.DONE;
        } else if (gameWorld.getTotalRobotTypeCount(robot.getTeam(), type) > 0) {
            return DependencyProgress.INPROGRESS;
        } else {
            return DependencyProgress.NONE;
        }
    }

    public boolean isSpawningUnit() {
        return robot.type.canSpawn();
    }

    public void assertIsSpawningUnit() throws GameActionException {
        if (!isSpawningUnit()) {
            throw new GameActionException(CANT_DO_THAT_BRO,
                    "Must be spawning unit.");
        }
    }

    public boolean hasSpawnRequirements(RobotType type) {
        if (!isSpawningUnit()) {
            return false;
        }

        if (type.partCost > gameWorld.resources(getTeam())) {
            return false;
        }

        return type.spawnSource == robot.type;
    }

    public boolean canSpawn(Direction dir, RobotType type) {
        MapLocation loc = getLocation().add(dir);
        return isPathableInternal(robot.type, loc)
                && hasSpawnRequirements(type);
    }

    public void spawn(Direction dir, RobotType type) throws GameActionException {
        assertIsSpawningUnit();

        assertIsCoreReady();

        if (type.spawnSource != robot.type) {
            throw new GameActionException(CANT_DO_THAT_BRO,
                    "This spawn can only be by a certain type");
        }

        // if (!options.getBoolean("bc.game.allow-air-units") && type ==
        // RobotType.DRONE) {
        // throw new GameActionException(CANT_DO_THAT_BRO,
        // "Game config doesn't allow spawning air units.");
        // } TODO: Will we have flying units?

        MapLocation loc = getLocation().add(dir);
        assertIsPathable(type, loc);

        double cost = type.partCost;
        assertHaveResource(cost);

        robot.activateMovement(new SpawnSignal(loc, type, robot.getTeam(),
                robot, 0), 0, type.buildTurns);
    }

    public boolean isBuildingUnit() {
        return robot.type.canBuild();
    }

    public void assertIsBuildingUnit() throws GameActionException {
        if (!isBuildingUnit()) {
            throw new GameActionException(CANT_DO_THAT_BRO,
                    "Only BEAVER can build");
        }
    }

    public void assertIsBuildable(RobotType type) throws GameActionException {
        if (!type.isBuildable()) {
            throw new GameActionException(CANT_DO_THAT_BRO,
                    "Can only build buildings");
        }
    }

    public boolean hasBuildRequirements(RobotType type) {
        return isBuildingUnit() && type.isBuildable()
                && type.partCost <= gameWorld.resources(getTeam());
    }

    public boolean canBuild(Direction dir, RobotType type) {
        MapLocation loc = getLocation().add(dir);
        return isPathableInternal(type, loc) && hasBuildRequirements(type);
    }

    public void build(Direction dir, RobotType type) throws GameActionException {
        assertIsBuildingUnit();
        assertIsBuildable(type);
        assertIsCoreReady();

        double cost = type.partCost;
        assertHaveResource(cost);

        MapLocation loc = getLocation().add(dir);
        assertIsPathable(type, loc);

        int delay = type.buildTurns;
        robot.activateMovement(new BuildSignal(loc, type, robot.getTeam(),
                robot, delay), delay, delay);
    }

    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************
    public void yield() {
        RobotMonitor.endRunner();
    }

    public void disintegrate() {
        throw new RobotDeathException();
    }

    public void resign() {
        for (InternalRobot obj : gameWorld.getAllGameObjects())
            if ((obj instanceof InternalRobot)
                    && obj.getTeam() == robot.getTeam())
                gameWorld.notifyDied((InternalRobot) obj);
        gameWorld.removeDead();
    }

    // ***********************************
    // ******** MISC. METHODS ************
    // ***********************************
    public void setTeamMemory(int index, long value) {
        gameWorld.setTeamMemory(robot.getTeam(), index, value);
    }

    public void setTeamMemory(int index, long value, long mask) {
        gameWorld.setTeamMemory(robot.getTeam(), index, value, mask);
    }

    public long[] getTeamMemory() {
        long[] arr = gameWorld.getOldTeamMemory()[robot.getTeam().ordinal()];
        return Arrays.copyOf(arr, arr.length);
    }

    // ***********************************
    // ******** DEBUG METHODS ************
    // ***********************************
    public void setIndicatorString(int stringIndex, String newString) {
        if (stringIndex >= 0
                && stringIndex < GameConstants.NUMBER_OF_INDICATOR_STRINGS)
            (new IndicatorStringSignal(robot, stringIndex, newString))
                    .accept(gameWorld);
    }

    public void setIndicatorDot(MapLocation loc, int red, int green, int blue) {
        assertNotNull(loc);
        new IndicatorDotSignal(robot, loc, red, green, blue).accept(gameWorld);
    }

    public void setIndicatorLine(MapLocation from, MapLocation to, int red,
            int green, int blue) {
        assertNotNull(from);
        assertNotNull(to);
        new IndicatorLineSignal(robot, from, to, red, green, blue)
                .accept(gameWorld);
    }

    public long getControlBits() {
        return robot.getControlBits();
    }

    public void addMatchObservation(String observation) {
        (new MatchObservationSignal(robot, observation)).accept(gameWorld);
    }

    public void breakpoint() {
        gameWorld.notifyBreakpoint();
    }
}
