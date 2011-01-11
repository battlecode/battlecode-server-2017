package battlecode.world;

import battlecode.common.Chassis;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotLevel;
import battlecode.common.Team;
import battlecode.common.TerrainTile;
import battlecode.engine.ErrorReporter;
import battlecode.engine.GenericWorld;
import battlecode.engine.instrumenter.RobotMonitor;
import battlecode.engine.signal.*;
import battlecode.serial.DominationFactor;
import battlecode.serial.GameStats;
import battlecode.serial.RoundStats;
import battlecode.world.signal.*;

/**
 * The primary implementation of the GameWorld interface for
 * containing and modifying the game map and the objects on it.
 */
/*
TODO:
- comments
- move methods from RCimpl to here, add signalhandler methods
 */
public class GameWorld extends BaseWorld<InternalObject> implements GenericWorld {

    private final GameMap gameMap;
    private RoundStats roundStats = null;	// stats for each round; new object is created for each round
    private final GameStats gameStats = new GameStats();		// end-of-game stats
    private double[] teamRoundResources = new double[2];
    private final Map<MapLocation3D, InternalObject> gameObjectsByLoc = new HashMap<MapLocation3D, InternalObject>();
    private double[] teamResources = new double[] { GameConstants.INITIAL_FLUX, GameConstants.INITIAL_FLUX };

    protected ArrayList<ComponentType> aTeamComponents = new ArrayList<ComponentType>();
    protected ArrayList<ComponentType> bTeamComponents = new ArrayList<ComponentType>();
    
    @SuppressWarnings("unchecked")
    public GameWorld(GameMap gm, String teamA, String teamB, long[][] oldArchonMemory) {
        super(gm.getSeed(), teamA, teamB, oldArchonMemory);
        gameMap = gm;
    }

