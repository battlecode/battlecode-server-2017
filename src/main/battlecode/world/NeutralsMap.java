package battlecode.world;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.MovementType;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TerrainTile;
import battlecode.world.signal.AttackSignal;
import battlecode.world.InternalObject;
import battlecode.world.InternalRobot;
import battlecode.world.signal.MovementSignal;

/**
 * Represents a map (scalar field) of a neutral AI.
 */
public class NeutralsMap {
    /**
     * The width and height of the map.
     */
    private final int mapWidth, mapHeight;
    /**
     * The integer scalar field showing the distribution of the neutral AI.
     */
    private boolean[][] passable;
    private double[][] currentAmount;
    private double[][] growthFactor;
    private double[][] dX, dY;
    private boolean[][] hasNoise;
    private int[][] nPastrs;
    private ArrayList<MapLocation> attacks;
    private Set[][] ids;

    public NeutralsMap() {
	    attacks = new ArrayList<MapLocation>();

        this.mapWidth = 0;
        this.mapHeight = 0;

        this.growthFactor = new double[0][0];
        this.currentAmount = new double[0][0];
        dX = new double[0][0];
        dY = new double[0][0];
        hasNoise = new boolean[0][0];
        passable = new boolean[0][0];
        ids = new Set[0][0];
    }

    public NeutralsMap(double[][] growthFactor, TerrainTile[][] mapTiles) {
        attacks = new ArrayList<MapLocation>();

        this.mapWidth = growthFactor.length;
        int tempMapHeight = 0;
        for (int i = 0; i < this.mapWidth; i++) {
            tempMapHeight = Math.max(this.mapHeight, growthFactor[i].length);
        }
        this.mapHeight = tempMapHeight;

        this.growthFactor = new double[this.mapWidth][this.mapHeight];
        this.currentAmount = new double[this.mapWidth][this.mapHeight];
        for (int i = 0; i < this.mapWidth; i++) {
            System.arraycopy(growthFactor[i], 0, this.growthFactor[i], 0,
                this.mapHeight);
        }

        dX = new double[this.mapWidth][this.mapHeight];
        dY = new double[this.mapWidth][this.mapHeight];
        hasNoise = new boolean[this.mapWidth][this.mapHeight];
        nPastrs = new int[this.mapWidth][this.mapHeight];
        passable = new boolean[this.mapWidth][this.mapHeight];
        for (int i = 0; i < this.mapWidth; i++) {
            for (int j = 0; j < this.mapHeight; j++) {
                passable[i][j] = mapTiles[i][j] != TerrainTile.VOID;
            }
        }
        ids = new Set[this.mapWidth][this.mapHeight];
        for (int i = 0; i < this.mapWidth; i++) {
            for (int j = 0; j < this.mapHeight; j++) {
                ids[i][j] = new TreeSet<Integer>();
            }
        }
    }

    public NeutralsMap(NeutralsMap nm) {
        this.mapWidth = nm.mapWidth;
        this.mapHeight = nm.mapHeight;
        this.currentAmount = new double[this.mapWidth][this.mapHeight];
        for (int i = 0; i < this.mapWidth; i++) {
            System.arraycopy(nm.currentAmount[i], 0, this.currentAmount[i], 0,
                this.mapHeight);
        }
        this.growthFactor = new double[mapWidth][mapHeight];
        for (int i = 0; i < this.mapWidth; i++) {
            System.arraycopy(nm.growthFactor[i], 0, this.growthFactor[i], 0,
                this.mapHeight);
        }
        this.dX = new double[this.mapWidth][this.mapHeight];
        this.dY = new double[this.mapWidth][this.mapHeight];
        this.hasNoise = new boolean[this.mapWidth][this.mapHeight];
        this.nPastrs = new int[this.mapWidth][this.mapHeight];
        this.passable = new boolean[this.mapWidth][this.mapHeight];
        for (int i = 0; i < this.mapWidth; i++) {
            System.arraycopy(nm.passable[i], 0, this.passable[i], 0, this.mapHeight);
        }
    }

    public void createVoid(MapLocation center, int manhattanRange) {
        for (int x = center.x - manhattanRange; x <= center.x + manhattanRange; x++) {
            for (int y = center.y - manhattanRange; y <= center.y + manhattanRange; y++) {
                if (isValid(x, y)) {
                    passable[x][y] = false;
                }
            }
        }
    }

    public double get(MapLocation m) {
        if (isValid(m.x, m.y)) {
            return currentAmount[m.x][m.y];
        } else {
            return 0;
        }
    }

    public double[][] copyOfData() {
        double[][] cowsCopy = new double[this.mapWidth][this.mapHeight];
        for (int i = 0; i < this.mapWidth; i++) {
            System.arraycopy(this.growthFactor[i], 0, cowsCopy[i], 0,
                this.mapHeight);
        }
        return cowsCopy;
    }

