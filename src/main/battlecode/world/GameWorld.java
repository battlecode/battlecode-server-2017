package battlecode.world;

import battlecode.common.*;
import battlecode.server.ErrorReporter;
import battlecode.server.GameState;
import battlecode.serial.GameStats;
import battlecode.world.control.RobotControlProvider;

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

    protected Team winner = null;
    protected final IDGenerator idGeneratorRobots;
    protected final IDGenerator idGeneratorTrees;
    protected final IDGenerator idGeneratorBullets;

    private final GameMap gameMap;
    private final TeamInfo teamInfo;
    private final ObjectInfo objectInfo;

    private final RobotControlProvider controlProvider;
    private final GameStats gameStats = new GameStats();
    private Random rand;

    @SuppressWarnings("unchecked")
    public GameWorld(GameMap gm, RobotControlProvider cp,
                     String teamA, String teamB,
                     long[][] oldTeamMemory) {
        
        this.currentRound = -1;
        this.idGeneratorRobots = new IDGenerator(gm.getSeed());
        this.idGeneratorTrees = new IDGenerator(gm.getSeed());
        this.idGeneratorBullets = new IDGenerator(gm.getSeed());

        this.gameMap = gm;
        this.objectInfo = new ObjectInfo(gm);
        this.teamInfo = new TeamInfo(teamA, teamB, oldTeamMemory);

        this.controlProvider = cp;

        controlProvider.matchStarted(this);

        // Add the robots contained in the GameMap to this world.
        for(GameMap.InitialRobotInfo robot : gameMap.getInitialRobots()){
            spawnRobot(robot.type, robot.getLocation(gameMap.getOrigin()), robot.team);
        }

        // Add the trees contained in the GameMap to this world.
        for(GameMap.InitialTreeInfo tree : gameMap.getInitialTrees()){
            spawnTree(tree.team, tree.radius, tree.getLocation(gameMap.getOrigin()),
                    tree.containedBullets, tree.containedRobot);
        }

        this.rand = new Random(gameMap.getSeed());
    }

    /**
     * Run a single round of the game.
     *
     * @return the state of the game after the round has run.
     */
    public synchronized GameState runRound() {
        if (!this.isRunning()) {
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
            return GameState.DONE;
        }

        return GameState.RUNNING;
    }

    private void updateTrees(){
        final int[] idsToRun = objectInfo.getTreeIDs();
        double[] totalTreeSupply = new double[3];
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

    public GameStats getGameStats() {
        return gameStats;
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public TeamInfo getTeamInfo() {
        return teamInfo;
    }

    public ObjectInfo getObjectInfo() {
        return objectInfo;
    }

    public Team getWinner() {
        return winner;
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
        currentRound++;
    }

    public void setWinner(Team t, DominationFactor d)  {
        winner = t;
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

    }

    // *********************************
    // ****** SPAWNING *****************
    // *********************************

    public int spawnTree(Team team, float radius, MapLocation center,
                         float containedBullets, RobotType containedRobot){
        int ID = idGeneratorTrees.nextID();

        InternalTree tree = new InternalTree(
                this, ID, team, radius, center, containedBullets, containedRobot);
        objectInfo.spawnTree(tree);

        return ID;
    }

    public int spawnRobot(RobotType type, MapLocation location, Team team){
        int ID = idGeneratorRobots.nextID();

        InternalRobot robot = new InternalRobot(this, ID, type, location, team);
        objectInfo.spawnRobot(robot);

        controlProvider.robotSpawned(robot);
        return ID;
    }

    public int spawnBullet(Team team, float speed, float damage, MapLocation location, Direction direction){
        int ID = idGeneratorBullets.nextID();

        InternalBullet bullet = new InternalBullet(
                this, ID, team, speed, damage, location, direction);
        objectInfo.spawnBullet(bullet);

        return ID;
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
    }

    public void destroyRobot(int id){
        InternalRobot robot = objectInfo.getRobotByID(id);

        controlProvider.robotKilled(robot);
        objectInfo.destroyRobot(id);

        setWinnerIfDestruction();
    }

    public void destroyBullet(int id){
        objectInfo.destroyBullet(id);
    }

}