    public int getMapSeed() {
        return gameMap.getSeed();
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public void processBeginningOfRound() {
        currentRound++;

        wasBreakpointHit = false;

        // process all gameobjects
        InternalObject[] gameObjects = new InternalObject[gameObjectsByID.size()];
        gameObjects = gameObjectsByID.values().toArray(gameObjects);
        for (int i = 0; i < gameObjects.length; i++) {
            gameObjects[i].processBeginningOfRound();
        }

    }

    public void processEndOfRound() {
        // process all gameobjects
        InternalObject[] gameObjects = new InternalObject[gameObjectsByID.size()];
        gameObjects = gameObjectsByID.values().toArray(gameObjects);
        for (int i = 0; i < gameObjects.length; i++) {
            gameObjects[i].processEndOfRound();
        }

        // calculate some stats
        double[] totalEnergon = new double[3];
        boolean teamADead = true, teamBDead = true;
        for (InternalObject obj : gameObjectsByID.values()) {
            if (!(obj instanceof InternalRobot))
                continue;
            InternalRobot r = (InternalRobot) obj;
			if(r.isOn()) {
            	Team team = r.getTeam();
            	totalEnergon[team.ordinal()] += r.getEnergonLevel();
				if(team==Team.A) teamADead = false;
				if(team==Team.B) teamBDead = false;
            }
        }

        long aPoints = Math.round(teamRoundResources[Team.A.ordinal()]*100), bPoints = Math.round(teamRoundResources[Team.B.ordinal()]*100);

        roundStats = new RoundStats(teamResources[0] * 100, teamResources[1] * 100, teamRoundResources[0] * 100 , teamRoundResources[1] * 100, aTeamComponents, bTeamComponents);
        teamRoundResources[0] = teamRoundResources[1] = 0;
        
		// the algorithm to determine the winner is:
        // (1) team that deactivated the other team's robots
		// (2) team that mined the most flux last turn
		// (3) team with the most hitpoints among activated robots
		// (4) team with the most flux
        // (5) team with the "smaller" team string
        // (6) Team A wins
        if (teamADead || teamBDead || currentRound >= gameMap.getMaxRounds() - 1) {

            running = false;

            for (InternalObject o : gameObjectsByID.values()) {
                if (o instanceof InternalRobot)
                    RobotMonitor.killRobot(o.getID());
            }

            //System.out.println("Game ended");

            gameStats.setPoints(Team.A, aPoints);
            gameStats.setPoints(Team.B, bPoints);
            gameStats.setTotalEnergon(Team.A, totalEnergon[0]);
            gameStats.setTotalEnergon(Team.B, totalEnergon[1]);
            if (!teamADead && teamBDead) {
                winner = Team.A;
                gameStats.setDominationFactor(DominationFactor.DESTROYED);
            } else if (!teamBDead && teamADead) {
                winner = Team.B;
                gameStats.setDominationFactor(DominationFactor.DESTROYED);
            } else if (aPoints > bPoints) {
				gameStats.setDominationFactor(DominationFactor.OWNED);
                winner = Team.A;
            } else if (bPoints > aPoints) {
                gameStats.setDominationFactor(DominationFactor.OWNED);
                winner = Team.B;
            } else if (totalEnergon[0]>totalEnergon[1]) {
                gameStats.setDominationFactor(DominationFactor.BEAT);
				winner = Team.A;
            } else if (totalEnergon[0]<totalEnergon[1]) {
				gameStats.setDominationFactor(DominationFactor.BEAT);
				winner = Team.B;
            } else if (teamResources[0]>teamResources[1]) {
				gameStats.setDominationFactor(DominationFactor.BARELY_BEAT);
				winner = Team.A;
			} else if (teamResources[0]<teamResources[1]) {
				gameStats.setDominationFactor(DominationFactor.BARELY_BEAT);
				winner = Team.B;
			} else {
                gameStats.setDominationFactor(DominationFactor.WON_BY_DUBIOUS_REASONS);
				if (teamAName.compareTo(teamBName) <= 0)
                	winner = Team.A;
                else
                    winner = Team.B;
            }
        }

    }

    public InternalObject getObject(MapLocation loc, RobotLevel level) {
        return gameObjectsByLoc.get(new MapLocation3D(loc, level));
    }

    public <T extends InternalObject> T getObjectOfType(MapLocation loc, RobotLevel level, Class<T> cl) {
        InternalObject o = getObject(loc, level);
        if (cl.isInstance(o))
            return cl.cast(o);
        else
            return null;
    }

    public InternalRobot getRobot(MapLocation loc, RobotLevel level) {
        InternalObject obj = getObject(loc, level);
        if (obj instanceof InternalRobot)
            return (InternalRobot) obj;
        else
            return null;
    }

    // should only be called by the InternalObject constructor
    public void notifyAddingNewObject(InternalObject o) {
        if (gameObjectsByID.containsKey(o.getID()))
            return;
        gameObjectsByID.put(o.getID(), o);
        if (o.getLocation() != null) {
            gameObjectsByLoc.put(new MapLocation3D(o.getLocation(), o.getRobotLevel()), o);
        }
    }

    public Collection<InternalObject> allObjects() {
        return gameObjectsByID.values();
    }

    // TODO: move stuff to here
    // should only be called by InternalObject.setLocation
    public void notifyMovingObject(InternalObject o, MapLocation oldLoc, MapLocation newLoc) {
        if (oldLoc != null) {
            MapLocation3D oldLoc3D = new MapLocation3D(oldLoc, o.getRobotLevel());
            if (gameObjectsByLoc.get(oldLoc3D) != o) {
                ErrorReporter.report("Internal Error: invalid oldLoc in notifyMovingObject");
                return;
            }
            gameObjectsByLoc.remove(oldLoc3D);
        }
        if (newLoc != null) {
            gameObjectsByLoc.put(new MapLocation3D(newLoc, o.getRobotLevel()), o);
        }
    }

    public void removeObject(InternalObject o) {
        if (o.getLocation() != null) {
            MapLocation3D loc3D = new MapLocation3D(o.getLocation(), o.getRobotLevel());
            if (gameObjectsByLoc.get(loc3D) == o)
                gameObjectsByLoc.remove(loc3D);
            else
                System.out.println("Couldn't remove " + o + " from the game");
        } else
            System.out.println("Couldn't remove " + o + " from the game");

        if (gameObjectsByID.get(o.getID()) == o)
            gameObjectsByID.remove(o.getID());

        if (o instanceof InternalRobot) {
            InternalRobot r = (InternalRobot) o;
            r.freeMemory();
        }
    }

    public boolean exists(InternalObject o) {
        return gameObjectsByID.containsKey(o.getID());
    }

    /**
     *@return the TerrainType at a given MapLocation <tt>loc<tt>
     */
    public TerrainTile getMapTerrain(MapLocation loc) {
        return gameMap.getTerrainTile(loc);
    }

    // TODO: optimize this too
    public int getUnitCount(Team team) {
        int result = 0;
        for (InternalObject o : gameObjectsByID.values()) {
            if (!(o instanceof InternalRobot))
                continue;
            if (((InternalRobot) o).getTeam() == team)
                result++;
        }

        return result;
    }

    public double getPoints(Team team) {
        return teamRoundResources[team.ordinal()];
    }

    public boolean canMove(InternalRobot r, Direction dir) {
        if (dir == Direction.NONE || dir == Direction.OMNI)
            return false;

        MapLocation loc = r.getLocation().add(dir);

        return canMove(r.getRobotLevel(), loc);
    }

    public boolean canMove(RobotLevel level, MapLocation loc) {

        return gameMap.getTerrainTile(loc).isTraversableAtHeight(level) && (gameObjectsByLoc.get(new MapLocation3D(loc, level)) == null);
    }

    public void splashDamageGround(MapLocation loc, double damage, double falloutFraction) {
        //TODO: optimize this
        InternalRobot[] robots = getAllRobotsWithinRadiusDonutSq(loc, 2, -1);
        for (InternalRobot r : robots) {
            if (r.getRobotLevel() == RobotLevel.ON_GROUND) {
                if (r.getLocation().equals(loc))
                    r.changeEnergonLevelFromAttack(-damage);
                else
                    r.changeEnergonLevelFromAttack(-damage * falloutFraction);
            }
        }
    }

    public InternalObject[] getAllGameObjects() {
        return gameObjectsByID.values().toArray(new InternalObject[gameObjectsByID.size()]);
    }

    public InternalRobot getRobotByID(int id) {
        return (InternalRobot) getObjectByID(id);
    }

    public Signal[] getAllSignals(boolean includeBytecodesUsedSignal) {
        ArrayList<InternalRobot> energonChangedRobots = new ArrayList<InternalRobot>();
        ArrayList<InternalRobot> allRobots = null;
        if (includeBytecodesUsedSignal)
            allRobots = new ArrayList<InternalRobot>();
        for (InternalObject obj : gameObjectsByID.values()) {
            if (!(obj instanceof InternalRobot))
                continue;
            InternalRobot r = (InternalRobot) obj;
            if (includeBytecodesUsedSignal)
                allRobots.add(r);
            if (r.clearEnergonChanged()) {
                energonChangedRobots.add(r);
            }
        }
        signals.add(new EnergonChangeSignal(energonChangedRobots.toArray(new InternalRobot[]{})));
        if (includeBytecodesUsedSignal)
            signals.add(new BytecodesUsedSignal(allRobots.toArray(new InternalRobot[]{})));
        return signals.toArray(new Signal[signals.size()]);
    }

    public RoundStats getRoundStats() {
        return roundStats;
    }

    public GameStats getGameStats() {
        return gameStats;
    }

    public void beginningOfExecution(int robotID) {
        InternalRobot r = (InternalRobot) getObjectByID(robotID);
        if (r != null)
            r.processBeginningOfTurn();
    }

    public void endOfExecution(int robotID) {
        InternalRobot r = (InternalRobot) getObjectByID(robotID);
		// if the robot is dead, it won't be in the map any more
		if(r!=null) {
        	r.setBytecodesUsed(RobotMonitor.getBytecodesUsed());
        	r.processEndOfTurn();
		}
    }

    public void resetStatic() {
    }

    public InternalMine createMine(MapLocation loc) {
        InternalMine m = new InternalMine(this, loc);
        notifyAddingNewObject(m);
        addSignal(new MineBirthSignal(m));
        return m;
    }
    // ******************************
    // SIGNAL HANDLER METHODS
    // ******************************
    SignalHandler<Exception> signalHandler = new AutoSignalHandler<Exception>(this) {

        public Exception exceptionResponse(Exception e) {
            return e;
        }
    };

    public Exception visitSignal(Signal s) {
        return signalHandler.visitSignal(s);
    }

    public Exception visitAttackSignal(AttackSignal s) {
        try {
            InternalRobot attacker = (InternalRobot) getObjectByID(s.getRobotID());
            MapLocation targetLoc = s.getTargetLoc();
            RobotLevel level = s.getTargetHeight();
            InternalRobot target = getRobot(targetLoc, level);

            double totalDamage = s.damage;

            if (target != null) {
                // takeDamage is responsible for checking the armor
                target.takeDamage(totalDamage);
            }

            /* splash, in case we still want it
            if (attacker.getRobotType() == RobotType.CHAINER) {
            InternalRobot[] hits = getAllRobotsWithinRadiusDonutSq(targetLoc, GameConstants.CHAINER_SPLASH_RADIUS_SQUARED, -1);
            for (InternalRobot r : hits) {
            if (r.getRobotLevel() == level)
            r.changeEnergonLevelFromAttack(-totalDamage);
            }
            } else if (target != null) {
            target.changeEnergonLevelFromAttack(-totalDamage);
            }
             */

            addSignal(s);
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    public Exception visitBroadcastSignal(BroadcastSignal s) {
        InternalObject sender = gameObjectsByID.get(s.robotID);
        Collection<InternalObject> objs = gameObjectsByLoc.values();
        Predicate<InternalObject> pred = Util.robotWithinDistance(sender.getLocation(), s.range);
        for (InternalObject o : Iterables.filter(objs, pred)) {
            InternalRobot r = (InternalRobot) o;
			if(r!=sender)
            	r.enqueueIncomingMessage((Message) s.message.clone());
        }
        s.message = null;
        addSignal(s);
        return null;
    }

    public Exception visitDeathSignal(DeathSignal s) {
        if (!running) {
            // All robots emit death signals after the game
            // ends.  We still want the client to draw
            // the robots.
            return null;
        }
        try {
            int ID = s.getObjectID();
            InternalObject obj = getObjectByID(ID);

            if (obj instanceof InternalRobot) {
                InternalRobot r = (InternalRobot) obj;
                RobotMonitor.killRobot(ID);
                if (r.hasBeenAttacked()) {
                    gameStats.setUnitKilled(r.getTeam(), currentRound);
                }
            }
            if (obj != null) {
                removeObject(obj);
                addSignal(s);
            }
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    public Exception visitEnergonChangeSignal(EnergonChangeSignal s) {
        int[] robotIDs = s.getRobotIDs();
        double[] energon = s.getEnergon();
        for (int i = 0; i < robotIDs.length; i++) {
            try {
                InternalRobot r = (InternalRobot) getObjectByID(robotIDs[i]);
                System.out.println("el " + energon[i] + " " + r.getEnergonLevel());
                r.changeEnergonLevel(energon[i] - r.getEnergonLevel());
            } catch (Exception e) {
                return e;
            }
        }
        return null;
    }

    public Exception visitEquipSignal(EquipSignal s) {
        InternalRobot r = (InternalRobot) getObjectByID(s.robotID);
        r.equip(s.component);
        addSignal(s);
        Team objTeam = r.getTeam();
        if(objTeam.equals(Team.A)){
        	aTeamComponents.add(s.component);
        }else{
        	bTeamComponents.add(s.component);
        }
        return null;
    }

    public Exception visitIndicatorStringSignal(IndicatorStringSignal s) {
        try {
            addSignal(s);
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    public Exception visitIronShieldSignal(IronShieldSignal s) {
        InternalRobot r = getRobotByID(s.robotID);
        r.activateShield();
        addSignal(s);
        return null;
    }

    public Exception visitLoadSignal(LoadSignal s) {
        InternalRobot transport = getRobotByID(s.transportID);
        InternalRobot passenger = getRobotByID(s.passengerID);
        transport.addPassenger(passenger);
        passenger.loadOnto(transport);
        addSignal(s);
        return null;
    }

    public Exception visitUnloadSignal(UnloadSignal s) {
        InternalRobot transport = getRobotByID(s.transportID);
        InternalRobot passenger = getRobotByID(s.passengerID);
        transport.removePassenger(passenger);
        passenger.unloadTo(s.unloadLoc);
        addSignal(s);
        return null;
    }

    public Exception visitMatchObservationSignal(MatchObservationSignal s) {
        addSignal(s);
        return null;
    }

    public Exception visitControlBitsSignal(ControlBitsSignal s) {
        try {
            InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
            r.setControlBits(s.getControlBits());

            addSignal(s);
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    public Exception visitMovementOverrideSignal(MovementOverrideSignal s) {
        try {
            InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
            if (!canMove(r.getRobotLevel(), s.getNewLoc()))
                return new GameActionException(GameActionExceptionType.CANT_MOVE_THERE, "Cannot move to location: " + s.getNewLoc());
            r.setLocation(s.getNewLoc());
        } catch (Exception e) {
            return e;
        }
        addSignal(s);
        return null;
    }

    public Exception visitMovementSignal(MovementSignal s) {
        try {
            InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
            MapLocation loc = s.getNewLoc();//(s.isMovingForward() ? r.getLocation().add(r.getDirection()) : r.getLocation().add(r.getDirection().opposite()));

            r.setLocation(loc);

            addSignal(s);
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    public Exception visitSetDirectionSignal(SetDirectionSignal s) {
        try {
            InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
            Direction dir = s.getDirection();

            r.setDirection(dir);

            addSignal(s);
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    public Exception visitSpawnSignal(SpawnSignal s) {
        try {
            InternalRobot parent;
            int parentID = s.getParentID();
            MapLocation loc;
            if (parentID == 0) {
                parent = null;
                loc = s.getLoc();
                if (loc == null) {
                    ErrorReporter.report("Null parent and loc in visitSpawnSignal", true);
                    return new Exception();
                }
            } else {
                parent = (InternalRobot) getObjectByID(parentID);
                loc = s.getLoc();
            }

            //note: this also adds the signal
            if (s.getType() == Chassis.DUMMY) {
                InternalRobot dummy = new InternalRobot(this, s.getType(), loc, s.getTeam(), false);
                this.addSignal(new SpawnSignal(dummy, parent));
            } else {
                GameWorldFactory.createPlayer(this, s.getType(), loc, s.getTeam(), parent);
            }

        } catch (Exception e) {
            return e;
        }
        return null;
    }
    
	public Exception visitTurnOnSignal(TurnOnSignal s) {
		for(int i : s.robotIDs)
			getRobotByID(i).setPower(true);
		addSignal(s);
		return null;
	}

	public Exception visitTurnOffSignal(TurnOffSignal s) {
		getRobotByID(s.robotID).setPower(false);
		addSignal(s);
		return null;
	}
	
	// *****************************
    //    UTILITY METHODS
    // *****************************
    private static MapLocation origin = new MapLocation(0, 0);

    protected static boolean inAngleRange(MapLocation sensor, Direction dir, MapLocation target, double cosHalfTheta) {
        MapLocation dirVec = origin.add(dir);
        double dx = target.getX() - sensor.getX();
        double dy = target.getY() - sensor.getY();
        int a = dirVec.getX();
        int b = dirVec.getY();
        double dotProduct = a * dx + b * dy;

        if (dotProduct < 0) {
            if (cosHalfTheta > 0)
                return false;
        } else if (cosHalfTheta < 0)
            return true;

        double rhs = cosHalfTheta * cosHalfTheta * (dx * dx + dy * dy) * (a * a + b * b);

        if (dotProduct < 0)
            return (dotProduct * dotProduct <= rhs + 0.00001d);
        else
            return (dotProduct * dotProduct >= rhs - 0.00001d);
    }

    // TODO: make a faster implementation of this
    private InternalRobot[] getAllRobotsWithinRadiusDonutSq(MapLocation center, int outerRadiusSquared, int innerRadiusSquared) {
        ArrayList<InternalRobot> robots = new ArrayList<InternalRobot>();

        for (InternalObject o : gameObjectsByID.values()) {
            if (!(o instanceof InternalRobot))
                continue;
            if (o.getLocation() != null && o.getLocation().distanceSquaredTo(center) <= outerRadiusSquared
                    && o.getLocation().distanceSquaredTo(center) > innerRadiusSquared)
                robots.add((InternalRobot) o);
        }

        return robots.toArray(new InternalRobot[robots.size()]);
    }

    // TODO: make a faster implementation of this
    public MapLocation[] getAllMapLocationsWithinRadiusSq(MapLocation center, int radiusSquared) {
        ArrayList<MapLocation> locations = new ArrayList<MapLocation>();

        int radius = (int) Math.sqrt(radiusSquared);

        int minXPos = center.getX() - radius;
        int maxXPos = center.getX() + radius;
        int minYPos = center.getY() - radius;
        int maxYPos = center.getY() + radius;

        for (int x = minXPos; x <= maxXPos; x++) {
            for (int y = minYPos; y <= maxYPos; y++) {
                MapLocation loc = new MapLocation(x, y);
                TerrainTile tile = gameMap.getTerrainTile(loc);
                if (!tile.equals(TerrainTile.OFF_MAP) && loc.distanceSquaredTo(center) < radiusSquared)
                    locations.add(loc);
            }
        }

        return locations.toArray(new MapLocation[locations.size()]);
    }

    public double resources(Team t) {
        return teamResources[t.ordinal()];
    }

    protected boolean spendResources(Team t, double amount) {
        if (teamResources[t.ordinal()] >= amount) {
            teamResources[t.ordinal()] -= amount;
            return true;
        } else
            return false;
    }

    protected void adjustResources(Team t, double amount) {
        if(amount >= GameConstants.MINE_DEPLETED_RESOURCES)
            teamRoundResources[t.ordinal()] += amount;
        teamResources[t.ordinal()] += amount;
    }

}
