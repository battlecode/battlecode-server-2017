package battlecode.world;

import battlecode.common.*;
import battlecode.server.ErrorReporter;
import battlecode.server.GameState;
import battlecode.server.TeamMapping;
import battlecode.world.control.RobotControlProvider;
import com.google.flatbuffers.FlatBufferBuilder;

import java.util.*;

/**
 * The primary implementation of the GameWorld interface for containing and
 * modifying the game map and the objects on it.
 */
public class GameWorld{
    /**
     * The current round we're running.
     */
    protected int currentRound;

    /**
     * Whether we're running.
     */
    protected boolean running = true;

    protected final IDGenerator idGenerator;
    protected final TeamMapping teamMapping;
    protected final GameStats gameStats;

    private final LiveMap gameMap;
    private final TeamInfo teamInfo;
    private final ObjectInfo objectInfo;

    private Collection<RobotInfo> previousBroadcasters;
    private Map<Integer, RobotInfo> currentBroadcasters;

    private final RobotControlProvider controlProvider;
    private Random rand;

    private final FlatBufferBuilder builder;
    private final MatchMaker matchMaker;

    @SuppressWarnings("unchecked")
    public GameWorld(LiveMap gm, RobotControlProvider cp,
                     TeamMapping teamMapping,
                     long[][] oldTeamMemory, FlatBufferBuilder builder) {
        
        this.currentRound = 0;
        this.idGenerator = new IDGenerator(gm.getSeed());
        this.teamMapping = teamMapping;
        this.gameStats = new GameStats();

        this.gameMap = gm;
        this.objectInfo = new ObjectInfo(gm);
        this.teamInfo = new TeamInfo(oldTeamMemory);

        this.previousBroadcasters = new ArrayList<>();
        this.currentBroadcasters = new HashMap<>();

        this.controlProvider = cp;

        this.rand = new Random(gameMap.getSeed());

        this.builder = builder;
        this.matchMaker = new MatchMaker(builder, teamMapping);

        controlProvider.matchStarted(this);

        // Add the robots and trees contained in the LiveMap to this world.
        for(BodyInfo body : gameMap.getInitialBodies()){
            if(body.isRobot()){
                RobotInfo robot = (RobotInfo) body;
                spawnRobot(robot.ID, robot.type, robot.location, robot.team);
            }else{
                TreeInfo tree = (TreeInfo) body;
                spawnTree(tree.ID, tree.team, tree.radius, tree.location, tree.containedBullets, tree.containedRobot);
            }
        }

        // Write match header at beginning of match
        matchMaker.makeMatchHeader(gameMap);
    }

    /**
     * Run a single round of the game.
     *
     * @return the state of the game after the round has run.
     */
    public synchronized GameState runRound() {
        if (!this.isRunning()) {
            // Write match footer if game is done
            matchMaker.makeMatchFooter(gameStats.getWinner(), currentRound);
            return GameState.DONE;
        }

        try {
            this.processBeginningOfRound();
            this.controlProvider.roundStarted();

            updateRobots();

            updateBullets();

            updateTrees();

            this.controlProvider.roundEnded();
            this.processEndOfRound();

            if (!this.isRunning()) {
                this.controlProvider.matchEnded();
            }

        } catch (Exception e) {
            ErrorReporter.report(e);
            // TODO throw out file?
            return GameState.DONE;
        }
        // Write out round data
        matchMaker.writeAndClearRoundData(currentRound);
        return GameState.RUNNING;
    }

    private void updateTrees(){
        final int[] idsToRun = objectInfo.getTreeIDs();
        float[] totalTreeSupply = new float[3];
        for(final int id : idsToRun){
            InternalTree tree = objectInfo.getTreeByID(id);
            totalTreeSupply[tree.getTeam().ordinal()] += tree.updateTree();
        }
        teamInfo.adjustBulletSupply(Team.A, totalTreeSupply[Team.A.ordinal()]);
        teamInfo.adjustBulletSupply(Team.B, totalTreeSupply[Team.B.ordinal()]);
    }

