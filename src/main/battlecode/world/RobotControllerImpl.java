package battlecode.world;

import static battlecode.common.GameConstants.BYTECODES_PER_ROUND;
import static battlecode.common.GameConstants.NUMBER_OF_INDICATOR_STRINGS;
import static battlecode.common.GameConstants.TELEPORT_FLUX_COST;
import static battlecode.common.GameConstants.YIELD_BONUS;
import battlecode.common.AuraType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.GameConstants;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TerrainTile;
import battlecode.engine.GenericController;
import battlecode.engine.scheduler.Scheduler;
import battlecode.world.signal.AttackSignal;
import battlecode.world.signal.DeploySignal;
import battlecode.world.signal.EnergonTransferSignal;
import battlecode.world.signal.EvolutionSignal;
import battlecode.world.signal.FluxTransferSignal;
import battlecode.world.signal.IndicatorStringSignal;
import battlecode.world.signal.MatchObservationSignal;
import battlecode.world.signal.MovementSignal;
import battlecode.world.signal.SetAuraSignal;
import battlecode.world.signal.SetDirectionSignal;
import battlecode.engine.signal.Signal;
import battlecode.world.signal.SpawnSignal;
import battlecode.world.signal.StartTeleportSignal;
import battlecode.world.signal.UndeploySignal;

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

- TEST energon transfer every round, with cap
- TEST better println tagging
- TEST optimized silencing
- TEST "control ints"
- TEST indicator strings
- TEST map offsets
- TEST tiebreaking conditions
- TEST senseNearbyUpgrades
- TEST no battery freeze
- TEST upgrades
- TEST upgrades on map
- TEST only one energonchangesignal / round / robot
- TEST exception polling for action queue
- TEST fix action timing
- TEST action queue
- TEST broadcast queue
- TEST emp detonate
- TEST locating enemy batteries -- return direction
- TEST InternalArchon
- TEST energon decay
- TEST new energon transfer
- TEST map parsing
- TEST run perl script to get robottypes
- TEST serializable stuff
- TEST getNextMessage arrays
- TEST responding to signals
- TEST clock
 */
public class RobotControllerImpl implements RobotController, GenericController {

    private final GameWorld myGameWorld;
    private final InternalRobot myRobot;

    public RobotControllerImpl(GameWorld gw, InternalRobot r) {
        myGameWorld = gw;
        myRobot = r;
    }

    //*********************************
    //****** QUERY METHODS ********
    //*********************************
    /**
     * {@inheritDoc}
     */
    public boolean canAttackAir() {
        return myRobot.getRobotType().canAttackAir();
    }

    /**
     * {@inheritDoc}
     */
    public boolean canAttackGround() {
        return myRobot.getRobotType().canAttackGround();
    }

    /**
     * {@inheritDoc}
     */
    public boolean canAttackSquare(MapLocation targetLoc) {
        assertNotNull(targetLoc);
        return myGameWorld.canAttack(myRobot, targetLoc);
    }

    /**
     * {@inheritDoc}
     */
    public boolean canMove(Direction dir) {
        assertNotNull(dir);
        return myGameWorld.canMove(myRobot, dir);
    }

    /**
     * {@inheritDoc}
     */
    public boolean canSenseObject(GameObject obj) throws GameActionException {
        assertNotNull(obj);
        assertInternalObject(obj);

        MapLocation objLoc = ((InternalObject) obj).getLocation();

        if (objLoc == null) {
            return false;
        } else
            return myGameWorld.isExistant((InternalObject) obj) && myGameWorld.canSense(myRobot, objLoc);
    }

    /**
     * {@inheritDoc}
     */
    public boolean canSenseSquare(MapLocation loc) {
        assertNotNull(loc);
        return myGameWorld.canSense(myRobot, loc);
    }

    /**
     * {@inheritDoc}}
     */
    public boolean canBurnFlux() {
        if (myRobot.getRobotType() != RobotType.ARCHON)
            return false;
        return ((InternalArchon) myRobot).canBurnFlux();
    }

    /**
     * {@inheritDoc}
     */
    public Direction getDirection() {
        return myRobot.getDirection();
    }

    /**
     * {@inheritDoc}
     */
    public double getEnergonLevel() {
        return myRobot.getEnergonLevel();
    }

    /**
     * {@inheritDoc}
     */
    public double getEnergonReserve() {
        return myRobot.getEnergonReserve();
    }

