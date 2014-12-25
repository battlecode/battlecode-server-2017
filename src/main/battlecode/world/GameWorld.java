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

import battlecode.common.Clock;
import battlecode.common.CommanderSkillType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
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
import battlecode.world.signal.ActionDelaySignal;
import battlecode.world.signal.AttackSignal;
import battlecode.world.signal.BroadcastSignal;
import battlecode.world.signal.BytecodesUsedSignal;
import battlecode.world.signal.CaptureSignal;
import battlecode.world.signal.CastSignal;
import battlecode.world.signal.ControlBitsSignal;
import battlecode.world.signal.DeathSignal;
import battlecode.world.signal.EnergonChangeSignal;
import battlecode.world.signal.FluxChangeSignal;
import battlecode.world.signal.HatSignal;
import battlecode.world.signal.IndicatorStringSignal;
import battlecode.world.signal.LocationSupplyChangeSignal;
import battlecode.world.signal.LocationOreChangeSignal;
import battlecode.world.signal.MatchObservationSignal;
import battlecode.world.signal.MineSignal;
import battlecode.world.signal.MinelayerSignal;
import battlecode.world.signal.MovementOverrideSignal;
import battlecode.world.signal.MovementSignal;
import battlecode.world.signal.NodeBirthSignal;
import battlecode.world.signal.RegenSignal;
import battlecode.world.signal.ResearchSignal;
import battlecode.world.signal.ResearchChangeSignal;
import battlecode.world.signal.RobotInfoSignal;
import battlecode.world.signal.ScanSignal;
import battlecode.world.signal.SelfDestructSignal;
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
    private int[] teamKills = new int[2];
    private final Map<MapLocation, InternalObject> gameObjectsByLoc = new HashMap<MapLocation, InternalObject>();
    private double[] teamResources = new double[2];
    private int[] teamCapturingNumber = new int[2];

    private List<MapLocation> encampments = new ArrayList<MapLocation>();
    private Map<MapLocation, Team> encampmentMap = new HashMap<MapLocation, Team>();
    private Map<Team, InternalRobot> baseHQs = new EnumMap<Team, InternalRobot>(Team.class);
    private Map<MapLocation, Team> mineLocations = new HashMap<MapLocation, Team>();
    private Map<MapLocation, Double> droppedSupplies = new HashMap<MapLocation, Double>();
    private Map<MapLocation, Double> oreMined = new HashMap<MapLocation, Double>();
    private Map<Team, GameMap.MapMemory> mapMemory = new EnumMap<Team, GameMap.MapMemory>(Team.class);
    private Map<Team, Set<MapLocation>> knownMineLocations = new EnumMap<Team, Set<MapLocation>>(Team.class);
    private Map<Team, Map<Upgrade, Integer>> research = new EnumMap<Team, Map<Upgrade, Integer>>(Team.class);

    private Map<Team, InternalRobot> commanders = new EnumMap<Team, InternalRobot>(Team.class);
    private Map<Team, Integer> numCommandersSpawned = new EnumMap<Team, Integer>(Team.class);
    private Map<Team, Map<CommanderSkillType, Integer>> skillCooldowns = new EnumMap<Team, Map<CommanderSkillType, Integer>>(Team.class);

    
    private Map<Team, Set<Upgrade>> upgrades = new EnumMap<Team, Set<Upgrade>>(Team.class);
    private Map<Team, Map<Integer, Integer>> radio = new EnumMap<Team, Map<Integer, Integer>>(Team.class);

    // a count for each robot type per team for tech tree checks and for tower counts
    private Map<Team, Map<RobotType, Integer>> robotTypeCount = new EnumMap<Team, Map<RobotType, Integer>>(Team.class);
    private Map<Team, Map<RobotType, Integer>> totalRobotTypeCount = new EnumMap<Team, Map<RobotType, Integer>>(Team.class);

    // robots to remove from the game at end of turn
    private List<InternalRobot> deadRobots = new ArrayList<InternalRobot>();
    private List<InternalRobot> revealedRobots = new ArrayList<InternalRobot>();
    private List<InternalRobot> nextRevealedRobots = new ArrayList<InternalRobot>();

    @SuppressWarnings("unchecked")
    public GameWorld(GameMap gm, String teamA, String teamB, long[][] oldTeamMemory) {
        super(gm.getSeed(), teamA, teamB, oldTeamMemory);
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
        radio.put(Team.A, new HashMap<Integer, Integer>());
        radio.put(Team.B, new HashMap<Integer, Integer>());
        robotTypeCount.put(Team.A, new EnumMap<RobotType, Integer>(RobotType.class));
        robotTypeCount.put(Team.B, new EnumMap<RobotType, Integer>(RobotType.class));
        totalRobotTypeCount.put(Team.A, new EnumMap<RobotType, Integer>(RobotType.class));
        totalRobotTypeCount.put(Team.B, new EnumMap<RobotType, Integer>(RobotType.class));

        // TODO: these lines don't go here, but have to because parts dealing with robotTypeCount are in the wrong places
        robotTypeCount.get(Team.A).put(RobotType.HQ, 1);
        robotTypeCount.get(Team.B).put(RobotType.HQ, 1);
        robotTypeCount.get(Team.A).put(RobotType.TOWER, 6);
        robotTypeCount.get(Team.B).put(RobotType.TOWER, 6);
        totalRobotTypeCount.get(Team.A).put(RobotType.HQ, 1);
        totalRobotTypeCount.get(Team.B).put(RobotType.HQ, 1);
        totalRobotTypeCount.get(Team.A).put(RobotType.TOWER, 6);
        totalRobotTypeCount.get(Team.B).put(RobotType.TOWER, 6);

        adjustResources(Team.A, GameConstants.ORE_INITIAL_AMOUNT);
        adjustResources(Team.B, GameConstants.ORE_INITIAL_AMOUNT);

	numCommandersSpawned.put(Team.A, 0);
	numCommandersSpawned.put(Team.B, 0);

	skillCooldowns.put(Team.A, new EnumMap<CommanderSkillType, Integer>(CommanderSkillType.class));
	skillCooldowns.put(Team.B, new EnumMap<CommanderSkillType, Integer>(CommanderSkillType.class));
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
        
        // process all gameobjects
        InternalObject[] gameObjects = new InternalObject[gameObjectsByID.size()];
        gameObjects = gameObjectsByID.values().toArray(gameObjects);
        for (int i = 0; i < gameObjects.length; i++) {
            gameObjects[i].processBeginningOfRound();
        }
	
	processSkillCooldowns();
    }
    public int getSkillCooldown(Team t, CommanderSkillType c) {
	Map<CommanderSkillType, Integer> m = skillCooldowns.get(t);

	if (m.get(c) == null) return 0;
	return m.get(c);
    }

    public void processSkillCooldowns() {
	Team teams[] = new Team[]{Team.A, Team.B};

	for (int t=0; t<2; ++t) {
	    for (Entry<CommanderSkillType, Integer> o : skillCooldowns.get(teams[t]).entrySet()) {
		CommanderSkillType skillType = o.getKey();
		int value = o.getValue()-1;
		if (value == 0) {
		    skillCooldowns.get(teams[t]).remove(skillType);
		}
		else {
		    skillCooldowns.get(teams[t]).put(skillType, value);
		}
	    }
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

    // only returns active robots
    public int getRobotTypeCount(Team team, RobotType type) {
        if (robotTypeCount.get(team).containsKey(type)) {
            return robotTypeCount.get(team).get(type);
        } else {
            return 0;
        }
    }

    public void incrementRobotTypeCount(Team team, RobotType type) {
        if (robotTypeCount.get(team).containsKey(type)) {
            robotTypeCount.get(team).put(type, robotTypeCount.get(team).get(type) + 1);
        } else {
            robotTypeCount.get(team).put(type, 1);
        }
    }

    public int getTotalRobotTypeCount(Team team, RobotType type) {
        if (totalRobotTypeCount.get(team).containsKey(type)) {
            return totalRobotTypeCount.get(team).get(type);
        } else {
            return 0;
        }
    }

    public MapLocation senseEnemyHQLocation(Team team) {
        return getBaseHQ(team.opponent()).getLocation();
    }

    public double getSupplyLevel(MapLocation loc) {
        if (droppedSupplies.containsKey(loc)) {
            return droppedSupplies.get(loc);
        } else {
            return 0;
        }
    }

    public int senseSupplyLevel(Team team, MapLocation loc) {
        return mapMemory.get(team).recallSupplyLevel(loc);
    }

    public void changeSupplyLevel(MapLocation loc, double delta) {
        double cur = 0;
        if (droppedSupplies.containsKey(loc)) {
            cur = droppedSupplies.get(loc);
        }

        cur += delta;

        if (cur == 0) {
            droppedSupplies.remove(loc);
        } else {
            droppedSupplies.put(loc, cur);
        }

        addSignal(new LocationSupplyChangeSignal(loc, cur));
    }

    public void mineOre(MapLocation loc, double amount) {
        double cur = 0;
        if (oreMined.containsKey(loc)) {
            cur = oreMined.get(loc);
        }
        oreMined.put(loc, cur + amount);
        addSignal(new LocationOreChangeSignal(loc, cur + amount));
    }

    public double getOre(MapLocation loc) {
        double mined = 0.0;
        if (oreMined.containsKey(loc)) {
            mined = oreMined.get(loc);
        }
        return gameMap.getInitialOre(loc) - mined;
    }

    public double senseOre(Team team, MapLocation loc) {
        double res = mapMemory.get(team).recallOreMined(loc);
        if (res < 0) {
            return res;
        } else {
            return gameMap.getInitialOre(loc) - res;
        }
    }

    public void processEndOfRound() {
        // process all gameobjects
        InternalObject[] gameObjects = new InternalObject[gameObjectsByID.size()];
        gameObjects = gameObjectsByID.values().toArray(gameObjects);
        for (int i = 0; i < gameObjects.length; i++) {
            gameObjects[i].processEndOfRound();
        }
        removeDead();

        // update map memory
        for (int i = 0; i < gameObjects.length; i++) {
            InternalRobot ir = (InternalRobot) gameObjects[i];
            mapMemory.get(ir.getTeam()).rememberLocations(ir.getLocation(), ir.type.sensorRadiusSquared, droppedSupplies, oreMined);
        }

        // free ore
        teamResources[Team.A.ordinal()] += GameConstants.HQ_ORE_INCOME;
        teamResources[Team.B.ordinal()] += GameConstants.HQ_ORE_INCOME;

        updateRevealedRobots();

        // robot info signal
        for (int i = 0; i < gameObjects.length; i++) {
            InternalRobot ir = (InternalRobot) gameObjects[i];
            addSignal(new RobotInfoSignal(ir.getID(), ir.getRobotInfo()));
        }
        
        addSignal(new FluxChangeSignal(teamResources));
		addSignal(new ResearchChangeSignal(research));

        if (timeLimitReached() && winner == null) {
            InternalRobot HQA = baseHQs.get(Team.A);
            InternalRobot HQB = baseHQs.get(Team.B);
            // tiebreak by number of towers
            // tiebreak by hq energon level
            if (!(setWinnerIfNonzero(getRobotTypeCount(Team.A, RobotType.TOWER) - getRobotTypeCount(Team.B, RobotType.TOWER), DominationFactor.BARELY_BEAT)) &&
                !(setWinnerIfNonzero(HQA.getEnergonLevel() - HQB.getEnergonLevel(), DominationFactor.BARELY_BEAT)))
            {
                // tiebreak by total tower health
                // tiebreak by number of handwash stations

                double towerDiff = 0.0;
                InternalObject[] objs = getAllGameObjects();
                for (InternalObject obj : objs) {
                    if (obj instanceof InternalRobot) {
                        InternalRobot ir = (InternalRobot) obj;
                        if (ir.type == RobotType.TOWER) {
                            if (ir.getTeam() == Team.A) {
                                towerDiff += ir.getEnergonLevel();
                            } else {
                                towerDiff -= ir.getEnergonLevel();
                            }
                        }
                    }
                }

                if ( !(setWinnerIfNonzero(towerDiff, DominationFactor.BARELY_BEAT )) &&
                     !(setWinnerIfNonzero(getRobotTypeCount(Team.A, RobotType.HANDWASHSTATION) - getRobotTypeCount(Team.B, RobotType.HANDWASHSTATION), DominationFactor.BARELY_BEAT)))
                {
                    // just tiebreak by ID
                    if (HQA.getID() < HQB.getID())
                        setWinner(Team.A, DominationFactor.WON_BY_DUBIOUS_REASONS);
                    else
                        setWinner(Team.B, DominationFactor.WON_BY_DUBIOUS_REASONS);
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

    public boolean hasCommander(Team t) {
	return getRobotTypeCount(t, RobotType.COMMANDER) > 0;
    }

    public InternalRobot getCommander(Team t) {
        return commanders.get(t);
    }

    public int getCommandersSpawned(Team t) {
	return numCommandersSpawned.get(t);
    }
    public int incrementCommandersSpawned(Team t) {
	numCommandersSpawned.put(t, numCommandersSpawned.get(t) + 1);

    public boolean timeLimitReached() {
        return currentRound >= gameMap.getMaxRounds() - 1;
    }

    public double[] getLastRoundResources() {
        return lastRoundResources;
    }

    public InternalObject getObject(MapLocation loc) {
        return gameObjectsByLoc.get(loc);
    }

    public <T extends InternalObject> T getObjectOfType(MapLocation loc, Class<T> cl) {
        InternalObject o = getObject(loc);
        if (cl.isInstance(o))
            return cl.cast(o);
        else
            return null;
    }

    public InternalRobot getRobot(MapLocation loc) {
        InternalObject obj = getObject(loc);
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

    public void resetUpgrade(Team t, Upgrade u) {
        research.get(t).put(u, 0);
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

    public void castFlash(Team t, MapLocation m) {
	//TODO(npinsker): error handling for this is done when the signal is visited -- is this good practice?
	addSignal(new CastSignal(getCommander(t), m));
    }
    public boolean hasSkill(Team t, CommanderSkillType sk) {
	InternalRobot commander = getCommander(t);

	if (sk == CommanderSkillType.REGENERATION) {
	    return true;
	}
	else if (sk == CommanderSkillType.LEADERSHIP) {
	    return ((InternalCommander)commander).getXP() >= GameConstants.XP_REQUIRED_LEADERSHIP;
	}
	else if (sk == CommanderSkillType.FLASH) {
	    return ((InternalCommander)commander).getXP() >= GameConstants.XP_REQUIRED_FLASH;
	}
	return false;
    }
    public boolean skillIsOnCooldown(Team t, CommanderSkillType sk) {
	Map<CommanderSkillType, Integer> cooldowns = skillCooldowns.get(t);

	return cooldowns.get(sk) != null && cooldowns.get(sk) > 0;
    }

    // should only be called by the InternalObject constructor
    public void notifyAddingNewObject(InternalObject o) {
        if (gameObjectsByID.containsKey(o.getID()))
            return;
        gameObjectsByID.put(o.getID(), o);
        if (o.getLocation() != null) {
            gameObjectsByLoc.put(o.getLocation(), o);
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
            if (gameObjectsByLoc.get(oldLoc) != o) {
                ErrorReporter.report("Internal Error: invalid oldLoc in notifyMovingObject");
                return;
            }
            gameObjectsByLoc.remove(oldLoc);
        }
        if (newLoc != null) {
            gameObjectsByLoc.put(newLoc, o);
        }
    }

    public void removeObject(InternalObject o) {
        if (o.getLocation() != null) {
            MapLocation loc = o.getLocation();
            if (gameObjectsByLoc.get(loc) == o)
                gameObjectsByLoc.remove(loc);
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

        if (gameObjectsByID.get(o.getID()) == o) {
            gameObjectsByID.remove(o.getID());
        }

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
    public TerrainTile senseMapTerrain(Team team, MapLocation loc) {
        return mapMemory.get(team).recallTerrain(loc);
    }
    
    public double getPoints(Team team) {
        return teamRoundResources[team.ordinal()];
    }

    public boolean canMove(MapLocation loc, RobotType type) {

        return (gameMap.getTerrainTile(loc).isTraversable() || gameMap.getTerrainTile(loc) == TerrainTile.VOID && (type == RobotType.DRONE || type == RobotType.MISSILE)) && (gameObjectsByLoc.get(loc) == null);
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

        if (includeBytecodesUsedSignal) {
        	signals.add(new BytecodesUsedSignal(allRobots.toArray(new InternalRobot[]{})));
        	signals.add(new ActionDelaySignal(allRobots.toArray(new InternalRobot[]{})));
        }

        return signals.toArray(new Signal[signals.size()]);
    }
    
    public int getMessage(Team t, int channel) {
    	Integer val = radio.get(t).get(channel);
    	return val == null ? 0 : val;
    }
    
    public boolean hasUpgrade(Team t, Upgrade upgrade) {
    	return upgrades.get(t).contains(upgrade);
    }
    
    public void addUpgrade(Team t, Upgrade upgrade) {
        upgrades.get(t).add(upgrade);
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

    public void updateRevealedRobots() {
        revealedRobots.clear();
        revealedRobots.addAll(nextRevealedRobots);
        nextRevealedRobots.clear();
    }

    // TODO(axc): maybe we should return a copy
    public List<InternalRobot> getRevealedRobots() {
        return revealedRobots;
    }
    
    public void setHQ(InternalRobot r, Team t) {
        baseHQs.put(t, r);
    }

    public void castLayWaste(MapLocation m) {
        InternalRobot target = getRobot(m);

        if (target != null) {
            target.takeDamage(80);
        }
    }


    // ******************************
    // SIGNAL HANDLER METHODS
    // ******************************
    SignalHandler signalHandler = new AutoSignalHandler(this);

    public void visitSignal(Signal s) {
        signalHandler.visitSignal(s);
    }

    public void visitSelfDestructSignal(SelfDestructSignal s) {
        InternalRobot attacker = (InternalRobot) getObjectByID(s.getRobotID());
        MapLocation targetLoc = s.getLoc();

        teamKills[attacker.getTeam().opponent().ordinal()]++;

        double damage = attacker.type.attackPower;
        InternalRobot target;
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++) {
                target = getRobot(targetLoc.add(dx, dy));

                if (target != null) {
                    if (!(dx == 0 && dy == 0)) {
                        target.takeDamage(damage, attacker);
                    }
                }
            }

        addSignal(s);
    }

    public void visitAttackSignal(AttackSignal s) {

        InternalRobot attacker = (InternalRobot) getObjectByID(s.getRobotID());

        MapLocation targetLoc = s.getTargetLoc();
        
        InternalRobot target;
        switch (attacker.type) {
        case BEAVER:
		case SOLDIER:
        case BASHER:
        case MINER:
        case DRONE:
        case TANK:
        case COMMANDER:
        case TOWER:
		case HQ:
            double rate = 1.0;
	    boolean isSplash = (attacker.type == RobotType.BASHER);
            if (attacker.type == RobotType.HQ) {
                int towerCount = getRobotTypeCount(attacker.getTeam(), RobotType.TOWER);
                if (towerCount >= 6) {
                    rate = 10.0;
                } else if (towerCount >= 3) {
                    rate = 1.5;
                }
		if (towerCount >= 5) {
		    isSplash = true;
		}
            }

            int underLeadership = 0;
            
            InternalRobot commander = getCommander(attacker.getTeam());

            if (commander != null && hasSkill(attacker.getTeam(), CommanderSkillType.LEADERSHIP) && commander.getLocation().distanceSquaredTo(attacker.getLocation()) <= GameConstants.LEADERSHIP_RANGE) {
                underLeadership = 1;
            }

            // TODO: splash damage is currently gone
			for (int dx = -1; dx <= 1; dx++)
				for (int dy = -1; dy <= 1; dy++) {

					target = getRobot(targetLoc.add(dx, dy));

					if (target != null && target.getTeam() != attacker.getTeam()) {
						if (dx == 0 && dy == 0 || isSplash) {
							target.takeDamage((attacker.type.attackPower + underLeadership) * rate, attacker);
                        }

                        if (target.getEnergonLevel() <= 0.0 && target.getTeam() != attacker.getTeam()) {
                            teamKills[attacker.getTeam().ordinal()]++;
                        }
                    }
				}
            
			break;
        case MISSILE:
            // TODO: splash damage is currently gone
			for (int dx = -1; dx <= 1; dx++)
				for (int dy = -1; dy <= 1; dy++) {

					target = getRobot(targetLoc.add(dx, dy));

					if (target != null) {
						if (dx == 0 && dy == 0) {
                            continue;
                        } else {
							target.takeDamage(attacker.type.attackPower, attacker);
                        }

                        if (target.getEnergonLevel() <= 0.0 && target.getTeam() != attacker.getTeam()) {
                            teamKills[attacker.getTeam().ordinal()]++;
                        }
                    }
				}
            
            break;
		default:
			// ERROR, should never happen
		}
        
        addSignal(s);
        removeDead();
    }

    public void visitBroadcastSignal(BroadcastSignal s) {        
        nextRevealedRobots.add((InternalRobot) getObjectByID(s.getRobotID()));
    	radio.get(s.getRobotTeam()).putAll(s.broadcastMap);
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

            // reset research
            if (r.researchSignal != null) {
                resetUpgrade(r.getTeam(), r.researchSignal.getUpgrade());
            }

            if (r.isActive()) {
                Integer currentCount = robotTypeCount.get(r.getTeam()).get(r.type);
                robotTypeCount.get(r.getTeam()).put(r.type, currentCount - 1);
            }

            Integer currentCount = totalRobotTypeCount.get(r.getTeam()).get(r.type);
            totalRobotTypeCount.get(r.getTeam()).put(r.type, currentCount - 1);

            RobotMonitor.killRobot(ID);
    		if (r.type == RobotType.SOLDIER && (r.getCapturingType() != null))
    			teamCapturingNumber[r.getTeam().ordinal()]--;
            if (r.hasBeenAttacked()) {
                gameStats.setUnitKilled(r.getTeam(), currentRound);
            }
            if (r.type == RobotType.HQ) {
            	setWinner(r.getTeam().opponent(), getDominationFactor(r.getTeam().opponent()));
            } else if (r.type.isBuilding)
            {
            	encampmentMap.put(r.getLocation(), Team.NEUTRAL);
            }
            if (r.type == RobotType.COMMANDER) {
                commanders.put(r.getTeam(), null);
            }

            // give XP
            MapLocation loc = r.getLocation();

            InternalRobot target = getCommander(r.getTeam().opponent());

            if (target != null && target.getLocation().distanceSquaredTo(loc) <= 24) {
                int xpYield = r.type.oreCost;

                ((InternalCommander)target).giveXP(xpYield);
            }

            // drop supplies
            changeSupplyLevel(loc, r.getSupplyLevel());
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
        if (!canMove(s.getNewLoc(), r.type))
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
        
        if (s.getType().isBuilding)
        {
        	encampmentMap.put(s.getLoc(), s.getTeam());
        }

        //note: this also adds the signal
        InternalRobot robot = GameWorldFactory.createPlayer(this, s.getType(), loc, s.getTeam(), parent, s.getDelay());
        if (s.getType() == RobotType.COMMANDER) {
            commanders.put(robot.getTeam(), robot);
        }
        
        Integer currentCount = totalRobotTypeCount.get(robot.getTeam()).get(robot.type);
        if (currentCount == null) {
            currentCount = 0;
        }
        totalRobotTypeCount.get(robot.getTeam()).put(robot.type, currentCount + 1);
    }
    
    public void visitResearchSignal(ResearchSignal s) {
    	InternalRobot hq = (InternalRobot)getObjectByID(s.getRobotID());
//    	hq.setResearching(s.getUpgrade());
    	researchUpgrade(hq.getTeam(), s.getUpgrade());
    	addSignal(s);
    }
    /*public void visitBurstSignal(BurstSignal s) {
        addSignal(s);
    }*/
    
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
        // TODO: calculate ore change amount based on unit type
        double baseOre = getOre(loc);
        double ore = 0;
        if (baseOre > 0) {
            if (s.getMinerType() == RobotType.BEAVER) {
                ore = Math.max(Math.min(GameConstants.BEAVER_MINE_MAX, baseOre / GameConstants.BEAVER_MINE_RATE), GameConstants.MINIMUM_MINE_AMOUNT);
            } else {
                if (hasUpgrade(s.getMineTeam(), Upgrade.IMPROVEDMINING)) {
                    ore = Math.max(Math.min(baseOre / GameConstants.MINER_MINE_RATE, GameConstants.MINER_MINE_MAX_UPGRADED), GameConstants.MINIMUM_MINE_AMOUNT);
                } else {
                    ore = Math.max(Math.min(baseOre / GameConstants.MINER_MINE_RATE, GameConstants.MINER_MINE_MAX), GameConstants.MINIMUM_MINE_AMOUNT);
                }
            }
        }
        ore = Math.min(ore, baseOre);
        mineOre(loc, ore);
        adjustResources(s.getMineTeam(), ore);
    	addSignal(s);
    }
    
    public void visitRegenSignal(RegenSignal s) {
    	InternalRobot medbay = (InternalRobot) getObjectByID(s.robotID);
    	
    	MapLocation targetLoc = medbay.getLocation();
    	
    	int dist = (int)Math.sqrt(medbay.type.attackRadiusSquared);
    	InternalRobot target;
        for (int dx=-dist; dx<=dist; dx++)
        	for (int dy=-dist; dy<=dist; dy++)
        	{
        		if (dx*dx+dy*dy > medbay.type.attackRadiusSquared) continue;
        		target = getRobot(targetLoc.add(dx, dy));
        		if (target != null)
        			if (target.getTeam() == medbay.getTeam() && target.type != RobotType.HQ)
        				target.takeDamage(-medbay.type.attackPower, medbay);
        	}
        addSignal(s);
    }
    
    public void visitShieldSignal(ShieldSignal s) {
    	InternalRobot shields = (InternalRobot) getObjectByID(s.robotID);
    	
    	MapLocation targetLoc = shields.getLocation();
    	
    	int dist = (int)Math.sqrt(shields.type.attackRadiusSquared);
    	InternalRobot target;
        for (int dx=-dist; dx<=dist; dx++)
        	for (int dy=-dist; dy<=dist; dy++)
        	{
        		if (dx*dx+dy*dy > shields.type.attackRadiusSquared) continue;
        		target = getRobot(targetLoc.add(dx, dy));
        		if (target != null)
        			if (target.getTeam() == shields.getTeam())
        				target.takeShieldedDamage(-shields.type.attackPower);
        	}
        addSignal(s);
    }
    public void visitCastSignal(CastSignal s) {
	//TODO(npinsker): finish this...
	
	InternalRobot commander = (InternalRobot) getObjectByID(s.getRobotID());

	MapLocation currentLoc = commander.getLocation(), targetLoc = s.getTargetLoc();

	if (currentLoc.distanceSquaredTo(targetLoc) <= GameConstants.FLASH_RANGE && canMove(targetLoc, commander.type)) {
	    commander.setLocation(targetLoc);
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
        int attackRadiusSquared = ir.getAttackRadiusSquared();
        return d <= attackRadiusSquared;
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
}