    private void updateRobots(){
        // We iterate through the IDs so that we avoid ConcurrentModificationExceptions
        // of an iterator. Kinda gross, but whatever.
        final int[] idsToRun = objectInfo.getRobotIDs();

        for (final int id : idsToRun) {
            final InternalRobot robot = objectInfo.getRobotByID(id);
            if (robot == null) {
                // Robot might have died earlier in the iteration; skip it
                continue;
            }

            robot.processBeginningOfTurn();
            this.controlProvider.runRobot(robot);
            robot.setBytecodesUsed(this.controlProvider.getBytecodesUsed(robot));

            if(robot.getHealth() > 0) { // Only processEndOfTurn if robot is still alive
                robot.processEndOfTurn();
            }

            // If the robot terminates but the death signal has not yet
            // been visited:
            if (this.controlProvider.getTerminated(robot) && objectInfo.getRobotByID(id) != null) {
                destroyRobot(id);
            }
        }
    }

    private void updateBullets(){
        final int[] idsToRun = objectInfo.getBulletIDs();
        for(final int id : idsToRun){
            InternalBullet bullet = objectInfo.getBulletByID(id);
            bullet.updateBullet();
        }
    }

    // *********************************
    // ****** BASIC MAP METHODS ********
    // *********************************

    public int getMapSeed() {
        return gameMap.getSeed();
    }

    public LiveMap getGameMap() {
        return gameMap;
    }

    public TeamInfo getTeamInfo() {
        return teamInfo;
    }

    public GameStats getGameStats() {
        return gameStats;
    }

    public ObjectInfo getObjectInfo() {
        return objectInfo;
    }

    public MatchMaker getMatchMaker() {
        return matchMaker;
    }

    public Team getWinner() {
        return gameStats.getWinner();
    }

