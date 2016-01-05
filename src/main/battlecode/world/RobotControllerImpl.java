package battlecode.world;

import battlecode.common.*;
import battlecode.instrumenter.RobotDeathException;
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
        if (o == null)
            throw new NullPointerException("Argument has an invalid null value");
    }

    @Override
    public int hashCode() {
        return robot.getID();
    }

    // *********************************
    // ****** GLOBAL QUERY METHODS *****
    // *********************************

    @Override
    public int getRoundLimit() {
        return gameWorld.getGameMap().getRounds();
    }

    @Override
    public double getTeamParts() {
        return gameWorld.resources(getTeam());
    }

    @Override
    public int getRoundNum() {
        return gameWorld.getCurrentRound();
    }

    @Override
    public ZombieSpawnSchedule getZombieSpawnSchedule() {
        return new ZombieSpawnSchedule(gameWorld.getGameMap()
                .getZombieSpawnSchedule());
    }

    // *********************************
    // ****** UNIT QUERY METHODS *******
    // *********************************

    @Override
    public int getID() {
        return robot.getID();
    }

    @Override
    public Team getTeam() {
        return robot.getTeam();
    }

    @Override
    public RobotType getType() {
        return robot.getType();
    }

    @Override
    public MapLocation getLocation() {
        return robot.getLocation();
    }

    @Override
    public double getCoreDelay() {
        return robot.getCoreDelay();
    }

    @Override
    public double getWeaponDelay() {
        return robot.getWeaponDelay();
    }

    @Override
    public double getHealth() {
        return robot.getHealthLevel();
    }
    
    @Override
    public int getInfectedTurns() {
        return Math.max(robot.getZombieInfectedTurns(), robot.getViperInfectedTurns());
    }
    
    @Override
    public int getZombieInfectedTurns() {
        return robot.getZombieInfectedTurns();
    }
    
    @Override
    public int getViperInfectedTurns() {
        return robot.getViperInfectedTurns();
    }
    
    @Override
    public boolean isInfected() {
        return robot.isInfected();
    }

    // ***********************************
    // ****** GENERAL SENSOR METHODS *****
    // ***********************************

    /**
     * Determine if our robot can sense a location
     *
     * @param loc the location to test
     * @return whether we can sense the location
     */
    public boolean canSense(MapLocation loc) {
        return robot.canSense(loc);
    }

    /**
     * Determine if our robot can sense a robot
     *
     * @param obj the robot to test
     * @return whether we can sense the robot
     */
    public boolean canSense(InternalRobot obj) {
        return obj.exists() && canSense(obj.getLocation());
    }

    /**
     * @param loc the location to assert we can sense
     * @throws GameActionException if we can't sense the location
     */
    public void assertCanSense(MapLocation loc) throws GameActionException {
        if (!canSense(loc)) {
            throw new GameActionException(OUT_OF_RANGE,
                    loc+" is not within this robot's sensor range.");
        }
    }

    @Override
    public boolean onTheMap(MapLocation loc) throws GameActionException {
        if (canSense(loc)) {
            return gameWorld.getGameMap().onTheMap(loc);
        }
        throw new GameActionException(OUT_OF_RANGE, "Location " + loc + " " +
                "is currently out of sensor range.");
    }

    @Override
    public double senseRubble(MapLocation loc) {
        assertNotNull(loc);

        if (canSense(loc)) {
            return gameWorld.getRubble(loc);
        }

        return -1;
    }

    @Override
    public double senseParts(MapLocation loc) {
        assertNotNull(loc);

        if (canSense(loc)) {
            return gameWorld.getParts(loc);
        }

        return -1;
    }

    @Override
    public boolean canSenseLocation(MapLocation loc) {
        assertNotNull(loc);

        return canSense(loc);
    }

    @Override
    public boolean isLocationOccupied(MapLocation loc)
            throws GameActionException {
        assertNotNull(loc);

        assertCanSense(loc);
        InternalRobot obj = gameWorld.getObject(loc);
        return obj != null;
    }

    @Override
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

    @Override
    public boolean canSenseRobot(int id) {
        InternalRobot obj = gameWorld.getObjectByID(id);
        if (obj == null) {
            return false;
        }
        return canSense(obj);
    }

    @Override
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
    @Override
    public RobotInfo[] senseNearbyRobots(final MapLocation center,
            final int radiusSquared, final Team team) {
        assertNotNull(center);

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

    @Override
    public RobotInfo[] senseNearbyRobots() {
        return senseNearbyRobots(-1);
    }

    @Override
    public RobotInfo[] senseNearbyRobots(int radiusSquared) {
        return senseNearbyRobots(radiusSquared, null);
    }

    @Override
    public RobotInfo[] senseNearbyRobots(final int radiusSquared,
            final Team team) {

        return senseNearbyRobots(robot.getLocation(), radiusSquared, team);
    }

    // ***********************************
    // ****** READINESS METHODS **********
    // ***********************************

    /**
     * @throws GameActionException if our core is not ready
     */
    public void assertIsCoreReady() throws GameActionException {
        if (!isCoreReady()) {
            throw new GameActionException(NOT_ACTIVE,
                    "This robot has core delay.");
        }
    }

    @Override
    public boolean isCoreReady() {
        return getCoreDelay() < 1;
    }

    @Override
    public boolean isWeaponReady() {
        return getWeaponDelay() < 1;
    }

    // ***************************
    // ********* RUBBLE **********
    // ***************************

    /**
     * @throws GameActionException if we are not a rubble clearing unit type
     */
    public void assertIsRubbleClearingUnit() throws GameActionException {
        if (!robot.getType().canClearRubble()) {
            throw new GameActionException(CANT_DO_THAT,
                    robot.getType().name() + " cannot clear rubble.");
        }
    }

    @Override
    public void clearRubble(Direction dir) throws GameActionException {
        assertNotNull(dir);

        assertIsCoreReady();
        assertIsRubbleClearingUnit();

        if (dir == null || dir.equals(Direction.OMNI)) {
            throw new IllegalArgumentException("You cannot clear rubble in " +
                    "the direction OMNI.");
        }

        robot.activateCoreAction(new ClearRubbleSignal(robot.getID(),
                        getLocation().add(dir), (int) (robot.getType()
                        .movementDelay)),
                robot.getType().cooldownDelay, robot.getType().movementDelay);
    }

    // ***********************************
    // ****** MOVEMENT METHODS ***********
    // ***********************************

    /**
     * Determine if it is possible for a robot to move to the target location.
     *
     * @param type the robot type
     * @param loc the location to move to
     * @return if robots of the given type can move to the given location
     */
    public boolean isPathableInternal(RobotType type, MapLocation loc) {
        return gameWorld.canMove(loc, type);
    }

    /**
     * Determine if a direction is valid to move / build in.
     *
     * @param dir the direction to check
     * @return whether the direction is valid
     */
    public boolean isValidMovementDirection(Direction dir) {
        return dir != null && dir != Direction.NONE && dir != Direction.OMNI;
    }

    /**
     * @param type the type to check
     * @param loc the location to check
     * @throws GameActionException if robots of the type cannot move to the location
     */
    public void assertIsPathable(RobotType type, MapLocation loc)
            throws GameActionException {
        if (!isPathableInternal(type, loc)) {
            throw new GameActionException(CANT_MOVE_THERE, "Cannot move robot" +
                    " of given type to that location. There might be too much" +
                            " rubble.");
        }
    }

    @Override
    public boolean canMove(Direction dir) {
        assertNotNull(dir);

        return robot.getType().canMove() && isValidMovementDirection(dir)
                && isPathableInternal(robot.getType(), getLocation().add(dir));
    }

    @Override
    public void move(Direction d) throws GameActionException {
        assertIsCoreReady();
        if (!robot.getType().canMove()) {
            throw new GameActionException(CANT_DO_THAT,
                    "This unit cannot move.");
        }
        if (!isValidMovementDirection(d)) {
            throw new IllegalArgumentException(
                    "You cannot move in the direction NONE, OMNI or in a null direction.");
        }
        assertIsPathable(robot.getType(), getLocation().add(d));

        double factor1 = (d.isDiagonal() ? GameConstants.DIAGONAL_DELAY_MULTIPLIER
                : 1.0); //
        double factor2 = 1.0;
        double factor3 = (!getType().ignoresRubble && gameWorld.getRubble(getLocation().add(d)) >=
                GameConstants.RUBBLE_SLOW_THRESH) ? 2.0 : 1.0;

        robot.activateCoreAction(new MovementSignal(robot.getID(),
                        getLocation().add(d), (int) (robot.getType()
                        .movementDelay * factor1)),
                robot.getType().cooldownDelay * factor2 * factor3,
                robot.getType().movementDelay * factor1 * factor3);
    }

    // **********************************
    // ********* ATTACK METHODS *********
    // **********************************

    /**
     * @param loc the location to check
     * @return whether this robot can attack the location
     */
    public boolean isValidAttackLocation(MapLocation loc) {
        return robot.getType().canAttack() && gameWorld.canAttackSquare(robot, loc);
    }

    @Override
    public boolean canAttackLocation(MapLocation loc) {
        assertNotNull(loc);

        return robot.getType().canAttack() && isValidAttackLocation(loc);
    }

    @Override
    public void attackLocation(MapLocation loc) throws GameActionException {
        assertNotNull(loc);

        if (!isWeaponReady())
            throw new GameActionException(
                    NOT_ACTIVE,
                    "This robot has weapon delay " + getWeaponDelay()
                            + " and cannot attack. "
            );
        if (!robot.getType().canAttack()) {
            throw new GameActionException(
                    CANT_DO_THAT,
                    robot.getType().name() + " is not an attacking unit type."
            );
        }
        if (!isValidAttackLocation(loc)) {
            throw new GameActionException(
                    OUT_OF_RANGE,
                    loc + " is out of this robot's attack range"
            );
        }

        robot.activateAttack(new AttackSignal(robot.getID(), loc),
                robot.getType().attackDelay, robot.getType().cooldownDelay);
    }

    // ***********************************
    // ****** BROADCAST METHODS **********
    // ***********************************

    @Override
    public Signal readSignal() {
        return robot.retrieveNextSignal();
    }

    @Override
    public Signal[] emptySignalQueue() {
        return robot.retrieveAllSignals();
    }

    @Override
    public void broadcastSignal(int radius) throws GameActionException {
        if (radius < 0) {
            throw new GameActionException(CANT_DO_THAT, "Cannot broadcast " +
                    "with negative radius.");
        }
        gameWorld.visitBroadcastSignal(new BroadcastSignal(getID(), new
                Signal(getLocation(), getID(), getTeam()), radius));
    }

    @Override
    public void broadcastMessageSignal(int message1, int message2, int radius)
            throws GameActionException {
        if (!robot.getType().canMessageSignal()) {
            throw new GameActionException(CANT_DO_THAT,
                    "Unit type " + robot.getType().name() + " cannot send a " +
                            "message signal; only ARCHON and SCOUT can send " +
                            "message signals.");
        }
        if (radius < 0) {
            throw new GameActionException(CANT_DO_THAT, "Cannot broadcast " +
                    "with negative radius.");
        }
        gameWorld.visitBroadcastSignal(new BroadcastSignal(getID(), new Signal
                (getLocation(), getID(), getTeam(), message1, message2),
                radius));
    }

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************

    /**
     * @param amount the amount to check
     * @throws GameActionException if our resources are not >= amount
     */
    public void assertHaveResource(double amount) throws GameActionException {
        if (amount > gameWorld.resources(getTeam())) {
            throw new GameActionException(NOT_ENOUGH_RESOURCE,
                    "You do not have enough PARTS to do that.");
        }
    }

    @Override
    public boolean hasBuildRequirements(RobotType type) {
        assertNotNull(type);
        return robot.getType().canBuild() && type.isBuildable()
                && type.partCost <= gameWorld.resources(getTeam()) && type
                .spawnSource == robot.getType();
    }

    @Override
    public boolean canBuild(Direction dir, RobotType type) {
        assertNotNull(dir);
        assertNotNull(type);
        MapLocation loc = getLocation().add(dir);
        if(robot.getType() == RobotType.ZOMBIEDEN) {
            return gameWorld.isEmpty(loc);
        } else {
            return isPathableInternal(type, loc) && hasBuildRequirements(type);
        }
    }

    @Override
    public void build(Direction dir, RobotType type) throws GameActionException {
        assertNotNull(dir);
        assertNotNull(type);

        if (!robot.getType().canBuild()) {
            throw new GameActionException(
                    CANT_DO_THAT,
                    "Unit type " + robot.getType().name() + " cannot build; " +
                            "only ARCHON can build."
            );
        }
        if (!type.isBuildable()) {
            throw new GameActionException(
                    CANT_DO_THAT,
                    "Unit type " + type.name() + " not buildable."
            );
        }

        assertIsCoreReady();

        double cost = type.partCost;
        assertHaveResource(cost);

        MapLocation loc = getLocation().add(dir);
        if(robot.getType() != RobotType.ZOMBIEDEN)
            assertIsPathable(type, loc);
        
        int delay = type.buildTurns;
        robot.activateCoreAction(
                new BuildSignal(robot.getID(),
                        loc, type, robot.getTeam(), delay
                ),
                delay, delay);
    }

    // ***********************************
    // ****** OTHER ACTION METHODS *******
    // ***********************************

    @Override
    public void activate(MapLocation loc) throws GameActionException {
        assertNotNull(loc);

        if (robot.getType() != RobotType.ARCHON) {
            throw new GameActionException(CANT_DO_THAT, "Only archons can" +
                    " activate.");
        }

        if (getLocation().distanceSquaredTo(loc) > GameConstants.ARCHON_ACTIVATION_RANGE) {
            throw new GameActionException(
                    OUT_OF_RANGE,
                    loc + " is out of this robot's range for activation."
            );
        }

        InternalRobot target = gameWorld.getRobot(loc);
        if (target == null) {
            throw new GameActionException(NO_ROBOT_THERE, "No robot there to " +
                    "activate.");
        }
        if (target.getTeam() != Team.NEUTRAL) {
            throw new GameActionException(CANT_DO_THAT, "Can only activate " +
                    "robots that are NEUTRAL.");
        }

        assertIsCoreReady();

        robot.repair(target);
        robot.activateCoreAction(new ActivationSignal(robot.getID(), loc),
                0, robot.getType().movementDelay);
    }

    @Override
    public void repair(MapLocation loc) throws GameActionException {
        assertNotNull(loc);

        if (robot.getType() != RobotType.ARCHON) {
            throw new GameActionException(CANT_DO_THAT, "Only archons can" +
                    " repair.");
        }

        if (!gameWorld.canAttackSquare(robot, loc)) {
            throw new GameActionException(
                    OUT_OF_RANGE,
                    loc + " is out of this robot's attack range for repair."
            );
        }

        InternalRobot target = gameWorld.getRobot(loc);
        if (target == null) {
            throw new GameActionException(NO_ROBOT_THERE, "No robot there to " +
                    "repair.");
        }
        if (target.getTeam() != robot.getTeam()) {
            throw new GameActionException(CANT_DO_THAT, "Can only repair " +
                    "robots on your own team.");
        }

        if (robot.getRepairCount() >= 1) {
            throw new GameActionException(CANT_DO_THAT, "Can only repair " +
                    "once per turn.");
        }
        if (target.getType() == RobotType.ARCHON) {
            throw new GameActionException(CANT_DO_THAT, "Can only repair " +
                    "non-Archon robots.");
        }
        robot.repair(target);
    }

    @Override
    public void pack() throws GameActionException {
        if(robot.getType().equals(RobotType.TURRET)) {
            robot.transform(RobotType.TTM);
        } else {
            throw new GameActionException(CANT_DO_THAT,
                    "Only Turrets can pack. ");
        }
    }

    @Override
    public void unpack() throws GameActionException {
        if(robot.getType().equals(RobotType.TTM)) {
            robot.transform(RobotType.TURRET);
        } else {
            throw new GameActionException(CANT_DO_THAT,
                    "Only TTMs can unpack. ");
        }
    }

    @Override
    public void disintegrate() {
        throw new RobotDeathException();
    }

    @Override
    public void resign() {
        for (InternalRobot obj : gameWorld.getAllGameObjects()) {
            if ((obj != null) && obj.getTeam() == robot.getTeam()) {
                gameWorld.visitDeathSignal(new DeathSignal(obj.getID()));
            }
        }
    }

    // ***********************************
    // ******** TEAM MEMORY **************
    // ***********************************

    @Override
    public void setTeamMemory(int index, long value) {
        gameWorld.setTeamMemory(robot.getTeam(), index, value);
    }

    @Override
    public void setTeamMemory(int index, long value, long mask) {
        gameWorld.setTeamMemory(robot.getTeam(), index, value, mask);
    }

    @Override
    public long[] getTeamMemory() {
        long[] arr = gameWorld.getOldTeamMemory()[robot.getTeam().ordinal()];
        return Arrays.copyOf(arr, arr.length);
    }

    // ***********************************
    // ******** DEBUG METHODS ************
    // ***********************************

    @Override
    public void setIndicatorString(int stringIndex, String newString) {
        assertNotNull(newString);
        if (stringIndex >= 0
                && stringIndex < GameConstants.NUMBER_OF_INDICATOR_STRINGS)
            gameWorld.visitSignal((new IndicatorStringSignal(robot.getID(), stringIndex, newString)));
    }

    @Override
    public void setIndicatorDot(MapLocation loc, int red, int green, int blue) {
        assertNotNull(loc);
        gameWorld.visitSignal(new IndicatorDotSignal(robot.getID(), robot.getTeam(), loc, red, green, blue));
    }

    @Override
    public void setIndicatorLine(MapLocation from, MapLocation to, int red,
            int green, int blue) {
        assertNotNull(from);
        assertNotNull(to);
        gameWorld.visitSignal(new IndicatorLineSignal(robot.getID(), robot
                .getTeam(), from, to, red, green, blue));
    }

    @Override
    public long getControlBits() {
        return robot.getControlBits();
    }

    @Override
    public void addMatchObservation(String observation) {
        assertNotNull(observation);
        gameWorld.visitSignal((new MatchObservationSignal(robot.getID(), observation)));
    }
}
