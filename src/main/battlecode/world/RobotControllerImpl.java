package battlecode.world;

import static battlecode.common.GameActionExceptionType.NOT_ACTIVE;
import static battlecode.common.GameActionExceptionType.CANT_DO_THAT_BRO;
import static battlecode.common.GameActionExceptionType.CANT_SENSE_THAT;
import static battlecode.common.GameActionExceptionType.MISSING_UPGRADE;
import static battlecode.common.GameActionExceptionType.NOT_ENOUGH_RESOURCE;
import static battlecode.common.GameActionExceptionType.NO_ROBOT_THERE;
import static battlecode.common.GameActionExceptionType.OUT_OF_RANGE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
import battlecode.common.Upgrade;
import battlecode.engine.GenericController;
import battlecode.engine.instrumenter.RobotDeathException;
import battlecode.engine.instrumenter.RobotMonitor;
import battlecode.world.signal.AttackSignal;
import battlecode.world.signal.CaptureSignal;
import battlecode.world.signal.HatSignal;
import battlecode.world.signal.IndicatorStringSignal;
import battlecode.world.signal.LocationSupplyChangeSignal;
import battlecode.world.signal.MatchObservationSignal;
import battlecode.world.signal.MineSignal;
import battlecode.world.signal.MovementSignal;
import battlecode.world.signal.ResearchSignal;
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

public class RobotControllerImpl extends ControllerShared implements RobotController, GenericController {

    public RobotControllerImpl(GameWorld gw, InternalRobot r) {
        super(gw, r);
    }

    public int hashCode() {
        return robot.getID();
    }

    // *********************************
    // ****** GLOBAL QUERY METHODS *****
    // *********************************

    public int getMapWidth() {
        return robot.myGameWorld.getGameMap().getWidth();
    }

    public int getMapHeight() {
        return robot.myGameWorld.getGameMap().getHeight();
    }

    public boolean hasUpgrade(Upgrade upgrade) {
        assertNotNull(upgrade);
        return gameWorld.hasUpgrade(getTeam(), upgrade);
    }
    
    public double getTeamOre() {
        return gameWorld.resources(getTeam());
    }

    public DependencyProgress checkDependencyProgress(RobotType type) {
        if (gameWorld.getRobotTypeCount(robot.getTeam(), type) > 0) {
            return DependencyProgress.DONE;
        } else if (gameWorld.getTotalRobotTypeCount(robot.getTeam(), type) > 0) {
            return DependencyProgress.INPROGRESS;
        } else {
            return DependencyProgress.NONE;
        }
    }

    public boolean hasCommander() {
        return gameWorld.getRobotTypeCount(robot.getTeam(), RobotType.COMMANDER) > 0;
    }
    
    public void assertHaveResource(double amount) throws GameActionException {
        if (amount > gameWorld.resources(getTeam()))
            throw new GameActionException(NOT_ENOUGH_RESOURCE, "You do not have enough ORE to do that.");
    }
    
    public void assertHaveUpgrade(Upgrade upgrade) throws GameActionException {
        if (!gameWorld.hasUpgrade(getTeam(), upgrade))
            throw new GameActionException(MISSING_UPGRADE, "You need the following upgrade: "+upgrade);
    }

    // *********************************
    // ****** UNIT QUERY METHODS *******
    // *********************************

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

    public double getTurnsUntilMovement() {
        return robot.getTimeUntilMovement();
    }

    public double getTurnsUntilAttack() {
        return robot.getTimeUntilAttack();
    }

    public double getHealth() {
        return robot.getEnergonLevel();
    }

    public double getSupplyLevel() {
        return robot.getSupplyLevel();
    }
    
    public int getXP() {
        return robot.getXP();
    }

    public boolean isBuildingSomething() {
        return getBuildingTypeBeingBuilt() != null;
    }

    public RobotType getBuildingTypeBeingBuilt() {
        return robot.getCapturingType();
    }

    public int getBuildingRoundsRemaining() {
        return robot.getCapturingRounds();
    }

    public int getMissileCount() {
        return robot.getMissileCount();
    }

    // ***********************************
    // ****** GENERAL SENSOR METHODS *****
    // ***********************************

