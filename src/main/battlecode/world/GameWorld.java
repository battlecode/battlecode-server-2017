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
import battlecode.world.signal.AttackSignal;
import battlecode.world.signal.BroadcastSignal;
import battlecode.world.signal.BytecodesUsedSignal;
import battlecode.world.signal.CastSignal;
import battlecode.world.signal.ControlBitsSignal;
import battlecode.world.signal.DeathSignal;
import battlecode.world.signal.DropSupplySignal;
import battlecode.world.signal.TeamOreSignal;
import battlecode.world.signal.IndicatorDotSignal;
import battlecode.world.signal.IndicatorLineSignal;
import battlecode.world.signal.IndicatorStringSignal;
import battlecode.world.signal.LocationSupplyChangeSignal;
import battlecode.world.signal.LocationOreChangeSignal;
import battlecode.world.signal.MatchObservationSignal;
import battlecode.world.signal.MineSignal;
import battlecode.world.signal.MovementSignal;
import battlecode.world.signal.MovementOverrideSignal;
import battlecode.world.signal.PickUpSupplySignal;
import battlecode.world.signal.ResearchSignal;
import battlecode.world.signal.ResearchChangeSignal;
import battlecode.world.signal.RobotInfoSignal;
import battlecode.world.signal.SelfDestructSignal;
import battlecode.world.signal.SpawnSignal;
import battlecode.world.signal.TransferSupplySignal;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * The primary implementation of the GameWorld interface for
 * containing and modifying the game map and the objects on it.
 */
public class GameWorld extends BaseWorld<InternalObject> implements GenericWorld {

    private final GameMap gameMap;
    private RoundStats roundStats = null;    // stats for each round; new object is created for each round
    private final GameStats gameStats = new GameStats();        // end-of-game stats

    private double[] teamResources = new double[2];

    private Map<Team, InternalRobot> baseHQs = new EnumMap<Team, InternalRobot>(Team.class);
    private final Map<MapLocation, InternalObject> gameObjectsByLoc = new HashMap<MapLocation, InternalObject>();

    private Map<MapLocation, Double> oreMined = new HashMap<MapLocation, Double>();
    private Map<MapLocation, Double> droppedSupplies = new HashMap<MapLocation, Double>();
    private Map<Team, GameMap.MapMemory> mapMemory = new EnumMap<Team, GameMap.MapMemory>(Team.class);

    private Map<Team, Map<Integer, Integer>> radio = new EnumMap<Team, Map<Integer, Integer>>(Team.class);

    private Map<Team, Set<Upgrade>> upgrades = new EnumMap<Team, Set<Upgrade>>(Team.class);
    private Map<Team, Map<Upgrade, Integer>> research = new EnumMap<Team, Map<Upgrade, Integer>>(Team.class);

    private Map<Team, InternalRobot> commanders = new EnumMap<Team, InternalRobot>(Team.class);
    private Map<Team, Integer> numCommandersSpawned = new EnumMap<Team, Integer>(Team.class);
    private Map<Team, Map<CommanderSkillType, Integer>> skillCooldowns = new EnumMap<Team, Map<CommanderSkillType, Integer>>(Team.class);
    

    // a count for each robot type per team for tech tree checks and for tower counts
    private Map<Team, Map<RobotType, Integer>> robotTypeCount = new EnumMap<Team, Map<RobotType, Integer>>(Team.class); // only includes active bots
    private Map<Team, Map<RobotType, Integer>> totalRobotTypeCount = new EnumMap<Team, Map<RobotType, Integer>>(Team.class); // includes inactive buildings

    // robots to remove from the game at end of turn
    private List<InternalRobot> deadRobots = new ArrayList<InternalRobot>();