    public boolean isRunning() {
        return running;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    // *********************************
    // ****** GAMEPLAY *****************
    // *********************************

    public void processBeginningOfRound() {
        // Increment round counter
        currentRound++;

        // Update broadcast data
        updateBroadCastData();

        // Process beginning of each robot's round
        for (InternalRobot robot : objectInfo.getAllRobots()) {
            robot.processBeginningOfRound();
        }
        // Process beginning of each tree's round
        for (InternalTree tree : objectInfo.getAllTrees()) {
            tree.processBeginningOfRound();
        }
    }

    public void setWinner(Team t, DominationFactor d)  {
        gameStats.setWinner(t);
        gameStats.setDominationFactor(d);
    }

    public void setWinnerIfDestruction(){
        if(objectInfo.getRobotCount(Team.A) == 0){
            setWinner(Team.B, DominationFactor.DESTROYED);
        }else if(objectInfo.getRobotCount(Team.B) == 0){
            setWinner(Team.A, DominationFactor.DESTROYED);
        }
    }

    public boolean timeLimitReached() {
        return currentRound >= gameMap.getRounds() - 1;
    }

    public void processEndOfRound() {
        // Process end of each robot's round
        for (InternalRobot robot : objectInfo.getAllRobots()) {
            robot.processEndOfRound();
        }
        // Process end of each tree's round
        for (InternalTree tree : objectInfo.getAllTrees()) {
            tree.processEndOfRound();
        }

        // Add the round bullet income
        teamInfo.adjustBulletSupply(Team.A, Math.max(0, GameConstants.ARCHON_BULLET_INCOME -
                GameConstants.BULLET_INCOME_UNIT_PENALTY * teamInfo.getBulletSupply(Team.A)));
        teamInfo.adjustBulletSupply(Team.B, Math.max(0, GameConstants.ARCHON_BULLET_INCOME -
                GameConstants.BULLET_INCOME_UNIT_PENALTY * teamInfo.getBulletSupply(Team.B)));

        // Check for end of match
        if (timeLimitReached() && gameStats.getWinner() == null) {
            boolean victorDetermined = false;

            // tiebreak by number of victory points
            if(teamInfo.getVictoryPoints(Team.A) != teamInfo.getVictoryPoints(Team.B)){
                setWinner(teamInfo.getVictoryPoints(Team.A) > teamInfo.getVictoryPoints(Team.B) ? Team.A : Team.B,
                        DominationFactor.PWNED);
                victorDetermined = true;
            }

            // tiebreak by bullet trees
            if(!victorDetermined){
                if(objectInfo.getTreeCount(Team.A) != objectInfo.getTreeCount(Team.B)){
                    setWinner(objectInfo.getTreeCount(Team.A) > objectInfo.getTreeCount(Team.B) ? Team.A : Team.B,
                            DominationFactor.OWNED);
                    victorDetermined = true;
                }
            }

            int bestRobotID = Integer.MIN_VALUE;
            Team bestRobotTeam = null;

            // tiebreak by total bullets
            if(!victorDetermined){
                float totalBulletSupplyA = teamInfo.getBulletSupply(Team.A);
                float totalBulletSupplyB = teamInfo.getBulletSupply(Team.B);
                for(InternalRobot robot : objectInfo.getAllRobots()){
                    if(robot.getID() > bestRobotID){
                        bestRobotID = robot.getID();
                        bestRobotTeam = robot.getTeam();

                    }
                    if(robot.getTeam() == Team.A){
                        totalBulletSupplyA += robot.getType().bulletCost;
                    }else{
                        totalBulletSupplyB += robot.getType().bulletCost;
                    }
                }
                if(totalBulletSupplyA != totalBulletSupplyB){
                    setWinner(totalBulletSupplyA > totalBulletSupplyB ? Team.A : Team.B,
                            DominationFactor.BARELY_BEAT);
                    victorDetermined = true;
                }
            }

            // tiebreak by robot id
            if(!victorDetermined){
                setWinner(bestRobotTeam, DominationFactor.WON_BY_DUBIOUS_REASONS);
            }
        }

        if (gameStats.getWinner() != null) {
            running = false;
        }
    }

    // *********************************
    // ****** SPAWNING *****************
    // *********************************

    public int spawnTree(int ID, Team team, float radius, MapLocation center,
                         int containedBullets, RobotType containedRobot){
        InternalTree tree = new InternalTree(
                this, ID, team, radius, center, containedBullets, containedRobot);
        objectInfo.spawnTree(tree);

        matchMaker.addSpawnedTree(tree);
        return ID;
    }

    public int spawnTree(Team team, float radius, MapLocation center,
                         int containedBullets, RobotType containedRobot){
        int ID = idGenerator.nextID();
        return spawnTree(ID, team, radius, center, containedBullets, containedRobot);
    }

    public int spawnRobot(int ID, RobotType type, MapLocation location, Team team){
        InternalRobot robot = new InternalRobot(this, ID, type, location, team);
        objectInfo.spawnRobot(robot);

        controlProvider.robotSpawned(robot);
        matchMaker.addSpawnedRobot(robot);
        return ID;
    }

    public int spawnRobot(RobotType type, MapLocation location, Team team){
        int ID = idGenerator.nextID();
        return spawnRobot(ID, type, location, team);
    }

    public int spawnBullet(int ID, Team team, float speed, float damage, MapLocation location, Direction direction){
        InternalBullet bullet = new InternalBullet(
                this, ID, team, speed, damage, location, direction);
        objectInfo.spawnBullet(bullet);

        matchMaker.addSpawnedBullet(bullet);
        return ID;
    }

    public int spawnBullet(Team team, float speed, float damage, MapLocation location, Direction direction){
        int ID = idGenerator.nextID();
        return spawnBullet(ID, team, speed, damage, location, direction);
    }

    // *********************************
    // ****** DESTROYING ***************
    // *********************************

    public void destroyTree(int id, Team destroyedBy){
        InternalTree tree = objectInfo.getTreeByID(id);
        RobotType toSpawn = tree.getContainedRobot();

        objectInfo.destroyTree(id);
        if(toSpawn != null && destroyedBy != Team.NEUTRAL){
            this.spawnRobot(toSpawn, tree.getLocation(), tree.getTeam());
        }

        matchMaker.addDied(id, false);
    }

    public void destroyRobot(int id){
        InternalRobot robot = objectInfo.getRobotByID(id);

        controlProvider.robotKilled(robot);
        objectInfo.destroyRobot(id);

        setWinnerIfDestruction();

        matchMaker.addDied(id, false);
    }

    public void destroyBullet(int id){
        objectInfo.destroyBullet(id);

        matchMaker.addDied(id, true);
    }

    // *********************************
    // ****** BROADCASTING *************
    // *********************************

    private void updateBroadCastData(){
        this.previousBroadcasters = this.currentBroadcasters.values();
        this.currentBroadcasters.clear();
    }

    public void addBroadcaster(RobotInfo robot){
        this.currentBroadcasters.put(robot.ID, robot);
    }

    public RobotInfo[] getPreviousBroadcasters(){
        return this.previousBroadcasters.toArray(
                new RobotInfo[this.previousBroadcasters.size()]);
    }

}
