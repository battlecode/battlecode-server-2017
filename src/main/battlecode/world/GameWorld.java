package battlecode.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TerrainTile;
import battlecode.common.Upgrade;
import battlecode.engine.ErrorReporter;
import battlecode.engine.GenericWorld;
import battlecode.engine.instrumenter.RobotDeathException;
import battlecode.engine.instrumenter.RobotMonitor;
import battlecode.engine.signal.AutoSignalHandler;
import battlecode.engine.signal.Signal;
import battlecode.engine.signal.SignalHandler;
import battlecode.serial.DominationFactor;
import battlecode.serial.GameStats;
import battlecode.serial.RoundStats;
import battlecode.world.signal.AttackSignal;
import battlecode.world.signal.BroadcastSignal;
import battlecode.world.signal.BytecodesUsedSignal;
import battlecode.world.signal.CaptureSignal;
import battlecode.world.signal.ControlBitsSignal;
import battlecode.world.signal.DeathSignal;
import battlecode.world.signal.EnergonChangeSignal;
import battlecode.world.signal.FluxChangeSignal;
import battlecode.world.signal.HatSignal;
import battlecode.world.signal.IndicatorStringSignal;
import battlecode.world.signal.MatchObservationSignal;
import battlecode.world.signal.MineSignal;
import battlecode.world.signal.MinelayerSignal;
import battlecode.world.signal.MovementOverrideSignal;
import battlecode.world.signal.MovementSignal;
import battlecode.world.signal.NodeBirthSignal;
import battlecode.world.signal.RegenSignal;
import battlecode.world.signal.ResearchSignal;
import battlecode.world.signal.ResearchChangeSignal;
import battlecode.world.signal.ScanSignal;
import battlecode.world.signal.SetDirectionSignal;
import battlecode.world.signal.ShieldChangeSignal;
import battlecode.world.signal.ShieldSignal;
import battlecode.world.signal.SpawnSignal;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * The primary implementation of the GameWorld interface for
 * containing and modifying the game map and the objects on it.
 */
/*
oODO:
- comments
- move methods from RCimpl to here, add signalhandler methods
 */
public class GameWorld extends BaseWorld<InternalObject> implements GenericWorld {

    private final GameMap gameMap;
    private RoundStats roundStats = null;    // stats for each round; new object is created for each round
    private final GameStats gameStats = new GameStats();        // end-of-game stats
    private double[] teamRoundResources = new double[2];
    private double[] lastRoundResources = new double[2];
    private final Map<MapLocation3D, InternalObject> gameObjectsByLoc = new HashMap<MapLocation3D, InternalObject>();
    private double[] teamResources = new double[2];
    private double[] teamSpawnRate = new double[2];
    private int[] teamCapturingNumber = new int[2];

    private List<MapLocation> encampments = new ArrayList<MapLocation>();
    private Map<MapLocation, Team> encampmentMap = new HashMap<MapLocation, Team>();
    private Map<Team, InternalRobot> baseHQs = new EnumMap<Team, InternalRobot>(Team.class);
    private Map<MapLocation, Team> mineLocations = new HashMap<MapLocation, Team>();
    private Map<Team, GameMap.MapMemory> mapMemory = new EnumMap<Team, GameMap.MapMemory>(Team.class);
    private Map<Team, Set<MapLocation>> knownMineLocations = new EnumMap<Team, Set<MapLocation>>(Team.class);
    private Map<Team, Map<Upgrade, Integer>> research = new EnumMap<Team, Map<Upgrade, Integer>>(Team.class);
    
    private Map<Team, Set<Upgrade>> upgrades = new EnumMap<Team, Set<Upgrade>>(Team.class);
    private Map<Integer, Integer> radio = new HashMap<Integer, Integer>();

    // robots to remove from the game at end of turn
    private List<InternalRobot> deadRobots = new ArrayList<InternalRobot>();