    /**
     * {@inheritDoc}
     */
    public double getEventualEnergonLevel() {
        return myRobot.getEventualEnergonLevel();
    }

    /**
     * {@inheritDoc}
     */
    public double getMaxEnergonLevel() {
        return myRobot.getMaxEnergon();
    }

    /**
     * {@inheritDoc}
     */
    public double getEnergonProduction() {
        if (myRobot.getRobotType() == RobotType.ARCHON)
            return ((InternalArchon) myRobot).getProduction();
        else
            return 0.0;
    }

    /**
     * {@inheritDoc}
     */
    public MapLocation getLocation() {
        return myRobot.getLocation();
    }

    /**
     * {@inheritDoc}
     */
    public Message getNextMessage() {
        return myRobot.dequeueIncomingMessage();
    }

    /**
     * {@inheritDoc}
     */
    public Message[] getAllMessages() {
        return myRobot.dequeueIncomingMessages();
    }

    /**
     * {@inheritDoc}
     */
    public int getRoundsUntilAttackIdle() {
        return myRobot.getRoundsUntilAttackIdle();
    }

    /**
     * {@inheritDoc}
     */
    public int getRoundsUntilMovementIdle() {
        return myRobot.getRoundsUntilMovementIdle();
    }

    /**
     * {@inheritDoc}
     */
    public Team getTeam() {
        return myRobot.getTeam();
    }

