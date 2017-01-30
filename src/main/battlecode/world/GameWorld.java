package battlecode.world;

import battlecode.common.*;
import battlecode.server.ErrorReporter;
import battlecode.server.GameMaker;
import battlecode.server.GameState;
import battlecode.world.control.RobotControlProvider;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.*;

/**
 * The primary implementation of the GameWorld interface for containing and
 * modifying the game map and the objects on it.
 */
public strictfp class GameWorld {
    /**
     * The current round we're running.
     */
    protected int currentRound;

    /**
     * Whether we're running.
     */
    protected boolean running = true;

    protected final IDGenerator idGenerator;
    protected final IDGenerator bulletIdGenerator;
    protected final GameStats gameStats;

    private final LiveMap gameMap;
    private final TeamInfo teamInfo;
    private final ObjectInfo objectInfo;

    private RobotInfo[] previousBroadcasters;
    private TIntObjectHashMap<RobotInfo> currentBroadcasters;

    private final RobotControlProvider controlProvider;
    private Random rand;

    private final GameMaker.MatchMaker matchMaker;

    @SuppressWarnings("unchecked")
    public GameWorld(LiveMap gm, RobotControlProvider cp,
                     long[][] oldTeamMemory, GameMaker.MatchMaker matchMaker) {

        this.currentRound = 0;
        this.idGenerator = new IDGenerator(gm.getSeed());
        this.bulletIdGenerator = new IDGenerator(gm.getSeed());
        this.bulletIdGenerator.setStart(GameConstants.MAX_ROBOT_ID+1);
        this.gameStats = new GameStats();

        this.gameMap = gm;
        this.objectInfo = new ObjectInfo(gm);
        this.teamInfo = new TeamInfo(oldTeamMemory);

        this.previousBroadcasters = new RobotInfo[0];
        this.currentBroadcasters = new TIntObjectHashMap<>();

        this.controlProvider = cp;

        this.rand = new Random(gameMap.getSeed());

        this.matchMaker = matchMaker;

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

            updateDynamicBodies();

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
        matchMaker.makeRound(currentRound);
        return GameState.RUNNING;
    }

    private void updateTrees(){
        float[] totalTreeSupply = new float[3];
        objectInfo.eachTree((tree) -> {
            totalTreeSupply[tree.getTeam().ordinal()] += tree.updateTree();
            return true;
        });
        teamInfo.adjustBulletSupply(Team.A, totalTreeSupply[Team.A.ordinal()]);
        teamInfo.adjustBulletSupply(Team.B, totalTreeSupply[Team.B.ordinal()]);
    }

    private void updateDynamicBodies(){
        objectInfo.eachDynamicBodyByExecOrder((body) -> {
            if (body instanceof InternalRobot) {
                return updateRobot((InternalRobot) body);
            } else if (body instanceof InternalBullet) {
                return updateBullet((InternalBullet) body);
            } else {
                throw new RuntimeException("non-robot non-bullet body registered as dynamic");
            }
        });
    }

    private boolean updateRobot(InternalRobot robot) {
        robot.processBeginningOfTurn();
        this.controlProvider.runRobot(robot);
        robot.setBytecodesUsed(this.controlProvider.getBytecodesUsed(robot));

        if(robot.getHealth() > 0) { // Only processEndOfTurn if robot is still alive
            robot.processEndOfTurn();
        }

        // If the robot terminates but the death signal has not yet
        // been visited:
        if (this.controlProvider.getTerminated(robot) && objectInfo.getRobotByID(robot.getID()) != null) {
            destroyRobot(robot.getID());
        }
        return true;
    }

    private boolean updateBullet(InternalBullet bullet) {
        bullet.updateBullet();
        return true;
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

    public GameMaker.MatchMaker getMatchMaker() {
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
        objectInfo.eachRobot((robot) -> {
            robot.processBeginningOfRound();
            return true;
        });
        objectInfo.eachTree((tree) -> {
            tree.processBeginningOfRound();
            return true;
        });
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

    public void setWinnerIfVictoryPoints() {
        if(teamInfo.getVictoryPoints(Team.A) >= GameConstants.VICTORY_POINTS_TO_WIN) {
            setWinner(Team.A, DominationFactor.PHILANTROPIED);
        } else if(teamInfo.getVictoryPoints(Team.B) >= GameConstants.VICTORY_POINTS_TO_WIN) {
            setWinner(Team.B, DominationFactor.PHILANTROPIED);
        }
    }

    public boolean timeLimitReached() {
        return currentRound >= gameMap.getRounds() - 1;
    }

    public void processEndOfRound() {
        // Process end of each robot's round
        objectInfo.eachRobot((robot) -> {
            robot.processEndOfRound();
            return true;
        });
        // Process end of each tree's round
        objectInfo.eachTree((tree) -> {
            tree.processEndOfRound();
            return true;
        });

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
                for(InternalRobot robot : objectInfo.robots()){
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

        // update the round statistics
        matchMaker.addTeamStat(Team.A,teamInfo.getBulletSupply(Team.A), teamInfo.getVictoryPoints(Team.A));
        matchMaker.addTeamStat(Team.B, teamInfo.getBulletSupply(Team.B), teamInfo.getVictoryPoints(Team.B));

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

    public int spawnBullet(int ID, Team team, float speed, float damage, MapLocation location, Direction direction, InternalRobot parent){
        InternalBullet bullet = new InternalBullet(
                this, ID, team, speed, damage, location, direction);

        matchMaker.addSpawnedBullet(bullet); // Even if the bullet will die this turn, make sure information about it is saved in the match file

        // Check for collisions in the spot the bullet is being spawned
        InternalRobot bot = this.objectInfo.getRobotAtLocation(location);
        InternalTree tree = this.objectInfo.getTreeAtLocation(location);

        if(bot != null) {
            // If a there is a bot at this location, damage it.
            bot.damageRobot(damage);
            matchMaker.addDied(ID,true);
        } else if (tree != null) {
            // If a there is a tree at this location, damage it.
            tree.damageTree(damage,team,false);
            matchMaker.addDied(ID,true);
        } else {
            // Else, nothing else exists where the bullet was spawned. Go ahead and add it to spatial index.
            objectInfo.spawnBullet(bullet, parent);
        }
        return ID;
    }

    public int spawnBullet(Team team, float speed, float damage, MapLocation location, Direction direction, InternalRobot parent){
        int ID = bulletIdGenerator.nextID();
        return spawnBullet(ID, team, speed, damage, location, direction, parent);
    }

    // *********************************
    // ****** DESTROYING ***************
    // *********************************

    public void destroyTree(int id, Team destroyedBy, boolean fromChop){
        InternalTree tree = objectInfo.getTreeByID(id);

        // Only chopping can release goodies
        if(fromChop) {
            RobotType toSpawn = tree.getContainedRobot();
            float containedBullets = tree.getContainedBullets();

            // Spawn a robot if there was one in the tree
            if (toSpawn != null && destroyedBy != Team.NEUTRAL && fromChop) {


                //First, kill any scouts that would overlap with the new robot
                InternalRobot[] overlappingBots = objectInfo.getAllRobotsWithinRadius(tree.getLocation(), toSpawn.bodyRadius);
                for(InternalRobot bot : overlappingBots) {
                    if(bot.getType() == RobotType.SCOUT) {
                        this.destroyRobot(bot.getID());
                    } else {
                        // TODO: seems like we only hit this on floating point errors
                        //throw new RuntimeException("The robot within the tree was overlapping with a non-scout robot");
                    }
                }

                // Now spawn the new robot
                this.spawnRobot(toSpawn, tree.getLocation(), destroyedBy);
            }
            if (containedBullets > 0 && fromChop) {
                this.teamInfo.adjustBulletSupply(destroyedBy,containedBullets);
            }
        }

        objectInfo.destroyTree(id);

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
        this.previousBroadcasters = this.currentBroadcasters.values(
                new RobotInfo[this.currentBroadcasters.size()]
        );
        this.currentBroadcasters.clear();
    }

    public void addBroadcaster(RobotInfo robot){
        this.currentBroadcasters.put(robot.ID, robot);
    }

    /**
     * Don't mutate this.
     */
    public RobotInfo[] getPreviousBroadcasters(){
        return this.previousBroadcasters;
    }

}