    @SuppressWarnings("unchecked")
    public GameWorld(GameMap gm, String teamA, String teamB, long[][] oldTeamMemory) {
        super(gm.getSeed(), teamA, teamB, oldTeamMemory);
        gameMap = gm;

        mapMemory.put(Team.A, new GameMap.MapMemory(gameMap));
        mapMemory.put(Team.B, new GameMap.MapMemory(gameMap));
        mapMemory.put(Team.NEUTRAL, new GameMap.MapMemory(gameMap));

        research.put(Team.A, new EnumMap<Upgrade, Integer>(Upgrade.class));
        research.put(Team.B, new EnumMap<Upgrade, Integer>(Upgrade.class));
        upgrades.put(Team.A, EnumSet.noneOf(Upgrade.class));
        upgrades.put(Team.B, EnumSet.noneOf(Upgrade.class));

        radio.put(Team.A, new HashMap<Integer, Integer>());
        radio.put(Team.B, new HashMap<Integer, Integer>());

        robotTypeCount.put(Team.A, new EnumMap<RobotType, Integer>(RobotType.class));
        robotTypeCount.put(Team.B, new EnumMap<RobotType, Integer>(RobotType.class));
        totalRobotTypeCount.put(Team.A, new EnumMap<RobotType, Integer>(RobotType.class));
        totalRobotTypeCount.put(Team.B, new EnumMap<RobotType, Integer>(RobotType.class));

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
    
    // *********************************
    // ****** MAP QUERY METHODS ********
    // *********************************

    public GameMap.MapMemory getMapMemory(Team t) {
    	return mapMemory.get(t);
    }

    public int getMapSeed() {
        return gameMap.getSeed();
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public MapLocation senseEnemyHQLocation(Team team) {
        return getBaseHQ(team.opponent()).getLocation();
    }

    // *********************************
    // ****** GAME STATE QUERY METHODS *
    // *********************************

    public int getSkillCooldown(Team t, CommanderSkillType c) {
        Map<CommanderSkillType, Integer> m = skillCooldowns.get(t);

        if (m.get(c) == null) return 0;
        return m.get(c);
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

    // returns active and inactive robots
    public int getTotalRobotTypeCount(Team team, RobotType type) {
        if (totalRobotTypeCount.get(team).containsKey(type)) {
            return totalRobotTypeCount.get(team).get(type);
        } else {
            return 0;
        }
    }

    public double getSupplyLevel(MapLocation loc) {
        if (droppedSupplies.containsKey(loc)) {
            return droppedSupplies.get(loc);
        } else {
            return 0;
        }
    }

    public double senseSupplyLevel(Team team, MapLocation loc) {
        return mapMemory.get(team).recallSupplyLevel(loc);
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
		return numCommandersSpawned.get(t);
    }

    // *********************************
    // ****** GAME ACTIONS *************
    // *********************************

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


    // *********************************
    // ****** GAMEPLAY *****************
    // *********************************

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
        
        addSignal(new TeamOreSignal(teamResources));
		addSignal(new ResearchChangeSignal(research));

        if (timeLimitReached() && winner == null) {
            InternalRobot HQA = baseHQs.get(Team.A);
            InternalRobot HQB = baseHQs.get(Team.B);
            // tiebreak by number of towers
            // tiebreak by hq energon level
            if (!(setWinnerIfNonzero(getRobotTypeCount(Team.A, RobotType.TOWER) - getRobotTypeCount(Team.B, RobotType.TOWER), DominationFactor.BARELY_BEAT)) &&
                !(setWinnerIfNonzero(HQA.getHealthLevel() - HQB.getHealthLevel(), DominationFactor.BARELY_BEAT)))
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
                                towerDiff += ir.getHealthLevel();
                            } else {
                                towerDiff -= ir.getHealthLevel();
                            }
                        }
                    }
                }

                if ( !(setWinnerIfNonzero(towerDiff, DominationFactor.BARELY_BEAT )) &&
                     !(setWinnerIfNonzero(getRobotTypeCount(Team.A, RobotType.HANDWASHSTATION) - getRobotTypeCount(Team.B, RobotType.HANDWASHSTATION), DominationFactor.WON_BY_DUBIOUS_REASONS)))
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