    /**
     * {@inheritDoc}
     */
    public double getFlux() {
        return myRobot.getFlux();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAttackActive() {
        return myRobot.getRoundsUntilAttackIdle() > 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isMovementActive() {
        return myRobot.getRoundsUntilMovementIdle() > 0;
    }

    /**
     * {@inheritDoc}
     */
    public InternalRobot getRobot() {
        return myRobot;
    }

    /**
     * {@inheritDoc}
     */
    public RobotType getRobotType() {
        return myRobot.getRobotType();
    }

    public int getMapMinPoints() {
        return myGameWorld.getGameMap().getMinPoints();
    }

    //***********************************
    //****** ACTION METHODS *************
    //***********************************
    /**
     * {@inheritDoc}
     */
    public void transferUnitEnergon(double amount, MapLocation loc, RobotLevel height) throws GameActionException {
        assertNotNull(loc);
        assertNotNull(height);

        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new GameActionException(GameActionExceptionType.NOT_ENOUGH_ENERGON, "Can't transfer '" + amount + "' units of energon");
        }

        if (myRobot.getRobotType().isBuilding())
            throw new GameActionException(GameActionExceptionType.CANT_TRANSFER_ENERGON_THERE, "Buildings cannot transfer Energon directly. Please transfer Flux.");
        if (myRobot.getLocation().distanceSquaredTo(loc) > 2)
            throw new GameActionException(GameActionExceptionType.CANT_TRANSFER_ENERGON_THERE, "Cannot transfer energon to a non-adjacent square");
        InternalRobot targetRobot = myGameWorld.getRobot(loc, height);
        if (targetRobot == null)
            throw new GameActionException(GameActionExceptionType.CANT_TRANSFER_ENERGON_THERE, "Cannot transfer energon to an empty square");
        if (targetRobot.getRobotType().isBuilding())
            throw new GameActionException(GameActionExceptionType.CANT_TRANSFER_ENERGON_THERE, "Cannot transfer Energon to a building. Please transfer flux");

        if (amount > myRobot.getEnergonLevel())
            throw new GameActionException(GameActionExceptionType.NOT_ENOUGH_ENERGON, "Not enough energon to transfer the amount: " + amount);

        if (amount < 0.0)
            throw new GameActionException(GameActionExceptionType.NOT_ENOUGH_ENERGON, "Cannot transfer a negative amount");

        GameActionException e = (GameActionException) (new EnergonTransferSignal(myRobot, loc, height, amount)).accept(myGameWorld);
        if (e != null)
            throw e;
    }

    public void transferFlux(double amount, MapLocation loc, RobotLevel height) throws GameActionException {
        assertNotNull(loc);
        assertNotNull(height);

        if (myRobot.getLocation().distanceSquaredTo(loc) > 2)
            throw new GameActionException(GameActionExceptionType.CANT_TRANSFER_FLUX_THERE, "Cannot transfer flux to a non-adjacent square");
        InternalRobot targetRobot = myGameWorld.getRobot(loc, height);
        if (targetRobot == null)
            throw new GameActionException(GameActionExceptionType.CANT_TRANSFER_FLUX_THERE, "Cannot transfer flux to an empty square");

        if (myRobot.getRobotType().isBuilding()) {
            double energoneq = GameConstants.FLUX_TO_ENERGON_CONVERSION * amount;
            if (energoneq >= myRobot.getEnergonLevel())
                throw new GameActionException(GameActionExceptionType.NOT_ENOUGH_FLUX, "Not enough energon to convert to flux to transfer the amount: " + energoneq);
        } else {
            if (amount > myRobot.getFlux())
                throw new GameActionException(GameActionExceptionType.NOT_ENOUGH_FLUX, "Not enough flux to transfer the amount: " + amount);
        }
        if (amount < 0)
            throw new GameActionException(GameActionExceptionType.NOT_ENOUGH_FLUX, "Cannot transfer a negative amount");

        GameActionException e = (GameActionException) (new FluxTransferSignal(myRobot, loc, height, amount)).accept(myGameWorld);
        if (e != null)
            throw e;
    }

    /**
     * {@inheritDoc}
     */
    public void clearAction() {
        Signal s = myRobot.getCurrentActionSignal();
        if (s instanceof EvolutionSignal) {
            EvolutionSignal e = (EvolutionSignal) s;
            //refund the energon we took away
            myRobot.changeEnergonLevel(+transformCost(e.getType()));
        } else if (s instanceof SpawnSignal) {
            SpawnSignal s2 = (SpawnSignal) s;
            myRobot.changeEnergonLevel(+s2.getType().spawnCost());
            myRobot.incrementFlux(s2.getType().spawnFluxCost());
        } else if (s instanceof SetAuraSignal) {
            SetAuraSignal a = (SetAuraSignal) s;
            AuraType t = a.getAura();
            int fluxCost = t.fluxCost();
            InternalAura myAuraRobot = (InternalAura) myRobot;
            if (myAuraRobot.getLastAura() != t)
                fluxCost += t.switchCost();
            myRobot.changeEnergonLevel(fluxCost*GameConstants.FLUX_TO_ENERGON_CONVERSION);
        } else if (s instanceof StartTeleportSignal) {
            myRobot.changeEnergonLevel(TELEPORT_FLUX_COST * GameConstants.FLUX_TO_ENERGON_CONVERSION);
        }

        myRobot.clearActionQueue();
    }

    private double transformCost(RobotType type) {
        return type.spawnCost() / 2;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasActionSet() {
        return myRobot.hasActionQueued();
    }

    /**
     * {@inheritDoc}
     */
    public GameActionException getLastActionException() {
        return myRobot.getLastException();
    }

    /**
     * {@inheritDoc}
     * @throws GameActionException
     */
    public void attackAir(MapLocation targetLoc) throws GameActionException {
        queueAttack(targetLoc, RobotLevel.IN_AIR);
    }

    /**
     * {@inheritDoc}
     * @throws GameActionException
     */
    public void attackGround(MapLocation targetLoc) throws GameActionException {
        queueAttack(targetLoc, RobotLevel.ON_GROUND);
    }

    private void queueAttack(MapLocation targetLoc, RobotLevel height) throws GameActionException {
        assertNotNull(targetLoc);
        assertNotNull(height);
        assertCanAttack(targetLoc, height);
        assertAttackIdle();

        myRobot.queueAction(new AttackSignal(myRobot, targetLoc, height));
    }

    /**
     * {@inheritDoc}
     */
    public void broadcast(Message msg) throws GameActionException {
        assertNotNull(msg);

        boolean result = myRobot.enqueueMessageToBroadcast(msg);
        if (!result)
            throw new GameActionException(GameActionExceptionType.DOUBLE_ACTION_ERROR,
                    "Attempting to queue two broadcasts for the same round.");
    }

    /**
     * {@inheritDoc}
     */
    public void clearBroadcast() {
        myRobot.clearBroadcastQueue();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasBroadcastMessage() {
        return myRobot.hasBroadcastMessage();
    }

    /**
     * {@inheritDoc}
     */
    public double getBroadcastCost() {
        return myRobot.getBroadcastQueueCost();
    }

    /**
     * {@inheritDoc}
     */
    public void moveForward() throws GameActionException {
        queueMove(true);
    }

    /**
     * {@inheritDoc}
     */
    public void moveBackward() throws GameActionException {
        queueMove(false);
    }

    /**
     * {@inheritDoc}
     */
    private void queueMove(boolean isMovingForward) throws GameActionException {
        assertCanMove(isMovingForward ? myRobot.getDirection() : myRobot.getDirection().opposite());
        assertMovementIdle();
        //assertNotDeployed();
        myRobot.queueAction(new MovementSignal(myRobot, null, isMovingForward));
    }

    /**
     * {@inheritDoc}
     */
    public void setDirection(Direction dir) throws GameActionException {
        assertNotNull(dir);
        if (dir == Direction.NONE)
            throw new GameActionException(GameActionExceptionType.BAD_DIRECTION, "Cannot set direction to NONE");
        if (dir == Direction.OMNI)
            throw new GameActionException(GameActionExceptionType.BAD_DIRECTION, "Cannot set direction to OMNI");
        assertMovementIdle();

        myRobot.queueAction(new SetDirectionSignal(myRobot, dir));
    }

    /**
     * {@inheritDoc}
     */
    public void spawn(RobotType type) throws GameActionException {
        assertNotNull(type);
		if (type.isBuilding()) {
			if (myRobot.getRobotType() != RobotType.ARCHON&&
				myRobot.getRobotType() != RobotType.WOUT)
				throw new GameActionException(GameActionExceptionType.ARCHONS_ONLY, "Only Archons and wouts can spawn buildings.");
		}
		else {
			if (myRobot.getRobotType() != RobotType.ARCHON)
				throw new GameActionException(GameActionExceptionType.ARCHONS_ONLY, "Only Archons can spawn units.");
		}
        if (type == RobotType.ARCHON)
            throw new GameActionException(GameActionExceptionType.CANT_SPAWN_THAT, "You cannot spawn a " + type);
        if (myRobot.getEnergonLevel() < type.spawnCost())
            throw new GameActionException(GameActionExceptionType.NOT_ENOUGH_ENERGON, "Not enough energon to spawn a " + type.toString());
        if (myRobot.getFlux() < type.spawnFluxCost())
            throw new GameActionException(GameActionExceptionType.NOT_ENOUGH_FLUX, "Not enough flux to spawn a " + type.toString());
        MapLocation loc = myRobot.getLocation().add(myRobot.getDirection());
        if (!myGameWorld.canMove(type, loc))
            throw new GameActionException(GameActionExceptionType.CANT_SPAWN_THERE, "Cannot spawn a " + type + " in location " + loc);

        myRobot.changeEnergonLevel(-type.spawnCost());
        myRobot.incrementFlux(-type.spawnFluxCost());
        myRobot.queueAction(new SpawnSignal(null, type, myRobot.getTeam(), myRobot));
    }

    /**
     * {@inheritDoc}
     */
    public void transform(RobotType type) throws GameActionException {
        assertNotNull(type);
        if (myRobot.getRobotType() == RobotType.ARCHON)
            throw new GameActionException(GameActionExceptionType.CANT_EVOLVE, "An ARCHON cannot transform.");
        if (myRobot.getRobotType().isBuilding())
            throw new GameActionException(GameActionExceptionType.CANT_EVOLVE, "A building cannot transform.");
        if (type == RobotType.ARCHON)
            throw new GameActionException(GameActionExceptionType.CANT_EVOLVE_INTO_THAT, "Cannot transform into ARCHON.");
        if (type.isBuilding())
            throw new GameActionException(GameActionExceptionType.CANT_EVOLVE_INTO_THAT, "Cannot transform into a building.");
        if (type == myRobot.getRobotType())
            throw new GameActionException(GameActionExceptionType.CANT_EVOLVE_INTO_THAT, "Cannot transform into same RobotType.");
        if (myRobot.getRobotType().isAirborne() && !type.isAirborne())
            throw new GameActionException(GameActionExceptionType.CANT_EVOLVE_INTO_THAT, "Airborne units can only transform into other airborne units.");
        if (!myRobot.getRobotType().isAirborne() && type.isAirborne())
            throw new GameActionException(GameActionExceptionType.CANT_EVOLVE_INTO_THAT, "Ground units can only transform into other ground units.");
        if (myRobot.getEnergonLevel() < transformCost(type))
            throw new GameActionException(GameActionExceptionType.NOT_ENOUGH_ENERGON, "Not have enough energon to transform into " + type.toString());

        //charge them for the energon
        myRobot.changeEnergonLevel(-transformCost(type));
        myRobot.queueAction(new EvolutionSignal(myRobot, type));
    }

    /**
     * {@inheritDoc}
     */
    public void yield() {
        myGameWorld.endOfExecution(myRobot.getID());
        myRobot.changeEnergonLevel(YIELD_BONUS * ((double) (BYTECODES_PER_ROUND - myRobot.getBytecodesUsed()) / BYTECODES_PER_ROUND) * myRobot.getRobotType().energonUpkeep());
        Scheduler.passToNextThread();
    }

    /**
     * {@inheritDoc}
     */
    public void suicide() {
        myRobot.suicide();
        Scheduler.passToNextThread();
    }

    /**
     * {@inheritDoc}
     */
    public void breakpoint() {
        myGameWorld.notifyBreakpoint();
    }

    /**
     * {@inheritDoc}
     */
    public void deploy() throws GameActionException {
        if (myRobot.getRobotType() != RobotType.TURRET)
            throw new GameActionException(GameActionExceptionType.TURRETS_ONLY, "Only Turrets can deploy.");
        if (myRobot.isDeployed()) {
            throw new GameActionException(GameActionExceptionType.CANT_DEPLOY, "This robot is already deployed.");
        }
        assertAttackIdle();
        assertMovementIdle();
        myRobot.queueAction(new DeploySignal(myRobot));
    }

    public void undeploy() throws GameActionException {
        if (myRobot.getRobotType() != RobotType.TURRET)
            throw new GameActionException(GameActionExceptionType.TURRETS_ONLY, "Only Turrets can deploy.");
        if (!myRobot.isDeployed()) {
            throw new GameActionException(GameActionExceptionType.CANT_DEPLOY, "This robot is not deployed.");
        }
        assertAttackIdle();
        assertMovementIdle();
        myRobot.queueAction(new UndeploySignal(myRobot));
    }

    public boolean isDeployed() {
        return myRobot.isDeployed();
    }

    public boolean isTeleporting() {
        return myRobot.isTeleporting();
    }

    //***********************************
    //****** SENSING METHODS *******
    //***********************************
    /**
     * {@inheritDoc}
     */
    public Robot senseAirRobotAtLocation(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanSense(loc);
        return (Robot) myGameWorld.getRobot(loc, RobotLevel.IN_AIR);
    }

    /**
     * {@inheritDoc}
     */
    public Robot senseGroundRobotAtLocation(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanSense(loc);
        return (Robot) myGameWorld.getRobot(loc, RobotLevel.ON_GROUND);
    }

    /**
     * {@inheritDoc}
     */
    public Robot[] senseNearbyAirRobots() {
        return (Robot[]) myGameWorld.senseNearbyRobots(myRobot, RobotLevel.IN_AIR);
    }

    /**
     * {@inheritDoc}
     */
    public Robot[] senseNearbyGroundRobots() {
        return (Robot[]) myGameWorld.senseNearbyRobots(myRobot, RobotLevel.ON_GROUND);
    }

    public int senseHeightOfLocation(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanSense(loc);
        return myGameWorld.getGameMap().getLocationHeight(loc);
    }

    /**
     * {@inheritDoc}
     */
    public TerrainTile senseTerrainTile(MapLocation loc) {
        assertNotNull(loc);

        return myRobot.getMapMemory().recallTerrain(loc);
    }

    public int senseFluxAtLocation(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanSense(loc);
        return myGameWorld.getGameMap().getFlux(loc);
    }

    /**
     * {@inheritDoc}
     */
    private Team senseTeamOf(InternalRobot r) {
        return r.getTeam();
    }

    /**
     * {@inheritDoc}
     */
    public int senseIDOf(Robot r) throws GameActionException {
        assertNotNull(r);
        assertInternalRobot(r);

        return r.getID();
    }

    /**
     * {@inheritDoc}
     */
    public boolean senseCanAttackSquare(Robot r, MapLocation loc) throws GameActionException {
        assertNotNull(r);
        assertNotNull(loc);
        assertInternalRobot(r);
        assertExists((InternalRobot) r);
        assertCanSense((InternalRobot) r);

        return myGameWorld.canAttack((InternalRobot) r, loc);
    }

    /**
     * {@inheritDoc}
     */
    public boolean senseCanSenseSquare(Robot r, MapLocation loc) throws GameActionException {
        assertNotNull(r);
        assertNotNull(loc);
        assertInternalRobot(r);
        assertExists((InternalRobot) r);
        assertCanSense((InternalRobot) r);

        return myGameWorld.canSense((InternalRobot) r, loc);
    }

    /**
     * {@inheritDoc}
     */
    public boolean senseCanSenseObject(Robot r, GameObject obj) throws GameActionException {
        assertNotNull(r);
        assertNotNull(obj);
        assertInternalRobot(r);
        assertInternalObject(obj);
        assertExists((InternalRobot) r);
        assertExists((InternalObject) obj);
        assertCanSense((InternalRobot) r);
        assertCanSense((InternalObject) obj);

        MapLocation objLoc = ((InternalObject) obj).getLocation();
        return myGameWorld.canSense((InternalRobot) r, objLoc);
    }

    /**
     * {@inheritDoc}
     */
    public MapLocation senseLocationOf(GameObject obj) throws GameActionException {
        assertNotNull(obj);
        assertInternalObject(obj);
        assertExists((InternalObject) obj);
        assertCanSense((InternalObject) obj);

        return ((InternalObject) obj).getLocation();
    }

    /**
     * {@inheritDoc}
     */
    public double senseEnergonProductionOf(Robot r) throws GameActionException {
        assertNotNull(r);
        assertInternalRobot(r);
        assertExists((InternalRobot) r);
        assertCanSense((InternalRobot) r);

        if (r instanceof InternalArchon)
            return ((InternalArchon) r).getProduction();
        else
            return 0.0;
    }

    /**
     * {@inheritDoc}
     */
    public Direction senseEnemyArchon() {
        InternalRobot r = myGameWorld.getClosestArchon(myRobot.getLocation(), myRobot.getTeam().opponent());

        if (r == null)
            return Direction.NONE;

        return myRobot.getLocation().directionTo(r.getLocation());
    }

    public double senseTeamPoints(Robot r) throws GameActionException {
        assertNotNull(r);
        assertInternalRobot(r);
        assertExists((InternalRobot) r);
        assertCanSense((InternalRobot) r);

        if (!((InternalRobot) r).getRobotType().isBuilding())
            throw new GameActionException(GameActionExceptionType.CANT_SENSE_THAT, "Can only sense points of a building");

        return (int) myGameWorld.getPoints(((InternalRobot) r).getTeam());
    }

    public MapLocation[] senseAlliedArchons() {
        InternalRobot[] archons = myGameWorld.getArchons(myRobot.getTeam());
        MapLocation[] ret = new MapLocation[archons.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = archons[i].getLocation();
        }
        return ret;
    }

    //************************************
    //******** MISC. METHODS **********
    //************************************
    /**
     * {@inheritDoc}
     */
    public void setIndicatorString(int stringIndex, String newString) {
        if (stringIndex >= 0 && stringIndex < NUMBER_OF_INDICATOR_STRINGS)
            (new IndicatorStringSignal(myRobot, stringIndex, newString)).accept(myGameWorld);
    }

    /**
     * {@inheritDoc}
     */
    public long getControlBits() {
        return myRobot.getControlBits();
    }

    /**
     * {@inheritDoc}
     */
    public void addMatchObservation(String observation) {
        (new MatchObservationSignal(myRobot, observation)).accept(myGameWorld);
    }

    /**
     * {@inheritDoc}
     */
    public void setArchonMemory(long memory) throws GameActionException {
        if (myRobot.getRobotType() != RobotType.ARCHON)
            throw new GameActionException(GameActionExceptionType.ARCHONS_ONLY, "Only Archons can save state.");

        ((InternalArchon) myRobot).setArchonMemory(memory);
    }

    public long[] getOldArchonMemory() {
        return myGameWorld.getOldArchonMemory()[myRobot.getTeam().ordinal()];
    }

    //************************************
    //********* ASSERTIONS ************
    //************************************
    private void assertNotNull(Object o) {
        if (o == null)
            throw new NullPointerException("Argument has an invalid null value");
        //~ throw new GameActionException(GameActionExceptionType.INVALID_OBJECT, "Invalid (null) Object");
    }

    private void assertInternalObject(GameObject o) throws GameActionException {
        if (!(o instanceof InternalObject))
            throw new GameActionException(GameActionExceptionType.INVALID_OBJECT, "Invalid GameObject (don't extend GameObject!)");
    }

    private void assertInternalRobot(Robot r) throws GameActionException {
        if (!(r instanceof InternalRobot))
            throw new GameActionException(GameActionExceptionType.INVALID_OBJECT, "Invalid Robot object (don't extend Robot!)");
    }

    private void assertExists(InternalObject o) throws GameActionException {
        if (!myGameWorld.isExistant(o))
            cantSense(o);
    }

    private void assertCanSense(InternalObject obj) throws GameActionException {
        if (obj.getLocation() == null) {
            cantSense(obj);
        } else if (!myGameWorld.canSense(myRobot, obj.getLocation()))
            cantSense(obj);
    }

    private void assertCanSense(MapLocation loc) throws GameActionException {
        if (!myGameWorld.canSense(myRobot, loc))
            throw new GameActionException(GameActionExceptionType.CANT_SENSE_THAT, "Cannot sense square: " + loc.toString());
    }

    private void assertAttackIdle() throws GameActionException {
        if (myRobot.getRoundsUntilAttackIdle() > 0)
            throw new GameActionException(GameActionExceptionType.INSUFFICIENT_ATTACK_COOLDOWN, "Cannot perform action until attack idle in " + myRobot.getRoundsUntilAttackIdle() + " rounds.");
    }

    private void assertNotDeployed() throws GameActionException {
        if (myRobot.isDeployed()) {
            throw new GameActionException(GameActionExceptionType.INSUFFICIENT_MOVEMENT_COOLDOWN, "Deployed turrets cannot move.");
        }
    }

    private void assertMovementIdle() throws GameActionException {
        if (myRobot.getRobotType().isBuilding())
            throw new GameActionException(GameActionExceptionType.INSUFFICIENT_MOVEMENT_COOLDOWN, "Buildings cannot move.");
        if (myRobot.getRoundsUntilMovementIdle() > 0)
            throw new GameActionException(GameActionExceptionType.INSUFFICIENT_MOVEMENT_COOLDOWN, "Cannot perform action until movement idle in " + myRobot.getRoundsUntilMovementIdle() + " rounds.");
    }

    private void assertCanMove(Direction dir) throws GameActionException {
        if (!myGameWorld.canMove(myRobot, dir))
            throw new GameActionException(GameActionExceptionType.CANT_MOVE_THERE, "Cannot move in the given direction: " + dir.toString());
    }

    private void assertCanAttack(MapLocation targetLoc, RobotLevel height) throws GameActionException {
        if (!myGameWorld.canAttack(myRobot, targetLoc, height))
            throw new GameActionException(GameActionExceptionType.OUT_OF_ATTACK_RANGE, "Cannot attack the given square: " + targetLoc.toString() + ", " + height.toString());
    }

    private void assertIsTeleporter(Robot r) throws GameActionException {
        if (((InternalRobot) r).getRobotType() != RobotType.TELEPORTER)
            throw new GameActionException(GameActionExceptionType.NOT_A_TELEPORTER, "This action requires a teleporter.");
    }

    private InternalRobot teleporterAt(MapLocation l) throws GameActionException {
        InternalRobot r = myGameWorld.getRobot(l, RobotLevel.ON_GROUND);
        if (r == null || r.getRobotType() != RobotType.TELEPORTER || getTeam() != r.getTeam())
            throw new GameActionException(GameActionExceptionType.NOT_A_TELEPORTER, "There is no allied teleporter at " + l.toString());
        return r;
    }

    private void assertWithinTeleporterRange(MapLocation tel, MapLocation loc) throws GameActionException {
        if (tel.distanceSquaredTo(loc) > RobotType.TELEPORTER.sensorRadiusSquared())
            throw new GameActionException(GameActionExceptionType.CANT_SENSE_THAT, "Location " + loc.toString() + " cannot be sensed by the teleporter at " + tel.toString());
    }

    private void assertIAmTeleporter() throws GameActionException {
        if (myRobot.getRobotType() != RobotType.TELEPORTER) {
            throw new GameActionException(GameActionExceptionType.NOT_A_TELEPORTER, "Only teleporters can do that.");
        }
    }

    private void cantSense(InternalObject o) throws GameActionException {
        throw new GameActionException(GameActionExceptionType.CANT_SENSE_THAT, "Cannot sense object: " + o.toString());
    }

    public RobotInfo senseRobotInfo(Robot r) throws GameActionException {
        assertNotNull(r);
        assertInternalRobot(r);
        InternalRobot ir = (InternalRobot) r;
        assertExists(ir);
        assertCanSense(ir);

        // TODO: get rid of senseNumBlocksInCargo()
        return new RobotInfo(ir.getID(),
				ir.getRobotType(),
                ir.getTeam(),
                ir.getLocation(),
                ir.getEnergonLevel(),
                ir.getEnergonReserve(),
                ir.getMaxEnergon(),
                ir.getRoundsUntilAttackIdle(),
                ir.getRoundsUntilMovementIdle(),
                ir.getDirection(),
                ir.getFlux(),
				ir.isDeployed(),
				ir.isTeleporting(),
				ir.getAura());
    }

    public MapLocation[] senseAlliedTeleporters() throws GameActionException {
        assertIAmTeleporter();
        return myGameWorld.senseTeleporters(getTeam());
    }

    public boolean canTeleport(MapLocation toTeleporter, MapLocation teleportLoc, RobotLevel level) throws GameActionException {
        assertIAmTeleporter();
        assertNotNull(toTeleporter);
        teleporterAt(toTeleporter);
        assertWithinTeleporterRange(toTeleporter, teleportLoc);
        return myGameWorld.canMove(level, teleportLoc);
    }

    public void teleport(Robot r, MapLocation toTeleporter, MapLocation teleportLoc) throws GameActionException {
        assertIAmTeleporter();
        assertNotNull(r);
        assertInternalRobot(r);
        InternalRobot ir = ((InternalRobot) r);
        assertExists(ir);
        assertCanSense(ir);
        if (ir.getTeam() != getTeam())
            throw new GameActionException(GameActionExceptionType.CANT_TELEPORT_THAT, "You cannot teleport enemy robots.");
        if (ir.getRobotType().isBuilding())
            throw new GameActionException(GameActionExceptionType.CANT_TELEPORT_THAT, "Buildings cannot be teleported.");
		if (ir.isDeployed())
            throw new GameActionException(GameActionExceptionType.CANT_TELEPORT_THAT, "Deployed turrets cannot be teleported.");
        if (ir.isTeleporting())
            throw new GameActionException(GameActionExceptionType.CANT_TELEPORT_THAT, "You cannot teleport a robot that is already in the process of teleporting.");
        assertNotNull(toTeleporter);
        InternalRobot toRobot = teleporterAt(toTeleporter);
        assertWithinTeleporterRange(toTeleporter, teleportLoc);
        // teleport should fail if a teleporter gets destroyed and
        // rebuilt, so store the robots rather than the locations
        if (myRobot.getEnergonLevel() < TELEPORT_FLUX_COST * GameConstants.FLUX_TO_ENERGON_CONVERSION)
            throw new GameActionException(GameActionExceptionType.NOT_ENOUGH_FLUX, "Not enough flux to teleport.");
        myRobot.changeEnergonLevel(-TELEPORT_FLUX_COST * GameConstants.FLUX_TO_ENERGON_CONVERSION);
        //myRobot.queueAction(new StartTeleportSignal(ir, myRobot, toRobot, teleportLoc));
		new StartTeleportSignal(ir,myRobot,toRobot,teleportLoc).accept(myGameWorld);
    }

    public void setAura(AuraType t) throws GameActionException {
        assertNotNull(t);
        if (!(myRobot instanceof InternalAura))
            throw new GameActionException(GameActionExceptionType.AURAS_ONLY, "Only aura buildings can do that.");
        InternalAura myAuraRobot = (InternalAura) myRobot;
        int fluxCost = t.fluxCost();
        if (myAuraRobot.getLastAura() != t)
            fluxCost += t.switchCost();
        if (myRobot.getFlux() < fluxCost)
            throw new GameActionException(GameActionExceptionType.NOT_ENOUGH_FLUX, "Not enough flux to produce aura " + t);
        myRobot.changeEnergonLevel(-fluxCost * GameConstants.FLUX_TO_ENERGON_CONVERSION);
        myRobot.queueAction(new SetAuraSignal(myRobot, t));
    }

	public AuraType getAura() {
		return myRobot.getAura();
	}

    public AuraType getLastAura() {
        return myRobot.getLastAura();
    }

	public int hashCode() {
		return myRobot.getID();
	}
}