    public double[][] copyOfCurrentAmounts() {
        double[][] cowsCopy = new double[this.mapWidth][this.mapHeight];
        for (int i = 0; i < this.mapWidth; i++) {
            System.arraycopy(this.currentAmount[i], 0, cowsCopy[i], 0,
                this.mapHeight);
        }
        return cowsCopy;
    }

    public int[][] copyOfCurrentAmountsAsInt() {
        int[][] cowsCopy = new int[this.mapWidth][this.mapHeight];
        for (int i = 0; i < this.mapWidth; i++) {
            for (int j = 0; j < this.mapHeight; j++) {
                cowsCopy[i][j] = (int) Math.round(this.currentAmount[i][j]);
            }
        }
        return cowsCopy;
    }

    public boolean isValid(int x, int y) {
        return x >= 0 && x < this.mapWidth && y >= 0 && y < this.mapHeight && passable[x][y];
    }

    @SuppressWarnings("unchecked")
    public boolean isValid(int x, int y, int fromX, int fromY) {
        // valid if ids of from is a subset of ids of to
        if (!isValid(x, y)) return false;
        return ids[x][y].containsAll(ids[fromX][fromY]);
    }

    public void print() {
        System.out.println("Neutrals Map!");
        for (int j = 0; j < this.mapHeight; j++) {
            for (int i = 0; i < this.mapWidth; i++) {
                System.out.print(Double.toString(this.currentAmount[i][j]).substring(0, 3) + " (" + this.growthFactor[i][j] + ")\t");
            }
            System.out.println();
        }
        System.out.println("END Neutrals Map");
    }

    public double getScoreChange(Team t, InternalObject[] objs) {
        double delta = 0.0;
        for (InternalObject obj : objs) {
            InternalRobot ir = (InternalRobot) obj;
            if (ir.getTeam() != t) {
                continue;
            }
            if (ir.type != RobotType.PASTR && ir.type != RobotType.SOLDIER) continue;
            if (ir.type == RobotType.SOLDIER && nPastrs[ir.getLocation().x][ir.getLocation().y] > 0) continue; // soldiers do not milk when in pastr range

            int captureRange = 0;
            double capturePercentage = GameConstants.ROBOT_MILK_PERCENTAGE;
            if (ir.type == RobotType.PASTR) {
                captureRange = GameConstants.PASTR_RANGE;
                capturePercentage = 1.0;
            }
            MapLocation[] affected = MapLocation.getAllMapLocationsWithinRadiusSq(ir.getLocation(), captureRange);
            for (MapLocation ml : affected) {
                if (isValid(ml.x, ml.y)) {
                    if (ir.type == RobotType.PASTR) {
                        delta += this.currentAmount[ml.x][ml.y] * capturePercentage / nPastrs[ml.x][ml.y];
                    } else {
                        delta += this.currentAmount[ml.x][ml.y] * capturePercentage;
                    }
                }
            }
        }
        return delta;
    }

    @SuppressWarnings("unchecked")
    public void updateIds(InternalObject obj) {
        InternalRobot ir = (InternalRobot) obj;
        if (ir.type != RobotType.PASTR && ir.type != RobotType.SOLDIER) return;

        int captureRange = 0;
        if (ir.type == RobotType.PASTR) captureRange = GameConstants.PASTR_RANGE;
        MapLocation[] affected = MapLocation.getAllMapLocationsWithinRadiusSq(ir.getLocation(), captureRange);
        for (MapLocation ml : affected) {
            if (isValid(ml.x, ml.y)) {
                this.ids[ml.x][ml.y].add(ir.getID());
                if (ir.type == RobotType.PASTR) {
                    nPastrs[ml.x][ml.y]++;
                }
            }
        }
    }

