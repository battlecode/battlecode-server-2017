package battlecode.world;

import battlecode.common.*;
import battlecode.engine.instrumenter.RobotDeathException;
import battlecode.world.signal.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static battlecode.common.GameActionExceptionType.*;

/**
 * The actual implementation of RobotController.
 * Its methods *must* be called from a player thread.
 *
 * It is theoretically possible to have multiple for a single InternalRobot,
 * but that may cause problems in practice, and anyway why would you want to?
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

    public RobotControllerImpl(GameWorld gameWorld, InternalRobot robot) {
        this.gameWorld = gameWorld;
        this.robot = robot;
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
        return gameWorld.getGameMap().getRounds();
    }

    public int getMapWidth() {
        return gameWorld.getGameMap().getWidth();
    }

    public int getMapHeight() {
        return gameWorld.getGameMap().getHeight();
    }

    public double getTeamParts() {
        return gameWorld.resources(getTeam());
    }

    public void assertHaveResource(double amount) throws GameActionException {
        if (amount > gameWorld.resources(getTeam())) {
            throw new GameActionException(NOT_ENOUGH_RESOURCE,
                    "You do not have enough PARTS to do that.");
        }
    }

    public ZombieCount[] getZombieSpawnSchedule(int round) {
        // TODO consider disabling this for not zombie dens
        return gameWorld.getGameMap().getZombieSpawnSchedule(round);
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

    // ***********************************
    // ****** GENERAL SENSOR METHODS *****
    // ***********************************
    public double senseRubble(MapLocation loc) {
        assertNotNull(loc);
        if (canSense(loc)) {
            return gameWorld.getRubble(loc);
        }
        return gameWorld.senseRubble(getTeam(), loc);
    }
    
    public double senseParts(MapLocation loc) {
        assertNotNull(loc);
        if (canSense(loc)) {
            return gameWorld.getRubble(loc);
        }
        return gameWorld.senseRubble(getTeam(), loc);
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

    public boolean canSenseLocation(MapLocation loc) {
        return canSense(loc);
    }

    public boolean isLocationOccupied(MapLocation loc)
            throws GameActionException {
        assertNotNull(loc);
        assertCanSense(loc);
        InternalRobot obj = gameWorld.getObject(loc);
        return obj != null;
    }

    public RobotInfo senseRobotAtLocation(MapLocation loc)
            throws GameActionException {
        assertNotNull(loc);
        assertCanSense(loc);
        InternalRobot obj = gameWorld.getObject(loc);
        if (obj != null && canSense(obj)) {
            return obj.getRobotInfo();
        } else {
            return null;
        }
    }

    public boolean canSenseRobot(int id) {
        InternalRobot obj = gameWorld.getObjectByID(id);
        if (obj == null) {
            return false;
        }
        return canSense(obj);
    }

    public RobotInfo senseRobot(int id) throws GameActionException {
        InternalRobot obj = gameWorld.getObjectByID(id);
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
                    && o.getLocation()
                    .distanceSquaredTo(center) > radiusSquared)
                continue;
            if (useTeam && o.getTeam() != team)
                continue;

            robots.add(o.getRobotInfo());
        }

        return robots.toArray(new RobotInfo[robots.size()]);
    }

    public RobotInfo[] senseNearbyRobots() {
        return senseNearbyRobots(-1);
    }

    public RobotInfo[] senseNearbyRobots(int radiusSquared) {
        return senseNearbyRobots(radiusSquared, null);
    }

    public RobotInfo[] senseNearbyRobots(final int radiusSquared,
            final Team team) {
        return senseNearbyRobots(robot.getLocation(), radiusSquared, team);
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
        // gameWorld.getMapTerrain(getLoc().add(d)) == TerrainType.VOID) {
        // factor1 *= GameConstants.DRONE_VOID_DELAY_MULTIPLIER;
        // factor2 *= GameConstants.DRONE_VOID_DELAY_MULTIPLIER;
        // } TODO: Will we have flying units?

        robot.activateMovement(new MovementSignal(robot.getID(), getLocation().add(d), true, (int) (robot.type.movementDelay * factor1)),
                robot.type.cooldownDelay * factor2,
                robot.type.movementDelay * factor1);
    }
    
    // ***********************************
    // ****** TTM/TURRET METHODS *********
    // ***********************************
    
    public void pack() throws GameActionException {
        if(robot.type.equals(RobotType.TURRET)) {
            robot.transform(RobotType.TTM);
        } else {
            throw new GameActionException(CANT_DO_THAT_BRO,
                    "Only Turrets can pack. ");
        }
    }
    
    public void unpack() throws GameActionException {
        if(robot.type.equals(RobotType.TTM)) {
            robot.transform(RobotType.TURRET);
        } else {
            throw new GameActionException(CANT_DO_THAT_BRO,
                    "Only TTMs can unpack. ");
        }
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

        robot.activateAttack(new AttackSignal(robot.getID(), loc),
                robot.type.attackDelay, robot.type.cooldownDelay);
    }

    // ***********************************
    // ****** BROADCAST METHODS **********
    // ***********************************

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
            return queued;
        }

        return gameWorld.getMessage(robot.getTeam(), channel);
    }

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************

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
                    "Only ARCHON can build");
        }
    }

    public void assertIsBuildable(RobotType type) throws GameActionException {
        if (!type.isBuildable()) {
            throw new GameActionException(CANT_DO_THAT_BRO,
                    "Unit type not buildable");
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
        robot.activateMovement(new BuildSignal(robot != null ? robot.getID() : 0, loc, type, robot.getTeam(), delay), delay, delay);
    }

    public void disintegrate() {
        throw new RobotDeathException();
    }

    public void resign() {
        for (InternalRobot obj : gameWorld.getAllGameObjects()) {
            if ((obj != null) && obj.getTeam() == robot.getTeam()) {
                gameWorld.visitDeathSignal(new DeathSignal(obj.getID()));
            }
        }
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
            gameWorld.visitSignal((new IndicatorStringSignal(robot.getID(), stringIndex, newString)));
    }

    public void setIndicatorDot(MapLocation loc, int red, int green, int blue) {
        assertNotNull(loc);
        gameWorld.visitSignal(new IndicatorDotSignal(robot.getID(), robot.getTeam(), loc, red, green, blue));
    }

    public void setIndicatorLine(MapLocation from, MapLocation to, int red,
            int green, int blue) {
        assertNotNull(from);
        assertNotNull(to);
        gameWorld.visitSignal(new IndicatorLineSignal(robot.getID(), robot.getTeam(), from, to, red, green, blue));
    }

    public long getControlBits() {
        return robot.getControlBits();
    }

    public void addMatchObservation(String observation) {
        gameWorld.visitSignal((new MatchObservationSignal(robot.getID(), observation)));
    }

    public int getRoundNum() {
        return gameWorld.getCurrentRound();
    }
}