    @SuppressWarnings("unchecked")
    public GameWorld(GameMap gm, String teamA, String teamB, long[][] oldArchonMemory) {
        super(gm.getSeed(), teamA, teamB, oldArchonMemory);
        gameMap = gm;
        mapMemory.put(Team.A, new GameMap.MapMemory(gameMap));
        mapMemory.put(Team.B, new GameMap.MapMemory(gameMap));
        mapMemory.put(Team.NEUTRAL, new GameMap.MapMemory(gameMap));
        upgrades.put(Team.A, EnumSet.noneOf(Upgrade.class));
        upgrades.put(Team.B, EnumSet.noneOf(Upgrade.class));
        knownMineLocations.put(Team.A, new HashSet<MapLocation>());
        knownMineLocations.put(Team.B, new HashSet<MapLocation>());
        research.put(Team.A, new EnumMap<Upgrade, Integer>(Upgrade.class));
        research.put(Team.B, new EnumMap<Upgrade, Integer>(Upgrade.class));
    }
    
    public GameMap.MapMemory getMapMemory(Team t) {
    	return mapMemory.get(t);
    }

    public int getMapSeed() {
        return gameMap.getSeed();
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public void processBeginningOfRound() {
        currentRound++;
        
        nextID += randGen.nextInt(10);

        wasBreakpointHit = false;

        // reset necessary game constants
        teamSpawnRate = new double[]{GameConstants.HQ_SPAWN_DELAY, GameConstants.HQ_SPAWN_DELAY};
        
        // process all gameobjects
        InternalObject[] gameObjects = new InternalObject[gameObjectsByID.size()];
        gameObjects = gameObjectsByID.values().toArray(gameObjects);
        for (int i = 0; i < gameObjects.length; i++) {
            gameObjects[i].processBeginningOfRound();
        }

    }

    public double getEnergonDifference() {
        double diff = 0.;
        for (InternalObject o : gameObjectsByID.values())
            if (o instanceof InternalRobot) {
                double energon = ((InternalRobot) o).getEnergonLevel();
                if (o.getTeam() == Team.A)
                    diff += energon;
                else if (o.getTeam() == Team.B)
                    diff -= energon;
            }
        return diff;
    }
    
    public int getMineDifference() {
        int diff = 0;
        for (Entry<MapLocation, Team> o : mineLocations.entrySet())
            if (o.getValue() == Team.A)
            	diff++;
            else if (o.getValue() == Team.B)
            	diff--;
        return diff;
    }
    
    public int getNumCapturing(Team team) {
    	return teamCapturingNumber[team.ordinal()];
    }

    public void processEndOfRound() {
        // process all gameobjects
        InternalObject[] gameObjects = new InternalObject[gameObjectsByID.size()];
        gameObjects = gameObjectsByID.values().toArray(gameObjects);
        for (int i = 0; i < gameObjects.length; i++) {
            gameObjects[i].processEndOfRound();
        }
        removeDead();
        
        addSignal(new FluxChangeSignal(teamResources));
		addSignal(new ResearchChangeSignal(research));

        if (timeLimitReached() && winner == null) {
            
//        	time limit damage to HQs
        	for (InternalRobot r : baseHQs.values()) {
        		r.takeDamage(GameConstants.TIME_LIMIT_DAMAGE);
        	}
        	removeDead();
        	
//        	if both are killed by time limit damage in the same round, then more tie breakers
        	if (baseHQs.get(Team.A).getEnergonLevel() <= 0.0 && baseHQs.get(Team.B).getEnergonLevel() <= 0.0) {
        		// TODO more TIE BREAKHERS HERE
            	InternalRobot HQA = baseHQs.get(Team.A);
            	InternalRobot HQB = baseHQs.get(Team.B);
            	double diff = HQA.getEnergonLevel() - HQB.getEnergonLevel();
            	
            	double campdiff = getEncampmentsByTeam(Team.A).size() - getEncampmentsByTeam(Team.B).size();
            	
            	if (!(
            			// first tie breaker - encampment count
            		       setWinnerIfNonzero(campdiff, DominationFactor.BARELY_BEAT)
            		    // second tie breaker - total energon difference
            			|| setWinnerIfNonzero(getEnergonDifference(), DominationFactor.BARELY_BEAT)
            			// third tie breaker - mine count
            			|| setWinnerIfNonzero(getMineDifference(), DominationFactor.BARELY_BEAT)
            		 ))
            	{
            			// fourth tie breaker - power difference
            		if (!(setWinnerIfNonzero(teamResources[Team.A.ordinal()] - teamResources[Team.B.ordinal()], DominationFactor.BARELY_BEAT)))
            		{
	            		if (HQA.getID() < HQB.getID())
	                        setWinner(Team.B, DominationFactor.WON_BY_DUBIOUS_REASONS);
	                    else
	                        setWinner(Team.A, DominationFactor.WON_BY_DUBIOUS_REASONS);
            		}
            	}
        	}
        }

        if (winner != null) {
            running = false;
            for (InternalObject o : gameObjectsByID.values()) {
                if (o instanceof InternalRobot)
                    RobotMonitor.killRobot(o.getID());
            }
        }

        long aPoints = Math.round(teamRoundResources[Team.A.ordinal()] * 100), bPoints = Math.round(teamRoundResources[Team.B.ordinal()] * 100);

        roundStats = new RoundStats(teamResources[0] * 100, teamResources[1] * 100, teamRoundResources[0] * 100, teamRoundResources[1] * 100);
        
        for (int x=0; x<teamResources.length; x++)
        {
        	if (hasUpgrade(Team.values()[x], Upgrade.FUSION))
            	teamResources[x] = teamResources[x]*GameConstants.POWER_DECAY_RATE_FUSION;
        	else
        		teamResources[x] = teamResources[x]*GameConstants.POWER_DECAY_RATE;
//        	System.out.print(teamResources[x]+" ");
        }
//        System.out.println();
        
        lastRoundResources = teamRoundResources;
        teamRoundResources = new double[2];

    }

    public boolean setWinnerIfNonzero(double n, DominationFactor d) {
        if (n > 0)
            setWinner(Team.A, d);
        else if (n < 0)
            setWinner(Team.B, d);
        return n != 0;
    }
    
    public int countEncampments(Team t) {
    	int total = 0;
    	Iterable<MapLocation> camps = getEncampmentsByTeam(t);
    	for (MapLocation camp : camps)
    		total++;
    	return total;
    }

    public DominationFactor getDominationFactor(Team winner) {
    	// TODO this needs to be recoded
    	// TODO CORY FIX IT
        if (countEncampments(winner) > 2*countEncampments(winner.opponent()))
            return DominationFactor.DESTROYED;
        else if (!timeLimitReached())
            return DominationFactor.OWNED;
        else
            return DominationFactor.BEAT;
    }

    public void setWinner(Team t, DominationFactor d) {
        winner = t;
        gameStats.setDominationFactor(d);
        //running = false;

    }

    public InternalRobot getBaseHQ(Team t) {
        return baseHQs.get(t);
    }

    public boolean timeLimitReached() {
        return currentRound >= gameMap.getMaxRounds() - 1;
    }

    public double[] getLastRoundResources() {
        return lastRoundResources;
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
    
    public Map<MapLocation, Team> getMineMaps() {
    	return mineLocations;
    }
    
    public Set<MapLocation> getKnownMineMap(Team t) {
    	return knownMineLocations.get(t);
    }
    
    public MapLocation[] getKnownMines(Team t) {
    	ArrayList<MapLocation> locs = new ArrayList<MapLocation>();
    	for (MapLocation m : knownMineLocations.get(t)) {
    		if (mineLocations.get(m) != null && mineLocations.get(m) != t)
    			locs.add(m);
    	}
    	return locs.toArray(new MapLocation[]{});
    }
    
    public void addKnownMineLocation(Team t, MapLocation loc) {
    	knownMineLocations.get(t).add(loc);
    }
    
    
    public boolean isKnownMineLocation(Team t, MapLocation loc) {
    	return knownMineLocations.get(t).contains(loc);
    }
    
    public void addMine(Team t, MapLocation loc) {
    	if(mineLocations.get(loc) == null) {
    		mineLocations.put(loc, t);
    		if(t==Team.A || t==Team.B)
    			addKnownMineLocation(t, loc);
    	}
    }
    
    public void removeMines(Team t, MapLocation loc) {
    	mineLocations.remove(loc);
    	knownMineLocations.get(t).remove(loc);
    	if (t != Team.NEUTRAL)
    		knownMineLocations.get(t.opponent()).remove(loc);
    }
    
    public Team getMine(MapLocation loc) {
    	return mineLocations.get(loc);
    }
    
    public void researchUpgrade(Team t, Upgrade u) {
    	Integer i = research.get(t).get(u);
    	if (i == null) i = 0;
    	i = i+1;
    	research.get(t).put(u, i);
    	if (i == u.numRounds)
    		addUpgrade(t, u);
    	
    	nextID += (randGen.nextDouble()<0.3) ? 1 : 0;
    }
    
    public int getUpgradeProgress(Team t, Upgrade u) {
    	Integer i = research.get(t).get(u);
    	if (i == null) i = 0;
    	return i;
    }
    

    // should only be called by the InternalObject constructor
    public void notifyAddingNewObject(InternalObject o) {
        if (gameObjectsByID.containsKey(o.getID()))
            return;
        gameObjectsByID.put(o.getID(), o);
        if (o.getLocation() != null) {
            gameObjectsByLoc.put(new MapLocation3D(o.getLocation(), o.getRobotLevel()), o);
        }
//        if (o instanceof InternalEncampment)
//        {
//        	addEncampment((InternalEncampment)o);
//        }
    }
    
    public boolean isEncampment(MapLocation loc) {
    	return encampmentMap.containsKey(loc);
    }
    
    public void addEncampment(MapLocation camp, Team team) {
    	encampments.add(camp);
    	encampmentMap.put(camp, team);
    }
    
    public Team getEncampment(MapLocation loc) {
    	return encampmentMap.get(loc);
    }
    
    public List<MapLocation> getAllEncampments() {
    	return encampments;
    }
    
    public List<MapLocation> getEncampmentsByTeam(final Team t) {
    	ArrayList<MapLocation> camps = new ArrayList<MapLocation>();
    	for (Entry<MapLocation, Team> entry : encampmentMap.entrySet())
    		if (entry.getValue() == t)
    			camps.add(entry.getKey());
        return camps;
    }
    
    public Map<MapLocation, Team> getEncampmentMap() {
    	return encampmentMap;
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
            	if (o instanceof InternalRobot) {
            		InternalRobot ir = (InternalRobot) o;
            		if (ir.type == RobotType.SOLDIER && ir.getCapturingRounds() == -1)
            			; // don't do anything
            		else
            			System.out.println("Couldn't remove " + o + " from the game");
            	} else
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
     * @return the TerrainType at a given MapLocation <tt>loc<tt>
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
    
    public double getSpawnRate(Team team) {
    	return teamSpawnRate[team.ordinal()];
    }

    public double getPoints(Team team) {
        return teamRoundResources[team.ordinal()];
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
        ArrayList<InternalRobot> shieldChangedRobots = new ArrayList<InternalRobot>();
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
            if (r.clearShieldChanged()) {
            	shieldChangedRobots.add(r);
            }
        }
        signals.add(new EnergonChangeSignal(energonChangedRobots.toArray(new InternalRobot[]{})));
        signals.add(new ShieldChangeSignal(shieldChangedRobots.toArray(new InternalRobot[]{})));

        if (includeBytecodesUsedSignal)
            signals.add(new BytecodesUsedSignal(allRobots.toArray(new InternalRobot[]{})));
        return signals.toArray(new Signal[signals.size()]);
    }
    
    public int getMessage(int channel) {
    	Integer val = radio.get(channel);
    	return val == null ? 0 : val;
    }
    
    public boolean hasUpgrade(Team t, Upgrade upgrade) {
    	return upgrades.get(t).contains(upgrade);
    }
    
    public void addUpgrade(Team t, Upgrade upgrade) {
    	if(upgrade == Upgrade.NUKE) {
    		getBaseHQ(t.opponent()).suicide();
    	} else {
    		upgrades.get(t).add(upgrade);
    	}
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
        if (r != null) {
            r.setBytecodesUsed(RobotMonitor.getBytecodesUsed());
            r.processEndOfTurn();
        }
    }

    public void resetStatic() {
    }

    public void notifyDied(InternalRobot r) {
        deadRobots.add(r);
    }

    public void removeDead() {
        boolean current = false;
        for (InternalRobot r : deadRobots) {
            if (r.getID() == RobotMonitor.getCurrentRobotID())
                current = true;
            visitSignal(new DeathSignal(r));
        }
        if (current)
            throw new RobotDeathException();
    }
    
    public void setHQ(InternalRobot r, Team t) {
        baseHQs.put(t, r);
    }

    // ******************************
    // SIGNAL HANDLER METHODS
    // ******************************
    SignalHandler signalHandler = new AutoSignalHandler(this);

    public void visitSignal(Signal s) {
        signalHandler.visitSignal(s);
    }

    public void visitAttackSignal(AttackSignal s) {
    	
        InternalRobot attacker = (InternalRobot) getObjectByID(s.getRobotID());
        MapLocation targetLoc = s.getTargetLoc();
        RobotLevel level = s.getTargetHeight();
        
        switch (attacker.type) {
		case SOLDIER:
			Direction dir = Direction.NORTH;
	    	MapLocation nearby;
	    	InternalRobot nearbyrobot;
	    	ArrayList<InternalRobot> todamage = new ArrayList<InternalRobot>();
	    	do {
	    		nearby = targetLoc.add(dir);
	    		nearbyrobot = getRobot(nearby, level);
	    		if (nearbyrobot != null)
	    			if (nearbyrobot.getTeam() != attacker.getTeam())
	    				todamage.add(nearbyrobot);
	    		dir = dir.rotateLeft();
	    	} while (dir != Direction.NORTH);
	    	if (todamage.size()>0) {
	            double damage = attacker.type.attackPower/todamage.size();
	            for (InternalRobot r : todamage)
	            	r.takeDamage(damage, attacker);
	    	}
			break;
		case ARTILLERY:
			InternalRobot target;
			for (int dx = -1; dx <= 1; dx++)
				for (int dy = -1; dy <= 1; dy++) {

					target = getRobot(targetLoc.add(dx, dy), level);

					if (target != null)
						if (dx == 0 && dy == 0)
							target.takeDamage(attacker.type.attackPower, attacker);
						else
							target.takeDamage(attacker.type.attackPower*GameConstants.ARTILLERY_SPLASH_RATIO, attacker);
				}

			break;
		default:
			// ERROR, should never happen
		}
        
        // TODO if we want units to not damange allied units
        // TODO CORY FIX IT
//        switch (attacker.type) {
//            case SOLDIER:
//                break;
//            case ARTILLERY:
//            	int dist = (int)Math.sqrt(GameConstants.ARTILLERY_SPLASH_RADIUS_SQUARED);
//            	InternalRobot target;
//                for (int dx=-dist; dx<=dist; dx++)
//                	for (int dy=-dist; dy<=dist; dy++)
//                	{
//                		if (dx==0 && dy==0) continue;
//                		target = getRobot(targetLoc.add(dx, dy), level);
//                		if (target != null)
//                			target.takeDamage(attacker.type.attackPower, attacker);
//                	}
//                		
//                break;
//            default:
//            	// ERROR, should never happen
//        }
        
        addSignal(s);
        removeDead();
    }

    public void visitBroadcastSignal(BroadcastSignal s) {        
    	radio.putAll(s.broadcastMap);
    	s.broadcastMap = null;
        addSignal(s);
    }

    public void visitDeathSignal(DeathSignal s) {
        if (!running) {
            // All robots emit death signals after the game
            // ends.  We still want the client to draw
            // the robots.
            return;
        }
        int ID = s.getObjectID();
        InternalObject obj = getObjectByID(ID);

        if (obj != null) {
            removeObject(obj);
            addSignal(s);
        }
        if (obj instanceof InternalRobot) {
            InternalRobot r = (InternalRobot) obj;
            RobotMonitor.killRobot(ID);
    		if (r.type == RobotType.SOLDIER && (r.getCapturingType() != null))
    			teamCapturingNumber[r.getTeam().ordinal()]--;
            if (r.hasBeenAttacked()) {
                gameStats.setUnitKilled(r.getTeam(), currentRound);
            }
            if (r.type == RobotType.HQ) {
            	setWinner(r.getTeam().opponent(), getDominationFactor(r.getTeam().opponent()));
            } else if (r.type.isEncampment)
            {
            	encampmentMap.put(r.getLocation(), Team.NEUTRAL);
            }
        }
    }

    public void visitEnergonChangeSignal(EnergonChangeSignal s) {
        int[] robotIDs = s.getRobotIDs();
        double[] energon = s.getEnergon();
        for (int i = 0; i < robotIDs.length; i++) {
            InternalRobot r = (InternalRobot) getObjectByID(robotIDs[i]);
            System.out.println("el " + energon[i] + " " + r.getEnergonLevel());
            r.changeEnergonLevel(energon[i] - r.getEnergonLevel());
        }
    }
    
    public void visitShieldChangeSignal(ShieldChangeSignal s) {
        int[] robotIDs = s.getRobotIDs();
        double[] shield = s.getShield();
        for (int i = 0; i < robotIDs.length; i++) {
            InternalRobot r = (InternalRobot) getObjectByID(robotIDs[i]);
            System.out.println("sh " + shield[i] + " " + r.getEnergonLevel());
            r.changeShieldLevel(shield[i] - r.getShieldLevel());
        }
    }

    public void visitIndicatorStringSignal(IndicatorStringSignal s) {
        addSignal(s);
    }

    public void visitMatchObservationSignal(MatchObservationSignal s) {
        addSignal(s);
    }
    
    public void visitHatSignal(HatSignal s) {
    	addSignal(s);
    }

    public void visitControlBitsSignal(ControlBitsSignal s) {
        InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
        r.setControlBits(s.getControlBits());

        addSignal(s);
    }

    public void visitMovementOverrideSignal(MovementOverrideSignal s) {
        InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
        if (!canMove(r.getRobotLevel(), s.getNewLoc()))
            throw new RuntimeException("GameActionException in MovementOverrideSignal", new GameActionException(GameActionExceptionType.CANT_MOVE_THERE, "Cannot move to location: " + s.getNewLoc()));
        r.setLocation(s.getNewLoc());
        addSignal(s);
    }

    public void visitMovementSignal(MovementSignal s) {
        InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
        MapLocation loc = s.getNewLoc();//(s.isMovingForward() ? r.getLocation().add(r.getDirection()) : r.getLocation().add(r.getDirection().opposite()));

        r.setLocation(loc);

        addSignal(s);
    }

    public void visitSetDirectionSignal(SetDirectionSignal s) {
        InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
        Direction dir = s.getDirection();

        r.setDirection(dir);

        addSignal(s);
    }

    @SuppressWarnings("unchecked")
    public void visitSpawnSignal(SpawnSignal s) {
        InternalRobot parent;
        int parentID = s.getParentID();
        MapLocation loc;
        if (parentID == 0) {
            parent = null;
            loc = s.getLoc();
        } else {
            parent = (InternalRobot) getObjectByID(parentID);
            loc = s.getLoc();
        }
        
        if (s.getType().isEncampment)
        {
        	encampmentMap.put(s.getLoc(), s.getTeam());
        }

        //note: this also adds the signal
        InternalRobot robot = GameWorldFactory.createPlayer(this, s.getType(), loc, s.getTeam(), parent);
        
        
    }
    
    public void visitResearchSignal(ResearchSignal s) {
    	InternalRobot hq = (InternalRobot)getObjectByID(s.getRobotID());
//    	hq.setResearching(s.getUpgrade());
    	researchUpgrade(hq.getTeam(), s.getUpgrade());
    	addSignal(s);
    }
    
    public void visitMinelayerSignal(MinelayerSignal s) {
    	// noop
    	addSignal(s);
    }
    
    public void visitCaptureSignal(CaptureSignal s) {
    	// noop
    	teamCapturingNumber[s.getTeam().ordinal()]++;
    	addSignal(s);
    }
    
    public void visitMineSignal(MineSignal s) {
    	MapLocation loc = s.getMineLoc();
    	if (s.shouldAdd()) {
        	if (gameMap.getTerrainTile(loc) == TerrainTile.LAND) {
        		addMine(s.getMineTeam(), loc);
        	}
    	} else {
    		if (s.getMineTeam() != getMine(s.getMineLoc()))
    			removeMines(s.getMineTeam(), loc);
    	}
    	addSignal(s);
    }
    
    public void visitRegenSignal(RegenSignal s) {
    	InternalRobot medbay = (InternalRobot) getObjectByID(s.robotID);
    	
    	MapLocation targetLoc = medbay.getLocation();
    	RobotLevel level = RobotLevel.ON_GROUND;
    	
    	int dist = (int)Math.sqrt(medbay.type.attackRadiusMaxSquared);
    	InternalRobot target;
        for (int dx=-dist; dx<=dist; dx++)
        	for (int dy=-dist; dy<=dist; dy++)
        	{
        		if (dx*dx+dy*dy > medbay.type.attackRadiusMaxSquared) continue;
        		target = getRobot(targetLoc.add(dx, dy), level);
        		if (target != null)
        			if (target.getTeam() == medbay.getTeam() && target.type != RobotType.HQ)
        				target.takeDamage(-medbay.type.attackPower, medbay);
        	}
        addSignal(s);
    }
    
    public void visitShieldSignal(ShieldSignal s) {
    	InternalRobot shields = (InternalRobot) getObjectByID(s.robotID);
    	
    	MapLocation targetLoc = shields.getLocation();
    	RobotLevel level = RobotLevel.ON_GROUND;
    	
    	int dist = (int)Math.sqrt(shields.type.attackRadiusMaxSquared);
    	InternalRobot target;
        for (int dx=-dist; dx<=dist; dx++)
        	for (int dy=-dist; dy<=dist; dy++)
        	{
        		if (dx*dx+dy*dy > shields.type.attackRadiusMaxSquared) continue;
        		target = getRobot(targetLoc.add(dx, dy), level);
        		if (target != null)
        			if (target.getTeam() == shields.getTeam())
        				target.takeShieldedDamage(-shields.type.attackPower);
        	}
        addSignal(s);
    }
    
    public void visitScanSignal(ScanSignal s) {
//    	nothing needs to be done
        addSignal(s);
    }
    
    public void visitNodeBirthSignal(NodeBirthSignal s) {
    	addEncampment(s.location, Team.NEUTRAL);
    	addSignal(s);
    }
    

    // *****************************
    //    UTILITY METHODS
    // *****************************
    private static MapLocation origin = new MapLocation(0, 0);

    protected static boolean canAttackSquare(InternalRobot ir, MapLocation loc) {
        MapLocation myLoc = ir.getLocation();
        int d = myLoc.distanceSquaredTo(loc);
        return d <= ir.type.attackRadiusMaxSquared && d >= ir.type.attackRadiusMinSquared;
//                && inAngleRange(myLoc, ir.getDirection(), loc, ir.type.attackCosHalfTheta);
    }

    protected static boolean inAngleRange(MapLocation sensor, Direction dir, MapLocation target, double cosHalfTheta) {
        MapLocation dirVec = origin.add(dir);
        double dx = target.x - sensor.x;
        double dy = target.y - sensor.y;
        int a = dirVec.x;
        int b = dirVec.y;
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
    protected InternalRobot[] getAllRobotsWithinRadiusDonutSq(MapLocation center, int outerRadiusSquared, int innerRadiusSquared) {
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

        int minXPos = center.x - radius;
        int maxXPos = center.x + radius;
        int minYPos = center.y - radius;
        int maxYPos = center.y + radius;

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
        teamResources[t.ordinal()] += amount;
    }
    
    protected void adjustSpawnRate(Team t, double factor) {
    	teamSpawnRate[t.ordinal()] = 10*GameConstants.HQ_SPAWN_DELAY/(10*GameConstants.HQ_SPAWN_DELAY/teamSpawnRate[t.ordinal()]+1);
    }
}
