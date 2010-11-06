package battlecode.world;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import battlecode.common.AuraType;
import battlecode.common.ActionType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TerrainTile;
import battlecode.engine.ErrorReporter;
import battlecode.engine.GenericWorld;
import battlecode.engine.PlayerFactory;
import battlecode.engine.instrumenter.RobotMonitor;
import battlecode.engine.signal.*;
import battlecode.serial.DominationFactor;
import battlecode.serial.GameStats;
import battlecode.serial.RoundStats;
import battlecode.server.Config;
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

    private static final int NUM_ARCHONS_PER_TEAM = 6; // also need to change value in Match.java
    private final GameMap gameMap;
    private RoundStats roundStats = null;	// stats for each round; new object is created for each round
    private final GameStats gameStats = new GameStats();		// end-of-game stats
    private double[] teamPoints;
    private final Map<MapLocation3D, InternalObject> gameObjectsByLoc;
    private final Set<InternalMortar> mortars;
    private final Set<MapLocation>[] teleportersByTeam;
    
    private static final int[][] fluxMineOffsets = GameMap.computeOffsets360(GameConstants.FLUX_RADIUS_SQUARED);
    private static final int[] fluxMineOffsetsX = fluxMineOffsets[0];
    private static final int[] fluxMineOffsetsY = fluxMineOffsets[1];
    public final ScoreCalculator[] scoreCalcs = new ScoreCalculator[]{new ScoreCalculator(), new ScoreCalculator()};
    private boolean[][] minedLocs;

    @SuppressWarnings("unchecked")
    public GameWorld(GameMap gm, String teamA, String teamB, long[][] oldArchonMemory) {
		super(gm.getSeed(),teamA,teamB,oldArchonMemory);
        gameMap = gm;
        gameObjectsByLoc = new HashMap<MapLocation3D, InternalObject>();
        mortars = new HashSet<InternalMortar>();
        teleportersByTeam = new Set[]{
                    new HashSet<MapLocation>(),
                    new HashSet<MapLocation>()};
        minedLocs = new boolean[gm.getHeight()][gm.getWidth()];
        teamPoints = new double[2];
        //testScoreCounter();
    }

    private void testScoreCounter() {
        ScoreCalculator sc = scoreCalcs[0];
        sc.add(new MapLocation(0, 2));
        sc.add(new MapLocation(0, 1));
        sc.add(new MapLocation(0, 0));
        sc.add(new MapLocation(1, 1));
        //sc.add(new MapLocation(-1, 1));
        //sc.remove(new MapLocation(0, 0));
        //System.out.println("score " + sc.getScore());
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
        InternalTerrainTile[][] tm = gameMap.getTerrainMatrix();
        for (InternalTerrainTile[] tmr : tm) {
            for (InternalTerrainTile tt : tmr) {
                tt.processBeginningOfRound(currentRound);
            }
        }

        for (int y = 0; y < gameMap.getHeight(); y++) {
            for (int x = 0; x < gameMap.getWidth(); x++) {
                minedLocs[y][x] = false;
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

        // add MineFluxSignal -- REMOVED FOR PERFORMANCE REASONS
        //addSignal(new MineFluxSignal(minedLocs));

        // calculate some stats
        double[] totalEnergon = new double[3];
        int[] numArchons = new int[2];
        double[][] archonProduction = new double[2][NUM_ARCHONS_PER_TEAM];
        //~ int[] numRobots = new int[2];
        boolean teamADead = true, teamBDead = true;
        for (InternalObject obj : gameObjectsByID.values()) {
            if (!(obj instanceof InternalRobot))
                continue;
            InternalRobot r = (InternalRobot) obj;
            int team = r.getTeam().ordinal();
            totalEnergon[team] += r.getEnergonLevel();
            //~ numRobots[team]++;
            if (r instanceof InternalRobot) {
                // Buildings can survive for a really long time, so the game
                // should end if one team only has buildings left.  It seems
                // more natural to end the game when all archons are killed
                // then when all non-buildings are killed.
                if (teamADead && r.getTeam() == Team.A) teamADead = false;
                if (teamBDead && r.getTeam() == Team.B) teamBDead = false;
                if (r instanceof InternalArchon && numArchons[team] < NUM_ARCHONS_PER_TEAM) {
                    archonProduction[team][numArchons[team]++] = ((InternalArchon) r).getProduction();
                }
            }
        }
        //~ stats.setActiveTotal(numRobots[0], Team.A);
        //~ stats.setActiveTotal(numRobots[1], Team.B);
        //~ stats.setEnergon(totalEnergon[0], Team.A);
        //~ stats.setEnergon(totalEnergon[1], Team.B);
        //~ stats.setNumArchons(numArchons[0], Team.A);
        //~ stats.setNumArchons(numArchons[1], Team.B);
        //~ stats.setArchonProduction(archonProduction[0], Team.A);
        //~ stats.setArchonProduction(archonProduction[1], Team.B);

        teamPoints[Team.A.ordinal()] += getRoundPoints(Team.A);
        teamPoints[Team.B.ordinal()] += getRoundPoints(Team.B);
        int aPoints = (int) (teamPoints[Team.A.ordinal()]), bPoints = (int) (teamPoints[Team.B.ordinal()]);

        roundStats = new RoundStats(archonProduction[0], archonProduction[1], aPoints, bPoints);

        // check for mercy rule
        //boolean teamAHasMinPoints = teamPoints[Team.A.ordinal()] >= gameMap.getMinPoints() || gameMap.getMaxRounds() < currentRound;
        //boolean teamBHasMinPoints = teamPoints[Team.B.ordinal()] >= gameMap.getMinPoints() || gameMap.getMaxRounds() < currentRound;
        //boolean teamAMercy = teamAHasMinPoints  &&
        //        ((teamPoints[Team.A.ordinal()] - teamPoints[Team.B.ordinal()]) >= gameMap.getMinPoints() * (1 - GameConstants.PointsDecreaseFactor * (currentRound - gameMap.getMaxRounds() + 1)));
        //boolean teamBMercy = teamBHasMinPoints && ((teamPoints[Team.B.ordinal()] - teamPoints[Team.A.ordinal()]) >= gameMap.getMinPoints() * (1 - GameConstants.PointsDecreaseFactor * (currentRound - gameMap.getMaxRounds() + 1)));


        double diff = teamPoints[Team.A.ordinal()] - teamPoints[Team.B.ordinal()];
        boolean teamAMercy = diff > gameMap.getMinPoints() || diff >= gameMap.getMinPoints() * (1 - GameConstants.POINTS_DECREASE_PER_ROUND_FACTOR * (currentRound - gameMap.getStraightMaxRounds() + 1));
        diff -= 2 * diff;
        boolean teamBMercy = diff > gameMap.getMinPoints() || diff >= gameMap.getMinPoints() * (1 - GameConstants.POINTS_DECREASE_PER_ROUND_FACTOR * (currentRound - gameMap.getStraightMaxRounds() + 1));

        /*
        // check tallest tower
        for (MapLocation loc : deposits.keySet()) {
        if (gameMap.getNumBlocks(loc) > gameStats.getTallestTower()) {
        gameStats.setTallestTower(gameMap.getNumBlocks(loc));
        gameStats.setTimeToTallestTower(currentRound);
        }
        }
         */

        // determine if the game is over, and if so, who the winner is
        // the game ends when either team has no living archons, when the round limit is up
        // or when one team has exceeded the map's MIN_POINTS and has a lead of at least MIN_POINTS / 2
        // the algorithm to determine the winner is:
        // (1) team that killed the other team's archons
        // (3) team that has the most points (mined the most flux)
        // (4) team with the greatest energon production among living archons
        // (5) team with the most total energon at the end of the game
        // (6) team with the "smaller" team string
        // (7) Team A wins
        if (teamADead || teamBDead || teamAMercy || teamBMercy) {// || currentRound >= gameMap.getMaxRounds() - 1) {

            running = false;

            for (InternalObject o : gameObjectsByID.values()) {
                if (o instanceof InternalRobot)
                    RobotMonitor.killRobot(o.getID());
            }

            //System.out.println("Game ended");

            gameStats.setPoints(Team.A, aPoints);
            gameStats.setPoints(Team.B, bPoints);
            gameStats.setNumArchons(Team.A, numArchons[0]);
            gameStats.setNumArchons(Team.B, numArchons[1]);
            gameStats.setTotalEnergon(Team.A, totalEnergon[0]);
            gameStats.setTotalEnergon(Team.B, totalEnergon[1]);
            if (!teamADead && teamBDead) {
                winner = Team.A;
                if (numArchons[0] >= NUM_ARCHONS_PER_TEAM)
                    gameStats.setDominationFactor(DominationFactor.DESTROYED);
                else
                    gameStats.setDominationFactor(DominationFactor.PWNED);
            } else if (!teamBDead && teamADead) {
                winner = Team.B;
                if (numArchons[1] >= NUM_ARCHONS_PER_TEAM)
                    gameStats.setDominationFactor(DominationFactor.DESTROYED);
                else
                    gameStats.setDominationFactor(DominationFactor.PWNED);
            } else if (aPoints != bPoints) {
                if (teamAMercy) {
                    gameStats.setDominationFactor(DominationFactor.OWNED);
                    winner = Team.A;
                } else if (teamBMercy) {
                    gameStats.setDominationFactor(DominationFactor.OWNED);
                    winner = Team.B;
                } else {
                    gameStats.setDominationFactor(DominationFactor.BEAT);
                    if (aPoints > bPoints)
                        winner = Team.A;
                    else
                        winner = Team.B;
                }
            } else {
                if (numArchons[0] > numArchons[1]) {
                    winner = Team.A;
                    gameStats.setDominationFactor(DominationFactor.BARELY_BEAT);
                } else if (numArchons[0] < numArchons[1]) {
                    winner = Team.B;
                    gameStats.setDominationFactor(DominationFactor.BARELY_BEAT);
                } else {
                    gameStats.setDominationFactor(DominationFactor.WON_BY_DUBIOUS_REASONS);
                    if (totalEnergon[0] > totalEnergon[1])
                        winner = Team.A;
                    else if (totalEnergon[1] > totalEnergon[0])
                        winner = Team.B;
                    else {
                        if (teamAName.compareTo(teamBName) <= 0)
                            winner = Team.A;
                        else
                            winner = Team.B;
                    }
                }
            }
        }

        // TESTING
        //~ if(winner != null) {
        //~ System.out.println("DF: " + gameStats.getDominationFactor());
        //~ System.out.println("XF: " + gameStats.getExcitementFactor());
        //~ int[] firstKill = gameStats.getTimeToFirstKill();
        //~ System.out.println("1st kill: " + firstKill[0] + ", " + firstKill[1]);
        //~ int[] firstArchonKill = gameStats.getTimeToFirstArchonKill();
        //~ System.out.println("1st archon kill: " + firstArchonKill[0] + ", " + firstArchonKill[1]);
        //~ }
    }

    // TODO: algo for points
    // FIXME: should only be called once per turn per team
    private double getRoundPoints(Team t) {
        return scoreCalcs[t.ordinal()].getScore();
    }

    public void mineFlux(InternalRobot r) {
        MapLocation robotLoc = r.getLocation();
        int i;
        int flux;
        int team = r.getTeam().ordinal();
        int x = robotLoc.getX();
        int y = robotLoc.getY();
        MapLocation[] locs = new MapLocation[fluxMineOffsetsX.length];
        for (i = fluxMineOffsetsX.length - 1; i >= 0; i--) {
            MapLocation loc = new MapLocation(x + fluxMineOffsetsX[i], y + fluxMineOffsetsY[i]);
            flux = gameMap.mineFlux(loc);
            r.incrementFlux(flux);
            //teamPoints gets changed in incrementFlux now
            //teamPoints[team] += flux;
            int normalX = x + fluxMineOffsetsX[i] - origin.getX();
            int normalY = y + fluxMineOffsetsY[i] - origin.getY();
            if (normalX < 0 || normalX >= gameMap.getWidth())
                continue;
            if (normalY < 0 || normalY >= gameMap.getHeight())
                continue;
            minedLocs[normalY][normalX] = true;
        }
    }

    public InternalObject getObject(MapLocation loc, RobotLevel level) {
        return gameObjectsByLoc.get(new MapLocation3D(loc, level));
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

    public void notifyAddingNewTeleporter(InternalRobot r) {
        teleportersByTeam[r.getTeam().ordinal()].add(r.getLocation());
    }

    public void notifyAddingNewMortar(InternalMortar m) {
        if (mortars.contains(m))
            return;
        mortars.add(m);
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
            if (r.getRobotType() == RobotType.TELEPORTER)
                teleportersByTeam[r.getTeam().ordinal()].remove(r.getLocation());
        }
    }

    public void removeMortar(InternalMortar m) {
        if (mortars.contains(m))
            mortars.remove(m);
    }

    public boolean isExistant(InternalObject o) {
        return gameObjectsByID.containsKey(o.getID());
    }

    /**
     *@return the TerrainType at a given MapLocation <tt>loc<tt>
     */
    public TerrainTile getMapTerrain(MapLocation loc) {
        return gameMap.getTerrainTile(loc);
    }

    // returns all robots at the given height in the sensor range of r, except for r itself
    public InternalRobot[] senseNearbyRobots(InternalRobot r, RobotLevel level) {
        MapLocation robotLoc = r.getLocation();
        Direction robotDir = r.getDirection();
        double cosHalfTheta = r.getRobotType().sensorCosHalfTheta();

        InternalRobot[] robots = getAllRobotsWithinRadiusDonutSq(robotLoc, r.getRobotType().sensorRadiusSquared(), -1);
        ArrayList<InternalRobot> result = new ArrayList<InternalRobot>(robots.length);

        for (int i = 0; i < robots.length; i++) {
            if (robots[i].getRobotLevel() != level || robots[i] == r)
                continue;

            if (inAngleRange(robotLoc, robotDir, robots[i].getLocation(), cosHalfTheta))
                result.add(robots[i]);
        }

        return result.toArray(new InternalRobot[result.size()]);
    }

    public MapLocation[] senseNearbyBlocks(InternalRobot r) {
        MapLocation robotLoc = r.getLocation();
        Direction robotDir = r.getDirection();
        double cosHalfTheta = r.getRobotType().sensorCosHalfTheta();

        MapLocation[] locs = getAllMapLocationsWithinRadiusSq(robotLoc, r.getRobotType().sensorRadiusSquared());
        ArrayList<MapLocation> result = new ArrayList<MapLocation>(locs.length);

        for (int i = 0; i < locs.length; i++) {
            if (gameMap.getNumBlocks(locs[i]) <= 0)
                continue;
            if (!inAngleRange(robotLoc, robotDir, locs[i], cosHalfTheta))
                continue;
            result.add(locs[i]);
        }

        return result.toArray(new MapLocation[result.size()]);
    }

    public MapLocation[] senseTeleporters(Team t) {
        Set<MapLocation> alliedTeleporters = teleportersByTeam[t.ordinal()];
        return alliedTeleporters.toArray(new MapLocation[alliedTeleporters.size()]);
    }

    public int senseNumBlocksAtLocation(MapLocation loc) {
        return gameMap.getNumBlocks(loc);
    }

    public InternalMortar[] senseNearbyMortars(MapLocation loc, int distSquared) {
        ArrayList<InternalMortar> result = new ArrayList<InternalMortar>();
        for (InternalMortar m : mortars) {
            if (m.getTarget().distanceSquaredTo(loc) <= distSquared)
                result.add(m);
        }
        return result.toArray(new InternalMortar[result.size()]);
    }

    public void applyAuraDamageDealt(InternalRobot auraBuilding) {
        InternalRobot[] robots = getAllRobotsWithinRadiusDonutSq(auraBuilding.getLocation(), auraBuilding.getRobotType().sensorRadiusSquared(), -1);
        Team teamAffected = auraBuilding.getTeam();
        for (InternalRobot robot : robots)
            if (robot.getTeam() == teamAffected) {
                if (!robot.getBuffs().containsBuff(BuffType.AURA_OFF)) {
                    robot.getBuffs().addBuff(new OffensiveAuraBuff(robot));
                }
            }
    }

    public void applyAuraMovement(InternalAura auraBuilding) {
        InternalRobot[] robots = getAllRobotsWithinRadiusDonutSq(auraBuilding.getLocation(), auraBuilding.getRobotType().sensorRadiusSquared(), -1);
        Team teamAffected = auraBuilding.getTeam();
        for (InternalRobot robot : robots)
            if (robot.getTeam() == teamAffected) {
                if (!robot.getBuffs().containsBuff(BuffType.AURA_MOV)) {
                    robot.getBuffs().addBuff(new MovementAuraBuff(robot));
                }
            }
    }

    public void applyAuraDamageReceived(InternalRobot auraBuilding) {
        InternalRobot[] robots = getAllRobotsWithinRadiusDonutSq(auraBuilding.getLocation(), auraBuilding.getRobotType().sensorRadiusSquared(), -1);
        Team teamAffected = auraBuilding.getTeam();
        for (InternalRobot robot : robots)
            if (robot.getTeam() == teamAffected) {
                if (!robot.getBuffs().containsBuff(BuffType.AURA_DEF)) {
                    robot.getBuffs().addBuff(new DefensiveAuraBuff(robot));
                }
            }
    }

    public int senseNumBlocksOfRobot(InternalRobot r) {
        if (r.getRobotType() == RobotType.WOUT)
            return ((InternalWorker) r).getNumBlocks();
        return 0;
    }

    // TODO: optimize this?
    public int getUnitCount(RobotType type, Team team) {
        int result = 0;
        for (InternalObject o : gameObjectsByID.values()) {
            if (!(o instanceof InternalRobot))
                continue;
            if (((InternalRobot) o).getRobotType() == type && ((InternalRobot) o).getTeam() == team)
                result++;
        }

        return result;
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
        return teamPoints[team.ordinal()];
    }

    public boolean canMove(InternalRobot r, Direction dir) {
        if (dir == Direction.NONE || dir == Direction.OMNI)
            return false;

        MapLocation loc = r.getLocation().add(dir);

        return canMove(r.getRobotType(), loc);
    }

    public boolean canMove(RobotType type, MapLocation loc) {
        RobotLevel level = (type.isAirborne() ? RobotLevel.IN_AIR : RobotLevel.ON_GROUND);

        return canMove(level, loc);
    }

    public boolean canMove(RobotLevel level, MapLocation loc) {

        return gameMap.getTerrainTile(loc).isTraversableAtHeight(level) && (gameObjectsByLoc.get(new MapLocation3D(loc, level)) == null);
    }

    public boolean canAttack(InternalRobot r, MapLocation loc, RobotLevel level) {
        RobotType type = r.getRobotType();

        // first, check that we can attack the specified height
        if (level == RobotLevel.ON_GROUND) {
            if (!type.canAttackGround())
                return false;
        } else if (level == RobotLevel.IN_AIR) {
            if (!type.canAttackAir())
                return false;
        } else return false;

        return canAttack(r, loc);
    }

    public boolean canAttack(InternalRobot r, MapLocation loc) {
        RobotType type = r.getRobotType();
        MapLocation robotLoc = r.getLocation();

        // first, check that we're in range
        int attackRangeSquared = type.attackRadiusMaxSquared();
        if (robotLoc.distanceSquaredTo(loc) > attackRangeSquared)
            return false;

        int minRangeSquared = type.attackRadiusMinSquared();
        if (robotLoc.distanceSquaredTo(loc) < minRangeSquared)
            return false;

        // finally, check that loc is in the right angular range
        return inAngleRange(robotLoc, r.getDirection(), loc, type.attackCosHalfTheta());
    }

    public void broadcastMessage(InternalRobot r, Message m) {
        int radius = r.getRobotType().broadcastRadius();
        InternalRobot[] robots = getAllRobotsWithinRadiusDonutSq(r.getLocation(), radius * radius, -1);

        for (int i = 0; i < robots.length; i++) {
            if (robots[i] != r)
                robots[i].enqueueIncomingMessage((Message) m.clone());
        }
    }

    public boolean canSense(InternalRobot r, MapLocation loc) {
        if (loc == null)
            return false;

        MapLocation robotLoc = r.getLocation();
        RobotType robotType = r.getRobotType();

        if (robotLoc.distanceSquaredTo(loc) > robotType.sensorRadiusSquared())
            return false;

        return inAngleRange(robotLoc, r.getDirection(), loc, robotType.sensorCosHalfTheta());
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

    public InternalMortar[] getAllMortars() {
        return mortars.toArray(new InternalMortar[mortars.size()]);
    }

    public InternalObject[] getAllGameObjects() {
        return gameObjectsByID.values().toArray(new InternalObject[gameObjectsByID.size()]);
    }

    public Signal[] getAllSignals(boolean includeBytecodesUsedSignal) {
        ArrayList<InternalRobot> energonChangedRobots = new ArrayList<InternalRobot>();
        ArrayList<InternalRobot> fluxChangedRobots = new ArrayList<InternalRobot>();
        ArrayList<InternalRobot> allRobots = null;
        if (includeBytecodesUsedSignal)
            allRobots = new ArrayList<InternalRobot>();
        for (InternalObject obj : gameObjectsByID.values()) {
            if (!(obj instanceof InternalRobot))
                continue;
            InternalRobot r = (InternalRobot) obj;
            if (includeBytecodesUsedSignal)
                allRobots.add(r);
            if (r.getRobotType().isBuilding()) {
                if (r.clearEnergonChanged()) {
                    energonChangedRobots.add(r);
                    fluxChangedRobots.add(r);
                }
            } else {
                if (r.clearEnergonChanged()) {
                    energonChangedRobots.add(r);
                }
                if (r.clearFluxChanged()) {
                    fluxChangedRobots.add(r);
                }
            }

        }
        signals.add(new FluxChangeSignal(fluxChangedRobots.toArray(new InternalRobot[]{})));
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
        int numBytecodes = RobotMonitor.getBytecodeNum();
        if (numBytecodes > GameConstants.BYTECODES_PER_ROUND)
            numBytecodes = GameConstants.BYTECODES_PER_ROUND;
        r.setBytecodesUsed(numBytecodes);
        r.processEndOfTurn();
    }

    public InternalRobot getClosestArchon(MapLocation loc, Team t) {
        InternalRobot closest = null;
        for (InternalObject obj : gameObjectsByID.values()) {
            if (!(obj instanceof InternalArchon))
                continue;
            if (obj.getTeam() != t)
                continue;
            if (closest == null || loc.distanceSquaredTo(obj.getLocation()) < loc.distanceSquaredTo(closest.getLocation())) {
                closest = (InternalRobot) obj;
            }
        }
        return closest;
    }

    public InternalRobot[] getArchons(Team t) {
        InternalRobot[] archons = new InternalRobot[NUM_ARCHONS_PER_TEAM];
        int i = 0;

        for (InternalObject obj : gameObjectsByID.values()) {
            if (!(obj instanceof InternalArchon))
                continue;
            if (obj.getTeam() != t)
                continue;
            archons[i++] = (InternalRobot) obj;
        }
        InternalRobot[] ret = new InternalRobot[i];
        System.arraycopy(archons, 0, ret, 0, i);
        return ret;
    }

	public void resetStatic() {
		InternalArchon.reset();
		Config options = Config.getGlobalConfig();
		InternalRobot.setUpkeepEnabled(options.getBoolean("bc.engine.upkeep"));
	}

    public void addConvexHullSignal(Team t) {
        signals.add(new ConvexHullSignal(t, scoreCalcs[t.ordinal()].hullArray()));
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

            double totalDamage = attacker.attackPower();

            /** Create an internal mortar -- we might use this for something similar...
            if(attacker.getRobotType() == RobotType.MORTAR) {
            new InternalMortar(this, targetLoc, attacker, totalDamage);
            }
             */
            if (attacker.getRobotType() == RobotType.CHAINER) {
                //MapLocation blank = targetLoc.add(targetLoc.directionTo(attacker.getLocation()));
                InternalRobot[] hits = getAllRobotsWithinRadiusDonutSq(targetLoc, GameConstants.CHAINER_SPLASH_RADIUS_SQUARED, -1);
                for (InternalRobot r : hits) {
                    if (r.getRobotLevel() == level /*&& !r.getLocation().equals(blank)*/)
                        r.changeEnergonLevelFromAttack(-totalDamage);
                }
            } else if (target != null) {
                target.changeEnergonLevelFromAttack(-totalDamage);
                if (attacker.getRobotType() == RobotType.SOLDIER)
                    target.getBuffs().addBuff(new MovementBuff(target, GameConstants.SOLDIER_MOVEMENT_DECREASE_ROUNDS, 0, GameConstants.SOLDIER_SLOW_ROUNDS));
            }

            attacker.setAction(ActionType.ATTACKING, attacker.getRobotType().attackDelay());

            addSignal(s);
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    public Exception visitBroadcastSignal(BroadcastSignal s) {
        return new Exception();
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
                if (r.getRobotType().isBuilding()) {
                    ScoreCalculator sc = scoreCalcs[r.getTeam().ordinal()];
                    sc.remove(r.getLocation());
                    addConvexHullSignal(r.getTeam());
                }
                if (r.hasBeenAttacked()) {
                    if (r.getRobotType() == RobotType.ARCHON)
                        gameStats.setArchonKilled(r.getTeam(), currentRound);
                    else
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

    public Exception visitEnergonTransferSignal(EnergonTransferSignal s) {
        try {
            InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
            MapLocation loc = s.getTargetLoc();
            double amount = s.getAmount();

            InternalRobot targetRobot = getRobot(loc, s.getTargetHeight());
            if (targetRobot == null) {
                System.out.println("Empty square should have been caught in RobotControllerImpl!");
                return new GameActionException(GameActionExceptionType.CANT_TRANSFER_ENERGON_THERE, "Cannot transfer energon to an empty square");
            }

            targetRobot.receiveTransfer(amount);
            r.changeEnergonLevel(-amount);

            addSignal(s);
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    public Exception visitFluxTransferSignal(FluxTransferSignal s) {
        try {
            InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
            MapLocation loc = s.getTargetLoc();
            double amount = s.getAmount();

            InternalRobot targetRobot = getRobot(loc, s.getTargetHeight());
            if (targetRobot == null) {
                System.out.println("Empty square should have been caught in RobotControllerImpl!");
                return new GameActionException(GameActionExceptionType.CANT_TRANSFER_FLUX_THERE, "Cannot transfer flux to an empty square");
            }

            double energoneq = GameConstants.FLUX_TO_ENERGON_CONVERSION * amount;

            if (targetRobot.getRobotType().isBuilding())
                targetRobot.receiveTransfer(energoneq);
            else
                targetRobot.incrementFlux(amount);

            if (r.getRobotType().isBuilding())
                r.changeEnergonLevel(-energoneq);
            else
                r.incrementFlux(-amount);
            // Ooops, someone transferred flux to the other team!
			/* handled in incrementFlux now
            if (r.getTeam() != targetRobot.getTeam()) {
            teamPoints[targetRobot.getTeam().ordinal()] += amount;
            teamPoints[r.getTeam().ordinal()] -= amount;
            }
             */

            addSignal(s);
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    public Exception visitEvolutionSignal(EvolutionSignal s) {
        try {
            InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
            RobotType type = s.getType();

            //they were already charged for the energon in RobotControllerImpl.evolve()
            r.setAction(ActionType.TRANSFORMING, type.wakeDelay() + (Math.max(r.getRoundsUntilAttackIdle(), r.getRoundsUntilMovementIdle())));
            r.setRobotType(type);

            addSignal(s);
        } catch (Exception e) {
            return e;
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
            if (!canMove(r.getRobotType(), s.getNewLoc()))
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
            MapLocation loc = (s.isMovingForward() ? r.getLocation().add(r.getDirection()) : r.getLocation().add(r.getDirection().opposite()));

            int delay = r.getDirection().isDiagonal() ? r.getRobotType().moveDelayDiagonal() : r.getRobotType().moveDelayOrthogonal();

            r.setLocation(loc);

            r.setAction(ActionType.MOVING, delay);

            addSignal(new MovementSignal(r, loc, s.isMovingForward(), delay));
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    public Exception visitSetAuraSignal(SetAuraSignal s) {
        try {
            InternalAura a = (InternalAura) getObjectByID(s.getRobotID());
            AuraType t = s.getAura();

            a.setAura(t);

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
            r.setAction(ActionType.SETTING_DIRECTION, 1);

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
                loc = parent.getLocation().add(parent.getDirection());
            }
            RobotType type = s.getType();

            //note: this also adds the signal
            GameWorldFactory.createPlayer(this, type, loc, s.getTeam(), parent);

            if (type.isBuilding()) {
                ScoreCalculator sc = scoreCalcs[s.getTeam().ordinal()];
                sc.add(loc);
                addConvexHullSignal(s.getTeam());
            }

            if (parent != null) {
                //we already charged them for the energon in RobotControllerImpl.spawn()
                //no need to charge them again
                parent.setAction(ActionType.SPAWNING, type.spawnDelay());
            }

        } catch (Exception e) {
            return e;
        }
        return null;
    }

    public Exception visitStartTeleportSignal(StartTeleportSignal s) {
        try {
            InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
            InternalRobot fromTeleporter = (InternalRobot) getObjectByID(s.getFromTeleporterID());
            InternalRobot toTeleporter = (InternalRobot) getObjectByID(s.getToTeleporterID());
            r.setTeleportAction(fromTeleporter, toTeleporter, s.getTeleportLoc());
            addSignal(s);
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    public Exception visitDoTeleportSignal(DoTeleportSignal s) {
        try {
            InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
            r.setLocation(s.getTeleportLoc());
            //addSignal(s);
//            addSignal(new MovementSignal(r, s.getTeleportLoc(), true, 0));
            addSignal(new DoTeleportSignal(r, s.getTeleportLoc()));
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    public Exception visitDeploySignal(DeploySignal s) {
        try {
            InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
            r.setAction(ActionType.DEPLOYING, GameConstants.TURRET_DEPLOY_TIME);
            r.getBuffs().addBuff(new TurretDeployBuff(r));
        } catch (Exception ex) {
            return ex;
        }
        return null;
    }

    public Exception visitUndeploySignal(UndeploySignal s) {
        try {
            InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
            r.setAction(ActionType.UNDEPLOYING, 1/*GameConstants.TURRET_UNDEPLOY_TIME*/);
            r.getBuffs().removeBuff(BuffType.TURRET_DEPLOY);
        } catch (Exception ex) {
            return ex;
        }
        return null;
    }

    public Exception visitLightningShieldSignal(LightningShieldSignal s) {
        try {
            InternalRobot r = (InternalRobot) getObjectByID(s.getRobotID());
            //r.getBuffs().addBuff(new LightningShieldDebuff(r, s.getRounds(), s.getRadius(), s.getPower()));
        } catch (Exception ex) {
            return ex;
        }
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

    protected void adjustTeamPoints(InternalRobot r, int points) {
        teamPoints[r.getTeam().ordinal()] += points;
    }

    public Exception visitFluxChangeSignal(FluxChangeSignal s) {
        int[] robotIDs = s.getRobotIDs();
        double[] flux = s.getFlux();
        return null;
    }

    public Exception visitMineFluxSignal(MineFluxSignal s) {
        return null;
    }
}