        roundStats = new RoundStats(teamResources[0], teamResources[1]);
    }

    public boolean setWinnerIfNonzero(double n, DominationFactor d) {
        if (n > 0)
            setWinner(Team.A, d);
        else if (n < 0)
            setWinner(Team.B, d);
        return n != 0;
    }
    
    public void setWinner(Team t, DominationFactor d) {
        winner = t;
        gameStats.setDominationFactor(d);
        //running = false;

    }

    public boolean timeLimitReached() {
        return currentRound >= gameMap.getMaxRounds() - 1;
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
        ArrayList<InternalRobot> allRobots = null;
        if (includeBytecodesUsedSignal)
            allRobots = new ArrayList<InternalRobot>();
        for (InternalObject obj : gameObjectsByID.values()) {
            if (!(obj instanceof InternalRobot))
                continue;
            InternalRobot ir = (InternalRobot) obj;
            signals.add(new RobotInfoSignal(ir.getID(), ir.getRobotInfo()));
            if (includeBytecodesUsedSignal)
                allRobots.add(ir);
        }

        if (includeBytecodesUsedSignal) {
        	signals.add(new BytecodesUsedSignal(allRobots.toArray(new InternalRobot[]{})));
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
            if (r.hasBeenAttacked()) {
                gameStats.setUnitKilled(r.getTeam(), currentRound);
            }
            if (r.type == RobotType.HQ) {
            	setWinner(r.getTeam().opponent(), DominationFactor.BEAT);
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

    public void visitIndicatorDotSignal(IndicatorDotSignal s) {
        addSignal(s);
    }

    public void visitIndicatorLineSignal(IndicatorLineSignal s) {
        addSignal(s);
    }

    public void visitIndicatorStringSignal(IndicatorStringSignal s) {
        addSignal(s);
    }

    public void visitMatchObservationSignal(MatchObservationSignal s) {
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

    public void visitDropSupplySignal(DropSupplySignal s) {
        InternalRobot robot = (InternalRobot) getObjectByID(s.getID());
        double amount = Math.min(s.getAmount(), robot.getSupplyLevel());

        robot.decreaseSupplyLevel(amount);
        changeSupplyLevel(robot.getLocation(), amount);
        //addSignal(s); // client doesn't need this
    }

    public void visitPickUpSupplySignal(PickUpSupplySignal s) {
        InternalRobot robot = (InternalRobot) getObjectByID(s.getID());
        double amount = Math.min(s.getAmount(), getSupplyLevel(robot.getLocation()));

        robot.increaseSupplyLevel(amount);
        changeSupplyLevel(robot.getLocation(), -amount);
        //addSignal(s); // client doesn't need this
    }

    public void visitTransferSupplySignal(TransferSupplySignal s) {
        InternalRobot robotFrom = (InternalRobot) getObjectByID(s.fromID);
        InternalRobot robotTo = (InternalRobot) getObjectByID(s.toID);
        double amount = Math.min(s.getAmount(), robotFrom.getSupplyLevel());

        robotFrom.decreaseSupplyLevel(amount);
        robotTo.increaseSupplyLevel(amount);
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

        double cost = (int) s.getType().oreCost;        
        if (s.getType() == RobotType.COMMANDER) {
            cost *= (1 << Math.min(getCommandersSpawned(s.getTeam()), 8));
        }
        adjustResources(s.getTeam(), -cost);
        
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

        if (robot.type == RobotType.COMMANDER) {
            incrementCommandersSpawned(robot.getTeam());
        }
    }
    
    public void visitResearchSignal(ResearchSignal s) {
    	InternalRobot hq = (InternalRobot)getObjectByID(s.getRobotID());
    	researchUpgrade(hq.getTeam(), s.getUpgrade());
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
    
    public void visitCastSignal(CastSignal s) {
	//TODO(npinsker): finish this...
	
	InternalRobot commander = (InternalRobot) getObjectByID(s.getRobotID());

	MapLocation currentLoc = commander.getLocation(), targetLoc = s.getTargetLoc();

	if (currentLoc.distanceSquaredTo(targetLoc) <= GameConstants.FLASH_RANGE && canMove(targetLoc, commander.type)) {
	    commander.setLocation(targetLoc);
	}
	
	addSignal(s);
    }

    // *****************************
    //    UTILITY METHODS
    // *****************************
    private static MapLocation origin = new MapLocation(0, 0);

    protected boolean canAttackSquare(InternalRobot ir, MapLocation loc) {
        MapLocation myLoc = ir.getLocation();
        int d = myLoc.distanceSquaredTo(loc);

        int radius = ir.type.attackRadiusSquared;
        if (ir.type == RobotType.HQ && getRobotTypeCount(ir.getTeam(), RobotType.TOWER) >= 2) {
            radius = GameConstants.ATTACK_RADIUS_SQUARED_BUFFED_HQ;
        }
        return d <= radius;
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
