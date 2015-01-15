package battlecode.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import battlecode.world.signal.BashSignal;
import battlecode.world.signal.BroadcastSignal;
import battlecode.world.signal.BuildSignal;
import battlecode.world.signal.BytecodesUsedSignal;
import battlecode.world.signal.CastSignal;
import battlecode.world.signal.ControlBitsSignal;
import battlecode.world.signal.DeathSignal;
import battlecode.world.signal.TeamOreSignal;
import battlecode.world.signal.HealthChangeSignal;
import battlecode.world.signal.IndicatorDotSignal;
import battlecode.world.signal.IndicatorLineSignal;
import battlecode.world.signal.IndicatorStringSignal;
import battlecode.world.signal.LocationOreChangeSignal;
import battlecode.world.signal.MatchObservationSignal;
import battlecode.world.signal.MineSignal;
import battlecode.world.signal.MissileCountSignal;
import battlecode.world.signal.MovementSignal;
import battlecode.world.signal.MovementOverrideSignal;
import battlecode.world.signal.RobotInfoSignal;
import battlecode.world.signal.SelfDestructSignal;
import battlecode.world.signal.SpawnSignal;
import battlecode.world.signal.TransferSupplySignal;
import battlecode.world.signal.XPSignal;

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
    private Map<Team, Set<InternalRobot>> baseTowers = new EnumMap<Team, Set<InternalRobot>>(Team.class);
    private final Map<MapLocation, InternalObject> gameObjectsByLoc = new HashMap<MapLocation, InternalObject>();

    private Map<MapLocation, Double> oreMined = new HashMap<MapLocation, Double>();
    private Map<Team, GameMap.MapMemory> mapMemory = new EnumMap<Team, GameMap.MapMemory>(Team.class);

    private Map<Team, Map<Integer, Integer>> radio = new EnumMap<Team, Map<Integer, Integer>>(Team.class);

    private int[] numCommandersSpawned = new int[2];
    private Map<Team, InternalRobot> commanders = new EnumMap<Team, InternalRobot>(Team.class);
    private Map<Team, Map<CommanderSkillType, Integer>> skillCooldowns = new EnumMap<Team, Map<CommanderSkillType, Integer>>(Team.class);
    
    // a count for each robot type per team for tech tree checks and for tower counts
    private Map<Team, Map<RobotType, Integer>> activeRobotTypeCount = new EnumMap<Team, Map<RobotType, Integer>>(Team.class); // only includes active bots
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

        radio.put(Team.A, new HashMap<Integer, Integer>());
        radio.put(Team.B, new HashMap<Integer, Integer>());

        activeRobotTypeCount.put(Team.A, new EnumMap<RobotType, Integer>(RobotType.class));
        activeRobotTypeCount.put(Team.B, new EnumMap<RobotType, Integer>(RobotType.class));
        totalRobotTypeCount.put(Team.A, new EnumMap<RobotType, Integer>(RobotType.class));
        totalRobotTypeCount.put(Team.B, new EnumMap<RobotType, Integer>(RobotType.class));

        baseTowers.put(Team.A, new HashSet<InternalRobot>());
        baseTowers.put(Team.B, new HashSet<InternalRobot>());

        adjustResources(Team.A, GameConstants.ORE_INITIAL_AMOUNT);
        adjustResources(Team.B, GameConstants.ORE_INITIAL_AMOUNT);

        skillCooldowns.put(Team.A, new EnumMap<CommanderSkillType, Integer>(CommanderSkillType.class));
        skillCooldowns.put(Team.B, new EnumMap<CommanderSkillType, Integer>(CommanderSkillType.class));
    }

    public void setHQ(InternalRobot r, Team t) {
        baseHQs.put(t, r);
    }

    public void addTower(InternalRobot tower, Team t) {
        baseTowers.get(t).add(tower);
    }
    
    // *********************************
    // ****** BASIC MAP METHODS ********
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

    public InternalRobot getBaseHQ(Team t) {
        return baseHQs.get(t);
    }

    public MapLocation[] senseTowerLocations(Team team) {
        ArrayList<MapLocation> locs = new ArrayList<MapLocation>();
        for (InternalRobot r: baseTowers.get(team)) {
            if (exists(r)) {
                locs.add(r.getLocation());
            }
        }

        Collections.sort(locs);
        if (team == Team.B) {
            Collections.reverse(locs);
        }
            
        return locs.toArray(new MapLocation[locs.size()]);
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
    
    public Collection<InternalObject> allObjects() {
        return gameObjectsByID.values();
    }

    public InternalObject[] getAllGameObjects() {
        return gameObjectsByID.values().toArray(new InternalObject[gameObjectsByID.size()]);
    }

    public InternalRobot getRobotByID(int id) {
        return (InternalRobot) getObjectByID(id);
    }

    public boolean exists(InternalObject o) {
        return gameObjectsByID.containsKey(o.getID());
    }

    public int getMessage(Team t, int channel) {
    	Integer val = radio.get(t).get(channel);
    	return val == null ? 0 : val;
    }

    public RoundStats getRoundStats() {
        return roundStats;
    }

    public GameStats getGameStats() {
        return gameStats;
    }

    // *********************************
    // ****** MISC UTILITIES ***********
    // *********************************

    public boolean canSense(Team team, MapLocation loc) {
        return mapMemory.get(team).canSense(loc);
    }

    public void updateMapMemoryAdd(Team team, MapLocation loc, int radiusSquared) {
        mapMemory.get(team).rememberLocation(loc, radiusSquared, oreMined);
    }

    public void updateMapMemoryRemove(Team team, MapLocation loc, int radiusSquared) {
        mapMemory.get(team).removeLocation(loc, radiusSquared);
    }

    public boolean canMove(MapLocation loc, RobotType type) {
        return (gameMap.getTerrainTile(loc).isTraversable() || gameMap.getTerrainTile(loc) == TerrainTile.VOID && (type == RobotType.DRONE || type == RobotType.MISSILE)) && (gameObjectsByLoc.get(loc) == null);
    }

    protected boolean canAttackSquare(InternalRobot ir, MapLocation loc) {
        MapLocation myLoc = ir.getLocation();
        int d = myLoc.distanceSquaredTo(loc);

        int radius = ir.type.attackRadiusSquared;
        if (ir.type == RobotType.HQ && getActiveRobotTypeCount(ir.getTeam(), RobotType.TOWER) >= 2) {
            radius = GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED;
        }
        return d <= radius;
    }

    // TODO: make a faster implementation of this
    public MapLocation[] getAllMapLocationsWithinRadiusSq(MapLocation center, int radiusSquared) {
        ArrayList<MapLocation> locations = new ArrayList<MapLocation>();

        int radius = (int) Math.sqrt(radiusSquared);
        radius = Math.min(radius, Math.max(GameConstants.MAP_MAX_HEIGHT, GameConstants.MAP_MAX_WIDTH));

        int minXPos = center.x - radius;
        int maxXPos = center.x + radius;
        int minYPos = center.y - radius;
        int maxYPos = center.y + radius;

        for (int x = minXPos; x <= maxXPos; x++) {
            for (int y = minYPos; y <= maxYPos; y++) {
                MapLocation loc = new MapLocation(x, y);
                TerrainTile tile = gameMap.getTerrainTile(loc);
                if (!tile.equals(TerrainTile.OFF_MAP) && loc.distanceSquaredTo(center) <= radiusSquared)
                    locations.add(loc);
            }
        }

        return locations.toArray(new MapLocation[locations.size()]);
    }

    // TODO: make a faster implementation of this
    protected InternalRobot[] getAllRobotsWithinRadiusSq(MapLocation center, int radiusSquared) {
        if (radiusSquared == 0) {
            if (getRobot(center) == null) {
                return new InternalRobot[0];
            } else {
                InternalRobot[] res = { getRobot(center) };
                return res;
            }
        } else if (radiusSquared < 16) {
            MapLocation[] locs = getAllMapLocationsWithinRadiusSq(center, radiusSquared);
            ArrayList<InternalRobot> robots = new ArrayList<InternalRobot>();
            for (MapLocation loc : locs) {
                InternalRobot res = getRobot(loc);
                if (res != null) {
                    robots.add(res);
                }
            }
            return robots.toArray(new InternalRobot[robots.size()]);
        }

        ArrayList<InternalRobot> robots = new ArrayList<InternalRobot>();

        for (InternalObject o : gameObjectsByID.values()) {
            if (!(o instanceof InternalRobot))
                continue;
            if (o.getLocation() != null && o.getLocation().distanceSquaredTo(center) <= radiusSquared)
                robots.add((InternalRobot) o);
        }

        return robots.toArray(new InternalRobot[robots.size()]);
    }

    // *********************************
    // ****** ENGINE ACTIONS ***********
    // *********************************

    // should only be called by the InternalObject constructor
    public void notifyAddingNewObject(InternalObject o) {
        if (gameObjectsByID.containsKey(o.getID()))
            return;
        gameObjectsByID.put(o.getID(), o);
        if (o.getLocation() != null) {
            gameObjectsByLoc.put(o.getLocation(), o);
        }
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
        while (deadRobots.size() > 0) {
            InternalRobot r = deadRobots.remove(deadRobots.size() - 1);
            if (r.getID() == RobotMonitor.getCurrentRobotID())
                current = true;
            visitSignal(new DeathSignal(r));
        }
        if (current)
            throw new RobotDeathException();
    }

    // *********************************
    // ****** COUNTING ROBOTS **********
    // *********************************
    
    // only returns active robots
    public int getActiveRobotTypeCount(Team team, RobotType type) {
        if (activeRobotTypeCount.get(team).containsKey(type)) {
            return activeRobotTypeCount.get(team).get(type);
        } else {
            return 0;
        }
    }

    public void incrementActiveRobotTypeCount(Team team, RobotType type) {
        if (activeRobotTypeCount.get(team).containsKey(type)) {
            activeRobotTypeCount.get(team).put(type, activeRobotTypeCount.get(team).get(type) + 1);
        } else {
            activeRobotTypeCount.get(team).put(type, 1);
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

    public void incrementTotalRobotTypeCount(Team team, RobotType type) {
        if (totalRobotTypeCount.get(team).containsKey(type)) {
            totalRobotTypeCount.get(team).put(type, totalRobotTypeCount.get(team).get(type) + 1);
        } else {
            totalRobotTypeCount.get(team).put(type, 1);
        }
    }

    // *********************************
    // ****** ORE METHODS **************
    // *********************************

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

    public void mineOre(MapLocation loc, double amount) {
        double cur = 0;
        if (oreMined.containsKey(loc)) {
            cur = oreMined.get(loc);
        }
        oreMined.put(loc, cur + amount);
        addSignal(new LocationOreChangeSignal(loc, cur + amount));
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

    public double resources(Team t) {
        return teamResources[t.ordinal()];
    }

    // *********************************
    // ****** TERRAIN METHODS **********
    // *********************************

    public TerrainTile getMapTerrain(MapLocation loc) {
        return gameMap.getTerrainTile(loc);
    }

    public TerrainTile senseMapTerrain(Team team, MapLocation loc) {
        return mapMemory.get(team).recallTerrain(loc);
    }

    // *********************************
    // ****** COMMANDER METHODS ********
    // *********************************

    public boolean hasCommander(Team t) {
        return getActiveRobotTypeCount(t, RobotType.COMMANDER) > 0;
    }

    public void putCommander(InternalRobot robot) {
        commanders.put(robot.getTeam(), robot);
    }

    public InternalRobot getCommander(Team t) {
        return commanders.get(t);
    }

    public int getCommandersSpawned(Team t) {
        return numCommandersSpawned[t.ordinal()];
    }

    public int incrementCommandersSpawned(Team t) {
        numCommandersSpawned[t.ordinal()]++;
		return numCommandersSpawned[t.ordinal()];
    }

    public void updateSkillCooldown(Team t, CommanderSkillType c, int cooldown) {
        Map<CommanderSkillType, Integer> m = skillCooldowns.get(t);
        m.put(c, cooldown);
    }

    public int getSkillCooldown(Team t, CommanderSkillType c) {
        Map<CommanderSkillType, Integer> m = skillCooldowns.get(t);

        if (m.get(c) == null) return 0;
        return m.get(c);
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
        else if (sk == CommanderSkillType.HEAVY_HANDS) {
            return ((InternalCommander)commander).getXP() >= GameConstants.XP_REQUIRED_HEAVY_HANDS;
        }
        return false;
    }

    public boolean skillIsOnCooldown(Team t, CommanderSkillType sk) {
        Map<CommanderSkillType, Integer> cooldowns = skillCooldowns.get(t);

        return cooldowns.get(sk) != null && cooldowns.get(sk) > 0;
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

    public void processEndOfRound() {
        // process all gameobjects
        InternalObject[] gameObjects = new InternalObject[gameObjectsByID.size()];
        gameObjects = gameObjectsByID.values().toArray(gameObjects);
        for (int i = 0; i < gameObjects.length; i++) {
            gameObjects[i].processEndOfRound();
        }
        removeDead();

        // update map memory
        /*
        for (int i = 0; i < gameObjects.length; i++) {
            InternalRobot ir = (InternalRobot) gameObjects[i];
            mapMemory.get(ir.getTeam()).rememberLocations(ir.getLocation(), ir.type.sensorRadiusSquared, oreMined);
        }
        */

        // free ore
        teamResources[Team.A.ordinal()] += GameConstants.HQ_ORE_INCOME;
        teamResources[Team.B.ordinal()] += GameConstants.HQ_ORE_INCOME;
        
        addSignal(new TeamOreSignal(teamResources));

        if (timeLimitReached() && winner == null) {
            InternalRobot HQA = baseHQs.get(Team.A);
            InternalRobot HQB = baseHQs.get(Team.B);
            // tiebreak by number of towers
            // tiebreak by hq energon level
            if (!(setWinnerIfNonzero(getActiveRobotTypeCount(Team.A, RobotType.TOWER) - getActiveRobotTypeCount(Team.B, RobotType.TOWER), DominationFactor.PWNED)) &&
                !(setWinnerIfNonzero(HQA.getHealthLevel() - HQB.getHealthLevel(), DominationFactor.OWNED)))
            {
                // tiebreak by total tower health
                double towerDiff = 0.0;
                double oreDiff = resources(Team.A) - resources(Team.B);
                InternalObject[] objs = getAllGameObjects();
                for (InternalObject obj : objs) {
                    if (obj instanceof InternalRobot) {
                        InternalRobot ir = (InternalRobot) obj;
                        if (ir.getTeam() == Team.A) {
                            oreDiff += ir.type.oreCost;
                        } else {
                            oreDiff -= ir.type.oreCost;
                        }
                        if (ir.type == RobotType.TOWER) {
                            if (ir.getTeam() == Team.A) {
                                towerDiff += ir.getHealthLevel();
                            } else {
                                towerDiff -= ir.getHealthLevel();
                            }
                        }
                    }
                }

                // tiebreak by number of handwash stations
                // total ore cost of units + ore stockpile
                if ( !(setWinnerIfNonzero(towerDiff, DominationFactor.BEAT )) &&
                     !(setWinnerIfNonzero(getActiveRobotTypeCount(Team.A, RobotType.HANDWASHSTATION) - getActiveRobotTypeCount(Team.B, RobotType.HANDWASHSTATION), DominationFactor.BARELY_BEAT)) &&
                     !(setWinnerIfNonzero(oreDiff, DominationFactor.BARELY_BARELY_BEAT )))
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

    public Signal[] getAllSignals(boolean includeBytecodesUsedSignal) {
        ArrayList<InternalRobot> allRobots = new ArrayList<InternalRobot>();
        for (InternalObject obj : gameObjectsByID.values()) {
            if (!(obj instanceof InternalRobot))
                continue;
            InternalRobot ir = (InternalRobot) obj;
            allRobots.add(ir);

            if (ir.type == RobotType.COMMANDER) {
                signals.add(new XPSignal(ir.getID(), ir.getXP()));
            }

            if (ir.type == RobotType.LAUNCHER && ir.missileCountChanged()) {
                signals.add(new MissileCountSignal(ir.getID(), ir.getMissileCount()));
                ir.clearMissileCountChanged();
            }
        }

        InternalRobot[] robots = allRobots.toArray(new InternalRobot[]{});
        if (includeBytecodesUsedSignal) {
        	signals.add(new BytecodesUsedSignal(robots));
        }
        signals.add(new RobotInfoSignal(robots));
        HealthChangeSignal healthChange = new HealthChangeSignal(robots);
        if (healthChange.getRobotIDs().length > 0) {
            signals.add(healthChange);
        }

        return signals.toArray(new Signal[signals.size()]);
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
            int splashRadius = 0;
            if (attacker.type == RobotType.BASHER) {
                splashRadius = GameConstants.BASH_RADIUS_SQUARED;
            } else if (attacker.type == RobotType.HQ) {
                int towerCount = getActiveRobotTypeCount(attacker.getTeam(), RobotType.TOWER);
                if (towerCount >= 6) {
                    rate = GameConstants.HQ_BUFFED_DAMAGE_MULTIPLIER_LEVEL_2;
                } else if (towerCount >= 3) {
                    rate = GameConstants.HQ_BUFFED_DAMAGE_MULTIPLIER_LEVEL_1;
                }

                if (towerCount >= 5) {
                    splashRadius = GameConstants.HQ_BUFFED_SPLASH_RADIUS_SQUARED;
                }
            }

            double underLeadership = 0;
            InternalRobot commander = getCommander(attacker.getTeam());
            if (commander != null && hasSkill(attacker.getTeam(), CommanderSkillType.LEADERSHIP) && commander.getLocation().distanceSquaredTo(attacker.getLocation()) <= GameConstants.LEADERSHIP_RANGE_SQUARED) {
                if (((InternalCommander)commander).getXP() >= GameConstants.XP_REQUIRED_IMPROVED_LEADERSHIP) {
                    underLeadership = GameConstants.IMPROVED_LEADERSHIP_DAMAGE_BONUS;
                }
                else {
                    underLeadership = GameConstants.LEADERSHIP_DAMAGE_BONUS;
                }
            }

            InternalRobot[] targets = getAllRobotsWithinRadiusSq(targetLoc, splashRadius);
            for (InternalRobot target : targets) {
                // disable friendly fire
                if (target.getTeam() != attacker.getTeam()) {
                    double finalRate = rate;
                    if (!target.getLocation().equals(targetLoc) && attacker.type == RobotType.HQ) {
                        finalRate *= GameConstants.HQ_BUFFED_SPLASH_RATE; // splash is only 50% damage for HQ
                    }
                    double damage = (attacker.type.attackPower + underLeadership) * finalRate;
                    if (target.type == RobotType.MISSILE) {
                        damage = Math.min(damage, GameConstants.MISSILE_MAXIMUM_DAMAGE);
                    }
                    if (attacker.type == RobotType.COMMANDER && hasSkill(attacker.getTeam(), CommanderSkillType.HEAVY_HANDS) && (target.type != RobotType.COMMANDER && target.type != RobotType.TOWER && target.type != RobotType.HQ)) {
                        target.addCooldownDelay(GameConstants.HEAVY_HANDS_MOVEMENT_DELAY);
                        target.addLoadingDelay(GameConstants.HEAVY_HANDS_ATTACK_DELAY);
                    }
                    target.takeDamage(damage, attacker);

                    // if you destroy a missile, then cause damage
                    if (target.type == RobotType.MISSILE && target.getHealthLevel() <= 0) {
                        visitSelfDestructSignal(new SelfDestructSignal(target, target.getLocation(), 0.5));
                    }
                }
            }
			break;
		default:
			// ERROR, should never happen
		}
        
        if (attacker.type != RobotType.BASHER) {
            addSignal(s);
        }
        removeDead();
    }

    public void visitBashSignal(BashSignal s) {
        InternalRobot attacker = (InternalRobot) getObjectByID(s.getRobotID());

        MapLocation targetLoc = s.getTargetLoc();
        // first, we should see if we actually do any damage
        InternalRobot[] targets = getAllRobotsWithinRadiusSq(targetLoc, GameConstants.BASH_RADIUS_SQUARED);

        boolean attacked = false;
        for (InternalRobot target : targets) {
            if (target.getTeam() != attacker.getTeam()) {
                attacked = true;
            }
        }

        if (attacked) {
            visitAttackSignal(new AttackSignal(s.getRobotID(), targetLoc));
            addSignal(s);
        }
    }

    public void visitBroadcastSignal(BroadcastSignal s) {
    	radio.get(s.getRobotTeam()).putAll(s.broadcastMap);
    	s.broadcastMap = null;
        addSignal(s);
    }

    public void visitBuildSignal(BuildSignal s) {
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
        adjustResources(s.getTeam(), -cost);
        
        //note: this also adds the signal
        InternalRobot robot = GameWorldFactory.createPlayer(this, s.getType(), loc, s.getTeam(), parent, s.getDelay());

        // add myBuilder and myBuilding
        robot.setMyBuilder(parent.getID());
        parent.setMyBuilding(robot.getID());

        //addSignal(s); //client doesn't need this one
    }
    
    // Right now this is always FLASH
    public void visitCastSignal(CastSignal s) {
        InternalRobot commander = (InternalRobot) getObjectByID(s.getRobotID());

        MapLocation currentLoc = commander.getLocation(), targetLoc = s.getTargetLoc();

        updateSkillCooldown(commander.getTeam(), CommanderSkillType.FLASH, GameConstants.FLASH_COOLDOWN);

        commander.setLocation(targetLoc);

        addSignal(s);
    }
    
    public void visitControlBitsSignal(ControlBitsSignal s) {
        InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
        r.setControlBits(s.getControlBits());

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

            // update robot counting
            if (r.isActive()) {
                Integer currentCount = activeRobotTypeCount.get(r.getTeam()).get(r.type);
                activeRobotTypeCount.get(r.getTeam()).put(r.type, currentCount - 1);
            }
            Integer currentCount = totalRobotTypeCount.get(r.getTeam()).get(r.type);
            totalRobotTypeCount.get(r.getTeam()).put(r.type, currentCount - 1);

            if (r.hasBeenAttacked()) {
                gameStats.setUnitKilled(r.getTeam(), currentRound);
            }
            if (r.type == RobotType.HQ) {
            	setWinner(r.getTeam().opponent(), DominationFactor.DESTROYED);
            }
            if (r.type == RobotType.COMMANDER) {
                commanders.put(r.getTeam(), null);
            }

            // give XP
            MapLocation loc = r.getLocation();
            InternalRobot target = getCommander(r.getTeam().opponent());
            if (target != null && target.getLocation().distanceSquaredTo(loc) <= GameConstants.XP_RANGE) {
                int xpYield = r.type.oreCost;
                ((InternalCommander)target).giveXP(xpYield);
            }

            // if it's a building, free the builder
            if (r.getMyBuilder() >= 0) {
                InternalRobot builder = getRobotByID(r.getMyBuilder());
                builder.clearBuildingAndFree(); // also reset delays
            }

            // if it's a builder, destroy the building
            if (r.getMyBuilding() >= 0) {
                InternalRobot building = getRobotByID(r.getMyBuilding());
                building.clearBuilding();
                building.takeDamage(2 * building.getHealthLevel());
            }

            updateMapMemoryRemove(r.getTeam(), r.getLocation(), r.type.sensorRadiusSquared);
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
    
    public void visitMineSignal(MineSignal s) {
    	MapLocation loc = s.getMineLoc();
        double baseOre = getOre(loc);
        double ore = 0;
        InternalRobot r = (InternalRobot) getObjectByID(s.getMinerID());
        if (baseOre > 0) {
            if (r.type == RobotType.BEAVER) {
                ore = Math.max(Math.min(GameConstants.BEAVER_MINE_MAX, baseOre / GameConstants.BEAVER_MINE_RATE), GameConstants.MINIMUM_MINE_AMOUNT);
            } else {
                ore = Math.max(Math.min(baseOre / GameConstants.MINER_MINE_RATE, GameConstants.MINER_MINE_MAX), GameConstants.MINIMUM_MINE_AMOUNT);
            }
        }
        ore = Math.min(ore, baseOre);
        mineOre(loc, ore);
        adjustResources(r.getTeam(), ore);

        mapMemory.get(Team.A).updateLocation(loc, oreMined.get(loc));
        mapMemory.get(Team.B).updateLocation(loc, oreMined.get(loc));

    	addSignal(s);
    }

    public void visitMovementSignal(MovementSignal s) {
        InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
        r.setLocation(s.getNewLoc());

        addSignal(s);
    }

    public void visitMovementOverrideSignal(MovementOverrideSignal s) {
        InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
        r.setLocation(s.getNewLoc());

        addSignal(s);
    }
    
    public void visitSelfDestructSignal(SelfDestructSignal s) {
        InternalRobot attacker = (InternalRobot) getObjectByID(s.getRobotID());
        MapLocation targetLoc = s.getLoc();

        // only MISSILES can self destruct this year
        double damage = RobotType.MISSILE.attackPower * s.getDamageFactor();
        InternalRobot[] targets = getAllRobotsWithinRadiusSq(targetLoc, GameConstants.MISSILE_RADIUS_SQUARED);
        for (InternalRobot target : targets) {
            if (target.type == RobotType.MISSILE) {
                target.takeDamage(Math.min(damage, GameConstants.MISSILE_MAXIMUM_DAMAGE), attacker);
            } else {
                target.takeDamage(damage, attacker);
            }
        }

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

        //addSignal(s); //client doesn't need this one
    }

    public void visitTransferSupplySignal(TransferSupplySignal s) {
        InternalRobot robotFrom = (InternalRobot) getObjectByID(s.fromID);
        InternalRobot robotTo = (InternalRobot) getObjectByID(s.toID);
        double amount = Math.min(s.getAmount(), robotFrom.getSupplyLevel());

        robotFrom.decreaseSupplyLevel(amount);
        robotTo.increaseSupplyLevel(amount);
        addSignal(s);
    }
}