    final double PI4 = Math.PI / 4;
    final double EPSILON = 1.0e-6;
    final int[][] dirs = {{-1, 0}, {-1, -1}, {0, -1}, {1, -1},
                             {1, 0}, {1, 1}, {0, 1}, {-1, 1}};
    public void next(InternalObject[] objs) {
        for (InternalObject obj : objs) {
            updateIds(obj);
        }

        // Current order:
        // 1) cows are destroyed due to attack
        // 2) cows move
        // 3) cow growth and decay happens
        for (int i = 0; i < attacks.size(); i++) {
            MapLocation target = attacks.get(i);
            if (target.x >= 0 && target.x < this.mapWidth &&
                target.y >= 0 && target.y < this.mapHeight) {
                this.currentAmount[target.x][target.y] = 0;
            }
        }

        double[][] temp = new double[this.mapWidth][this.mapHeight];
        for (int i = 0; i < this.mapWidth; i++) {
            for (int j = 0; j < this.mapHeight; j++) {
                if (this.hasNoise[i][j]) {
                    if (this.dX[i][j] != 0 || this.dY[i][j] != 0) {
                        double theta = Math.atan2(this.dY[i][j], this.dX[i][j]); // angle the cows want to move in
                        double closestThetaDiff = Math.PI / 2;
                        int movesCount = 0;
                        for (int k = -4; k < 4; k++) {
                            double theta0 = k * PI4;
                            int x = i + dirs[k + 4][0];
                            int y = j + dirs[k + 4][1];
                            boolean valid = isValid(x, y, i, j);

                            if (valid) {
                                double diff = Math.min(Math.abs(theta - theta0), 2 * Math.PI - Math.abs(theta - theta0));
                                if (diff < closestThetaDiff - EPSILON) {
                                    closestThetaDiff = diff;
                                    movesCount = 1;
                                } else if (diff <= closestThetaDiff + EPSILON) {
                                    movesCount++;
                                }
                            }
                        }
                        if (movesCount > 0) {
                            for (int k = -4; k < 4; k++) {
                                double theta0 = k * PI4;
                                int x = i + dirs[k + 4][0];
                                int y = j + dirs[k + 4][1];
                                boolean valid = isValid(x, y, i, j);

                                if (valid) {
                                    double diff = Math.min(Math.abs(theta - theta0), 2 * Math.PI - Math.abs(theta - theta0));
                                    if (diff <= closestThetaDiff + EPSILON) {
                                        temp[x][y] += this.currentAmount[i][j] / movesCount;
                                    }
                                }
                            }
                        } else {
                            temp[i][j] += this.currentAmount[i][j];
                        }
                    } else {
                        // scatter
                        int nScatter = 0;
                        for (int di = -1; di <= 1; di++) {
                            for (int dj = -1; dj <= 1; dj++) {
                                if (!(di == 0 && dj == 0) && isValid(i + di, j + dj, i, j)) {
                                    nScatter++;
                                }
                            }
                        }
                        if (nScatter == 0) {
                            temp[i][j] += this.currentAmount[i][j];
                        } else {
                            for (int di = -1; di <= 1; di++) {
                                for (int dj = -1; dj <= 1; dj++) {
                                    if (!(di == 0 && dj == 0) && isValid(i + di, j + dj, i, j)) {
                                        temp[i + di][j + dj] += this.currentAmount[i][j] / nScatter;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    temp[i][j] += this.currentAmount[i][j];
                }
            }
        }
        for (int i = 0; i < this.mapWidth; i++) {
            for (int j = 0; j < this.mapHeight; j++) {
                this.currentAmount[i][j] = temp[i][j];
            }
        }

        for (int i = 0; i < this.mapWidth; i++) {
            for (int j = 0; j < this.mapHeight; j++) {
                if (this.isValid(i, j)) {
                    this.currentAmount[i][j] = GameConstants.NEUTRALS_TURN_DECAY * this.currentAmount[i][j] + this.growthFactor[i][j];
                }
            }
        }

        //this.print();
    }

    // for when an attack doesn't generate noise
    public void updateWithQuietAttack(MapLocation source) {
        attacks.add(source);
    }

    public void updateWithNoiseSource(MapLocation source, int radiusSquared) {
        MapLocation[] affected = MapLocation.getAllMapLocationsWithinRadiusSq(source, radiusSquared);
        for (int i = 0; i < affected.length; i++) {
            if (isValid(affected[i].x, affected[i].y)) {
                hasNoise[affected[i].x][affected[i].y] = true;
                int curdX = affected[i].x - source.x;
                int curdY = affected[i].y - source.y;
                dX[affected[i].x][affected[i].y] += curdX;
                dY[affected[i].x][affected[i].y] += curdY;
            }
        }
    }

    public void updateWithMovement(MovementSignal movement) {
        if (movement.getMovementType() != MovementType.SNEAK) {
            MapLocation source = movement.getNewLoc();
            updateWithNoiseSource(source, GameConstants.MOVEMENT_SCARE_RANGE);
        }
    }

    public void updateWithAttack(AttackSignal attack) {
        attacks.add(attack.getTargetLoc());
        updateWithNoiseSource(attack.getTargetLoc(), GameConstants.ATTACK_SCARE_RANGE);
    }

    public void resetAfterTurn() {
        for (int i = 0; i < this.mapWidth; i++) {
            for (int j = 0; j < this.mapHeight; j++) {
                this.dX[i][j] = 0;
                this.dY[i][j] = 0;
                this.ids[i][j].clear();
                this.hasNoise[i][j] = false;
                this.nPastrs[i][j] = 0;
            }
        }
        attacks.clear();
    }
}