    public boolean checkCanSense(MapLocation loc) {
        
        int sensorRadius = robot.type.sensorRadiusSquared;

        if (robot.myLocation.distanceSquaredTo(loc) <= sensorRadius) {
            return true;
        }
       
        for (InternalObject o : gameWorld.allObjects()) {
            if ((Robot.class.isInstance(o)) && (o.getTeam() == robot.getTeam() || loc.distanceSquaredTo(o.getLocation()) <= sensorRadius)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean checkCanSense(InternalObject obj) {
        return obj.exists() && (obj.getTeam() == getTeam() || checkCanSense(obj.getLocation()));
    }

    public void assertCanSense(MapLocation loc) throws GameActionException {
        if (!checkCanSense(loc))
            throw new GameActionException(CANT_SENSE_THAT, "That location is not within the robot's sensor range.");
    }

    public void assertCanSense(InternalObject obj) throws GameActionException {
        if (!checkCanSense(obj))
            throw new GameActionException(CANT_SENSE_THAT, "That object is not within the robot's sensor range.");
    }

    public MapLocation senseHQLocation() {
        return gameWorld.getBaseHQ(getTeam()).getLocation();
    }
    
    public MapLocation senseEnemyHQLocation() {
        return gameWorld.senseEnemyHQLocation(getTeam());
    }

    public TerrainTile senseTerrainTile(MapLocation loc) {
        assertNotNull(loc);
        return gameWorld.senseMapTerrain(getTeam(), loc);
    }
    
    public boolean canSenseObject(GameObject o) {
        return checkCanSense(castInternalObject(o));
    }

    public boolean canSenseSquare(MapLocation loc) {
        return checkCanSense(loc);
    }

    public RobotInfo senseRobotAtLocation(MapLocation loc) throws GameActionException {
        assertNotNull(loc);
        assertCanSense(loc);
        InternalRobot obj = (InternalRobot) gameWorld.getObject(loc);
        if (obj != null && checkCanSense(obj)) {
            return obj.getRobotInfo();
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends GameObject> T[] senseNearbyGameObjects(final Class<T> type) {
        Predicate<InternalObject> p = new Predicate<InternalObject>() {
            public boolean apply(InternalObject o) {
                return checkCanSense(o) && (type.isInstance(o)) && (!o.equals(robot));
            }
        };
        return Iterables.toArray((Iterable<T>) Iterables.filter(gameWorld.allObjects(), p), type);
    }

    // USE THIS METHOD CAREFULLY
    public <T extends GameObject> RobotInfo[] getRobotsFromGameObjects(T[] array) {
        RobotInfo[] robots = new RobotInfo[array.length];
        for (int i = 0; i < robots.length; ++i) {
            InternalRobot ir = (InternalRobot) array[i];
            robots[i] = ir.getRobotInfo();
        }
        return robots;
    }

    public RobotInfo[] senseNearbyRobots() {
        return getRobotsFromGameObjects(senseNearbyGameObjects(Robot.class));
    }
    
    @SuppressWarnings("unchecked")
    public <T extends GameObject> T[] senseNearbyGameObjects(final Class<T> type, final int radiusSquared) {
        Predicate<InternalObject> p = new Predicate<InternalObject>() {
            public boolean apply(InternalObject o) {
                return o.myLocation.distanceSquaredTo(robot.myLocation) <= radiusSquared 
                        && checkCanSense(o) && (type.isInstance(o)) && (!o.equals(robot));
            }
        };
        return Iterables.toArray((Iterable<T>) Iterables.filter(gameWorld.allObjects(), p), type);
    }

    public RobotInfo[] senseNearbyRobots(int radiusSquared) {
        return getRobotsFromGameObjects(senseNearbyGameObjects(Robot.class, radiusSquared));
    }

    @SuppressWarnings("unchecked")
    public <T extends GameObject> T[] senseNearbyGameObjects(final Class<T> type, final int radiusSquared, final Team team) {
        if (team == null) {
            return senseNearbyGameObjects(type, radiusSquared);
        }
        Predicate<InternalObject> p = new Predicate<InternalObject>() {
            public boolean apply(InternalObject o) {
                return o.myLocation.distanceSquaredTo(robot.myLocation) <= radiusSquared
                        && o.getTeam() == team
                        && checkCanSense(o) && (type.isInstance(o)) && (!o.equals(robot));
            }
        };
        return Iterables.toArray((Iterable<T>) Iterables.filter(gameWorld.allObjects(), p), type);
    }

    public RobotInfo[] senseNearbyRobots(int radiusSquared, Team team) {
        return getRobotsFromGameObjects(senseNearbyGameObjects(Robot.class, radiusSquared, team));
    }

    @SuppressWarnings("unchecked")
    public <T extends GameObject> T[] senseNearbyGameObjects(final Class<T> type, final MapLocation center, final int radiusSquared, final Team team) {
        if (team == null) {
            Predicate<InternalObject> p = new Predicate<InternalObject>() {
                public boolean apply(InternalObject o) {
                    return o.myLocation.distanceSquaredTo(center) <= radiusSquared
                            && checkCanSense(o) && (type.isInstance(o)) && (!o.equals(robot));
                }
            };
            return Iterables.toArray((Iterable<T>) Iterables.filter(gameWorld.allObjects(), p), type);
        }
        Predicate<InternalObject> p = new Predicate<InternalObject>() {
            public boolean apply(InternalObject o) {
                return o.myLocation.distanceSquaredTo(center) <= radiusSquared
                        && o.getTeam() == team
                        && checkCanSense(o) && (type.isInstance(o)) && (!o.equals(robot));
            }
        };
        return Iterables.toArray((Iterable<T>) Iterables.filter(gameWorld.allObjects(), p), type);
    }

    public RobotInfo[] senseNearbyRobots(MapLocation center, int radiusSquared, Team team) {
        return getRobotsFromGameObjects(senseNearbyGameObjects(Robot.class, center, radiusSquared, team));
    }

    // ***********************************
    // ****** MOVEMENT METHODS ***********
    // ***********************************

    public void assertNotMoving() throws GameActionException {
        if (!isMovementActive()) {
            throw new GameActionException(NOT_ACTIVE, "This robot has movement delay and cannot move.");
        }
    }

    public void assertCanMove(Direction d) throws GameActionException {
        if (!canMove(d)) {
            throw new GameActionException(GameActionExceptionType.CANT_MOVE_THERE, "Cannot move in the given direction: " + d);
        }
    }

    protected void assertValidDirection(Direction d) {
        assertNotNull(d);
        if (d == Direction.NONE || d == Direction.OMNI) {
            throw new IllegalArgumentException("You cannot move in the direction NONE or OMNI.");
        }
    }

    public boolean isMovementActive() {
        return getTurnsUntilMovement() < 1;
    }

    public boolean canMove(Direction d) {
        if (d == Direction.NONE || d == Direction.OMNI)
            return false;
        assertValidDirection(d);
        return gameWorld.canMove(getLocation().add(d), robot.type);
    }

    public void move(Direction d) throws GameActionException {
        if (robot.type.isBuilding)
            throw new GameActionException(CANT_DO_THAT_BRO, "Buildings can't move");
        assertNotMoving();
        assertCanMove(d);
        double delay = robot.calculateMovementActionDelay(getLocation(), getLocation().add(d), senseTerrainTile(getLocation()));

        int factor = 1;
        if (robot.getSupplyLevel() >= robot.type.supplyUpkeep) {
            robot.decreaseSupplyLevel(robot.type.supplyUpkeep);
        } else {
            factor = 2;
        }

        robot.activateMovement(new MovementSignal(robot, getLocation().add(d),
                true, ((int) delay) * factor), robot.getLoadingDelayForType(), delay * factor);
    }

    // ***********************************
    // ****** ATTACK METHODS *************
    // ***********************************

    public boolean isAttackActive() {
        return getTurnsUntilAttack() < 1;
    }

    protected void assertNotAttacking() throws GameActionException {
        if (!isAttackActive())
            throw new GameActionException(NOT_ACTIVE, "This robot has action delay and cannot attack.");
    }

    protected void assertCanAttack(MapLocation loc) throws GameActionException {
        if (!canAttackSquare(loc))
            throw new GameActionException(OUT_OF_RANGE, "That location is out of this robot's attack range");
    }

    public boolean canAttackSquare(MapLocation loc) {
        assertNotNull(loc);
        return GameWorld.canAttackSquare(robot, loc);
    }

    public void attackSquare(MapLocation loc) throws GameActionException {
        assertNotAttacking();
        assertNotNull(loc);
        assertCanAttack(loc);
        if (robot.type == RobotType.BASHER) {
            throw new GameActionException(CANT_DO_THAT_BRO, "Bashers can only attack using the attack() method.");
        }

        int factor = 1;
        if (robot.getSupplyLevel() >= robot.type.supplyUpkeep) {
            robot.decreaseSupplyLevel(robot.type.supplyUpkeep);
        } else {
            factor = 2;
        }

        robot.activateAttack(new AttackSignal(robot, loc), robot.calculateAttackActionDelay(robot.type) * factor, robot.getCooldownDelayForType());
    }

    public void bash() throws GameActionException {
        assertNotAttacking();
        if (robot.type != RobotType.BASHER) {
            throw new GameActionException(CANT_DO_THAT_BRO, "Only Bashers can attack using the attack() method.");
        }

        int factor = 1;
        if (robot.getSupplyLevel() >= robot.type.supplyUpkeep) {
            robot.decreaseSupplyLevel(robot.type.supplyUpkeep);
        } else {
            factor = 2;
        }

        robot.activateAttack(new AttackSignal(robot, getLocation()), robot.calculateAttackActionDelay(robot.type) * factor, robot.getCooldownDelayForType());
    }

    public void explode() throws GameActionException {
        if (robot.type != RobotType.MISSILE) {
            throw new GameActionException(GameActionExceptionType.CANT_DO_THAT_BRO, "only missiles can self destruct");
        }
        if (robot.type == RobotType.MISSILE) {
            robot.setSelfDestruct();
        }
        throw new RobotDeathException();
    }

    // ***********************************
    // ****** BROADCAST METHODS **********
    // ***********************************

    public boolean hasBroadcasted() {
        return robot.hasBroadcasted();
    }
    
    public void broadcast(int channel, int data) throws GameActionException {
        if (channel<0 || channel>GameConstants.BROADCAST_MAX_CHANNELS)
            throw new GameActionException(CANT_DO_THAT_BRO, "Can only use radio channels from 0 to "+GameConstants.BROADCAST_MAX_CHANNELS+", inclusive");
        
        robot.addBroadcast(channel, data);
    }
    
    @Override
    public int readBroadcast(int channel) throws GameActionException {
        if (channel<0 || channel>GameConstants.BROADCAST_MAX_CHANNELS)
            throw new GameActionException(CANT_DO_THAT_BRO, "Can only use radio channels from 0 to "+GameConstants.BROADCAST_MAX_CHANNELS+", inclusive");
        int m = gameWorld.getMessage(robot.getTeam(), channel);
        return m;
    }

    // ***********************************
    // ****** SUPPLY METHODS *************
    // ***********************************

    public double senseSupplyLevelAtLocation(MapLocation loc) throws GameActionException {
        checkCanSense(loc);

        return gameWorld.senseSupplyLevel(getTeam(), loc);
    }

    public void dropSupplies(int amount) throws GameActionException {
        if (robot.getSupplyLevel() < amount) {
            throw new GameActionException(CANT_DO_THAT_BRO, "Not enough supply to drop");
        }

        // some signal here
        robot.decreaseSupplyLevel(amount);
        gameWorld.changeSupplyLevel(robot.getLocation(), amount);
    }

    public void transferSupplies(int amount, Direction dir) throws GameActionException {
        if (robot.getSupplyLevel() < amount) {
            throw new GameActionException(CANT_DO_THAT_BRO, "Not enough supply to drop");
        }
        MapLocation target = robot.getLocation().add(dir);
        InternalObject obj = gameWorld.getObject(target);
        if (obj == null) {
            throw new GameActionException(CANT_DO_THAT_BRO, "No one to receive supply from transfer in that direction.");
        }
        robot.decreaseSupplyLevel(amount);
        InternalRobot other = (InternalRobot) obj;
        other.increaseSupplyLevel(amount);
    }

    public void pickUpSupplies(int amount) throws GameActionException {
        if (gameWorld.getSupplyLevel(robot.getLocation()) < amount) {
            throw new GameActionException(CANT_DO_THAT_BRO, "Not enough supply to pick up");
        }

        // some signal here

        robot.increaseSupplyLevel(amount);
        gameWorld.changeSupplyLevel(robot.getLocation(), -amount);
    }

    public void transferSuppliesToHQ() throws GameActionException {
        if (robot.type != RobotType.SUPPLYDEPOT) {
            throw new GameActionException(CANT_DO_THAT_BRO, "Only supply depot can transfer supplies to hq");
        }

        double amount = robot.getSupplyLevel();
        robot.decreaseSupplyLevel(amount);
        gameWorld.getBaseHQ(getTeam()).increaseSupplyLevel(amount);
    }

    // ***********************************
    // ****** MINING METHODS *************
    // ***********************************

    public void mine() throws GameActionException {
        if (robot.type != RobotType.FURBY && robot.type != RobotType.MINER) {
            throw new GameActionException(CANT_DO_THAT_BRO, "Only FURBY and MINER can mine");
        }
        assertNotMoving();
        MapLocation loc = getLocation();
        robot.activateMovement(new MineSignal(loc, getTeam(), getType()), 1, 1);
    }

    public int senseOre(MapLocation loc) throws GameActionException {
        assertCanSense(loc);
        return gameWorld.getOre(loc);
    }   

    // ***********************************
    // ****** BUILDING/SPAWNING **********
    // ***********************************

    public void launchMissile(Direction dir) throws GameActionException {
        if (robot.type != RobotType.LAUNCHER)
            throw new GameActionException(CANT_DO_THAT_BRO, "Only LAUNCHER can launch missiles");

        if (robot.getMissileCount() == 0) {
            throw new GameActionException(CANT_DO_THAT_BRO, "No missiles to launch");
        }

        assertNotMoving();

        MapLocation loc = getLocation().add(dir);
        if (!gameWorld.canMove(loc, RobotType.MISSILE))
            throw new GameActionException(GameActionExceptionType.CANT_MOVE_THERE, "That square is occupied.");

        robot.decrementMissileCount();
        robot.activateMovement(
                new SpawnSignal(loc, RobotType.MISSILE, robot.getTeam(), robot, 0), 0, 0);
    }

    public boolean canSpawn(Direction dir, RobotType type) {
        if (!robot.type.isBuilding || type.spawnSource != robot.type || type == RobotType.COMMANDER && hasCommander()) {
            return false;
        }

        MapLocation loc = getLocation().add(dir);
        if (!gameWorld.canMove(loc, type))
            return false;

        double cost = type.oreCost;
        if (cost > gameWorld.resources(getTeam())) {
            return false;
        }

        return true;
    }

    public void spawn(Direction dir, RobotType type) throws GameActionException {
        if (!robot.type.isBuilding) {
            throw new GameActionException(CANT_DO_THAT_BRO, "Only buildings can spawn");
        }
        if (type.spawnSource != robot.type) {
            throw new GameActionException(CANT_DO_THAT_BRO, "This spawn can only be by a certain type");
        }
        if (type == RobotType.COMMANDER && hasCommander()) {
            throw new GameActionException(CANT_DO_THAT_BRO, "Only one commander per team!");
        }

        assertNotMoving();
        double cost = type.oreCost;
        
        assertHaveResource(cost);
        gameWorld.adjustResources(getTeam(), -cost);

        MapLocation loc = getLocation().add(dir);
        if (!gameWorld.canMove(loc, type))
            throw new GameActionException(GameActionExceptionType.CANT_MOVE_THERE, "That square is occupied.");

        robot.activateMovement(
                new SpawnSignal(loc, type, robot.getTeam(), robot, 0), robot.type == RobotType.HQ ? 0 : type.buildTurns, type.buildTurns 
                );
        robot.resetSpawnCounter();
    }

    public boolean canBuild(Direction dir, RobotType type) {
        if (robot.type != RobotType.FURBY && robot.type != RobotType.BUILDER)
            return false;
        if (!type.isBuilding)
            return false;

        // check dependencies
        for (RobotType dependency : type.dependencies) {
            if (gameWorld.getRobotTypeCount(getTeam(), dependency) == 0) {
                return false;
            }
        }
        MapLocation loc = getLocation().add(dir);
        if (!gameWorld.canMove(loc, type))
            return false;

        double cost = type.oreCost;
        if (cost > gameWorld.resources(getTeam())) {
            return false;
        }

        return true;
    }
    
    public void build(Direction dir, RobotType type) throws GameActionException {
        if (robot.type != RobotType.FURBY && robot.type != RobotType.BUILDER)
            throw new GameActionException(CANT_DO_THAT_BRO, "Only FURBY and BUILDER can build");
        if (!type.isBuilding)
            throw new GameActionException(CANT_DO_THAT_BRO, "Can only build buildings");

        // check dependencies
        for (RobotType dependency : type.dependencies) {
            if (gameWorld.getRobotTypeCount(getTeam(), dependency) == 0) {
                throw new GameActionException(CANT_DO_THAT_BRO, "Missing depency for build of " + type);
            }
        }

        assertNotMoving();
        double cost = type.oreCost;
        
        assertHaveResource(cost);
        gameWorld.adjustResources(getTeam(), -cost);

        MapLocation loc = getLocation().add(dir);
        if (!gameWorld.canMove(loc, type))
            throw new GameActionException(GameActionExceptionType.CANT_MOVE_THERE, "That square is occupied.");

        int delay = type.buildTurns;
        if (robot.type == RobotType.BUILDER) {
            if (gameWorld.hasUpgrade(getTeam(), Upgrade.IMPROVEDBUILDING)) {
                delay = delay / 2;
            } else {
                delay = delay * 2 / 3;
            }
        }

        robot.activateMovement(
                new SpawnSignal(loc, type, robot.getTeam(), robot, delay), delay, delay
                );
        robot.resetSpawnCounter();
    }
    

    //***********************************
    //****** UPGRADE METHODS ************
    //***********************************

    public void researchUpgrade(Upgrade upgrade) throws GameActionException {
        if (robot.type != upgrade.researcher)
            throw new GameActionException(CANT_DO_THAT_BRO, "Only certain units can research.");
        if (gameWorld.hasUpgrade(getTeam(), upgrade))
            throw new GameActionException(CANT_DO_THAT_BRO, "You already have that upgrade. ("+upgrade+")");
        if (checkResearchProgress(upgrade) > 0) {
            throw new GameActionException(CANT_DO_THAT_BRO, "You already started researching this upgrade. ("+upgrade+")");
        }
        assertNotMoving();
        assertHaveResource(upgrade.oreCost);
        gameWorld.adjustResources(getTeam(), -upgrade.oreCost);
        robot.activateResearch(new ResearchSignal(robot, upgrade), upgrade.numRounds, upgrade.numRounds);
    }
    
    public int checkResearchProgress(Upgrade upgrade) throws GameActionException {
        return gameWorld.getUpgradeProgress(getTeam(), upgrade);
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
        for (InternalObject obj : gameWorld.getAllGameObjects())
            if ((obj instanceof InternalRobot) && obj.getTeam() == robot.getTeam())
                gameWorld.notifyDied((InternalRobot) obj);
        gameWorld.removeDead();
    }

    public void win() {
        for (InternalObject obj : gameWorld.getAllGameObjects())
            if ((obj instanceof InternalRobot) && obj.getTeam() == robot.getTeam())
                gameWorld.notifyDied((InternalRobot) obj);
        gameWorld.removeDead();
    }

    // ***********************************
    // ******** MISC. METHODS ************
    // ***********************************

    public void wearHat() throws GameActionException {
        assertNotMoving();
        if (!(robot.getHatCount() == 0 && robot.type == RobotType.HQ)) {
            assertHaveResource(GameConstants.HAT_ORE_COST);
            gameWorld.adjustResources(getTeam(), -GameConstants.HAT_ORE_COST);
        }
        robot.incrementHatCount();
        robot.activateMovement(new HatSignal(robot, gameWorld.randGen.nextInt()), 0, 1);
    }
   
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
        if (stringIndex >= 0 && stringIndex < GameConstants.NUMBER_OF_INDICATOR_STRINGS)
            (new IndicatorStringSignal(robot, stringIndex, newString)).accept(gameWorld);
    }

    public void addMatchObservation(String observation) {
        (new MatchObservationSignal(robot, observation)).accept(gameWorld);
    }

    public void breakpoint() {
        gameWorld.notifyBreakpoint();
    }
}
